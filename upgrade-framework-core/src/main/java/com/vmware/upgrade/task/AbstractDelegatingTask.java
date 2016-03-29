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

import java.util.Arrays;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReporter;
import com.vmware.upgrade.progress.impl.SimpleAggregatingProgressReporter;
import com.vmware.upgrade.progress.impl.SimpleProgressReporter;

/**
 * {@link Task} that encapsulates another {@link Task}. Executing an {@link AbstractDelegatingTask}
 * involves delegating to the encapsulated task to get work done.
 *
 * @author Ankit Shah ankitsha@vmware.com
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractDelegatingTask extends AbstractTask<SimpleAggregatingProgressReporter> {
    private final Task delegateTask;

    /**
     * A {@link ProgressReporter} to track the "internal" share of the work.
     */
    private final SimpleProgressReporter internalReporter;

    /**
     * Constructs a named task enclosing the provided delegate task which has
     * {@link SimpleProgressReporter#UNKNOWN} "ticks" of internal progress.
     *
     * @param name
     *          see {@link Task#getName()}
     * @param task
     *          the delegate to execute
     * @throws IllegalArgumentException
     *          if {@code task} is {@code null}
     */
    protected AbstractDelegatingTask(final String name, final Task task) {
        this(name, task, SimpleProgressReporter.UNKNOWN);
    }

    /**
     * Constructs a named task enclosing the provided delegate task
     * with the specified number of "ticks" of internal progress.
     *
     * @param name
     *          see {@link Task#getName()}
     * @param task
     *          the delegate to execute
     * @param ticks
     *          total number of ticks; must be {@code > 0}
     * @throws IllegalArgumentException
     *          if {@code task} is {@code null}
     *          if {@code ticks} is {@code <= 0}
     */
    protected AbstractDelegatingTask(final String name, final Task task, final int ticks) {
        super(name);

        if (task == null) {
            throw new IllegalArgumentException("task");
        }

        this.delegateTask = task;
        this.internalReporter = new SimpleProgressReporter(ticks);

        createAndSetReporter();
    }

    private void createAndSetReporter() {
        SimpleAggregatingProgressReporter parentReporter = new SimpleAggregatingProgressReporter();

        parentReporter.setChildren(Arrays.asList(internalReporter, delegateTask));

        setReporter(parentReporter);
    }

    /**
     * Records progress of the "internal" work (i.e. work that is not part of the delegate task).
     *
     * This method but should only be called during task execution (i.e. within the {@link #doCall()} stack).
     *
     * @see SimpleProgressReporter#advance()
     */
    protected final void advance() {
        internalReporter.advance();
    }

    protected final Task getDelegateTask() {
        return delegateTask;
    }

    /**
     * Invokes {@link #doCall()} in a context within which {@link #advance()} can be called.
     *
     * {@inheritDoc}
     */
    @Override
    public final Void call() throws Exception {
        internalReporter.setState(ExecutionState.RUNNING);
        try {
            doCall();
            internalReporter.setState(ExecutionState.COMPLETED);
        } catch (Exception e) {
            getReporter().terminateWithFailure();
            throw e;
        }

        return null;
    }

    /**
     * Performs the body of work (including execution of the delegate task).
     *
     * It is expected that any concrete implementation of this class which overrides this method
     * will use {@code super.doCall()} to execute the delegate task.
     *
     * Any exception thrown will cause the state to be reported as {@link ExecutionState#FAILED}.
     *
     * @throws Exception if the delegate task throws an exception.
     */
    protected void doCall() throws Exception {
        delegateTask.call();
    }

    @Override
    public String toString() {
        return "Wrapper around " + delegateTask.getName();
    }
}
