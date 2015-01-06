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

package com.vmware.upgrade.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.impl.SimpleAggregatingProgressReporter;

/**
 * An {@link AbstractAggregateTask} encapsulates the core functionality of a {@link Task} which
 * aggregates other {@link Task}s
 *
 * @see AbstractSimpleTask
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractAggregateTask extends AbstractTask<SimpleAggregatingProgressReporter> {

    private final List<Task> children;

    /**
     * Constructs a named task aggregating the supplied children
     *
     * @param name
     *          see {@link Task#getName()}
     * @param children
     *          the children to execute
     * @throws IllegalArgumentException
     *          if {@code children} is {@code null}
     */
    public AbstractAggregateTask(final String name, final List<Task> children) {
        super(name);

        if (children == null) {
            throw new IllegalArgumentException("children");
        }

        if (children.contains(null)) {
            throw new IllegalArgumentException("children");
        }

        this.children = new ArrayList<Task>(children.size());
        for (final Task child : children) {
            this.children.add(child);
        }

        createAndSetReporter();
    }

    private void createAndSetReporter() {
        SimpleAggregatingProgressReporter reporter =
            new SimpleAggregatingProgressReporter();

        if (!children.isEmpty()) {
            reporter.setChildren(children);
        }

        setReporter(reporter);
    }

    /**
     * Returns all tasks
     *
     * @return an unmodifiable list of {@link Task}s
     */
    protected final List<Task> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Delegate to {@link SimpleAggregatingProgressReporter#getState()}
     */
    protected final ExecutionState getState() {
        return getReporter().getState();
    }
}

