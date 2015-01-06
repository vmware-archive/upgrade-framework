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

package com.vmware.upgrade.dsl.syntax

import java.util.ArrayDeque
import java.util.Deque

import com.vmware.upgrade.dsl.Processor
import com.vmware.upgrade.dsl.TaskResolver
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel
import com.vmware.upgrade.task.ParallelAggregateTask
import com.vmware.upgrade.task.SerialAggregateTask

/**
 * Syntax to parse an upgrade object defining a {@link UpgradeDefinitionModel}.
 *
 * @author Emil Sit <sit@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class UpgradeDefinitionSyntax {
    boolean metadataAllowed = true
    UpgradeDefinitionModel upgrade
    TaskResolver taskResolver
    Processor processor
    private final Deque<String> queue = new ArrayDeque<>()

    private UpgradeDefinitionSyntax() {
    }

    UpgradeDefinitionSyntax(TaskResolver taskResolver, Processor processor) {
        this()
        this.processor = processor
        upgrade = new UpgradeDefinitionModel(taskResolver)
    }

    UpgradeDefinitionSyntax(String position, TaskResolver taskResolver, Processor processor) {
        this()
        this.processor = processor
        upgrade = new UpgradeDefinitionModel(position, taskResolver)
    }

    def methodMissing(String name, args) {
        def keywordProcessors = processor.getKeywordProcessors()
        if (name in keywordProcessors.keySet()) {
            def processor = keywordProcessors.get(name)
            processor.delegate = this

            return UnknownKeywordWrapper.wrap(processor.call(*args))
        }

        if (!(name in upgrade.properties.keySet())) {
            throw new UnknownKeywordException(name, args)
        }
        if (!metadataAllowed) {
            throw new UpgradeCompilationException("Attempted to set '${name}' when metadata not allowed")
        }
        if (upgrade."${name}" != null) {
            throw new IllegalArgumentException("Attempted to set '${name}' more than once. Current value is '${upgrade."${name}"}'")
        }
        if (args?.size() == 1) {
            upgrade."${name}" = args[0]
        } else {
            upgrade."${name}" = args
        }
    }

    def propertyMissing(String name) {
        def propertyProcessors = processor.getPropertyProcessors()

        for (Object propertyProcessor : propertyProcessors) {
            if (propertyProcessor.hasProperty(name)) {
                return propertyProcessor."${name}"
            }
        }

        throw new UnknownKeywordException("Unknown keyword '${name}'")
    }

    def addTask(name, taskClass, Object... args) {
        // Metadata is only allowed at the top of the upgrade, if at all.
        metadataAllowed = false
        upgrade.addTask name, taskClass, args
    }

    /**
     * Add an arbitrary Java class that implements Task to this upgrade
     * @return
     */
    def java(fullClassName) {
        def classObject = Class.forName(fullClassName)
        addTask fullClassName, classObject
    }

    private List<UpgradeDefinitionModel.TaskDescriptor> aggregateTasksFrom(Closure cl, Processor processor = processor) {
        // Recursively handle closure to get sub tasks
        UpgradeDefinitionModel subModel = new UpgradeDefinitionModel(taskResolver)
        UpgradeDefinitionSyntax subBuilder = new UpgradeDefinitionSyntax(
                upgrade: subModel,
                metadataAllowed: false,
                taskResolver: taskResolver,
                processor: processor
        )
        subBuilder.with(cl)
        subModel.tasks
    }

    /**
     * Add a parallel sub-task to this upgrade.
     */
    def parallel(String name, Closure cl) {
        addTask name, ParallelAggregateTask, aggregateTasksFrom(cl)
    }

    /**
     * Add a serial sub-task to this upgrade.
     */
    def serial(String name, Closure cl) {
        addTask name, SerialAggregateTask, aggregateTasksFrom(cl)
    }

    /**
     * Looks backwards through the stacktrace to find the first {@code doCall} method being called,
     * which should correspond to the call site of the innermost DSL keyword.
     *
     * When an upgrade step keyword, such as {@code sql} is being called, this will correspond to
     * the line where the keyword is used.
     */
    def getPosition() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace()
        for (StackTraceElement stackFrame : stackTraceElements) {
            if (stackFrame.getClassName().contains("_run_closure")) {
                return stackFrame.getFileName() + ":" + stackFrame.getLineNumber()
            }
        }
    }

    /**
     * Add an error message to the stack.
     *
     * @param error the error message to push
     */
    public void pushError(String error) {
        queue.push(error)
    }

    /**
     * Retrieve an error message and remove it from the stack.
     *
     *  @return the most recent error message as a {@link String}, or {@code null} if
     *  there are no errors on the stack
     */
    public String popError() {
        return (queue.isEmpty()) ? null : queue.pop()
    }
}
