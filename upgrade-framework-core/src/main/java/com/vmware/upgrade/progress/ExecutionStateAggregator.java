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

import java.util.Collection;

/**
 * This class contains a method to calculate task state of a parent task from a collection
 * of task states of its children.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface ExecutionStateAggregator {
    /**
     * Calculates the state of a parent task from a collection of states of child tasks.
     * <p>
     * Parent states calculated by this method must be compatible with the state transition diagram
     * defined in {@link ExecutionState}, e.g. valid state transitions of the child states must
     * produce valid state transitions of the parent state.
     *
     * @param childStates non-empty collection of task states to aggregate
     * @return an aggregate state
     * @throws IllegalArgumentException if {@code taskStates} is {@code null} or is empty or
     *          contains a {@code null} member
     */
    public ExecutionState aggregate(Collection<ExecutionState> childStates);
}
