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

package com.vmware.upgrade.dsl.sql.util

import java.util.HashMap
import java.util.Map

import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement
import static com.vmware.upgrade.dsl.sql.util.ReservedKeywords.RESERVED

/**
 * A utility class to facilitate validation of strings to SQL-related DSL keywords.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class ValidationUtil {
    private static final int ORACLE_MAX_ENTITY_LENGTH = 30
    private static final int MSSQL_MAX_COMMENT_LENGTH = 128

    /**
     * Checks if the supplied {@code word} is a reserved keyword in any of the supported databases.
     *
     * @param word Either a {@link String}, {@link DatabaseType}-to-{@link String} {@link Map}, or {@link SQLStatement}.
     * @throws IllegalArgumentException if the supplied {@code word} is reserved.
     */
    static def validateNotReserved(def word) {
        final List<String> dbs = ReservedKeywords.KEYWORDS_FOR
        for (final String db : dbs) {
            final String sql = SQLStatementFactory.create(word).get(new DbType(db))

            if (RESERVED.get(sql.toUpperCase())?.contains(db)) {
                throw new IllegalArgumentException("'$sql' is a reserved keyword in: ${RESERVED.get(sql.toUpperCase())}")
            }
        }
    }

    /**
     * Oracle has a maximum column name length of 30 characters. Exceeding that limit results in an
     * ORA-00972. This validation method throws an {@link IllegalArgumentException} if the supplied
     * {@code entityName} exceeds that limit.
     *
     * @param entityName Either a {@link String}, {@link DatabaseType}-to-{@link String} {@link Map}, or {@link SQLStatement}.
     * @throws IllegalArgumentException if the supplied {@code entityName} would exceed {@link ORACLE_MAX_ENTITY_LENGTH} on Oracle.
     */
    static def validateEntityName(def entityName) {
        String oracleEntityName = SQLStatementFactory.create(entityName).get(new DbType("ORACLE"))

        if (oracleEntityName.length() > ORACLE_MAX_ENTITY_LENGTH) {
            throw new IllegalArgumentException("'$oracleEntityName' exceeds maximum length of $ORACLE_MAX_ENTITY_LENGTH characters")
        }
    }

    /**
     * MSSQL disallows comments which exceed 128 characters. This validation method throws an {@link IllegalArgumentException}
     * if the supplied {@code comment} exceeds that limit.
     *
     * @param comment Either a {@link String} or {@link DatabaseType}-to-{@link String} {@link Map}.
     * @throws IllegalArgumentException if the supplied {@code comment} would exceed {@link MSSQL_MAX_COMMENT_LENGTH} on MSSQL.
     */
    static def validateCommentLength(def comment) {
        String mssqlComment = SQLStatementFactory.create(comment).get(new DbType("MS_SQL"))

        if (mssqlComment.length() > MSSQL_MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("'$mssqlComment' exceeds maximum length of $MSSQL_MAX_COMMENT_LENGTH characters")
        }
    }

    private static class DbType extends DatabaseType {
        private String dbType;

        public DbType(String dbType) {
            this.dbType = dbType;
        }

        @Override
        public String toString() {
            return dbType;
        }

        @Override
        public String load(String string) {
            // Required by the contract for DatabaseType, but unused by SQLStatementFactory.
            throw new AssertionError();
        }
    }
}
