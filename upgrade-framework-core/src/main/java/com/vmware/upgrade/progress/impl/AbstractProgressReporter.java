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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReport;
import com.vmware.upgrade.progress.ProgressReporter;

import org.apache.commons.lang.NullArgumentException;

/**
 * An abstract implementation of a {@link ProgressReporter} which handles management of listeners
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractProgressReporter implements ProgressReporter {
    private final Set<ProgressListener> progressListeners;
    private final AtomicReference<ImmutableProgressReport> report =
        new AtomicReference<ImmutableProgressReport>(new ImmutableProgressReport(ExecutionState.PENDING, 0 /*progress*/));

    /**
     * Creates an instance with no listeners, zero progress and {@link ExecutionState#PENDING} state.
     */
    public AbstractProgressReporter() {
        this.progressListeners = new CopyOnWriteArraySet<ProgressListener>();
    }

    @Override
    public final boolean addListener(final ProgressListener progressListener) {
        if (progressListener == null) {
            throw new NullArgumentException("progressListener");
        }

        return progressListeners.add(progressListener);
    }

    @Override
    public final boolean removeListener(final ProgressListener progressListener) {
        if (progressListener == null) {
            throw new NullArgumentException("progressListener");
        }

        return progressListeners.remove(progressListener);
    }

    @Override
    public final void requestProgressReport() {
        reportProgress();
    }

    /**
     * Returns current state.
     *
     * @return current state
     */
    public final ExecutionState getState() {
        return report.get().getState();
    }

    /**
     * Returns current progress (0..100).
     *
     * @return current progress
     */
    public final int getProgress() {
        return report.get().getProgress();
    }

    /**
     * Update the {@link ExecutionState} and report to all listeners
     * <p>
     * This method becomes a no-op if new state is the same as current state.
     * <p>
     * This method is not thread-safe.
     * <p>
     * The derived class must synchronize invocations of {@code updateProgress} and
     * {@code updateState} methods to ensure that listeners receive updates in the right order.
     *
     * @param state
     *          The new state
     * @throws IllegalStateException
     *          If the transition is invalid
     */
    protected final void updateState(final ExecutionState state) {
        final ExecutionState currentState = report.get().getState();
        if (currentState == state) {
            return;
        }

        if (!currentState.canTransitionTo(state)) {
             throw new IllegalStateException("Cannot transition from " + currentState + " to " + state);
        }

        final int currentProgress = report.get().getProgress();

        storeAndReportUpdates(state, currentProgress);
    }

    /**
     * Updates current progress and reports new progress to all registered listeners.
     * <p>
     * This method becomes a no-op if new progress value is {@code <=} current progress.
     * <p>
     * This method is not thread-safe.
     * <p>
     * The derived class must synchronize invocations of {@code updateProgress} and
     * {@code updateState} methods to ensure that listeners receive updates in the right order.
     *
     * @param progress
     *          new progress; must be between 0 and 100
     * @throws IllegalArgumentException if {@code progress < 0} or {@code progress > 100}
     */
    protected final void updateProgress(final int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("progress");
        }

        final int currentProgress = report.get().getProgress();

        if (progress <= currentProgress) {
            return;
        }

        final ExecutionState currentState = report.get().getState();

        storeAndReportUpdates(currentState, progress);
    }

    private void storeAndReportUpdates(final ExecutionState ExecutionState, final int progress) {
        report.set(new ImmutableProgressReport(ExecutionState, progress));
        reportProgress();
    }

    /**
     * Reports progress to all registered listeners.
     */
    private void reportProgress() {
        ProgressReport currentReport = report.get();

        for (ProgressListener progressListener : progressListeners) {
            progressListener.reportProgress(currentReport);
        }
    }
}
