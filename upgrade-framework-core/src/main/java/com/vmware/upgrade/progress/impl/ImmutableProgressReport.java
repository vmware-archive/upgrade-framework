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

import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReport;

/**
 * An immutable {@link ProgressReport}.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public final class ImmutableProgressReport implements ProgressReport {
    private final ExecutionState state;
    private final int progress;

    /**
     * Constructs a {@link ProgressReport}
     *
     * @param state see {@link ProgressReport#getState()}
     * @param progress see {@link ProgressReport#getProgress()}
     * @throws IllegalArgumentException if progress is not between 0..100 or if state is null
     */
    public ImmutableProgressReport(final ExecutionState state, final int progress) {
        if (state == null) {
            throw new IllegalArgumentException("state");
        }

        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("progress");
        }

        this.state = state;
        this.progress = progress;
    }

    /**
     * A copy constructor.
     * <p>
     * This can be used to create an immutable copy of a mutable {@link ProgressReport}.
     *
     * @param progressReport the {@link ProgressReport} to copy
     */
    public ImmutableProgressReport(final ProgressReport progressReport) {
        this(progressReport.getState(), progressReport.getProgress());
    }

    @Override
    public ExecutionState getState() {
        return state;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString());
        builder.append(" {");
        builder.append("state: ").append(state).append(", ");
        builder.append("progress: ").append(progress).append("%");
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + progress;
        result = prime * result + state.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ImmutableProgressReport)) {
            return false;
        }

        final ImmutableProgressReport other = (ImmutableProgressReport) o;

        if (state != other.state) {
            return false;
        }

        if (progress != other.progress) {
            return false;
        }

        return true;
    }
}
