/* ****************************************************************************
 * Copyright (c) 2012-2014 VMware, Inc. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * ****************************************************************************/

package com.vmware.upgrade.dsl.sql.util

/**
 * A utility for deterministically abbreviating names.
 * <p>
 * This is primarily used to generate constraint names due to DBMS imposed
 * upper bounds on their length.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class ConstraintNameUtil {
    /**
     * Abbreviates an underscore delimited {@code name} to be less than or
     * equal to {@code maxLength}.
     * <p>
     * Example:
     * <ul>
     *   <li>name: {@code virtual_machine_record}, maxLength: {@code 13}
     *       &rarr; {@code virt_mach_rec"}</li>
     * </ul>
     *
     * @param name string to abbreviate
     * @param maxLength
     * @return name if its length is less than or equal to maxLength, otherwise
     *         the abbreviated name.
     */
    public static String abbreviate(String name, int maxLength) {
        if (maxLength == 0) {
            return ""
        }

        if (name.length() <= maxLength) {
            return name
        }

        List<String> splitName = name.split("_")
        List<String> untouchedSplitName = name.split("_")
        int charsPerWord = (int) Math.floor(maxLength / splitName.size())
        String abbr

        while ((abbr = splitName.join("_")).length() > maxLength) {
            if (charsPerWord == 0) {
                untouchedSplitName.remove(untouchedSplitName.size() - 1)
                splitName = new ArrayList(untouchedSplitName)

                charsPerWord = (int) Math.floor(maxLength / splitName.size())
                if (charsPerWord == 0) {
                    charsPerWord = 1
                }
            }

            for (int i = splitName.size() - 1; i >= 0; i--) {
                final int wordLength = splitName[i].length()

                if (wordLength > charsPerWord) {
                    splitName[i] = splitName[i].substring(0, charsPerWord)

                    String abbrTest = splitName.join("_")
                    if (abbrTest.length() <= maxLength) {
                        return abbrTest
                    }
                }
            }

            charsPerWord--
        }

        return abbr
    }
}
