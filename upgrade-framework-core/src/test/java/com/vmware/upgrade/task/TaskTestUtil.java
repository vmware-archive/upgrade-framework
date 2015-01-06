/* ****************************************************************************
 * Copyright (c) 2012-2014 VMware, Inc. All Rights Reserved.
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

import org.easymock.EasyMock;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.progress.ProgressReport;
import com.vmware.upgrade.progress.ProgressReporter;
import com.vmware.upgrade.progress.ProgressReporter.ProgressListener;

/**
 * A utility class to facilitate creation of fake {@link Task}s for testing.
 *
 * These fake tasks can be used to verify the functionality of logic that operates on tasks.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class TaskTestUtil {
    private static final String MOCK_NAME = "Mock Task";

    /**
     * Creates a mock object that implements the {@link Task} interface, with order checking disabled.
     *
     * The mock expects exactly one call to {@link Task#call()}.
     *
     * The mock will always indicate that its preconditions are met.
     *
     * The mock will have the name {@link #MOCK_NAME}.
     *
     * The mock will not attempt to perform any validation of the methods from {@link ProgressReporter}, but will behave like a "nice mock" and accept any calls to them.
     *
     * @return a mock task
     */
    public static Task createMockTask() throws Exception {
        // Note: A NiceMock cannot be used because we must verify that the call method is called exactly once.
        Task mock = EasyMock.createMock(Task.class);

        // Configure addListener method to return 'true'
        EasyMock.expect(mock.addListener((ProgressListener) EasyMock.anyObject())).andReturn(true).anyTimes();

        // Configure removeListener method to return 'true'
        EasyMock.expect(mock.removeListener((ProgressListener) EasyMock.anyObject())).andReturn(true).anyTimes();

        // Allow requestProgressReport method to be called any number times
        mock.requestProgressReport();
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(mock.getName()).andReturn(MOCK_NAME).anyTimes();

        // Verify that the call method is called exactly once
        EasyMock.expect(mock.call()).andReturn(null).times(1);

        EasyMock.replay(mock);
        return mock;
    }

    public static ProgressListener createLastReportTrackingListener(final AtomicReference<ProgressReport> holder) {
        return new ProgressReporter.ProgressListener() {
            @Override
            public void reportProgress(ProgressReport progress) {
                holder.set(progress);
            }
        };
    }
}
