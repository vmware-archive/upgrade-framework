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

package com.vmware.upgrade.dsl.sql.syntax

import com.vmware.upgrade.dsl.sql.model.ReferenceModel
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException
import java.lang.reflect.Method

/**
 * {@code ReferenceSyntax} defines the syntax for the {@code reference} keyword.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
class ReferenceSyntax {
    ReferenceModel model

    def sourceSide = false

    ReferenceSyntax(ReferenceModel model, String targetColumn) {
        this.model = model
        model.addToTargetColumns(targetColumn)
    }

    def and(column) {
        if (sourceSide) {
            model.addToSourceColumns(column)
        } else {
            model.addToTargetColumns(column)
        }

        this
    }

    def of(table) {
        if (sourceSide) {
            if (model.sourceColumns.size() != model.targetColumns.size()) {
                throw new IllegalStateException("Number of source columns does not equal number of target columns");
            }

            model.setSourceTable(table)
        } else {
            model.setTargetTable(table)
        }

        this
    }

    def from(column) {
        if (model.targetTable == null) {
            throw new IllegalStateException("No target table specified. An 'of' keyword is probably missing")
        }

        sourceSide = true
        and(column)
    }

    def named(referenceName) {
        model.setReferenceName(referenceName)
        this
    }

    def on_delete(action) {
        model.setDeleteConstraint(action)
        this
    }

    def propertyMissing(String name) {
        String msg
        if (this.getClass().methods.name.contains(name)) {
            msg = "Missing keyword following '$name'"
        } else {
            msg = "Unknown keyword '$name'"
        }
        throw new UnknownKeywordException(msg)
    }

}
