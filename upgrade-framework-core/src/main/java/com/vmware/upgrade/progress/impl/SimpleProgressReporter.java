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

import java.util.concurrent.atomic.AtomicInteger;

import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReporter;

/**
 * {@link SimpleProgressReporter} is a default implementation of {@link ProgressReporter} which
 * handles the simplest case of tracking progress which is incremented periodically.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class SimpleProgressReporter extends AbstractProgressReporter {

    public static final int UNKNOWN = Integer.MAX_VALUE;

    private final int totalTicks;
    private final AtomicInteger ticks;

    private static final double LOG_MAX_TICKS = Math.log(Integer.MAX_VALUE);

    /**
     * Constructor which defaults {@code totalTicks} to {@link #UNKNOWN}.
     */
    public SimpleProgressReporter() {
        this(UNKNOWN);
    }

    /**
     * Constructor which takes a value for {@code totalTicks}.
     *
     * @param totalTicks total number of ticks; must be {@code > 0}.
     */
    public SimpleProgressReporter(final int totalTicks) {
        if (totalTicks <= 0) {
            throw new IllegalArgumentException("totalTicks");
        }

        this.ticks = new AtomicInteger();
        this.totalTicks = totalTicks;
    }

    /**
     * Updates the current progress and notifies all registered listeners.
     * <p>
     * Progress in excess of {@code totalTicks} is not reported.
     */
    public final synchronized void advance() {
        final int newTicks = ticks.incrementAndGet();
        if (newTicks <= this.totalTicks) {
            updateProgress(calculateProgress());
        }
    }

    /**
     * Updates the current state and notifies all registered listeners.
     *
     * @param state new state.
     * @throws IllegalArgumentException if {@code state} is {@code null} or state transition is not
     *          valid or {@code state} is the same as the current state.
     */
    public final synchronized void setState(final ExecutionState state) {
        if (state == getState()) {
            throw new IllegalArgumentException("state");
        }

        updateState(state);
    }

    private int calculateProgress() {
        final int currentTicks = this.ticks.get();
        double scaledProgress;

        if (totalTicks == UNKNOWN) {
            if (currentTicks == 0) {
                scaledProgress = 0.0;
            } else {
                scaledProgress = Math.log(currentTicks)/LOG_MAX_TICKS;
            }
        } else {
            scaledProgress = (currentTicks)/((double) totalTicks);
        }

        final int progress = (int) Math.round(100.0 * scaledProgress);
        return progress;
    }
}
