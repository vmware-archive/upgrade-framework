/* ****************************************************************************
 * Copyright (c) 2016 VMware, Inc. All Rights Reserved.
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
 * Extension of {@link Transformation} to hold information detailing how a {@link Task}
 * that adds or drops a reference would alter a database.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class ReferenceTransformation extends Transformation {

    private final String referencedTableName;
    private final boolean hasDeleteConstraint;

    public ReferenceTransformation(String tableName, String referencedTableName,
            boolean hasDeleteConstraint, TransformationType alterationType) {
        super(tableName, alterationType);
        if (alterationType != TransformationType.ADD_FOREIGN_KEY && alterationType != TransformationType.DROP_FOREIGN_KEY) {
            throw new AssertionError("alterationType must be either ADD_FOREIGN_KEY or DROP_FOREIGN_KEY");
        }
        this.referencedTableName = referencedTableName;
        this.hasDeleteConstraint = hasDeleteConstraint;
    }

    /**
     * The name of the table that would be referenced by a foreign key.
     *
     * @return {@link String} the referenced table name
     */
    public String getReferencedTableName() {
        return referencedTableName;
    }

    public boolean hasDeleteConstraint() {
        return hasDeleteConstraint;
    }

    @Override
    public String toString() {
        return String.format("Transforming table %s (referencing %s) via %s",
                tableName, referencedTableName, transformationType);
    }
}
