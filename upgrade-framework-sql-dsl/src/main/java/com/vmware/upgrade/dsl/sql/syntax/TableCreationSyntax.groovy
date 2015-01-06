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

import com.vmware.upgrade.dsl.sql.model.ConstraintModel
import com.vmware.upgrade.dsl.sql.model.TableCreationModel
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException

/**
 * {@code TableCreationSyntax} defines the syntax for the {@code create} keyword.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class TableCreationSyntax {
    TableCreationModel model

    TableCreationSyntax(TableCreationModel model) {
        this.model = model
    }

    private class ConstraintSyntax {
        def constraints = []
        ConstraintModel constraintModel

        def primary(column) {
            this.constraintModel = new ConstraintModel()
            constraintModel.addPrimary(column)

            constraints.add(constraintModel)

            return this
        }

        def unique(column) {
            this.constraintModel = new ConstraintModel()
            constraintModel.addUnique(column)

            constraints.add(constraintModel)

            return this
        }

        def and(column) {
            if (constraintModel == null) {
                throw new IllegalStateException("'and' keyword used before 'primary' or 'unique'")
            }

            constraintModel.addAdditionalColumn(column)

            return this
        }
    }

    def columns(Closure cl) {
        def aggregator = new TableCreationColumnSyntax()
        try {
            aggregator.with(cl)

            model.setColumns(aggregator.columns.toList())
        } catch (MissingPropertyException e) {
            throw new UnknownKeywordException("Unknown column type")
        }

        return this
    }

    def constraints(Closure cl) {
        def aggregator = new ConstraintSyntax()
        aggregator.with(cl)

        model.setConstraints(aggregator.constraints)

        this
    }
}
