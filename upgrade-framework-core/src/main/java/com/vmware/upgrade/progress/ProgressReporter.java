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

import java.util.EventListener;

import org.apache.commons.lang.NullArgumentException;

/**
 * A ProgressReporter is an object which provides progress information about its execution.
 *
 * Progress information is assumed to be monotonically increasing.
 *
 * Progress can either be pulled on an as-needed basis (for cases such as when something displaying
 * the progress is force-refreshed by the user) or {@link ProgressListener}s can be registered.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public interface ProgressReporter {

    /**
     * Implementations of {@link ProgressListener} can be registered to receive callbacks from one
     * or more {@link ProgressReporter}s.
     */
    public interface ProgressListener extends EventListener {
        /**
         * A method which may be called when the progress changes.
         *
         * @param progress
         *          The new point-in-time representation of the progress.
         */
        void reportProgress(final ProgressReport progress);
    }

    /**
     * Add a listener which may receive a callback when the progress changes.
     *
     * Addition of listeners is idempotent; if the listener being added is equal to an existing
     * listener, no change will occur. Implementations should use the {@code equals} or
     * {@code hashcode} method of the listener to determine equality.
     *
     * @param progressListener
     *          The listener to register
     * @return true if {@link ProgressListener} was added; false if it was not
     * @throws NullArgumentException
     *          if {@code progressListener} is {@code null}
     */
    boolean addListener(final ProgressListener progressListener);

    /**
     * Removes the listener which is equal to the provided listener if one exists.
     *
     * Implementations should use the {@code equals} or {@code hashcode} method of the listener to
     * determine equality.
     *
     * A listener in the process of being removed may still be triggered. A listener will not be
     * triggered after being completely removed.
     *
     * @param progressListener
     *          The listener to remove
     * @return true if the {@link ProgressListener} was removed; false if it was not.
     * @throws NullArgumentException
     *          if {@code progressListener} is {@code null}
     */
    boolean removeListener(final ProgressListener progressListener);


    /**
     * Request that each listener be called with a progress update.
     */
    void requestProgressReport();
}
