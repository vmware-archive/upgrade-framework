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

package com.vmware.upgrade.dsl.sql.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vmware.upgrade.sql.DatabaseType;
import com.vmware.upgrade.sql.SQLStatement;

/**
 * A factory that creates an {@link SQLStatement}.
 * <p>
 * The {@code create()} method will convert its list of arguments into a single
 * {@code SQLStatement}. When the {@code sql} keyword is used in an upgrade
 * definition, this will manage converting the strings and database-dependent
 * maps into single statement to be executed.
 *
 * @see SQLStatementFactory#create(Object...)
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class SQLStatementFactory  {
    private SQLStatementFactory() { }

    /**
     * Regardless of database type, always return the same SQL string. This
     * represents database-independent SQL.
     */
    private static class ConstantSQLStatement implements SQLStatement {
        private final String statement;

        public ConstantSQLStatement(final String statement) {
            this.statement = statement;
        }

        @Override
        public String get(final com.vmware.upgrade.sql.DatabaseType databaseType) {
            return statement;
        }

        @Override
        public String toString() {
            return ConstantSQLStatement.class.getSimpleName() + ": " + statement;
        }
    }

    /**
     * Return the appropriate database-dependent SQL string.
     */
    private static class MapBasedSQLStatement implements SQLStatement {
        private static final String DEFAULT_KEY_STRING = "default";

        private final String defaultValue;
        private final Map<String, String> map;

        /**
         * Internally creates an {@link EnumMap} from {@code statementMap}.
         *
         * @throws IllegalArgumentException if a key in {@code statementMap}
         *         can not be transformed (via capitalization) to a
         *         {@link DatabaseType}
         */
        public MapBasedSQLStatement(final Map<String, String> statementMap) {
            this.map = new HashMap<String, String>();

            for (final Map.Entry<String,String> entry : statementMap.entrySet()) {
                if (entry.getKey() != DEFAULT_KEY_STRING) {
                    map.put(entry.getKey().toUpperCase(), entry.getValue());
                }
            }

            defaultValue = statementMap.get(DEFAULT_KEY_STRING);
        }

        /**
         * {@inheritDoc}
         *
         * @throws IllegalArgumentException if the internal {@link EnumMap}
         *         does not have a key matching the given {@code databaseType}
         */
        @Override
        public String get(final DatabaseType databaseType) {
            final String keyString = databaseType.toString().toUpperCase();

            if (map.containsKey(keyString)) {
                return map.get(keyString);
            } else if (defaultValue != null) {
                return defaultValue;
            } else {
                throw new IllegalArgumentException("map does not contain a record for database type " + databaseType);
            }
        }

        @Override
        public String toString() {
            return MapBasedSQLStatement.class.getSimpleName() + ": " + map.toString();
        }
    }

    /**
     * Creates a {@link List} of {@link ConstantSQLStatement},
     * {@link MapBasedSQLStatement}, {@link SQLStatement} delegates.
     */
    private static class CompositeSQLStatement implements SQLStatement {
        private final List<SQLStatement> factories;

        /**
         * Create a list of delegate {@link SQLStatement}s
         *
         * @param parts any number of {@link String}, {@link Map Map&lt;String, String&gt;},
         *        and {@link SQLStatement} components
         * @throws IllegalArgumentException if {@code parts} contains an object
         *         of a type other than {@link String}, {@link SQLStatement} or
         *         {@link Map Map&lt;String, String&gt;}.
         */
        public CompositeSQLStatement(final Object... parts) {
            factories = new ArrayList<SQLStatement>();

            for (final Object part : parts) {
                final SQLStatement statement;

                if (part instanceof String) {
                    statement = new ConstantSQLStatement((String) part);
                } else if (part instanceof Map) {
                    final Map<?, ?> map = ((Map<?,?>) part);

                    // Verify the Map is really Map<String, String>
                    final Map<String, String> typedMap = new HashMap<String, String>(map.size());
                    for (final Map.Entry<?, ?> entry : map.entrySet()) {
                        final Object key = entry.getKey();
                        final Object value = entry.getValue();

                        if (!(key instanceof String)) {
                            throw new IllegalArgumentException("Parts contains map with key of unsupported type: " + key.getClass());
                        } else if (!(value instanceof String)) {
                            throw new IllegalArgumentException("Parts contains map with value of unsupported type: " + value.getClass());
                        } else {
                            typedMap.put((String) key, (String) value);
                        }
                    }

                    statement = new MapBasedSQLStatement(typedMap);
                } else if (part instanceof SQLStatement) {
                    statement = (SQLStatement) part;
                } else {
                    throw new IllegalArgumentException("Parts contains unsupported instance type: " + part.getClass());
                }

                factories.add(statement);
            }
        }

        @Override
        public String get(final com.vmware.upgrade.sql.DatabaseType databaseType) {
            final StringBuilder sb = new StringBuilder();

            for (final SQLStatement factory : factories) {
                sb.append(factory.get(databaseType));
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return CompositeSQLStatement.class.getSimpleName() + ": " + factories.toString();
        }
    }

    /**
     * Create a new {@link SQLStatement} such that the SQL string returned by
     * {@link SQLStatement#get(DatabaseType)} is the string one would get if
     * all the {@code parts} were appended together.
     *
     * @param parts any number of {@link String}, {@link Map Map&lt;String, String&gt;},
     *        and {@link SQLStatement} components
     * @return an {@link SQLStatement}
     * @throws IllegalArgumentException if {@code parts} contains an object
     *         of a type other than {@link String}, {@link Map}, or
     *         {@link SQLStatement}
     * @throws IllegalArgumentException if a key in {@code statementMap}
     *         can not be transformed (via capitalization) to a
     *         {@link DatabaseType}
     */
    public static SQLStatement create(final Object... parts) {
        return new CompositeSQLStatement(parts);
    }

    /**
     * Returns a formatted string based on the given {@code format}.
     * <p>
     * The {@code databaseType} is specified so that any database-dependent
     * {@code objects} can have the appropriate data selected from them.
     *
     * @param format A format compatible with
     *               {@link String#format(String, Object...) String.format}
     * @param databaseType
     * @param objects Objects compatible with
     *                {@link SQLStatementFactory#create(Object...)}
     * @return The formatted String
     */
    public static String format(String format, DatabaseType databaseType, Object... objects) {
        List<String> args = new ArrayList<String>();

        for (Object object : objects) {
            args.add(SQLStatementFactory.create(object).get(databaseType));
        }

        return String.format(format, args.toArray());
    }

    /**
     * Inspects a statement created by the factory to determine whether the supplied statement
     * contains any unexpected type information.
     *
     * @param statement The {@link SQLStatement} to inspect.
     * @param databaseTypes The {@link Set} of known types.
     * @return {@code true} if and only if the supplied {@link SQLStatement} contains records
     *          for one or more {@link DatabaseType}s that are not known.
     */
    public static boolean containsUnknownTypes(final SQLStatement statement, final Set<DatabaseType> databaseTypes, final boolean defaultValue) {
        if (statement instanceof ConstantSQLStatement) {
            return false;
        } else if (statement instanceof MapBasedSQLStatement) {
            final MapBasedSQLStatement typedStatement = (MapBasedSQLStatement) statement;

            final Set<String> knownTypeStrings = new HashSet<String>(databaseTypes.size());
            for (final DatabaseType type : databaseTypes) {
                knownTypeStrings.add(type.toString());
            }

            for (final String type : typedStatement.map.keySet()) {
                if (!knownTypeStrings.contains(type)) {
                    return true;
                }
            }

            return false;
        } else if (statement instanceof CompositeSQLStatement) {
            final CompositeSQLStatement typedStatement = (CompositeSQLStatement) statement;

            for (final SQLStatement subStatement : typedStatement.factories) {
                if (containsUnknownTypes(subStatement, databaseTypes, defaultValue)) {
                    return true;
                }
            }

            return false;
        } else {
            return defaultValue;
        }
    }
}
