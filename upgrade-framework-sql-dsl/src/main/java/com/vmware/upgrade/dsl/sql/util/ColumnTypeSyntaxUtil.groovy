/* ****************************************************************************
 * Copyright (c) 2015-2017 VMware, Inc. All Rights Reserved.
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

import com.vmware.upgrade.dsl.sql.syntax.DataType

/**
 * Utility class for syntax-related classes to perform common operations on a {@link ColumnType}
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
class ColumnTypeSyntaxUtil {

    /**
     * Checks which interfaces are implemented by {@code type} and calls requisite methods as
     * applicable.
     *
     * @param type any valid column type object, e.g. {@link Map}, {@link String}, {@link ColumnType}
     * @return an object representing a column type, this may be a reference to the same object or a
     * copy of one (i.e. callers should <i>not</i> assume the returned object references the same one
     * passed).
     */
    static def getColumnType(type) {
        def columnType = type

        if (type in DataType) {
            columnType = columnType.sql()
        }

        if (type in NullAware) {
            columnType = columnType.makeCopy()
        }

        return columnType
    }

    /**
     * Returns a closure with additional column syntax for the appropriate type.
     *
     * @param type
     * @return {@link Closure}, or the passed {@code type} if it is neither {@link NullAware}
     * or {@link InitialAware}
     */
    static def getAdditionalSyntax(type) {
        def allowing = { type.makeNullable(it) }
        def initial = { type.setInitialValue(it) }

        if (type in NullAware && type in InitialAware) {
            return [ allowing: allowing, initial: initial ]
        } else if (type in NullAware) {
            return [ allowing: allowing ]
        } else if (type in InitialAware) {
            return [ initial: initial ]
        }

        return type
    }

}
