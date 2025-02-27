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
package net.draycia.carbon.common.util;

import java.util.Arrays;
import java.util.UUID;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Jon Chambers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * This is a modified FastUUID implementation. The primary difference is that it does not dash its
 * UUIDs. As the native Java 9+ UUID.toString() implementation dashes its UUIDs, we use the FastUUID
 * methods, which ought to be faster than a String.replace().
 */
public final class FastUuidSansHyphens {

    private static final int MOJANG_BROKEN_UUID_LENGTH = 32;

    private static final char[] HEX_DIGITS =
        new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final long[] HEX_VALUES = new long[128];

    static {
        Arrays.fill(HEX_VALUES, -1);

        HEX_VALUES['0'] = 0x0;
        HEX_VALUES['1'] = 0x1;
        HEX_VALUES['2'] = 0x2;
        HEX_VALUES['3'] = 0x3;
        HEX_VALUES['4'] = 0x4;
        HEX_VALUES['5'] = 0x5;
        HEX_VALUES['6'] = 0x6;
        HEX_VALUES['7'] = 0x7;
        HEX_VALUES['8'] = 0x8;
        HEX_VALUES['9'] = 0x9;

        HEX_VALUES['a'] = 0xa;
        HEX_VALUES['b'] = 0xb;
        HEX_VALUES['c'] = 0xc;
        HEX_VALUES['d'] = 0xd;
        HEX_VALUES['e'] = 0xe;
        HEX_VALUES['f'] = 0xf;

        HEX_VALUES['A'] = 0xa;
        HEX_VALUES['B'] = 0xb;
        HEX_VALUES['C'] = 0xc;
        HEX_VALUES['D'] = 0xd;
        HEX_VALUES['E'] = 0xe;
        HEX_VALUES['F'] = 0xf;
    }

    private FastUuidSansHyphens() {
        // A private constructor prevents callers from accidentally instantiating FastUUID instances
    }

    /**
     * Parses a UUID from the given character sequence. The character sequence must represent a
     * Mojang UUID.
     *
     * @param uuidSequence the character sequence from which to parse a UUID
     *
     * @return the UUID represented by the given character sequence
     *
     * @throws IllegalArgumentException if the given character sequence does not conform to the string
     *         representation of a Mojang UUID.
     */
    public static UUID parseUuid(final CharSequence uuidSequence) {
        if (uuidSequence.length() != MOJANG_BROKEN_UUID_LENGTH) {
            throw new IllegalArgumentException("Illegal UUID string: " + uuidSequence);
        }

        long mostSignificantBits = hexValueForChar(uuidSequence.charAt(0)) << 60;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(1)) << 56;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(2)) << 52;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(3)) << 48;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(4)) << 44;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(5)) << 40;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(6)) << 36;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(7)) << 32;

        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(8)) << 28;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(9)) << 24;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(10)) << 20;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(11)) << 16;

        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(12)) << 12;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(13)) << 8;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(14)) << 4;
        mostSignificantBits |= hexValueForChar(uuidSequence.charAt(15));

        long leastSignificantBits = hexValueForChar(uuidSequence.charAt(16)) << 60;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(17)) << 56;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(18)) << 52;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(19)) << 48;

        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(20)) << 44;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(21)) << 40;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(22)) << 36;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(23)) << 32;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(24)) << 28;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(25)) << 24;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(26)) << 20;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(27)) << 16;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(28)) << 12;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(29)) << 8;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(30)) << 4;
        leastSignificantBits |= hexValueForChar(uuidSequence.charAt(31));

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Returns a string representation of the given UUID. The returned string is formatted as a
     * Mojang-style UUID.
     *
     * @param uuid the UUID to represent as a string
     *
     * @return a string representation of the given UUID
     */
    public static String toString(final UUID uuid) {
        final long mostSignificantBits = uuid.getMostSignificantBits();
        final long leastSignificantBits = uuid.getLeastSignificantBits();

        final char[] uuidChars = new char[MOJANG_BROKEN_UUID_LENGTH];

        uuidChars[0] = HEX_DIGITS[(int) ((mostSignificantBits & 0xf000000000000000L) >>> 60)];
        uuidChars[1] = HEX_DIGITS[(int) ((mostSignificantBits & 0x0f00000000000000L) >>> 56)];
        uuidChars[2] = HEX_DIGITS[(int) ((mostSignificantBits & 0x00f0000000000000L) >>> 52)];
        uuidChars[3] = HEX_DIGITS[(int) ((mostSignificantBits & 0x000f000000000000L) >>> 48)];
        uuidChars[4] = HEX_DIGITS[(int) ((mostSignificantBits & 0x0000f00000000000L) >>> 44)];
        uuidChars[5] = HEX_DIGITS[(int) ((mostSignificantBits & 0x00000f0000000000L) >>> 40)];
        uuidChars[6] = HEX_DIGITS[(int) ((mostSignificantBits & 0x000000f000000000L) >>> 36)];
        uuidChars[7] = HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000f00000000L) >>> 32)];
        uuidChars[8] = HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000f0000000L) >>> 28)];
        uuidChars[9] = HEX_DIGITS[(int) ((mostSignificantBits & 0x000000000f000000L) >>> 24)];
        uuidChars[10] = HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000000f00000L) >>> 20)];
        uuidChars[11] = HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000000f0000L) >>> 16)];
        uuidChars[12] = HEX_DIGITS[(int) ((mostSignificantBits & 0x000000000000f000L) >>> 12)];
        uuidChars[13] = HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000000000f00L) >>> 8)];
        uuidChars[14] = HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000000000f0L) >>> 4)];
        uuidChars[15] = HEX_DIGITS[(int) (mostSignificantBits & 0x000000000000000fL)];
        uuidChars[16] = HEX_DIGITS[(int) ((leastSignificantBits & 0xf000000000000000L) >>> 60)];
        uuidChars[17] = HEX_DIGITS[(int) ((leastSignificantBits & 0x0f00000000000000L) >>> 56)];
        uuidChars[18] = HEX_DIGITS[(int) ((leastSignificantBits & 0x00f0000000000000L) >>> 52)];
        uuidChars[19] = HEX_DIGITS[(int) ((leastSignificantBits & 0x000f000000000000L) >>> 48)];
        uuidChars[20] = HEX_DIGITS[(int) ((leastSignificantBits & 0x0000f00000000000L) >>> 44)];
        uuidChars[21] = HEX_DIGITS[(int) ((leastSignificantBits & 0x00000f0000000000L) >>> 40)];
        uuidChars[22] = HEX_DIGITS[(int) ((leastSignificantBits & 0x000000f000000000L) >>> 36)];
        uuidChars[23] = HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000f00000000L) >>> 32)];
        uuidChars[24] = HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000f0000000L) >>> 28)];
        uuidChars[25] = HEX_DIGITS[(int) ((leastSignificantBits & 0x000000000f000000L) >>> 24)];
        uuidChars[26] = HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000000f00000L) >>> 20)];
        uuidChars[27] = HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000000f0000L) >>> 16)];
        uuidChars[28] = HEX_DIGITS[(int) ((leastSignificantBits & 0x000000000000f000L) >>> 12)];
        uuidChars[29] = HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000000000f00L) >>> 8)];
        uuidChars[30] = HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000000000f0L) >>> 4)];
        uuidChars[31] = HEX_DIGITS[(int) (leastSignificantBits & 0x000000000000000fL)];

        return new String(uuidChars);
    }

    private static long hexValueForChar(final char c) {
        try {
            if (HEX_VALUES[c] < 0) {
                throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
        }

        return HEX_VALUES[c];
    }

}
