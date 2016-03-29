/* ****************************************************************************
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.sql.syntax

import com.vmware.upgrade.dsl.sql.model.UnindexModel
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException

/**
 * {@code UnindexSyntax} defines the syntax for the {@code unindex} keyword.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
class UnindexSyntax {
    UnindexModel model

    UnindexSyntax(UnindexModel model, firstColumnOrName) {
        this.model = model
        if (firstColumnOrName instanceof String && ((String) firstColumnOrName).startsWith("ix_")) {
            model.setIndexName(firstColumnOrName)
        } else {
            model.addColumn(firstColumnOrName)
        }

    }

    def of(tableName) {
        model.setTable(tableName)
        this
    }

    def and(column) {
        model.addColumn(column)
        this
    }

    def propertyMissing(String name) {
        String msg
        try {
            this.getClass().getMethod(name, Object.class)
            msg = "Missing keyword following '$name'"
        } catch (NoSuchMethodException e) {
            msg = "Unknown keyword '$name'"
        }
        throw new UnknownKeywordException(msg)
    }
}
