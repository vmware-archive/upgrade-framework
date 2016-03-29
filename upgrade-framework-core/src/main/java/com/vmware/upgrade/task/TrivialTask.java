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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.vmware.upgrade.progress.ExecutionState;

/**
 * Creates a trivial task from a {@link Runnable} or {@link Callable}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class TrivialTask extends AbstractSimpleTask {

    private final Callable<?> callable;

    public TrivialTask(String name, Runnable r) {
        this(name, Executors.callable(r));
    }

    public TrivialTask(String name, Callable<?> callable) {
        super(name, 1);

        this.callable = callable;
    }

    @Override
    public Void call() throws Exception {
        setState(ExecutionState.RUNNING);
        try {
            callable.call();
            incrementProgress();
        } catch (Exception e) {
            setState(ExecutionState.FAILED);
            throw e;
        }
        setState(ExecutionState.COMPLETED);

        return null;
    }
}
