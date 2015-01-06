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

import com.vmware.upgrade.sql.SQLStatement;

/**
 * {@code TableAlterationModel} is an {@link SQLStatement} that represents the
 * alteration of table.
 * <p>
 * Supported alterations include:
 * <ul>
 *   <li>Adding a column</li>
 *   <li>Dropping a column</li>
 *   <li>Renaming a column</li>
 *   <li>Change the type of a column</li>
 *   <li>Drop primary/unique keys</li>
 * </ul>
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public interface TableAlterationModel extends SQLStatement {
    /**
     * Add a column of the specified type.
     *
     * @param name column name
     * @param type column type
     */
    public void addColumn(name, type)

    /**
     * Drop a column by the specified name.
     *
     * @param name column name
     */
    public void dropColumn(name)

    /**
     * Rename a column.
     *
     * @param name original column name
     * @param newName new column name
     */
    public void renameColumn(name, newName)

    /**
     * Change the type of the specified column.
     *
     * @param name column name
     * @param type new column type
     */
    public void retypeColumn(name, type)

    /**
     * Set the {@link ConstraintModel} to be used when altering a table's constraints.
     */
    public void setConstraint(ConstraintModel constraint)
}
