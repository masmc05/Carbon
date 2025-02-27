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
package net.draycia.carbon.velocity.users;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Locale;
import java.util.Optional;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.draycia.carbon.common.util.EmptyAudienceWithPointers;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerVelocity extends WrappedCarbonPlayer implements ForwardingAudience.Single {

    private final ProxyServer server;

    public CarbonPlayerVelocity(final ProxyServer server, final CarbonPlayerCommon carbonPlayerCommon) {
        super(carbonPlayerCommon);
        this.server = server;
    }

    @Override
    public @NotNull Audience audience() {
        return this.player().map(value -> (Audience) value).orElseGet(() -> EmptyAudienceWithPointers.forCarbonPlayer(this));
    }

    @Override
    public boolean vanished() {
        //TODO: VelocityVanish compatibility
        return false;
    }

    public Optional<Player> player() {
        return this.server.getPlayer(this.uuid());
    }

    @Override
    public @Nullable Locale locale() {
        return this.player().map(value -> value.getPlayerSettings().getLocale()).orElse(null);
    }

    @Override
    public double distanceSquaredFrom(final CarbonPlayer other) {
        return -1;
    }

    @Override
    public boolean sameWorldAs(final CarbonPlayer other) {
        final Optional<Player> player = this.player();
        final Optional<Player> otherPlayer = this.server.getPlayer(other.uuid());

        if (player.isEmpty() || otherPlayer.isEmpty()) {
            return false;
        }

        final var playerServer = player.get().getCurrentServer();
        return playerServer.isPresent() && playerServer.equals(otherPlayer.get().getCurrentServer());
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return null;
    }

    @Override
    public boolean online() {
        final var player = this.player();
        return player.isPresent() && player.get().isActive();
    }

}
