/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.command.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class OptionValueParser<C> implements ArgumentParser<C, String> {

    private static final char ESCAPE = '\\';
    private static final String FLAG_STARTER = "-";

    public OptionValueParser() {
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull String> parse(
        @NonNull final CommandContext<@NonNull C> commandContext,
        @NonNull final Queue<@NonNull String> inputQueue
    ) {
        final @Nullable String input = inputQueue.peek();
        if (input == null || input.startsWith(FLAG_STARTER)) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                OptionValueParser.class,
                commandContext
            ));
        }

        final char startChar = input.charAt(0);

        // If quoted string: read until a word ending with a quote
        if (startChar == '\'' || startChar == '\"') {
            final StringBuilder result = new StringBuilder();
            result.append(inputQueue.remove(), 1, input.length());
            @Nullable String next;
            while ((next = inputQueue.peek()) != null) {
                result.append(" ");
                if (next.length() > 0 && next.charAt(next.length() - 1) == startChar) {
                    // We've found the end of a quoted string, maybe
                    // if escaped, append without escape then continue
                    if (next.charAt(next.length() - 1) == ESCAPE) {
                        result.append(inputQueue.remove(), 0, next.length() - 2)
                            .append(startChar);
                        continue;
                    } else {
                        result.append(inputQueue.remove(), 0, next.length() - 1);
                        // then return our full quoted string
                        return ArgumentParseResult.success(result.toString());
                    }
                }
                result.append(inputQueue.remove());
            }

            // If we made it to the end without finding an end quote, throw an error
            return ArgumentParseResult.failure(new MissingEndQuoteException(input));
        } else { // otherwise, read until end of line, or a word starting with '-'
            final StringBuilder result = new StringBuilder();
            result.append(inputQueue.remove());
            @Nullable String next;
            while ((next = inputQueue.peek()) != null) {
                if (next.startsWith(FLAG_STARTER)) {
                    break;
                }
                result.append(" ").append(inputQueue.remove());
            }

            return ArgumentParseResult.success(result.toString());
        }
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

    public static final class MissingEndQuoteException extends RuntimeException {

        private static final long serialVersionUID = 2969516711506622423L;
        private final String input;

        public MissingEndQuoteException(
            final @NonNull String input
        ) {
            this.input = input;
        }

        public @NonNull String input() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return "Missing end quote for input: '" + this.input + "'";
        }

    }

}
