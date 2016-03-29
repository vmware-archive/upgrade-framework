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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vmware.upgrade.DummyUpgradeContext;
import com.vmware.upgrade.Task;
import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.UpgradeContext;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * Test cases for the aggregate tasks
 *
 * @see AbstractAggregateTask
 * @see ParallelAggregateTask
 * @see SerialAggregateTask
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class TaskAggregationTest {
    private static final String TEST_TASK_NAME = "Test task";
    private static final int MANY = 31;
    private static final List<Task> EMPTY_TASK_LIST = Collections.emptyList();

    private static final UpgradeContext DUMMY_UPGRADE_CONTEXT = new DummyUpgradeContext();

    /**
     * Verify that {@link SerialAggregateTask} correctly handles a null list
     */
    @Test(groups = { TestGroups.UNIT }, expectedExceptions = {IllegalArgumentException.class})
    public void testSerialAggregationWithNullList() throws Exception {
        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, null);
        aggregateTask.call();
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handles a null list
     */
    @Test(groups = { TestGroups.UNIT }, expectedExceptions = {IllegalArgumentException.class})
    public void testParallelAggregationWithNullList() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, null);
        aggregateTask.call();
    }

    /**
     * Verify that {@link SerialAggregateTask} correctly handles an empty list
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSerialAggregationWithNoTasks() throws Exception {
        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, EMPTY_TASK_LIST);
        aggregateTask.call();
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handles an empty list
     */
    @Test(groups = { TestGroups.UNIT })
    public void testParallelAggregationWithNoTasks() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, EMPTY_TASK_LIST);
        aggregateTask.call();
    }

    /**
     * Verify that {@link SerialAggregateTask} correctly handle a null task
     */
    @Test(groups = { TestGroups.UNIT }, expectedExceptions = {IllegalArgumentException.class})
    public void testSerialAggregationWithNullTask() throws Exception {
        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, Arrays.asList(new Task[] {null}));
        aggregateTask.call();
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handle a null task
     */
    @Test(groups = { TestGroups.UNIT }, expectedExceptions = {IllegalArgumentException.class})
    public void testParallelAggregationWithNullTask() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, Arrays.asList(new Task[] {null}));
        aggregateTask.call();
    }

    /**
     * Verify that {@link SerialAggregateTask} correctly handle a single task
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSerialAggregationWithOneTasks() throws Exception {
        Task mock = TaskTestUtil.createMockTask();

        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, Arrays.asList(new Task[] { mock }));
        aggregateTask.call();
        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handle a single task
     */
    @Test(groups = { TestGroups.UNIT })
    public void testParallelAggregationWithOneTasks() throws Exception {
        Task mock = TaskTestUtil.createMockTask();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, Arrays.asList(new Task[] { mock }));
        aggregateTask.call();
        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SerialAggregateTask} correctly handles two tasks
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSerialAggregationWithTwoTasks() throws Exception {
        Task mock1 = TaskTestUtil.createMockTask();
        Task mock2 = TaskTestUtil.createMockTask();

        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, Arrays.asList(new Task[] { mock1, mock2 }));
        aggregateTask.call();
        EasyMock.verify(mock1);
        EasyMock.verify(mock2);
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handles two tasks
     */
    @Test(groups = { TestGroups.UNIT })
    public void testParallelAggregationWithTwoTasks() throws Exception {
        Task mock1 = TaskTestUtil.createMockTask();
        Task mock2 = TaskTestUtil.createMockTask();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, Arrays.asList(new Task[] { mock1, mock2 }));
        aggregateTask.call();
        EasyMock.verify(mock1);
        EasyMock.verify(mock2);
    }

    /**
     * Verify that {@link SerialAggregateTask} correctly handles {@link #MANY} tasks
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSerialAggregationWithTaskList() throws Exception {
        List<Task> mocks = new ArrayList<Task>();
        for (int i = 0; i < MANY; i++) {
            mocks.add(TaskTestUtil.createMockTask());
        }

        Task aggregateTask = new SerialAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, mocks);
        aggregateTask.call();
        for (Task mock : mocks) {
            EasyMock.verify(mock);
        }
    }

    /**
     * Verify that {@link ParallelAggregateTask} correctly handles {@link #MANY} tasks
     */
    @Test(groups = { TestGroups.UNIT })
    public void testParallelAggregationWithTaskList() throws Exception {
        List<Task> mocks = new ArrayList<Task>();
        for (int i = 0; i < MANY; i++) {
            mocks.add(TaskTestUtil.createMockTask());
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        Task aggregateTask = new ParallelAggregateTask(DUMMY_UPGRADE_CONTEXT, TEST_TASK_NAME, executor, mocks);
        aggregateTask.call();
        for (Task mock : mocks) {
            EasyMock.verify(mock);
        }
    }
}
