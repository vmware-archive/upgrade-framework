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

package com.vmware.upgrade.sql.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.sql.DatabasePersistenceContext;
import com.vmware.upgrade.sql.DatabaseType;
import com.vmware.upgrade.sql.script.TaskAggregator;
import com.vmware.upgrade.task.AbstractDelegatingTask;
import com.vmware.upgrade.task.SerialAggregateTask;

/**
 * Task that {@linkplain DatabaseType#load loads} and then
 * {@linkplain DatabasePersistenceContext#parseWithAggregator parses} an SQL script and executes
 * the resulting {@link RawSQLTask}s.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public final class ScriptTask extends AbstractDelegatingTask  {
    private ScriptTask(final String filename, final Task task) {
        super(filename, task);
    }

    /**
     * Factory method to read the input file and construct a {@link ScriptTask} that encapsulates the
     * individual sql statements as a {@link SerialAggregateTask} of {@link RawSQLTask}s
     *
     * @param context
     *            the {@link UpgradeContext} within which the {@link Task} should be run
     * @param filename
     *            the file to create a task for
     * @return {@link ScriptTask} encapsulating the script file
     * @throws IOException
     *             if there is a problem reading the file
     */
    public static ScriptTask from(final UpgradeContext context, final String filename) throws IOException {
        final DatabasePersistenceContext databaseContext = context.getPersistenceContext(DatabasePersistenceContext.class);

        final String contents = databaseContext.getDatabaseType().load(filename);

        final TaskAggregator taskAggregator = new TaskAggregator(context, filename);
        final Task delegate = databaseContext.parseWithAggregator(contents, taskAggregator);

        return new ScriptTask(filename, delegate);
    }

    /**
     * Factory method to create a {@link List} of {@link ScriptTask}s based on multiple files.
     *
     * @param context
     *            the {@link UpgradeContext} within which the {@link Task} should be run
     * @param filenames
     *            the files to create a tasks for
     * @return {@link List} of {@link ScriptTask}s encapsulating the script files
     * @throws IOException
     *             if there is a problem reading one of the files
     */
    public static List<ScriptTask> from(final UpgradeContext context, final String ... filenames) throws IOException {
        final List<ScriptTask> tasks = new ArrayList<ScriptTask>(filenames.length);

        for (final String fileName : filenames) {
            final ScriptTask task = ScriptTask.from(context, fileName);
            tasks.add(task);
        }

        return tasks;
    }

    /**
     * @return name of the file from which this file task has been constructed.
     */
    public String getFileName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ScriptTask [fileName=" + getFileName() + "]";
    }
}
