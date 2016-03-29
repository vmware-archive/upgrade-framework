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

package com.vmware.upgrade.dsl.sql.syntax

import com.vmware.upgrade.dsl.sql.model.ConstraintModel
import com.vmware.upgrade.dsl.sql.model.TableAlterationModel
import com.vmware.upgrade.dsl.sql.util.ColumnTypeSyntaxUtil
import com.vmware.upgrade.dsl.sql.util.ValidationUtil

/**
 * {@code TableAlterationSyntax} defines the syntax for the {@code alter} keyword.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
class TableAlterationSyntax {
    TableAlterationModel model
    ConstraintModel constraintModel

    TableAlterationSyntax(TableAlterationModel model) {
        this.model = model
    }

    def add(column) {
        ValidationUtil.validateEntityName(column)

        return [storing: { type ->
            def columnType = ColumnTypeSyntaxUtil.getColumnType(type)
            model.addColumn(column, columnType)

            return ColumnTypeSyntaxUtil.getAllowingNullSyntax(columnType)
        }]
    }

    def drop(column) {
        model.dropColumn(column)
    }

    def rename(column) {
        return [to: { newName ->
            ValidationUtil.validateEntityName(newName)

            model.renameColumn(column, newName)
        }]
    }

    def retype(column) {
        return [to: { type ->
            def newType = ColumnTypeSyntaxUtil.getColumnType(type)
            model.retypeColumn(column, newType)

            return ColumnTypeSyntaxUtil.getAllowingNullSyntax(newType)
        }]
    }

    def add_primary(column) {
        this.constraintModel = new ConstraintModel()
        constraintModel.addPrimary(column)

        model.setConstraint(constraintModel)

        this
    }

    def drop_primary(column) {
        this.constraintModel = new ConstraintModel()
        constraintModel.dropPrimary(column)

        model.setConstraint(constraintModel)

        this
    }

    def add_unique(column) {
        this.constraintModel = new ConstraintModel()
        constraintModel.addUnique(column)

        model.setConstraint(constraintModel)

        this
    }

    def drop_unique(column) {
        this.constraintModel = new ConstraintModel()
        constraintModel.dropUnique(column)

        model.setConstraint(constraintModel)

        this
    }

    def and(column) {
        if (constraintModel == null) {
            throw new IllegalStateException("'and' keyword used before 'drop_primary' or 'drop_unique'")
        }

        constraintModel.addAdditionalColumn(column)

        this
    }
}
