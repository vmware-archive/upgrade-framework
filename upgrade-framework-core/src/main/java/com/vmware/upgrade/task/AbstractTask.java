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

import java.util.concurrent.atomic.AtomicReference;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.progress.ProgressReporter;

/**
 * An {@link AbstractTask} encapsulates the core functionality of any {@link Task}
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTask<T extends ProgressReporter> implements Task {
    private final String name;

    private final AtomicReference<T> reporter;

    /**
     * Constructs a named task with no progress reporter.
     * <p>
     * The client must call {@code setReporter} method to set progress reporter.
     *
     * @param name
     *          see {@link Task#getName()}
     * @throws IllegalArgumentException
     *          if name is {@code null}
     */
    protected AbstractTask(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        this.name = name;
        this.reporter = new AtomicReference<T>();
    }

    /**
     * Sets progress reporter.
     * <p>
     * This method must be called once.
     *
     * TODO: Consider passing reporter to the constructor and remove this method altogether.
     *
     * @param reporter
     *          an instance of the progress reporter; must not be null
     * @throws IllegalArgumentException if {@code reporter} is {@code null}
     * @throws IllegalStateException if called multiple times
     */
    protected final void setReporter(final T reporter) {
        if (reporter == null) {
            throw new IllegalArgumentException("reporter");
        }

        final boolean ok = this.reporter.compareAndSet(null, reporter);

        if (!ok) {
            throw new IllegalStateException("ProgressReporter is already set");
        }
    }

    /**
     * Returns progress reporter provides to {@code setReporter} method.
     * <p>
     * {@code setReporter} method must be called before calling this method.
     *
     * @return progress reporter provides to {@code setReporter} method
     * @throws IllegalStateException if {@code setReporter} method hasn't been called yet
     */
    protected final T getReporter() {
        T result = reporter.get();

        if (result == null) {
            throw new IllegalStateException("ProgressReporter was not set");
        }

        return result;
    }

    @Override
    public final boolean addListener(final ProgressListener progressListener) {
        return getReporter().addListener(progressListener);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final boolean removeListener(final ProgressListener progressListener) {
        return getReporter().removeListener(progressListener);
    }

    @Override
    public void requestProgressReport() {
        getReporter().requestProgressReport();
    }
}
