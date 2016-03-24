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

package com.vmware.upgrade.progress.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ExecutionStateAggregator;
import com.vmware.upgrade.progress.ProgressReport;
import com.vmware.upgrade.progress.ProgressReporter;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

/**
 * An implementation of {@link ProgressReporter} which aggregates progress and state of child tasks.
 * <p>
 * Progress is aggregated so that each child task contributes equally to the overall progress of the parent task.
 * <p>
 * State is aggregated using {@link ExecutionStateAggregator#aggregate(java.util.Collection)}.
 *
 * @see ExecutionStateAggregator#aggregate(java.util.Collection)
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class SimpleAggregatingProgressReporter extends AbstractProgressReporter {

    /**
     * A listener which tracks the last reported progress and
     * notifies its parent when new progress arrives by invoking parent's {@code recalculate} method.
     */
    private class PropagatingListener implements ProgressReporter.ProgressListener {
        private final AtomicReference<ProgressReport> latestReport =
            new AtomicReference<ProgressReport>(new ImmutableProgressReport(ExecutionState.PENDING, 0));

        private final ProgressReporter reporter;

        /**
         * Constructor. Initializes {@code reporter} with specified instance of {@link ProgressReporter}.
         *
         * @param reporter
         *              an instance of {@link ProgressReporter} to monitor; must be non-null
         * @throws IllegalArgumentException if {@code reporter} is {@code null}
         */
        public PropagatingListener(final ProgressReporter reporter) {
            if (reporter == null) {
                throw new IllegalArgumentException("reporter");
            }

            this.reporter = reporter;
        }

        /**
         * Starts listening to progress reports from {@code reporter} specified in the constructor.
         * <p>
         * This method must be called only once.
         */
        public void start() {
            reporter.addListener(this);
            reporter.requestProgressReport();
        }

        /**
         * Starts listening to progress reports from {@code reporter} specified in the constructor.
         * <p>
         * This method must be called only once and only if {@code start} method has been called earlier.
         */
        public void stop() {
            reporter.removeListener(this);
        }

        @Override
        public void reportProgress(final ProgressReport progress) {
            latestReport.set(progress);
            recalculate();
        }

        /**
         * Returns the latest progress report received from {@code reporter}.
         * <p>
         * This method should be called after {@code start} method has been called and
         * before {@code stop} method is called. If called before {@code start}, returns
         * a default progress report with zero progress and {@link ExecutionState#PENDING} state.
         * If called after {@code stop}, return the latest progress report received before
         * stopped listening to progress reports from {@code reporter}.
         *
         * @return
         *      the latest progress report received from {@code} reporter
         */
        public ProgressReport getCurrentProgressReport() {
            return latestReport.get();
        }
    }

    private final List<PropagatingListener> childListeners;

    private final ExecutionStateAggregator stateAggregator;

    private final AtomicBoolean terminated;

    /**
     * Constructs a reporter which initially has no children. This reporter will
     * use {@link DefaultExecutionStateAggregator} to aggregate child task states.
     * <p>
     * The client must call the {@link #setChildren} method to set the child tasks.
     */
    public SimpleAggregatingProgressReporter() {
        this(new DefaultExecutionStateAggregator());
    }

    /**
     * Constructs a reporter which will use specified {@code ExecutionStateAggregator}
     * to aggregate child task states. Initially the reporter has no children.
     * <p>
     * The client must call the {@link #setChildren} method to set the child tasks.
     *
     * @param stateAggregator the {@link ExecutionStateAggregator} the reporter will use
     */
    public SimpleAggregatingProgressReporter(final ExecutionStateAggregator stateAggregator) {
        super();

        if (stateAggregator == null) {
            throw new IllegalArgumentException("stateAggregator");
        }

        this.childListeners = new ArrayList<PropagatingListener>();
        this.terminated = new AtomicBoolean();
        this.stateAggregator = stateAggregator;
    }

    /**
     * Sets child progress reporters to monitor and aggregate.
     *
     * @param children
     *          a non-empty collection of child progress reporters
     * @throws IllegalArgumentException if {@code children} is {@code null} or empty
     *                  or contains a {@code null} member
     */
    public final void setChildren(final Collection<? extends ProgressReporter> children) {
        if (children == null) {
            throw new IllegalArgumentException("children");
        }

        if (children.isEmpty()) {
            throw new IllegalArgumentException("children");
        }

        if (children.contains(null)) {
            throw new IllegalArgumentException("children");
        }

        for (final ProgressReporter child : children) {
            final PropagatingListener childListener = new PropagatingListener(child);
            childListeners.add(childListener);
        }

        startChildListeners();
    }

    /**
     * Stops aggregating state and progress of child tasks and sets aggregated state to FAILED.
     */
    public synchronized void terminateWithFailure() {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }

        updateState(ExecutionState.FAILED);

        // break circular references between this object and its children
        stopChildListeners();
    }

    private synchronized void recalculate() {
        if (terminated.get()) {
            return;
        }

        final ExecutionState aggregateState = calculateState();

        if (aggregateState == ExecutionState.COMPLETED) {
            updateProgress(100);
        } else {
            updateProgress(calculateProgress());
        }

        updateState(aggregateState);

        if (aggregateState.isTerminal()) {
            // break circular references between this object and its children
            stopChildListeners();
        }
    }

    private void startChildListeners() {
        for (final PropagatingListener listener : childListeners) {
            listener.start();
        }
    }

    private void stopChildListeners() {
        for (final PropagatingListener listener : childListeners) {
            listener.stop();
        }
    }

    private int calculateProgress() {
        final SummaryStatistics childProgress = new SummaryStatistics();
        for (final PropagatingListener listener : childListeners) {
            childProgress.addValue(listener.getCurrentProgressReport().getProgress());
        }

        final int roundedProgress = (int)Math.round(childProgress.getMean());
        return roundedProgress;
    }

    private ExecutionState calculateState() {
        final List<ExecutionState> childStates = new ArrayList<ExecutionState>();

        for (final PropagatingListener listener : childListeners) {
            childStates.add(listener.getCurrentProgressReport().getState());
        }

        return stateAggregator.aggregate(childStates);
    }
}
