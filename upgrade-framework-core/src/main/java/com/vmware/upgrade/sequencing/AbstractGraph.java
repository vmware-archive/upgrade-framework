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

package com.vmware.upgrade.sequencing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;

/**
 * An abstract base class to facilitate implementation of {@link Graph}s.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractGraph implements Graph {
    /**
     * A trivial {@link Edge} implementation suitable for use when all {@link Tasks} have been
     * constructed prior to creation of the {@link Graph}.
     * <p>
     * This class should <em>not</em> be used when complex {@link Graph} structure is expected;
     * it requires all {@link Task}s to be constructed (instead of deferring construction and
     * instantiating only those which will be executed).
     */
    protected final class ImmutableEdge implements Edge {
        private final Version source;
        private final Version target;
        private final Task task;

        public ImmutableEdge(final Version source, final Version target, final Task task) {
            this.source = source;
            this.target = target;
            this.task = task;
        }

        @Override
        public Version getSource() {
            return source;
        }

        @Override
        public Version getTarget() {
            return target;
        }

        @Override
        public Task createTask(UpgradeContext context) {
            return task;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImmutableEdge)) {
                return false;
            }

            final ImmutableEdge o = (ImmutableEdge) obj;

            return Objects.equals(source, o.source) &&
                    Objects.equals(target, o.target) &&
                    Objects.equals(task, o.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target, task);
        }
    }

    /**
     * Retrieve all {@link Graph.Edge Edge}s in the {@link Graph}.
     *
     * @return a mapping of {@link Graph.Edge#getSource() Edge#getSource()} to
     *          {@link Graph.Edge Edge} for each {@link Graph.Edge Edge} currently in the
     *          {@link Graph}.
     */
    protected abstract Map<Version, Edge> getEdges();

    @Override
    public Graph.Edge getEdge(Version source) {
        return getEdges().get(source);
    }

    @Override
    public boolean containsNode(Version version) {
        if (getEdges().containsKey(version)) {
            return true;
        }

        for (Edge edge : getEdges().values()) {
            if (edge.getTarget().equals(version)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Version getTerminalVersion() {
        final Set<Version> versions = getEdges().keySet();

        if (versions.size() == 0) {
            return null;
        }

        final Version nextToLastNode = Collections.max(versions);
        final Version terminalNode = getEdge(nextToLastNode).getTarget();

        return terminalNode;
    }
}
