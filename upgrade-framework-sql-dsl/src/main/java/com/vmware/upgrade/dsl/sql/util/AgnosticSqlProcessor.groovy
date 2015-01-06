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

package com.vmware.upgrade.dsl.sql.util

import com.vmware.upgrade.dsl.sql.model.CommentModel
import com.vmware.upgrade.dsl.sql.model.IndexModel
import com.vmware.upgrade.dsl.sql.model.ReferenceModel
import com.vmware.upgrade.dsl.sql.model.SQLStatementProxy
import com.vmware.upgrade.dsl.sql.model.TableAlterationModel
import com.vmware.upgrade.dsl.sql.model.TableCreationModel
import com.vmware.upgrade.dsl.sql.model.UnindexModel
import com.vmware.upgrade.dsl.sql.model.UnreferenceModel
import com.vmware.upgrade.dsl.sql.model.defaults.DefaultSQLStatementProxy
import com.vmware.upgrade.dsl.sql.model.safe.SafeSQLStatementProxy
import com.vmware.upgrade.dsl.sql.syntax.ColumnType
import com.vmware.upgrade.dsl.sql.syntax.CommentSyntax
import com.vmware.upgrade.dsl.sql.syntax.Constraints
import com.vmware.upgrade.dsl.sql.syntax.IndexSyntax
import com.vmware.upgrade.dsl.sql.syntax.ReferenceSyntax
import com.vmware.upgrade.dsl.sql.syntax.TableAlterationSyntax
import com.vmware.upgrade.dsl.sql.syntax.TableCreationSyntax
import com.vmware.upgrade.dsl.sql.syntax.UnindexSyntax
import com.vmware.upgrade.dsl.sql.syntax.UnreferenceSyntax
import com.vmware.upgrade.dsl.util.AggregateProcessor
import com.vmware.upgrade.sql.SQLStatement
import com.vmware.upgrade.task.SerialAggregateTask

/**
 * A {@link Processor} which defines keywords for expressing SQL in a database-agnostic way.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class AgnosticSqlProcessor extends BasicSqlProcessor {
    SQLStatementProxy sqlStatementProxy

    Map<String, Closure> keywordProcessors = [
        "drop_view" : { viewName ->
            final SQLStatement model = sqlStatementProxy.dropView(viewName)
            sql(model)
        },
        "create" : { tableName ->
            final TableCreationModel model = sqlStatementProxy.create(tableName)
            sql(model)
            return new TableCreationSyntax(model)
        },
        "alter" : { tableName ->
            final TableAlterationModel model = sqlStatementProxy.alter(tableName)
            sql(model)
            return new TableAlterationSyntax(model)
        },
        "reference" : { columnName ->
            final ReferenceModel model = sqlStatementProxy.reference()
            sql(model)
            return new ReferenceSyntax(model, columnName)
        },
        "unreference" : { tableName ->
            final UnreferenceModel model = sqlStatementProxy.unreference()
            sql(model)
            return new UnreferenceSyntax(model, tableName)
        },
        "index" : { firstColumn ->
            final IndexModel model = sqlStatementProxy.index()
            sql(model)
            return new IndexSyntax(model, firstColumn)
        },
        "index_unique" : { firstColumn ->
            final IndexModel model = sqlStatementProxy.index()
            sql(model)
            model.setUnique(true)
            return new IndexSyntax(model, firstColumn)
        },
        "unindex" : { firstColumnOrName ->
            final UnindexModel model = sqlStatementProxy.unindex()
            sql(model)
            return new UnindexSyntax(model, firstColumnOrName)
        },
        "comment" : { text ->
            final CommentModel model = sqlStatementProxy.comment()
            sql(model)
            return new CommentSyntax(model, text)
        },
        "safe" : { reason, cl = null ->
            if (reason == null || cl == null) {
                throw new IllegalArgumentException("When using the 'safe' keyword, you must supply a reason for doing so. Usage: safe('your reason') { ... }")
            }
            /*
             * Creating a serial sub task that uses the safe statement proxy is a
             * nice way to ensure that once the safe block ends, the default proxy
             * goes back into use.
             */
            def safeProcessor = new AggregateProcessor(processor, new AgnosticSqlProcessor(new SafeSQLStatementProxy()))

            addTask reason, SerialAggregateTask, aggregateTasksFrom(cl, safeProcessor)
        }
    ]
    List<Object> propertyProcessors = [new ColumnType(), new Constraints()]

    public AgnosticSqlProcessor() {
        this(new DefaultSQLStatementProxy())
    }

    private AgnosticSqlProcessor(SQLStatementProxy sqlStatementProxy) {
        this.sqlStatementProxy = sqlStatementProxy
    }

    @Override
    public Map<String, Closure<?>> getKeywordProcessors() {
        return super.getKeywordProcessors() + keywordProcessors + ColumnType.getKeywords()
    }

    @Override
    public List<?> getPropertyProcessors() {
        return super.getPropertyProcessors() + propertyProcessors
    }
}
