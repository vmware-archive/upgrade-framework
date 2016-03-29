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

package com.vmware.upgrade.progress;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents the current state of a {@link ProgressReporter}
 * <p>
 * Transition diagram:
 * <pre>
 *                           FAILED
 *                          /
 *                         /
 *                        /
 *                       /
 *                      /
 *                     /
 * PENDING ---> RUNNING ---> COMPLETED
 *                 A   \
 *                 |    \
 *                 |     \
 *                 |      \
 *                 |       \
 *                 |        \
 *                 V         \
 *             SUSPENDED ---> CANCELLED
 * </pre>
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public enum ExecutionState {
    /**
     * The {@link ProgressReporter} has been told to stop execution.
     */
    CANCELLED,
    /**
     * The {@link ProgressReporter} has completed execution successfully.
     */
    COMPLETED,
    /**
     * The {@link ProgressReporter} has completed execution in an error state.
     */
    FAILED,
    /**
     * The {@link ProgressReporter} has been created, but has not begun execution.
     */
    PENDING,
    /**
     * The {@link ProgressReporter} is currently running.
     */
    RUNNING,
    /**
     * The {@link ProgressReporter} is currently paused.
     */
    SUSPENDED;

    private static final Map<ExecutionState, Set<ExecutionState>> validTransitions;
    static {
        Map<ExecutionState, Set<ExecutionState>> transitions = new HashMap<ExecutionState, Set<ExecutionState>>();
        transitions.put(CANCELLED, Collections.<ExecutionState>emptySet());
        transitions.put(COMPLETED, Collections.<ExecutionState>emptySet());
        transitions.put(FAILED, Collections.<ExecutionState>emptySet());
        transitions.put(PENDING, EnumSet.of(RUNNING));
        transitions.put(SUSPENDED, EnumSet.of(CANCELLED, RUNNING));
        transitions.put(RUNNING, EnumSet.of(CANCELLED, COMPLETED, FAILED, SUSPENDED));
        validTransitions = Collections.unmodifiableMap(transitions);
    }

    /**
     * Validate whether a transition can occur from this state to the specified target state.
     *
     * @param targetState the destination state.
     * @return {@code true} iff the state transition is valid.
     */
    public boolean canTransitionTo(final ExecutionState targetState) {
        for (ExecutionState validState : validTransitions.get(this)) {
            if (targetState.equals(validState)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether this state is terminal.
     *
     * @return {@code true} if there are no valid exit transitions for this state.
     */
    public boolean isTerminal() {
        return validTransitions.get(this).isEmpty();
    }
}

