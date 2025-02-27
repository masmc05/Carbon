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
package net.draycia.carbon.common.users.db;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.CachingUserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;

@DefaultQualifier(NonNull.class)
public abstract class DatabaseUserManager extends CachingUserManager {

    protected final Jdbi jdbi;
    protected final QueriesLocator locator;
    protected final ChannelRegistry channelRegistry;

    protected DatabaseUserManager(
        final Jdbi jdbi,
        final QueriesLocator locator,
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final ChannelRegistry channelRegistry
    ) {
        super(
            logger,
            profileResolver,
            playerInjector,
            messagingManager,
            packetFactory
        );
        this.jdbi = jdbi;
        this.locator = locator;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public final void saveSync(final CarbonPlayerCommon player) {
        this.jdbi.withHandle(handle -> {
            this.bindPlayerArguments(handle.createUpdate(this.locator.query("save-player")), player)
                .execute();

            if (!player.ignoredPlayers().isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-ignores"));

                for (final UUID ignoredPlayer : player.ignoredPlayers()) {
                    batch.bind("id", player.uuid()).bind("ignoredplayer", ignoredPlayer).add();
                }

                batch.execute();
            }
            if (!player.leftChannels().isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-leftchannels"));

                for (final Key leftChannel : player.leftChannels()) {
                    batch.bind("id", player.uuid()).bind("channel", leftChannel).add();
                }

                batch.execute();
            }
            // TODO: save ignoredplayers
            return null;
        });
    }

    abstract protected Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player);

}
