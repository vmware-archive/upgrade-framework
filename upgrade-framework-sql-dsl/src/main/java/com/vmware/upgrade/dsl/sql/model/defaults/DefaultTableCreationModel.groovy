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

package com.vmware.upgrade.dsl.sql.model.defaults

import com.vmware.upgrade.dsl.sql.model.TableCreationModel
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.sql.util.ValidationUtil
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@code DefaultTableCreationModel} is the core implementation of {@link TableCreationModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * non-existence of the table before creation.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class DefaultTableCreationModel implements TableCreationModel {
    private static final String TABLE_CREATION_SQL = "CREATE TABLE %s ( %s )"

    protected def tableName

    private List columns = []
    private List constraints = []

    public DefaultTableCreationModel(tableName) {
        ValidationUtil.validateNotReserved(tableName)
        ValidationUtil.validateEntityName(tableName)
        this.tableName = tableName
    }

    @Override
    public void setColumns(List columns) {
        this.columns = columns
    }

    @Override
    public void setConstraints(List constraints) {
        this.constraints = constraints
    }

    @Override
    public String get(final DatabaseType databaseType) {
        String createDefinitions = columns*.get(databaseType).join(', ')
        tableName = SQLStatementFactory.create(tableName).get(databaseType)

        if (!constraints.isEmpty()) {
            createDefinitions += ", " + constraints*.getCreateInline(tableName).join(', ')
        }

        return SQLStatementFactory.format(TABLE_CREATION_SQL, databaseType, tableName, createDefinitions)
    }
}
