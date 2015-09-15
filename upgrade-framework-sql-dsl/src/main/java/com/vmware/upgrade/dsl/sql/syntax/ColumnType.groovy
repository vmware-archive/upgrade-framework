/* ****************************************************************************
 * Copyright (c) 2014-2015 VMware, Inc. All Rights Reserved.
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

import com.vmware.upgrade.dsl.sql.util.ClassUtil
import com.vmware.upgrade.dsl.sql.util.NullAware
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException
import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement

/**
 * A series of common types of data stored in table columns.
 * <p>
 * For example, storing a <em>bool</em> is a common type of
 * data to have to persist, so rather than remember that in Oracle, the
 * appropriate column type is {@code NUMBER(1,0)} and in MSSQL it is
 * {@code TINYINT}, one can just say, {@code BOOL}
 * and it will be converted automatically.
 * </p>
 *
 * @author Matthew Frost <mfrost@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class ColumnType {

    /* @Override from MetaObjectProtocol */
    public MetaProperty hasProperty(String name) {
        return ClassUtil.hasField(Type.class, name)
    }

    def propertyMissing(String name) {
        try {
            return Type.valueOf(name)
        } catch (IllegalArgumentException e) {
            throw new UnknownKeywordException("Unknown column type '${name}'")
        }
    }

    public static def NVARCHAR(int length) {
        new NullableMapBasedSQLStatement([
            oracle:'NVARCHAR2(' + length + ')',
            ms_sql:'NVARCHAR(' + length + ')',
            postgres:'VARCHAR(' + length + ')'])
    }

    public static def VARCHAR(int length) {
        new NullableMapBasedSQLStatement([
            oracle:'VARCHAR2(' + length + ')',
            ms_sql:'VARCHAR(' + length + ')',
            postgres:'VARCHAR(' + length + ')'])
    }

    public static Map<String, Closure<?>> getKeywords() {
        return ["NVARCHAR" : { NVARCHAR(it) }, "VARCHAR" : { VARCHAR(it) }]
    }

    private enum Type implements DataType {
        BOOL([oracle:'NUMBER(1,0) DEFAULT 0', ms_sql:'TINYINT DEFAULT 0', postgres:'BOOLEAN DEFAULT FALSE']),
        DATE([oracle:'TIMESTAMP (6) DEFAULT SYSTIMESTAMP', ms_sql:'DATETIME DEFAULT GETDATE()', postgres:'TIMESTAMP (6) DEFAULT NOW()']),
        INTEGER([oracle:'NUMBER(10,0)', ms_sql:'INT', postgres:'INT']),
        LONG([oracle:'NUMBER(19,0)', ms_sql:'BIGINT', postgres:'BIGINT'])

        private final Map ddl

        private Type(Map ddl) {
            this.ddl = ddl
        }

        @Override
        public SQLStatement sql() {
            return new NullableMapBasedSQLStatement(ddl)
        }
    }

    /**
     * Returns a {@link SQLStatement} that allows specifying columns as nullable.
     */
    public static class NullableMapBasedSQLStatement implements NullAware, SQLStatement {
        private boolean allowNulls = false
        private final SQLStatement sqlStatement
        private static final String NOT_NULL = "NOT NULL"
        private static final String NULL = "NULL"

        public NullableMapBasedSQLStatement(Map<String, String> statementMap) {
            sqlStatement = SQLStatementFactory.create(statementMap)
        }

        /**
         * Checks if {@code arg} is {@code null}, if so will permit null values
         * for this column, otherwise will throw an {@link IllegalArgumentException}
         *
         * @param arg
         */
        @Override
        public void makeNullable(def arg) {
            if (arg == null) {
                this.allowNulls = true
            } else {
                throw new IllegalArgumentException("expected null following 'allowing' but found '${arg}'")
            }
        }

        @Override
        public boolean isNullable() {
            return allowNulls
        }

        @Override
        public String get(DatabaseType databaseType) {
            String sql = sqlStatement.get(databaseType)
            String nullConstraint = (allowNulls) ? NULL : NOT_NULL
            return String.format("${sql} %s", nullConstraint)
        }
    }
}
