/* ****************************************************************************
 * Copyright (c) 2011-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.sql.script;

import java.util.ArrayList;
import java.util.List;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.sql.task.RawSQLTask;
import com.vmware.upgrade.task.SerialAggregateTask;

/**
 * Captures each individual SQL as a {@link RawSQLTask} and returns the list of such
 * {@link RawSQLTask}s as a {@link SerialAggregateTask}
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class TaskAggregator implements SQLParsedDataAggregator<Task> {
    final List<Task> sqlTaskList = new ArrayList<Task>();

    final String taskName;
    final UpgradeContext context;

    /**
     * Constructor
     *
     * @param context
     *            {@link UpgradeContext} representing database to communicate to. The aggregator
     *            does not communicate with the database, but is used to create {@link RawSQLTask}s
     *            which will communicate with the database at runtime.
     * @param taskName
     *            Name for the resulting task
     */
    public TaskAggregator(UpgradeContext context, String taskName) {
        this.taskName = taskName;
        this.context = context;
    }

    /**
     * Capture the SQL command as {@link RawSQLTask}. The task's name is generated as:
     * "&lt;filename&gt;[&lt;startLineNumber&gt;-&lt;endLineNumber&gt;]"
     *
     * @param startLineNo
     *            line number for start of the SQL in the file
     * @param endLineNo
     *            line number for end of the SQL in the file
     * @param sql
     *            the SQL that was parsed
     */
    @Override
    public void append(int startLineNo, int endLineNo, String sql) {
        sqlTaskList.add(new RawSQLTask(taskName + "[" + startLineNo + "-" + endLineNo + "]",
                                       context, sql));
    }

    /**
     * @return Returns the parsed input as a {@link SerialAggregateTask} of {@link RawSQLTask}s
     */
    @Override
    public Task getParsedData() {
        return new SerialAggregateTask(context, taskName, sqlTaskList);
    }
}
