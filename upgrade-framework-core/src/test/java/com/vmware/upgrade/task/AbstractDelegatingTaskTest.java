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
import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReport;

import org.easymock.EasyMock;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for {@link AbstractDelegatingTask}
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
@Test(groups = { TestGroups.UNIT })
public class AbstractDelegatingTaskTest {
    private static final String PARENT_NAME = "Parent";

    @Test
    public void testExecutionOfDelegateTask() throws Exception {
        Task delegate = TaskTestUtil.createMockTask();
        Task parent = new AbstractDelegatingTask(PARENT_NAME, delegate) {
        };

        parent.call();

        EasyMock.verify(delegate);
    }

    @Test
    public void testHandlingOfException() throws Exception {
        Task delegate = TaskTestUtil.createMockTask();
        Task parent = new AbstractDelegatingTask(PARENT_NAME, delegate) {
            @Override
            protected void doCall() {
                throw new RuntimeException();
            }
        };

        AtomicReference<ProgressReport> lastReportHolder = new AtomicReference<ProgressReport>();
        parent.addListener(TaskTestUtil.createLastReportTrackingListener(lastReportHolder));

        boolean exceptionThrown = false;
        try {
            parent.call();
        }
        catch (RuntimeException e) {
            exceptionThrown = true;
        }

        AssertJUnit.assertEquals(ExecutionState.FAILED, lastReportHolder.get().getState());
        AssertJUnit.assertTrue(exceptionThrown);
    }

    @Test
    public void testAdvanceOfProgress() throws Exception {
        Task delegate = TaskTestUtil.createMockTask();
        Task parent = new AbstractDelegatingTask(PARENT_NAME, delegate, 1) {
            @Override
            protected void doCall() {
                advance();
            }
        };

        AtomicReference<ProgressReport> lastReportHolder = new AtomicReference<ProgressReport>();
        parent.addListener(TaskTestUtil.createLastReportTrackingListener(lastReportHolder));
        parent.call();

        AssertJUnit.assertTrue(lastReportHolder.get().getProgress() > 0);
    }
}
