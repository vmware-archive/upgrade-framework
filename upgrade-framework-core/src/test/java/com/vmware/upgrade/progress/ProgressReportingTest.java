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

import java.util.ArrayList;
import java.util.List;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ProgressReport;
import com.vmware.upgrade.progress.ProgressReporter;
import com.vmware.upgrade.progress.ProgressReporter.ProgressListener;
import com.vmware.upgrade.progress.impl.ImmutableProgressReport;
import com.vmware.upgrade.progress.impl.SimpleAggregatingProgressReporter;
import com.vmware.upgrade.progress.impl.SimpleProgressReporter;

import org.easymock.EasyMock;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for direct progress reporting
 *
 * @see ImmutableProgressReport
 * @see SimpleProgressReporter
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class ProgressReportingTest {

    private static final int TOTAL_TICKS = 5;

    private static final int INITIAL_PROGRESS = 0;
    private static final int FIRST_PROGRESS = 20;
    private static final int SECOND_PROGRESS = 40;
    private static final int FINAL_PROGRESS = 100;

    static final ProgressReport PENDING_REPORT = new ImmutableProgressReport(ExecutionState.PENDING, INITIAL_PROGRESS);
    static final ProgressReport RUNNING_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, INITIAL_PROGRESS);
    static final ProgressReport INCREMENTED_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, FIRST_PROGRESS);
    static final ProgressReport COMPLETED_RUNNING_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, FINAL_PROGRESS);
    static final ProgressReport COMPLETED_REPORT = new ImmutableProgressReport(ExecutionState.COMPLETED, FINAL_PROGRESS);
    static final ProgressReport INCREMENTED_TWICE_REPORT = new ImmutableProgressReport(ExecutionState.RUNNING, SECOND_PROGRESS);
    static final ProgressReport COMPLETED_TWICE_REPORT = new ImmutableProgressReport(ExecutionState.COMPLETED, FINAL_PROGRESS);

    private static final int MANY = 31;

    /**
     * Verify that {@link SimpleProgressReporter} does not fail if no listeners are present
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleProgressReporterWithNoListener() {
        SimpleProgressReporter reporter = new SimpleProgressReporter();

        reporter.requestProgressReport();
        reporter.setState(ExecutionState.RUNNING);
        reporter.advance();
    }

    /**
     * Verify that {@link SimpleProgressReporter} reports the correct default values
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleProgressReporterWithDefaults() {
        SimpleProgressReporter reporter = new SimpleProgressReporter();
        ProgressListener mock = createMockListener(PENDING_REPORT);

        reporter.addListener(mock);
        reporter.requestProgressReport();
        reporter.removeListener(mock);

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleProgressReporter} reports progress as changes are made
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleProgressReporterWithOneListener() {
        SimpleProgressReporter reporter = new SimpleProgressReporter(TOTAL_TICKS);

        ProgressListener mock = createMockListener(PENDING_REPORT, RUNNING_REPORT, INCREMENTED_REPORT);

        reporter.addListener(mock);
        reporter.requestProgressReport();

        reporter.setState(ExecutionState.RUNNING);
        reporter.advance();

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleProgressReporter} reports progress to two listeners
     * with one being added and another being removed part way through the process
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleProgressReporterWithTwoListeners() {
        SimpleProgressReporter reporter = new SimpleProgressReporter(TOTAL_TICKS);

        ProgressListener mock1 = createMockListener(PENDING_REPORT, RUNNING_REPORT, INCREMENTED_REPORT);
        ProgressListener mock2 = createMockListener(RUNNING_REPORT, INCREMENTED_REPORT, INCREMENTED_TWICE_REPORT);

        reporter.addListener(mock1);

        reporter.requestProgressReport();

        reporter.addListener(mock2);

        reporter.setState(ExecutionState.RUNNING);
        reporter.advance();

        reporter.removeListener(mock1);

        reporter.advance();

        reporter.removeListener(mock2);

        EasyMock.verify(mock1);
        EasyMock.verify(mock2);
    }

    /**
     * Verify that {@link SimpleProgressReporter} reports progress to {@link #MANY} listeners
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleProgressReporterWithManyListeners() {
        SimpleProgressReporter reporter = new SimpleProgressReporter(TOTAL_TICKS);

        List<ProgressListener> mocks = new ArrayList<ProgressListener>(MANY);
        for (int i = 0; i < MANY; i++) {
            ProgressListener mock = createMockListener(PENDING_REPORT, RUNNING_REPORT, INCREMENTED_REPORT);
            mocks.add(mock);
            reporter.addListener(mock);
        }

        reporter.requestProgressReport();
        reporter.setState(ExecutionState.RUNNING);
        reporter.advance();

        for (ProgressListener mock : mocks) {
            reporter.removeListener(mock);
            EasyMock.verify(mock);
        }
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} does not fail if no listeners and no children are present
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithNoListenerAndNoChildren() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        reporter.requestProgressReport();
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} does not fail if no listeners are present
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithNoListener() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        SimpleProgressReporter child = new SimpleProgressReporter();

        reporter.setChildren(java.util.Arrays.asList(child));

        reporter.requestProgressReport();

        child.setState(ExecutionState.RUNNING);
        child.advance();
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} does not fail if no children are present
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithNoChildren() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        ProgressListener mock = createMockListener(PENDING_REPORT);
        reporter.addListener(mock);

        reporter.requestProgressReport();

        reporter.removeListener(mock);

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} correctly propagates progress one level for a single child and listener
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithOneChildAndOneListener() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        SimpleProgressReporter child = new SimpleProgressReporter(TOTAL_TICKS);
        reporter.setChildren(java.util.Arrays.asList(child));

        ProgressReport[] expectedReports =
            new ProgressReport[] {
                PENDING_REPORT,
                RUNNING_REPORT,
                INCREMENTED_REPORT,
                INCREMENTED_TWICE_REPORT,
                COMPLETED_RUNNING_REPORT,
                COMPLETED_TWICE_REPORT
        };

        ProgressListener mock = createMockListener(expectedReports);
        reporter.addListener(mock);

        reporter.requestProgressReport();
        child.setState(ExecutionState.RUNNING);
        child.advance();
        child.advance();
        child.setState(ExecutionState.COMPLETED);

        reporter.removeListener(mock);

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} correctly propagates progress one level for a single child and {@link #MANY} listeners
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithOneChildAndManyListeners() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        SimpleProgressReporter child = new SimpleProgressReporter(TOTAL_TICKS);
        reporter.setChildren(java.util.Arrays.asList(child));

        ProgressReport[] expectedReports =
            new ProgressReport[] {
                PENDING_REPORT,
                RUNNING_REPORT,
                INCREMENTED_REPORT,
                INCREMENTED_TWICE_REPORT,
                COMPLETED_RUNNING_REPORT,
                COMPLETED_TWICE_REPORT
        };

        List<ProgressListener> mocks = new ArrayList<ProgressListener>(MANY);
        for (int i = 0; i < MANY; i++) {
            ProgressListener mock = createMockListener(expectedReports);
            mocks.add(mock);
            reporter.addListener(mock);
        }

        reporter.requestProgressReport();
        child.setState(ExecutionState.RUNNING);
        child.advance();
        child.advance();
        child.setState(ExecutionState.COMPLETED);

        for (ProgressListener mock : mocks) {
            reporter.removeListener(mock);
            EasyMock.verify(mock);
        }
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} correctly propagates progress one level for multiple children and a single listener
     */
    @Test(groups = { TestGroups.UNIT })
    public void testSimpleAggregatingProgressReporterWithTwoChildrenAndOneListener() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();

        SimpleProgressReporter child1 = new SimpleProgressReporter(TOTAL_TICKS);
        SimpleProgressReporter child2 = new SimpleProgressReporter(TOTAL_TICKS);

        reporter.setChildren(java.util.Arrays.asList(child1, child2));

        ProgressReport[] expectedReports =
            new ProgressReport[] {
                PENDING_REPORT,
                RUNNING_REPORT,
                new ImmutableProgressReport(ExecutionState.RUNNING, 10),
                new ImmutableProgressReport(ExecutionState.RUNNING, 20),
                new ImmutableProgressReport(ExecutionState.RUNNING, 30),
                new ImmutableProgressReport(ExecutionState.RUNNING, 100),
                new ImmutableProgressReport(ExecutionState.COMPLETED, 100)};

        ProgressListener mock = createMockListener(expectedReports);
        reporter.addListener(mock);

        reporter.requestProgressReport();       // pending
        child1.setState(ExecutionState.RUNNING);     // running
        child1.advance();             // 10%
        child2.setState(ExecutionState.RUNNING);     // no-op: still running
        child2.advance();             // 20%
        child2.setState(ExecutionState.COMPLETED);   // no-op: still running
        child1.advance();             // 30%
        child1.setState(ExecutionState.COMPLETED);   // complete

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} correctly propagates progress several levels for a single child and listener
     */
    @Test(groups = { TestGroups.UNIT })
    public void testNestedSimpleAggregatingProgressReporterOneListener() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();
        SimpleAggregatingProgressReporter child = new SimpleAggregatingProgressReporter();
        SimpleAggregatingProgressReporter grandchild = new SimpleAggregatingProgressReporter();
        SimpleProgressReporter leaf = new SimpleProgressReporter(TOTAL_TICKS);

        grandchild.setChildren(java.util.Arrays.asList(leaf));
        child.setChildren(java.util.Arrays.asList(grandchild));
        reporter.setChildren(java.util.Arrays.asList(child));

        ProgressReport[] expectedReports =
            new ProgressReport[] {
                PENDING_REPORT,
                RUNNING_REPORT,
                INCREMENTED_REPORT,
                INCREMENTED_TWICE_REPORT,
                COMPLETED_RUNNING_REPORT,
                COMPLETED_TWICE_REPORT};

        ProgressListener mock = createMockListener(expectedReports);

        reporter.addListener(mock);

        reporter.requestProgressReport();

        leaf.setState(ExecutionState.RUNNING);
        leaf.advance();
        leaf.advance();
        leaf.setState(ExecutionState.COMPLETED);

        reporter.removeListener(mock);

        EasyMock.verify(mock);
    }

    /**
     * Verify that {@link SimpleAggregatingProgressReporter} correctly propagates progress several
     * levels for a single listener regardless of the order in which the child chain is assembled
     *
     * @see #testNestedSimpleAggregatingProgressReporterOneListener()
     */
    @Test(groups = { TestGroups.UNIT })
    public void testOutOfOrderNestedSimpleAggregatingProgressReporterOneListener() {
        SimpleAggregatingProgressReporter reporter = new SimpleAggregatingProgressReporter();
        SimpleAggregatingProgressReporter child = new SimpleAggregatingProgressReporter();
        SimpleAggregatingProgressReporter grandchild = new SimpleAggregatingProgressReporter();
        SimpleProgressReporter leaf = new SimpleProgressReporter(TOTAL_TICKS);

        child.setChildren(java.util.Arrays.asList(grandchild));
        grandchild.setChildren(java.util.Arrays.asList(leaf));
        reporter.setChildren(java.util.Arrays.asList(child));

        ProgressReport[] expectedReports =
            new ProgressReport[] {
                PENDING_REPORT,
                RUNNING_REPORT,
                INCREMENTED_REPORT,
                INCREMENTED_TWICE_REPORT,
                COMPLETED_RUNNING_REPORT,
                COMPLETED_TWICE_REPORT};

        ProgressListener mock = createMockListener(expectedReports);

        reporter.addListener(mock);

        reporter.requestProgressReport();

        leaf.setState(ExecutionState.RUNNING);
        leaf.advance();
        leaf.advance();
        leaf.setState(ExecutionState.COMPLETED);

        reporter.removeListener(mock);

        EasyMock.verify(mock);
    }

    /**
     * Verify results of {@link ProgressReporter.addListener} and {@link ProgressReporter.removeListener}.
     */
    @Test(groups = { TestGroups.UNIT })
    public void testAddRemoveListener() {
        SimpleProgressReporter reporter = new SimpleProgressReporter();
        ProgressListener mock = createMockListener(PENDING_REPORT);

        if (!reporter.addListener(mock)) {
            AssertJUnit.fail("Didn't add listener");
        }

        if (reporter.addListener(mock)) {
            AssertJUnit.fail("Duplicate listener added");
        }

        if (!reporter.removeListener(mock)) {
            AssertJUnit.fail("Didn't remove listener");
        }

        if (reporter.removeListener(mock)) {
            AssertJUnit.fail("Removed listener twice");
        }
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
