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

package com.vmware.upgrade.dsl.sql.model.defaults

import com.vmware.upgrade.dsl.sql.model.IndexModel
import com.vmware.upgrade.dsl.sql.util.ConstraintNameUtil
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@code DefaultIndexModel} is the core implementation of {@link IndexModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of the table before execution.
 *
 * @author Matthew Frost <mfrost@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class DefaultIndexModel implements IndexModel {
    private static final String CREATE_INDEX_SQL = "CREATE%sINDEX %s ON %s (%s)"
    private static final String UNIQUE_SQL = " UNIQUE "
    private static final String INDEX_NAME_PREFIX = "ix_"
    private static final String INDEX_NAME_SQL = INDEX_NAME_PREFIX + "%s"
    private static final int INDEX_NAME_MAX_LENGTH = 30 - INDEX_NAME_PREFIX.length()

    private def tableName
    private List columns = []
    private String unique = " "

    @Override
    public void setTable(tableName) {
        this.tableName = tableName
    }

    @Override
    public String getTable(DatabaseType databaseType) {
        return SQLStatementFactory.create(tableName).get(databaseType)
    }

    @Override
    public void addColumn(column) {
        columns.add(column)
    }

    @Override
    public void setUnique(boolean unique) {
        this.unique = (unique) ? UNIQUE_SQL : " "
    }

    protected String getIndexName(final DatabaseType databaseType) {
        final List cols = columns.collect{ SQLStatementFactory.create(it).get(databaseType) }
        final String indexCols = ConstraintNameUtil.abbreviate(cols.join("_"), INDEX_NAME_MAX_LENGTH)

        return String.format(INDEX_NAME_SQL, indexCols)
    }

    @Override
    public String get(final DatabaseType databaseType) {
        return SQLStatementFactory.format(
            CREATE_INDEX_SQL,
            databaseType,
            unique,
            getIndexName(databaseType),
            getTable(databaseType),
            columns.collect{ SQLStatementFactory.create(it).get(databaseType) }.join(", ")
        )
    }
}
