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
package net.draycia.carbon.paper.messages;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PlaceholderAPIMiniMessageParser {

    private final MiniMessage miniMessage;

    private PlaceholderAPIMiniMessageParser(final MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    public static PlaceholderAPIMiniMessageParser create(final MiniMessage backingInstance) {
        return new PlaceholderAPIMiniMessageParser(backingInstance);
    }

    private static boolean containsLegacyColorCodes(final String string) {
        final char[] charArray = ChatColor.translateAlternateColorCodes('&', string).toCharArray();

        for (final char c : charArray) {
            if (c == LegacyComponentSerializer.SECTION_CHAR) {
                return true;
            }
        }

        return false;
    }

    public Component parse(final OfflinePlayer player, final String input, final TagResolver tagResolver) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(player, match),
            input,
            tagResolver
        );
    }

    public Component parse(final OfflinePlayer player, final String input) {
        return this.parse(player, input, TagResolver.empty());
    }

    public Component parseRelational(final Player one, final Player two, final String input, final TagResolver tagResolver) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(one, PlaceholderAPI.setRelationalPlaceholders(one, two, match)),
            input,
            tagResolver
        );
    }

    public Component parseRelational(final Player one, final Player two, final String input) {
        return this.parseRelational(one, two, input, TagResolver.empty());
    }

    private Component parseLegacy(final String s) {
        return LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', s));
    }

    private Component parse(
        final Pattern pattern,
        final UnaryOperator<String> placeholderResolver,
        final String input,
        final TagResolver originalTags
    ) {
        final Matcher matcher = pattern.matcher(input);
        final TagResolver.Builder tagResolver = TagResolver.builder().resolvers(originalTags);
        final StringBuilder builder = new StringBuilder();
        int id = 0;

        while (matcher.find()) {
            final String match = matcher.group();
            final String replaced = placeholderResolver.apply(match);

            if (match.equals(replaced) || !containsLegacyColorCodes(replaced)) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(replaced));
            } else {
                final String key = "papi_generated_template_" + id;
                id++;
                tagResolver.tag(key, Tag.inserting(this.parseLegacy(replaced)));
                matcher.appendReplacement(builder, Matcher.quoteReplacement("<" + key + ">"));
            }
        }

        matcher.appendTail(builder);

        return this.miniMessage.deserialize(builder.toString(), tagResolver.build());
    }

}
