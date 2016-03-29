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

import com.vmware.upgrade.Task;
import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReport;
import com.vmware.upgrade.progress.ProgressReporter.ProgressListener;
import com.vmware.upgrade.progress.impl.ImmutableProgressReport;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link TrivialTask}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class TrivialTaskTest {
    static final ProgressReport PENDING_REPORT = new ImmutableProgressReport(ExecutionState.PENDING, 0);
    static final ProgressReport RUNNING_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, 0);
    static final ProgressReport INCREMENTED_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, 100);
    static final ProgressReport COMPLETED_REPORT = new ImmutableProgressReport(ExecutionState.COMPLETED, 100);
    static final ProgressReport FAILED_REPORT = new ImmutableProgressReport(ExecutionState.FAILED, 0);


    @Test(groups = { TestGroups.UNIT })
    public void runnableTest() throws Exception {
        final Runnable mock = createMockRunnable();

        final Task t = new TrivialTask("Foo", mock);

        t.call();

        EasyMock.verify(mock);
    }

    @Test(groups = { TestGroups.UNIT })
    public void runnableProgressTest() throws Exception {
        final Runnable mock = createMockRunnable();
        final ProgressListener listener = createMockListener(PENDING_REPORT, RUNNING_REPORT, INCREMENTED_REPORT, COMPLETED_REPORT);

        final Task t = new TrivialTask("Foo", mock);
        t.addListener(listener);
        t.requestProgressReport();

        t.call();

        EasyMock.verify(mock);
        EasyMock.verify(listener);
    }

    @Test(groups = { TestGroups.UNIT })
    public void callableTest() throws Exception {
        final Callable<?> mock = createMockCallable();

        final Task t = new TrivialTask("Foo", mock);

        t.call();

        EasyMock.verify(mock);
    }

    @Test(groups = { TestGroups.UNIT })
    public void callableProgressTest() throws Exception {
        final Callable<?> mock = createMockCallable();
        final ProgressListener listener = createMockListener(PENDING_REPORT, RUNNING_REPORT, INCREMENTED_REPORT, COMPLETED_REPORT);

        final Task t = new TrivialTask("Foo", mock);
        t.addListener(listener);
        t.requestProgressReport();

        t.call();

        EasyMock.verify(mock);
        EasyMock.verify(listener);
    }

    @Test(groups = { TestGroups.UNIT }, expectedExceptions = {RuntimeException.class})
    public void callableErrorTest() throws Exception {
        final Task t = new TrivialTask("Foo", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                throw new RuntimeException();
            }
        });

        t.call();
    }

    @Test(groups = { TestGroups.UNIT })
    public void callableErrorProgressTest() throws Exception {
        final Task t = new TrivialTask("Foo", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                throw new RuntimeException();
            }
        });
        final ProgressListener listener = createMockListener(PENDING_REPORT, RUNNING_REPORT, FAILED_REPORT);
        t.addListener(listener);
        t.requestProgressReport();

        try {
            t.call();
        } catch (RuntimeException e) {
        }

        EasyMock.verify(listener);
    }

    private Runnable createMockRunnable() {
        final Runnable mock = EasyMock.createStrictMock(Runnable.class);
        mock.run();
        EasyMock.expectLastCall();

        EasyMock.replay(mock);

        return mock;
    }

    private Callable<?> createMockCallable() throws Exception {
        final Callable<?> mock = EasyMock.createStrictMock(Callable.class);
        EasyMock.expect(mock.call()).andReturn(null);

        EasyMock.replay(mock);

        return mock;
    }

    /**
     * Create a strict mock {@link ProgressListener}
     *
     * @param expectedReports
     *          an ordered list of the reports the listener should expect
     * @return a mock {@link ProgressListener}
     */
    private static ProgressListener createMockListener(ProgressReport ... expectedReports) {
        ProgressListener mock = EasyMock.createStrictMock(ProgressListener.class);

        for (ProgressReport expectedReport : expectedReports) {
            mock.reportProgress(EasyMock.eq(expectedReport));
            EasyMock.expectLastCall().once();
        }

        EasyMock.replay(mock);

        return mock;
    }
}
