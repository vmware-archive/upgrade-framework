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

package com.vmware.upgrade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.vmware.upgrade.context.PersistenceContextHelper;
import com.vmware.upgrade.factory.CompositeUpgradeDefinitionFactory;
import com.vmware.upgrade.factory.GraphUpgradeDefinitionFactory;
import com.vmware.upgrade.factory.UpgradeDefinitionFactory;
import com.vmware.upgrade.logging.UpgradeLogger;
import com.vmware.upgrade.logging.UpgradeLoggerHelper;
import com.vmware.upgrade.sequencing.AbstractGraph;
import com.vmware.upgrade.sequencing.Graph;
import com.vmware.upgrade.sequencing.Version;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class is intended to test the workflow a user of the framework would be expected to use.
 * <p>
 * This class doubles as documentation of that expected workflow.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class WorkflowTest {
    /**
     * An {@link UpgradeContext} implementation which delegates to {@link UpgradeLoggerHelper} and
     * {@link PersistenceContextHelper} and stores {@link Version} information in-memory.
     */
    private class TrivialUpgradeContext implements UpgradeContext {
        private final AtomicReference<Version> version = new AtomicReference<Version>(Version.INITIAL);
        private final PersistenceContextHelper persistenceContextHelper = new PersistenceContextHelper();

        @Override
        public UpgradeLogger getLogger(Class<?> clazz) {
            return UpgradeLoggerHelper.asUpgradeLogger(Logger.getLogger(clazz.getName()));
        }

        @Override
        public Version getVersion() {
            return version.get();
        }

        @Override
        public void setVersion(Version version) {
            this.version.set(version);
        }

        @Override
        public <T extends PersistenceContext> T getPersistenceContext(Class<T> type) {
            return persistenceContextHelper.getPersistenceContext(type);
        }

        @Override
        public <T extends PersistenceContext> T getPersistenceContext(Class<T> type, String qualifier) {
            return persistenceContextHelper.getPersistenceContext(type, qualifier);
        }
    }

    /**
     * A {@link Graph} implementation that allows test code to easily add {@link Graph.Edge}s
     * that will produce mock {@link Task}s that can be used to verify the expected behavior.
     */
    private class SimpleGraph extends AbstractGraph {
        private final IMocksControl control;

        private final Map<Version, Edge> map = new HashMap<Version, Edge>();

        private SimpleGraph(final IMocksControl control) {
            this.control = control;
        }

        private Task createVerifyingTask() {
            final Task mock = control.createMock(Task.class);
            try {
                EasyMock.expect(mock.call()).andReturn(null).once();
            } catch (Exception e) {
                throw new AssertionError("impossible; Task is a mock");
            }

            return mock;
        }

        private void addEdge(final Version source, final Version target) {
            map.put(source, new ImmutableEdge(source, target, createVerifyingTask()));
        }

        @Override
        protected Map<Version, Edge> getEdges() {
            return map;
        }
    }

    private final IMocksControl control = EasyMock.createNiceControl();

    // This seems silly, but because of the difference between how simpleTest and complexTest work,
    // we must reset both before and after to ensure that the control is reset for each execution.
    @BeforeMethod(alwaysRun = true)
    @AfterMethod(alwaysRun = true)
    public void before() {
        control.reset();
    }

    private Graph createLinearUpgradeGraph(Version ... path) {
        final SimpleGraph graph = new SimpleGraph(control);

        Version previous = Version.INITIAL;
        for (final Version version : path) {
            graph.addEdge(previous, version);
            previous = version;
        }

        return graph;
    }

    @DataProvider(name = "graphs")
    public Object[][] graphProvider() {
        return new Object[][] {
                new Object[] {createLinearUpgradeGraph(Version.INITIAL.getNext()), 1},
                new Object[] {createLinearUpgradeGraph(Version.lookup("1"), Version.lookup("2")), 2},
        };
    }

    /**
     * Create an {@link UpgradeContext} and a {@link GraphUpgradeDefinitionFactory} using the
     * supplied {@code graph} and then use the {@link UpgradeContext} in conjunction with the
     * {@link UpgradeDefinitionFactory} to {@linkplain UpgradeDefinitionFactory#create create} an
     * {@link UpgradeDefinition}.
     * <p>
     * {@linkplain Task#call() Call} all {@link Task}s in the produced {@link UpgradeDefinition}
     * and then verify expectations.
     *
     * @param graph
     *          A {@link Graph} to operate on. In a real system, this might be defined in Spring
     *          or using the functionality from {@link com.vmware.upgrade.dsl}.
     * @param pathLength
     *          The expected number of {@link Task}s in the path from {@link Version#INITIAL}.
     * @throws Exception
     *          If an error occurs in the test.
     */
    @Test(groups = { TestGroups.MINIMUM }, dataProvider = "graphs")
    public void simpleTest(final Graph graph, final int pathLength) throws Exception {
        control.replay();

        final UpgradeContext context = new TrivialUpgradeContext();

        final UpgradeDefinitionFactory factory = new GraphUpgradeDefinitionFactory(graph);

        Assert.assertTrue(factory.isUpgradeSupported(context));

        final UpgradeDefinition upgrade = factory.create(context);

        Assert.assertNotNull(upgrade);

        final List<Task> tasks = upgrade.getUpgradeTasks();

        Assert.assertNotNull(tasks);
        Assert.assertEquals(tasks.size(), pathLength);

        for (Task task : tasks) {
            task.call();
        }

        control.verify();
    }

    /**
     * Create a {@link CompositeUpgradeDefinitionFactory} demonstrating both the ordered and
     * unordered functionality from several {@link GraphUpgradeDefinitionFactory} instances.
     * <p>
     * {@linkplain Task#call() Call} all {@link Task}s in the produced {@link UpgradeDefinition}
     * and then verify expectations.
     */
    @Test(groups = { TestGroups.MINIMUM })
    public void complexTest() throws Exception {
        final Graph firstGraph = createLinearUpgradeGraph(Version.INITIAL.getNext());
        final Graph secondGraph = createLinearUpgradeGraph(Version.lookup("2"), Version.lookup("3"));
        final Graph aGraph = createLinearUpgradeGraph(Version.INITIAL.getNext());
        final Graph bGraph = createLinearUpgradeGraph(Version.INITIAL.getNext());

        control.replay();

        final UpgradeDefinitionFactory first = new GraphUpgradeDefinitionFactory(firstGraph);
        final UpgradeDefinitionFactory second = new GraphUpgradeDefinitionFactory(secondGraph);

        final UpgradeDefinitionFactory a = new GraphUpgradeDefinitionFactory(aGraph);
        final UpgradeDefinitionFactory b = new GraphUpgradeDefinitionFactory(bGraph);
        final Map<String, UpgradeDefinitionFactory> unordered = new HashMap<String, UpgradeDefinitionFactory>(2);
        unordered.put("A", a);
        unordered.put("B", b);

        final List<UpgradeDefinitionFactory> ordered = Arrays.asList(first, second, new CompositeUpgradeDefinitionFactory(unordered));


        final UpgradeContext context = new TrivialUpgradeContext();

        final UpgradeDefinitionFactory factory = new CompositeUpgradeDefinitionFactory(ordered);

        Assert.assertTrue(factory.isUpgradeSupported(context));

        final Version target = factory.getTargetVersion();

        final UpgradeDefinition upgrade = factory.create(context);

        Assert.assertNotNull(upgrade);

        final List<Task> tasks = upgrade.getUpgradeTasks();

        Assert.assertNotNull(tasks);
        Assert.assertNotEquals(tasks.size(), 0);

        for (Task task : tasks) {
            task.call();
        }

        control.verify();

        Assert.assertEquals(context.getVersion(), Version.lookup("[\"0.0.1\", \"3.0.0\", {\"A\": \"0.0.1\", \"B\": \"0.0.1\"}]"));
        Assert.assertEquals(target, context.getVersion());
    }
}
