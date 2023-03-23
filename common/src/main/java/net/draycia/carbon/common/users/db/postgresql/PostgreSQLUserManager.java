/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.users.db.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.db.ComponentArgumentFactory;
import net.draycia.carbon.common.users.db.DBType;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.users.db.KeyArgumentFactory;
import net.draycia.carbon.common.users.db.QueriesLocator;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

// TODO: Dispatch updates using messaging system when users are modified
@DefaultQualifier(NonNull.class)
public final class PostgreSQLUserManager extends DatabaseUserManager {

    private final ProfileResolver profileResolver;
    private final Map<UUID, CompletableFuture<CarbonPlayerCommon>> cache = new HashMap<>();

    private PostgreSQLUserManager(final Jdbi jdbi, final Logger logger, final ProfileResolver profileResolver) {
        super(jdbi, new QueriesLocator(DBType.POSTGRESQL), logger);
        this.profileResolver = profileResolver;
    }

    public static PostgreSQLUserManager manager(
        final DatabaseSettings databaseSettings,
        final Logger logger,
        final ProfileResolver profileResolver
    ) {
        try {
            Class.forName("org.postgresql.Driver");
            Flyway.configure().getPluginRegister().REGISTERED_PLUGINS.add(new PostgreSQLDatabaseType());
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setJdbcUrl(databaseSettings.url());
        hikariConfig.setUsername(databaseSettings.username());
        hikariConfig.setPassword(databaseSettings.password());
        hikariConfig.setThreadFactory(ConcurrentUtil.carbonThreadFactory(logger, "PSQLUserManagerHCP"));

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        Flyway.configure(CarbonChat.class.getClassLoader())
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .locations("queries/migrations/postgresql")
            .dataSource(dataSource)
            .validateOnMigrate(true)
            .load()
            .migrate();

        final Jdbi jdbi = Jdbi.create(dataSource)
            .registerArgument(new ComponentArgumentFactory())
            .registerArgument(new KeyArgumentFactory())
            .registerRowMapper(new PostgreSQLPlayerRowMapper())
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());

        return new PostgreSQLUserManager(jdbi, logger, profileResolver);
    }

    @Override
    public synchronized CompletableFuture<CarbonPlayerCommon> user(final UUID uuid) {
        return this.cache.computeIfAbsent(uuid, $ -> {
            final CompletableFuture<CarbonPlayerCommon> future = CompletableFuture.supplyAsync(() -> {
                final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

                if (cachedPlayer != null) {
                    return cachedPlayer;
                }

                return this.jdbi.withHandle(handle -> {
                    try {
                        final @Nullable CarbonPlayerCommon carbonPlayerCommon = handle.createQuery(this.locator.query("select-player"))
                            .bind("id", uuid)
                            .mapTo(CarbonPlayerCommon.class)
                            .first();

                        handle.createQuery(this.locator.query("select-ignores"))
                            .bind("id", uuid)
                            .mapTo(UUID.class)
                            .forEach(ignoredPlayer -> carbonPlayerCommon.ignoring(ignoredPlayer, true));

                        handle.createQuery(this.locator.query("select-leftchannels"))
                            .bind("id", uuid)
                            .mapTo(Key.class)
                            .forEach(channel -> {
                                final @Nullable ChatChannel chatChannel = CarbonChatProvider.carbonChat()
                                    .channelRegistry()
                                    .get(channel);
                                if (chatChannel == null) {
                                    return;
                                }
                                carbonPlayerCommon.leftChannels().add(channel);
                            });
                        return carbonPlayerCommon;
                    } catch (final IllegalStateException exception) {
                        // Player doesn't exist in the DB, create them!
                        final String name = Objects.requireNonNull(
                            this.profileResolver.resolveName(uuid).join());

                        final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

                        this.bindPlayerArguments(handle.createUpdate(this.locator.query("insert-player")), player)
                            .execute();

                        return player;
                    }
                });
            }, this.executor);

            // Don't keep failed requests so they can be retried on the next request
            // The caller is expected to handle the error
            future.whenComplete((result, $$$) -> {
                if (result == null) {
                    synchronized (this) {
                        this.cache.remove(uuid);
                    }
                }
            });

            return future;
        });
    }

    @Override
    protected Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player) {
        return update
            .bind("id", player.uuid())
            .bind("muted", player.muted())
            .bind("deafened", player.deafened())
            .bind("selectedchannel", player.selectedChannelKey())
            .bind("username", player.username())
            .bind("displayname", player.displayName())
            .bind("lastwhispertarget", player.lastWhisperTarget())
            .bind("whisperreplytarget", player.whisperReplyTarget())
            .bind("spying", player.spying());
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        final CompletableFuture<@Nullable CarbonPlayerCommon> remove = this.cache.remove(uuid);
        final @Nullable CarbonPlayerCommon join = remove.join();
        if (remove.isDone() && join != null) { // don't need to save if it never finished loading
            return this.save(join);
        }
        return CompletableFuture.completedFuture(null);
    }

}
