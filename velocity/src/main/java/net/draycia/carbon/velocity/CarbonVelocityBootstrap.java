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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import net.draycia.carbon.common.util.CarbonDependencies;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@Plugin(
    id = "$[ID]",
    name = "$[NAME]",
    version = "$[VERSION]",
    description = "$[DESCRIPTION]",
    url = "$[URL]",
    authors = {"Draycia"},
    dependencies = {
        @Dependency(id = "luckperms"),
        @Dependency(id = "miniplaceholders", optional = true),
        @Dependency(id = "unsignedvelocity", optional = true)
    }
)
public final class CarbonVelocityBootstrap {

    private final Injector parentInjector;
    private final PluginContainer pluginContainer;
    private final ProxyServer proxy;
    private final Path dataDirectory;
    private @MonotonicNonNull Injector injector;

    @Inject
    public CarbonVelocityBootstrap(
        final Injector injector,
        final ProxyServer proxyServer,
        final PluginContainer pluginContainer,
        @DataDirectory final Path dataDirectory
    ) {
        this.proxy = proxyServer;
        this.parentInjector = injector;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        CarbonDependencies.load(
            this.dataDirectory.resolve("libraries"),
            path -> this.proxy.getPluginManager().addToClasspath(this, path)
        );

        this.injector = this.parentInjector.createChildInjector(
            new CarbonChatVelocityModule(this.pluginContainer, this.proxy, this.dataDirectory));
        this.injector.getInstance(CarbonChatVelocity.class).onInitialization(this);
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        this.injector.getInstance(CarbonChatVelocity.class).onShutdown();
    }

}
