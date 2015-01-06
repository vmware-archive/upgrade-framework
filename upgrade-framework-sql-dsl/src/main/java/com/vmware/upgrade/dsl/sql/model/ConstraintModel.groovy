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

package com.vmware.upgrade.dsl.sql.model

import com.vmware.upgrade.dsl.sql.util.ConstraintNameUtil

/**
 * {@code ConstraintModel} manages the different supported constraint types.
 * <p>
 * A model can be built to represent adding/dropping primary/unique constraints
 * and partial-sql can be returned to be used in conjunction with create or
 * alter statements.
 * <p>
 * Constraints names are generated using the following naming convention:
 * <ol>
 *   <li>Primary or Unique qualifier: {@code pk} or {@code uk}</li>
 *   <li>Table name: Without this, the generated name would not be unique enough
 *       in a system table (e.g. pk_id vs. pk_table1_id).</li>
 *   <li>Column name (if a composite constraint is being created, then all
 *       referenced columns will be underscore separated.)</li>
 * </ol>
 * The above are joined using an underscore.
 * <p>
 * As an example, if a primary key on the "id" column of a table named
 * "example" was to be created, it would have the name "pk_example_id".
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class ConstraintModel {
    private static final String CREATE_INLINE_CONSTRAINT_SQL = "CONSTRAINT %s %s ( %s )"
    private static final String DROP_INLINE_CONSTRAINT_SQL = "%s CONSTRAINT %s"
    private static final String ADD_INLINE_CONSTRAINT_SQL = "%s CONSTRAINT %s %s ( %s )"

    private static final String CONSTRAINT_NAME_FORMAT = "%s_%s_%s"
    private static final int CONSTRAINT_NAME_MAX_PART_LENGTH = 13

    private static enum ConstraintType {
        ADD_PRIMARY_KEY("ADD", "PRIMARY KEY", "pk"),
        ADD_UNIQUE_KEY("ADD", "UNIQUE", "uq"),
        DROP_PRIMARY_KEY("DROP", "PRIMARY KEY", "pk"),
        DROP_UNIQUE_KEY("DROP", "UNIQUE", "uq")

        private final def action
        private final def keyword
        private final def columnPrefix

        private ConstraintType(action, keyword, columnPrefix) {
            this.action = action
            this.keyword = keyword
            this.columnPrefix = columnPrefix
        }
    }

    private ConstraintType type

    private def names = []

    /**
     * For a composite constraint, add an additional columns to the model.
     */
    public void addAdditionalColumn(column) {
        names.add(column)
    }

    /**
     * Model represents the addition of a primary key.
     */
    public void addPrimary(column) {
        type = ConstraintType.ADD_PRIMARY_KEY
        names.add(column)
    }

    /**
     * Model represents the addition of a unique key.
     */
    public void addUnique(column) {
        type = ConstraintType.ADD_UNIQUE_KEY
        names.add(column)
    }

    /**
     * Model represents dropping a primary key.
     */
    public void dropPrimary(column) {
        type = ConstraintType.DROP_PRIMARY_KEY
        names.add(column)
    }

    /**
     * Model represents dropping a unique key.
     */
    public void dropUnique(column) {
        type = ConstraintType.DROP_UNIQUE_KEY
        names.add(column)
    }

    /**
     * Return a string representing the constraint clause of a create table
     * statement.
     */
    public String getCreateInline(String tableName) {
        final String constraintName = generateConstraintName(type.columnPrefix, tableName, names.join('_'))

        return String.format(CREATE_INLINE_CONSTRAINT_SQL, constraintName, type.keyword, names.join(', '))
    }

    /**
     * Return a string representing the constraint action to be performed in an
     * alter table statement.
     */
    public String getAlterInline(String tableName) {
        final String constraintName = generateConstraintName(type.columnPrefix, tableName, names.join('_'))

        if (type == ConstraintType.ADD_PRIMARY_KEY || type == ConstraintType.ADD_UNIQUE_KEY) {
            return String.format(ADD_INLINE_CONSTRAINT_SQL, type.action, constraintName, type.keyword, names.join(', '))
        } else {
            return String.format(DROP_INLINE_CONSTRAINT_SQL, type.action, constraintName)
        }
    }

    private String generateConstraintName(String prefix, String tableName, String columns) {
        final String abbrTableName = ConstraintNameUtil.abbreviate(tableName, CONSTRAINT_NAME_MAX_PART_LENGTH)
        int columnsSectionMaxLength = CONSTRAINT_NAME_MAX_PART_LENGTH

        if (abbrTableName.length() < CONSTRAINT_NAME_MAX_PART_LENGTH) {
            columnsSectionMaxLength += (CONSTRAINT_NAME_MAX_PART_LENGTH - abbrTableName.length())
        }

        final String abbrColumns = ConstraintNameUtil.abbreviate(columns, columnsSectionMaxLength)

        return String.format(
            CONSTRAINT_NAME_FORMAT,
            prefix,
            abbrTableName,
            abbrColumns
        )
    }
}
