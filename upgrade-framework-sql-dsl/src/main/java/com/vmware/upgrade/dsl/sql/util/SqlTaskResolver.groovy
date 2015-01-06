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

import com.vmware.upgrade.Task
import com.vmware.upgrade.UpgradeContext
import com.vmware.upgrade.dsl.util.BasicTaskResolver
import com.vmware.upgrade.sql.task.RawSQLTask
import com.vmware.upgrade.sql.task.ScriptTask
import com.vmware.upgrade.sql.task.TransactionTask

class SqlTaskResolver extends BasicTaskResolver {
    @Override
    public Task resolve(UpgradeContext context, Class<?> taskClass, String name, List<?> args) {
        final Task t
        switch (taskClass) {
            case RawSQLTask:
                t = new RawSQLTask(name, context, args[0])
                break
            case ScriptTask:
                t = ScriptTask.from(context, args[0])
                break
            default:
                t = super.resolve(context, taskClass, name, args)
                break
        }
        return t
    }

    @Override
    public Task combine(UpgradeContext context, List<Task> tasks, String name) {
        return new TransactionTask("Transaction boundary for " + name, super.combine(context, tasks, name), context);
    }
}
