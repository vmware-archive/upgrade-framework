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

import com.vmware.upgrade.dsl.Processor
import com.vmware.upgrade.sql.SQLStatement
import com.vmware.upgrade.sql.task.RawSQLTask
import com.vmware.upgrade.sql.task.ScriptTask

/**
 * A {@link Processor} which defines the basic {@code sql} and {@code file} keywords.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class BasicSqlProcessor implements Processor {
    Map<String, Closure> keywordProcessors = [
        "sql" : { arg ->
            final String name = getPosition()

            if (arg instanceof String) {
                final String rawSql = (String) arg

                addTask name, RawSQLTask, rawSql
            } else if (arg instanceof Map) {
                final Map rawSqlMap = (Map) arg

                try {
                    sql SQLStatementFactory.create(rawSqlMap)
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(e.getMessage() + " (" + getPosition() + ")", e)
                }
            } else if (arg instanceof SQLStatement) {
                final SQLStatement statementModel = (SQLStatement) arg

                addTask name, RawSQLTask, statementModel
            } else {
                throw new IllegalArgumentException("Expected the argument to the 'sql' keyword to be a String, Map, or SQLStatement at " + name)
            }
        },
        "file" : { String fileName -> addTask fileName, ScriptTask, fileName }
    ]

    @Override
    public Map<String, Closure<?>> getKeywordProcessors() {
        return keywordProcessors;
    }

    @Override
    public List<?> getPropertyProcessors() {
        return Collections.emptyList();
    }
}
