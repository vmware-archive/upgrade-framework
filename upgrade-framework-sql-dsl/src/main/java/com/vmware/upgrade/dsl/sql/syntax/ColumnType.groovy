/* ****************************************************************************
 * Copyright (c) 2014-2017 VMware, Inc. All Rights Reserved.
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
import com.vmware.upgrade.dsl.sql.util.DefaultAware
import com.vmware.upgrade.dsl.sql.util.HasClosureMap
import com.vmware.upgrade.dsl.sql.util.InitialAware
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
 * @author Matthew Frost mfrost@vmware.com
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
        new ModifiableMapBasedSQLStatement([
            oracle:'NVARCHAR2(' + length + ')',
            ms_sql:'NVARCHAR(' + length + ')',
            postgres:'VARCHAR(' + length + ')'])
    }

    public static def VARCHAR(int length) {
        new ModifiableMapBasedSQLStatement([
            oracle:'VARCHAR2(' + length + ')',
            ms_sql:'VARCHAR(' + length + ')',
            postgres:'VARCHAR(' + length + ')'])
    }

    public static Map<String, Closure<?>> getKeywords() {
        return ["NVARCHAR" : { NVARCHAR(it) }, "VARCHAR" : { VARCHAR(it) }]
    }

    private enum Type implements DataType {
        BOOL([oracle:'NUMBER(1,0)', ms_sql:'TINYINT', postgres:'BOOLEAN'], [oracle:'0', ms_sql:'0', postgres:'FALSE']),
        DATE([oracle:'TIMESTAMP (6)', ms_sql:'DATETIME', postgres:'TIMESTAMP (6)'], [oracle:'SYSTIMESTAMP', ms_sql:'GETDATE()', postgres:'NOW()']),
        INTEGER([oracle:'NUMBER(10,0)', ms_sql:'INT', postgres:'INT']),
        LONG([oracle:'NUMBER(19,0)', ms_sql:'BIGINT', postgres:'BIGINT']),
        BLOB([oracle:'BLOB', ms_sql:'VARBINARY(MAX)', postgres:'BYTEA']),
        CLOB([oracle:'NCLOB', ms_sql:'NVARCHAR(MAX)', postgres:'TEXT'])

        private final Map ddl
        private final Map defaultMap

        private Type(Map ddl) {
            this.ddl = ddl
        }

        private Type(Map ddl, Map defaultMap) {
            this.ddl = ddl
            this.defaultMap = defaultMap
        }

        @Override
        public SQLStatement sql() {
            return new ModifiableMapBasedSQLStatement(ddl, defaultMap)
        }
    }

    /**
     * Returns a {@link SQLStatement} that allows modification of nullability, initial value, and
     * default value.
     */
    public static class ModifiableMapBasedSQLStatement implements DefaultAware, InitialAware, NullAware, SQLStatement {
        private boolean allowNulls = false
        private final SQLStatement sqlStatement
        private final Map<String, String> statementMap

        private Object initialValue = null
        private Object defaultValue = null
        private Object defaultDefaultValue = null

        private Map<String, Closure<Object>> closureMap = getClosureMap(this)

        private static final String NOT_NULL = "NOT NULL"
        private static final String NULL = "NULL"
        private static final String DEFAULT = "DEFAULT"

        private static final String ALREADY_SPECIFIED = "'%s' has already been specified"

        public ModifiableMapBasedSQLStatement(Map<String, String> statementMap) {
            this(statementMap, null, false)
        }

        public ModifiableMapBasedSQLStatement(Map<String, String> statementMap, Object defaultValue) {
            this(statementMap, defaultValue, false)
        }

        public ModifiableMapBasedSQLStatement(Map<String, String> statementMap, Object defaultValue, boolean allowNulls) {
            sqlStatement = SQLStatementFactory.create(statementMap)
            this.statementMap = statementMap
            this.defaultDefaultValue = defaultValue
            this.allowNulls = allowNulls
        }

        /**
         * Checks if {@code arg} is {@code null}, if so will permit null values
         * for this column, otherwise will throw an {@link IllegalArgumentException}
         *
         * @param arg
         */
        @Override
        public Object makeNullable(def arg) {
            checkNotAlreadySpecified(allowNulls, "allowing null")

            if (arg == null) {
                this.allowNulls = true
                return closureMap
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
            String defaultConstraint = " "
            Object dv = getDefaultValue()
            if (dv != null) {
                SQLStatement defaultSQLStatement = SQLStatementFactory.create(dv)
                defaultConstraint = String.format(" %s %s ", DEFAULT, defaultSQLStatement.get(databaseType))
            }

            return String.format("${sql}%s%s", defaultConstraint, nullConstraint)
        }

        @Override
        public Object setInitialValue(def arg) {
            checkNotAlreadySpecified(initialValue != null, "initial_value")

            if (arg != null) {
                if (arg in String || arg in Number) {
                    initialValue = "'" + arg.toString() + "'"
                } else if (arg in Map) {
                    initialValue = arg.collectEntries { k, v ->
                        [(k): (v in String || v in Number) ? "'" + v + "'" : v]
                    }
                }
            }
            return closureMap
        }

        @Override
        public Object getInitialValue() {
            return initialValue
        }

        @Override
        public Object setDefaultValue(Object arg) {
            checkNotAlreadySpecified(defaultValue != null, (arg in RawSql) ? "default_sql" : "default_value")

            if (arg == null && !allowNulls) {
                throw new IllegalArgumentException(
                    "Default cannot be null unless 'allowing null' was specified ('default_value null' may " +
                    "be provided with 'allowing null' but is not necessary)")
            }

            if (arg != null) {
                if (arg in String || arg in Number) {
                    defaultValue = "'" + arg.toString() + "'"
                } else if (arg in Map) {
                    defaultValue = arg.collectEntries { k, v ->
                        [(k): (v in String || v in Number) ? "'" + v + "'" : v]
                    }
                } else if (arg in RawSql) {
                    defaultValue = arg.getSql()
                }
            }

            return closureMap
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue ?: defaultDefaultValue
        }

        @Override
        public NullAware makeCopy() {
            return new ModifiableMapBasedSQLStatement(statementMap, getDefaultValue(), allowNulls)
        }

        @Override
        public NullAware makeNullableCopy() {
            return new ModifiableMapBasedSQLStatement(statementMap, getDefaultValue(), true)
        }

        @Override
        public DefaultAware makeNoDefaultCopy() {
            return new ModifiableMapBasedSQLStatement(statementMap, null, allowNulls)
        }

        @Override
        public Map<String,Closure<Object>> getClosureMap(HasClosureMap type) {
            return Collections.unmodifiableMap([
                allowing: { type.makeNullable(it) },
                initial_value: { type.setInitialValue(it) },
                default_value: { type.setDefaultValue(it) },
                default_sql: { type.setDefaultValue(new RawSql(it)) }
            ])
        }

        private void checkNotAlreadySpecified(boolean alreadySpecified, String keyword) {
            if (alreadySpecified) {
                throw new IllegalArgumentException(String.format(ALREADY_SPECIFIED, keyword))
            }
        }

        private static class RawSql {
            private def sql

            public RawSql(def sql) {
                boolean valid = sql in String || sql in Map || sql == null
                if (!valid) {
                    throw new IllegalArgumentException("Expected a String or Map of Strings following 'default_sql'")
                }
                this.sql = sql
            }

            public def getSql() {
                return sql
            }
        }
    }
}
