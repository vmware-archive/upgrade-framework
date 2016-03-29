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

package com.vmware.upgrade.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.UpgradeDefinition;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.sequencing.Graph;
import com.vmware.upgrade.sequencing.Graph.Edge;
import com.vmware.upgrade.sequencing.GraphHelper;
import com.vmware.upgrade.sequencing.Version;
import com.vmware.upgrade.task.AbstractDelegatingTask;
import com.vmware.upgrade.task.AbstractSimpleTask;
import com.vmware.upgrade.task.SerialAggregateTask;

/**
 * A factory which will create {@link UpgradeDefinition}s from a supplied {@link Graph} for
 * a specified {@link UpgradeContext}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public final class GraphUpgradeDefinitionFactory implements UpgradeDefinitionFactory {
    /**
     * An {@link UpgradeTask} is a concrete {@link Task} which represents the work necessary to
     * upgrade from one {@link Version} to another.
     */
    private static final class UpgradeTask extends AbstractDelegatingTask {
        private final Version source;
        private final UpgradeContext context;

        /**
         * A {@link VersionUpdatingTask} is a concrete {@link Task} which handles updating the
         * {@link Version} as the first and final steps in the execution of an {@link UpgradeTask}.
         */
        private static final class VersionUpdatingTask extends AbstractSimpleTask {
            private final UpgradeContext context;
            private final Version destination;

            public VersionUpdatingTask(final UpgradeContext context, final Version destination) {
                super("Record version " + destination, 1);
                this.context = context;
                this.destination = destination;
            }

            /**
             * {@link UpgradeContext#setVersion(Version) Set} the {@link Version}.
             *
             * @return {@code null}
             */
            @Override
            public Void call() {
                setState(ExecutionState.RUNNING);
                try {
                    context.setVersion(destination);
                } catch (RuntimeException e) {
                    setState(ExecutionState.FAILED);
                    throw e;
                }
                setState(ExecutionState.COMPLETED);

                return null;
            }
        }

        private UpgradeTask(final Version source, final Version destination, final Task task, final UpgradeContext context) {
            super("Upgrade to " + destination, task);
            this.source = source;
            this.context = context;
        }

        /**
         * The factory method to be used to create instances of {@link UpgradeTask}.
         *
         * @param delegate The {@link Task} to be executed.
         * @param source The source {@link Version} to be verified at the beginning of the upgrade.
         * @param destination The destination {@link Version} to be set at the end of the
         *              upgrade.
         * @param context The context within which the upgrade should be executed.
         * @return An {@link UpgradeTask}.
         */
        public static UpgradeTask from(final Task delegate, final Version source, final Version destination, final UpgradeContext context) {
            final String stepsName = "Steps to upgrade to " + destination;

            final List<Task> steps = new ArrayList<Task>(3);
            steps.add(new VersionUpdatingTask(context, source.getTransition(destination)));
            steps.add(delegate);
            steps.add(new VersionUpdatingTask(context, destination));

            final Task aggregateTask = new SerialAggregateTask(context, stepsName, steps);

            return new UpgradeTask(source, destination, aggregateTask, context);
        }

        @Override
        protected void doCall() throws Exception {
            if (!context.getVersion().equals(source)) {
                throw new IllegalStateException("Schema version " + context.getVersion() + " does not match expected version of " + source + ".");
            }

            super.doCall();
        }
    }

    private final Graph graph;

    /**
     * An {@link UpgradeDefinition} containing no {@link Task}s.
     */
    private static final UpgradeDefinition NOOP = new UpgradeDefinition() {
        @Override
        public List<Task> getUpgradeTasks() {
            return Collections.emptyList();
        }
    };

    public GraphUpgradeDefinitionFactory(final Graph graph) {
        this.graph = graph;
    }

    @Override
    public boolean isUpgradeSupported(UpgradeContext context) {
        final Version sourceVersion = context.getVersion();

        final boolean upgradePathExists = graph.containsNode(sourceVersion);
        final boolean noOpUpgrade = sourceVersion.equals(getTargetVersion());

        return upgradePathExists || noOpUpgrade;
    }

    @Override
    public UpgradeDefinition create(final UpgradeContext context) {
        final Version current = context.getVersion();
        final Version required = getTargetVersion();

        if (current.equals(required)) {
            return NOOP;
        }

        final List<Edge> path = GraphHelper.extractPath(graph, current);

        final List<Task> upgradeTasks = new ArrayList<Task>(path.size());
        for (final Edge edge : path) {
            final Task rawTask = edge.createTask(context);
            final Task upgradeTask = UpgradeTask.from(rawTask, edge.getSource(), edge.getTarget(), context);
            upgradeTasks.add(upgradeTask);
        }

        return new UpgradeDefinition() {
            @Override
            public List<Task> getUpgradeTasks() {
                return Collections.unmodifiableList(upgradeTasks);
            }
        };
    }

    @Override
    public Version getTargetVersion() {
        return graph.getTerminalVersion();
    }

    @Override
    public String toString() {
        return "Graph upgrade definition: " + graph.toString();
    }
}
