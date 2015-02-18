/* ****************************************************************************
 * Copyright (c) 2012-2015 VMware, Inc. All Rights Reserved.
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

import com.vmware.upgrade.dsl.sql.util.NullAware
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.sql.util.ValidationUtil
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@code TableCreationColumnSyntax} defines the syntax for the table creation
 * {@code add} column keyword.
 * <p>
 * This class isn't an inner class or {@link TableCreationSyntax} because of a
 * bug in Groovy that prevents a user-defined propertyMissing method being set
 * on one. See {@link http://jira.codehaus.org/browse/GROOVY-4862}
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class TableCreationColumnSyntax {
    List columns = []

    private class Column {
        def name
        def type

        Column(name, type) {
            this.name = name
            this.type = type
        }

        public String get(DatabaseType databaseType) {
            return SQLStatementFactory.create(name, " ", type).get(databaseType)
        }

        @Override
        public int hashCode() {
            return name.hashCode()
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Column && obj.name.equals(name))
        }
    }

    def add(column) {
        ValidationUtil.validateEntityName(column)

        return [storing: { type ->
            def columnType = (type in DataType) ? type.sql() : type
            Column col = new Column(column, columnType)
            columns.add(col)
            if (columnType in NullAware) {
                return [allowing: { columnType.makeNullable(it) }]
            }
        }]
    }
}
