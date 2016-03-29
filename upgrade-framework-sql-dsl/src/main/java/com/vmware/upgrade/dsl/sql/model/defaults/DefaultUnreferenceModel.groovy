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

import com.vmware.upgrade.dsl.sql.model.UnreferenceModel;
import com.vmware.upgrade.dsl.sql.util.ConstraintNameUtil;
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory;
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@code DefaultUnreferenceModel} is the core implementation of {@link UnreferenceModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of the table before creation.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class DefaultUnreferenceModel implements UnreferenceModel {
    private static final String DROP_CONSTRAINT_SQL = "ALTER TABLE %s DROP CONSTRAINT %s";

    private static final String FOREIGN_KEY_NAME_FORMAT = "fk_%s2%s"
    private static final int FOREIGN_KEY_TABLE_NAME_LENGTH = 13

    protected def sourceTable
    protected def targetTable

    @Override
    public void setSourceTable(table) {
        this.sourceTable = table
    }

    @Override
    public void setTargetTable(table) {
        this.targetTable = table
    }

    @Override
    public String get(final DatabaseType databaseType) {
        if (sourceTable == null || targetTable == null) {
            throw new IllegalStateException("model is missing required atributes")
        }

        // table names might be a map
        sourceTable = SQLStatementFactory.create(sourceTable).get(databaseType)
        targetTable = SQLStatementFactory.create(targetTable).get(databaseType)

        final String referenceName = String.format(
            FOREIGN_KEY_NAME_FORMAT,
            ConstraintNameUtil.abbreviate(sourceTable, FOREIGN_KEY_TABLE_NAME_LENGTH),
            ConstraintNameUtil.abbreviate(targetTable, FOREIGN_KEY_TABLE_NAME_LENGTH)
        )

        return SQLStatementFactory.format(DROP_CONSTRAINT_SQL, databaseType, sourceTable, referenceName)
    }
}
