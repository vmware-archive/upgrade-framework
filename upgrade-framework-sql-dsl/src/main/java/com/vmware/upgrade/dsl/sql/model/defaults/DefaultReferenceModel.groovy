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

import com.vmware.upgrade.dsl.sql.model.ReferenceModel
import com.vmware.upgrade.dsl.sql.util.ConstraintNameUtil
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@code DefaultReferenceModel} is the core implementation of {@link ReferenceModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of the view before execution.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class DefaultReferenceModel implements ReferenceModel {
    private static final String ADD_CONSTRAINT_SQL =
        "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY ( %s ) REFERENCES %s ( %s )"

    private static final String FOREIGN_KEY_NAME_FORMAT = "fk_%s2%s"
    private static final int FOREIGN_KEY_TABLE_NAME_LENGTH = 13

    protected List sourceColumns = []
    protected List targetColumns = []

    protected def sourceTable
    protected def targetTable

    protected def deleteAction
    protected def updateAction

    private def referenceName

    @Override
    public void addToSourceColumns(sourceColumn) {
        this.sourceColumns.add(sourceColumn)
    }

    @Override
    public void addToTargetColumns(targetColumn) {
        this.targetColumns.add(targetColumn)
    }

    @Override
    public void setSourceTable(tableName) {
        this.sourceTable = tableName
    }

    @Override
    public void setTargetTable(tableName) {
        this.targetTable = tableName
    }

    @Override
    public void setReferenceName(name) {
        this.referenceName = name
    }

    @Override
    public void setDeleteConstraint(referentialAction) {
        if (referentialAction) {
            this.deleteAction = referentialAction
        }
    }

    @Override
    public void setUpdateConstraint(referentialAction) {
        if (referentialAction) {
            this.updateAction = referentialAction
        }
    }

    /**
     * Combines the on delete and on update constraints into a single {@link String} if applicable,
     * otherwise returns the constraint specified
     *
     * @param databaseType
     * @return a SQL fragment, as a {@link String}, for the propagation constraint(s), or null if
     * no propagation constraints were specified
     */
    private String createPropagationConstraint(final DatabaseType databaseType) {
        List<String> propagationConstraint = []
        if (deleteAction) {
            propagationConstraint.add("ON DELETE " + SQLStatementFactory.create(deleteAction).get(databaseType))
        }
        if (updateAction) {
            propagationConstraint.add("ON UPDATE " + SQLStatementFactory.create(updateAction).get(databaseType))
        }

        return propagationConstraint.join(" ")
    }

    @Override
    public String get(final DatabaseType databaseType) {
        if (referenceName == null) {
            // table names might be a map
            sourceTable = SQLStatementFactory.create(sourceTable).get(databaseType)
            targetTable = SQLStatementFactory.create(targetTable).get(databaseType)

            referenceName = String.format(
                FOREIGN_KEY_NAME_FORMAT,
                ConstraintNameUtil.abbreviate(sourceTable, FOREIGN_KEY_TABLE_NAME_LENGTH),
                ConstraintNameUtil.abbreviate(targetTable, FOREIGN_KEY_TABLE_NAME_LENGTH)
            )
        }

        final String propagationConstraint = createPropagationConstraint(databaseType)

        return SQLStatementFactory.format(
            (propagationConstraint) ? ADD_CONSTRAINT_SQL + " %s" : ADD_CONSTRAINT_SQL,
            databaseType,
            sourceTable,
            referenceName,
            sourceColumns.join(', '),
            targetTable,
            targetColumns.join(', '),
            propagationConstraint ?: ""
        )
    }
}
