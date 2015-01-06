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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ExecutionStateAggregator;

/**
 * This class contains a method to calculate task state of a parent task from a collection
 * of task states of its children.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public final class DefaultExecutionStateAggregator implements ExecutionStateAggregator {
    /**
     * Calculates the state of a parent task from a collection of states of child tasks.
     * <p>
     * This method defines the parent task state as:
     * <ul>
     * <li> pending iff all child tasks are pending;
     * <li> running iff at least one child is running or pending
     *          but not all children are pending;
     * <li> suspended iff at least one child is suspended
     *          and each other child is either suspended or canceled or failed or completed;
     * <li> canceled iff at least one child is canceled
     *          and each other child is either canceled or failed or completed;
     * <li> failed iff at least one child has failed
     *          and each other child is either failed or completed;
     * <li> completed iff all children are completed;
     * </ul>
     * Parent states calculated by this method are compatible with the state transition diagram
     * defined in {@link ExecutionState}, e.g. valid state transitions of the child states produce
     * valid state transitions of the parent state.
     *
     * @param executionStates non-empty collection of task states to aggregate
     * @return an aggregate state
     * @throws IllegalArgumentException if {@code executionStates} is {@code null} or is empty or
     *          contains a {@code null} member
     */
    @Override
    public ExecutionState aggregate(Collection<ExecutionState> executionStates) {
        if (executionStates == null) {
            throw new IllegalArgumentException("executionStates");
        }

        if (executionStates.contains(null)) {
            throw new IllegalArgumentException("executionStates");
        }

        if (executionStates.isEmpty()) {
            throw new IllegalArgumentException("executionStates");
        }

        final Set<ExecutionState> uniqueStates = new LinkedHashSet<ExecutionState>(executionStates);

        // If all states are the same
        if (uniqueStates.size() == 1) {
            return uniqueStates.iterator().next();
        }

        // If anything is still running or pending (but not everything is pending)
        if (uniqueStates.contains(ExecutionState.RUNNING) || uniqueStates.contains(ExecutionState.PENDING)) {
            return ExecutionState.RUNNING;
        }

        // If everything not in a completion state is suspended (and there's at least one thing suspended)
        if (uniqueStates.contains(ExecutionState.SUSPENDED)) {
            return ExecutionState.SUSPENDED;
        }

        // If everything not complete has been canceled
        if (uniqueStates.contains(ExecutionState.CANCELLED)) {
            return ExecutionState.CANCELLED;
        }

        // If anything has failed
        if (uniqueStates.contains(ExecutionState.FAILED)) {
            return ExecutionState.FAILED;
        }

        // If everything has completed
        if (uniqueStates.contains(ExecutionState.COMPLETED)) {
            return ExecutionState.COMPLETED;
        }

        // A new state was added without updating this method
        throw new AssertionError("Unknown ExecutionState encountered");
    }
}
