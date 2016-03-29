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

package com.vmware.upgrade.dsl.model

import com.vmware.upgrade.Task
import com.vmware.upgrade.UpgradeContext
import com.vmware.upgrade.dsl.TaskResolver

/**
 * A DSL model object encapsulating the information necessary to construct a {@link Task}.
 *
 * @see #createTask(UpgradeContext)
 *
 * @author Emil Sit sit@vmware.com
 * @version 1.0
 * @since 1.0
 */
class UpgradeDefinitionModel {
    String name
    String position
    TaskResolver taskResolver

    /**
     * A TaskDescriptor captures how we can construct a concrete Task when instantiated
     */
    class TaskDescriptor {
        def name
        def taskClass
        List<?> args
    }
    private List<TaskDescriptor> tasks = []

    public UpgradeDefinitionModel(TaskResolver taskResolver) {
        this.taskResolver = taskResolver
    }

    public UpgradeDefinitionModel(String position, TaskResolver taskResolver) {
        this.position = position
        this.taskResolver = taskResolver
    }

    /**
     * Convenience method to add a task to this model
     *
     * @param name the name of the task, to be used during logging
     * @param taskClass the Class object for the type of task to create
     * @param args any additional constructor args for the taskClass
     */
    def addTask(name, taskClass, Object... args) {
        if (!implementsTask(taskClass)) {
            throw new IllegalArgumentException("${taskClass} does not implement Task")
        }

        // Is there no better way to do this in groovy? XXX
        def t = new TaskDescriptor()
        t.args = args as List
        t.name = name
        t.taskClass = taskClass
        tasks << t
    }

    List<TaskDescriptor> getTasks() {
        Collections.unmodifiableList(tasks)
    }

    /**
     * @return true if c.newInstance(...) instanceOf com.vmware.vcloud.upgrade.Task would be true
     */
    private static boolean implementsTask(Class c) {
        // Perhaps there is a better way of doing this...
        def interfaces = [] as Set
        while (c != Object) {
            interfaces.addAll(c.interfaces)
            c = c.getSuperclass()
        }
        return com.vmware.upgrade.Task in interfaces
    }

    List<Task> instantiate(UpgradeContext context) {
        tasks.collect { taskResolver.resolve(context, it.taskClass, it.name, it.args) }
    }

    Task createTask(UpgradeContext context) {
        taskResolver.combine(context, instantiate(context), name ?: "<null>")
    }

    @Override
    public String toString() {
        return UpgradeDefinitionModel.class.getSimpleName() + " " + name + " containing tasks: " + tasks.toString()
    }
}
