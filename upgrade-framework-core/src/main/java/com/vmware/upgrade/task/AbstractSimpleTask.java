/* ****************************************************************************
 * Copyright (c) 2011-2018 VMware, Inc. All Rights Reserved.
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

import com.vmware.upgrade.Task;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.impl.SimpleProgressReporter;

/**
 * An {@link AbstractSimpleTask} encapsulates the core functionality of a non-aggregating {@link Task}
 *
 * @see AbstractAggregateTask
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSimpleTask extends AbstractTask<SimpleProgressReporter> {
    private final int maximumProgress;

    /**
     * Constructs a named task which has an {@link SimpleProgressReporter#UNKNOWN} maximum progress
     *
     * @param name
     *          see {@link Task#getName()}
     * @throws IllegalArgumentException
     *          if name is {@code null}
     */
    public AbstractSimpleTask(final String name) {
        this(name, SimpleProgressReporter.UNKNOWN);
    }

    /**
     * Constructs a named task with the provided maximum progress
     * @param name
     *          see {@link Task#getName()}
     * @param maximumProgress
     *          maximum progress
     * @throws IllegalArgumentException
     *          if name is {@code null} or {@code maximumProgress <= 0}
     */
    public AbstractSimpleTask(final String name, final int maximumProgress) {
        super(name);

        if (maximumProgress <= 0) {
            throw new IllegalArgumentException("maximumProgress");
        }

        this.maximumProgress = maximumProgress;

        setReporter(new SimpleProgressReporter(this.maximumProgress));
    }

    /**
     * Increments progress by 1.
     */
    protected final void incrementProgress() {
        getReporter().advance();
    }

    /**
     * Delegates to {@link SimpleProgressReporter#setState}
     */
    protected void setState(final ExecutionState state) {
        getReporter().setState(state);
    }
}
