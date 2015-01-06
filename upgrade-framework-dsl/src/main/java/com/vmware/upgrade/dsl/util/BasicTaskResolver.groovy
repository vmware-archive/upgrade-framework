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

package com.vmware.upgrade.dsl.util

import java.util.concurrent.Executors

import com.vmware.upgrade.Task
import com.vmware.upgrade.UpgradeContext
import com.vmware.upgrade.dsl.TaskResolver
import com.vmware.upgrade.task.ParallelAggregateTask
import com.vmware.upgrade.task.SerialAggregateTask

class BasicTaskResolver implements TaskResolver {
    @Override
    public Task resolve(UpgradeContext context, Class<?> taskClass, String name, List<?> args) {
        // Find the first constructor that has a signature we can fulfill
        final Task t
        switch (taskClass) {
            case SerialAggregateTask:
                t = new SerialAggregateTask(context, name, args[0].collect { resolve(context, it.taskClass, it.name, it.args) })
                break
            case ParallelAggregateTask:
                def executor = Executors.newFixedThreadPool(10)
                // XXX Manage the lifecycle of this executor
                t = new ParallelAggregateTask(context, name, executor, args[0].collect { resolve(context, it.taskClass, it.name, it.args) })
                break
            default:
                t = attemptTaskConstruction(context, taskClass, args)
                break
        }
        return t
    }

    @Override
    public Task combine(UpgradeContext context, List<Task> tasks, String name) {
        return new SerialAggregateTask(context, name, tasks);
    }

    protected Task attemptTaskConstruction(UpgradeContext context, Class taskClass, List args) {
        // Use reflection to try and find something we can construct
        def knownConstructors = [
                    [UpgradeContext]: [context]
                ]
        def o = taskClass.constructors.findResult {
            def consFormal = it.parameterTypes
            def consActual = knownConstructors.findResult {
                def kcArgCount = it.key.size()
                def argCount = args.size()
                def constructorArgCount = consFormal.size()

                if (kcArgCount + argCount == constructorArgCount &&
                consFormal[0..it.key.size()-1] == it.key) {
                    return it.value + args
                }
                return null
            }
            if (consActual != null) {
                return it.class.metaClass.invokeMethod(it, 'newInstance', consActual.toArray())
            } else {
                return null
            }
        }
        return o as Task
    }
}
