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

package com.vmware.upgrade.dsl.sql.model.defaults

import com.vmware.upgrade.dsl.sql.model.CommentModel
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.sql.util.ValidationUtil
import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement

/**
 * {@code DefaultCommentModel} is the core implementation of {@link CommentModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of table or column before execution.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class DefaultCommentModel implements CommentModel {
    public static enum CommentType {
        COLUMN(
            SQLStatementFactory.create(
                [
                    ms_sql: """
DECLARE @schema sysname
SELECT @schema = SCHEMA_NAME()
Exec sp_addextendedproperty "MS_Description", "%1\$s", "SCHEMA", @schema, "TABLE", "%2\$s", "COLUMN", "%3\$s"
""",
                    default: "COMMENT ON COLUMN %2\$s.%3\$s IS '%1\$s'"
                ]
            )
        ),
        TABLE(
            SQLStatementFactory.create(
                [
                    ms_sql: """
DECLARE @schema sysname
SELECT @schema = SCHEMA_NAME()
Exec sp_addextendedproperty "MS_Description", "%1\$s", "SCHEMA", @schema, "TABLE", "%2\$s"
""",
                    default: "COMMENT ON TABLE %2\$s IS '%1\$s'"
                ]
            )
        )

        private SQLStatement sql

        private CommentType(SQLStatement sql) {
            this.sql = sql
        }

        private getSql(DatabaseType databaseType) {
            this.sql.get(databaseType)
        }
    }

    private def text

    protected CommentType commentType

    protected def entity

    protected def tableName

    @Override
    public void setText(text) {
        ValidationUtil.validateCommentLength(text)
        this.text = text
    }

    @Override
    public void setEntity(name) {
        this.commentType = CommentType.TABLE
        this.entity = name
    }

    @Override
    public void columnComment(tableName) {
        this.commentType = CommentType.COLUMN
        this.tableName = tableName
    }

    @Override
    public String get(DatabaseType databaseType) {
        switch (commentType) {
            case CommentType.COLUMN:
                return SQLStatementFactory.format(commentType.getSql(databaseType), databaseType, text, tableName, entity)
            case CommentType.TABLE:
                return SQLStatementFactory.format(commentType.getSql(databaseType), databaseType, text, entity)
            default:
                throw new IllegalStateException("Unsupported comment type")
        }
    }
}
