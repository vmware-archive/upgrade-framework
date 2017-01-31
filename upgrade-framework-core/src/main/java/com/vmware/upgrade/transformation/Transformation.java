/* ****************************************************************************
 * Copyright (c) 2016-2017 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.transformation;

import com.vmware.upgrade.Task;

/**
 * Holds information detailing how a {@link Task} would alter a database.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class Transformation {

    protected final String tableName;
    protected final String columnName;
    protected final TransformationType transformationType;

    public Transformation(String tableName, TransformationType alterationType) {
        this(tableName, null, alterationType);
    }

    public Transformation(String tableName, String columnName, TransformationType transformationType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.transformationType = transformationType;
    }

    /**
     * The name of the table that would be altered.
     *
     * @return {@link String} the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * The name of the column that would be altered.
     *
     * @return {@link String} if the transformation alters a column, or {@code null}
     * if not applicable.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * An {@link TransformationType} indicating how the transformation would alter the table
     * and/or column.
     *
     * @return {@link TransformationType}
     */
    public TransformationType getTransformationType() {
        return transformationType;
    }

    @Override
    public String toString() {
        if (columnName == null) {
            return String.format("Transforming table %s via %s", tableName, transformationType);
        }

        return String.format("Transforming column %s of table %s via %s", columnName, tableName, transformationType);
    }

    public enum TransformationType {
        ADD_COLUMN_NOT_NULL,
        ADD_COLUMN_NOT_NULL_DEFAULT,
        ADD_COLUMN_NOT_NULL_INITIAL,
        ADD_COLUMN_NULL,
        DROP_COLUMN,
        RENAME_COLUMN,
        RETYPE_COLUMN,
        ADD_CONSTRAINT,
        DROP_CONSTRAINT,
        CREATE_TABLE,
        ADD_FOREIGN_KEY,
        DROP_FOREIGN_KEY,
        ADD_NON_UNIQUE_INDEX,
        ADD_UNIQUE_INDEX,
        DROP_INDEX,
        COMMENT
    }
}
