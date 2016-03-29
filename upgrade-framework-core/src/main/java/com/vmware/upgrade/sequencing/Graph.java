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

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;

/**
 * A {@link Graph} consists of a set of {@link Version} nodes connected by directed {@link Edge}s
 * which express the mechanisms (i.e. {@link Task}s) to transition between increasing versions.
 * <p>
 * Except for the terminal {@link Version} node, all nodes should have out-degree one.
 * <p>
 * The target of all {@link Edge}s must be strictly greater than the source.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface Graph {
    /**
     * A directed connection between two {@link Version}s within a {@link Graph}.
     */
    interface Edge {
        /**
         * Accessor for the origin {@link Version} node.
         *
         * @return the source {@link Version}
         */
        Version getSource();

        /**
         * Accessor for the destination {@link Version} node.
         *
         * @return the target {@link Version}
         */
        Version getTarget();

        /**
         * Factory method for constructing a {@link Task} to transition from the
         * {@link #getSource() source version} to the {@link #getTarget() target version}.
         *
         * @param context the context within the transition should occur.
         * @return the constructed {@link Task}.
         */
        Task createTask(final UpgradeContext context);
    }

    /**
     * Retrieve the {@link Edge} for a given {@link Version}.
     *
     * @param source the {@link Version} for which the lookup should occur.
     * @return the {@link Edge} with a {@link Edge#getSource()} of a given {@link Version} or
     *         {@code null} if none exists.
     */
    Edge getEdge(final Version source);

    /**
     * Determine whether or not the {@link Graph} contains the specified {@link Version} as either
     * the source or a destination of an {@link Edge}.
     *
     * @param source the {@link Version} for which the lookup should occur.
     * @return {@code true} if and only if the {@link Graph} contains the {@link Version}.
     */
    boolean containsNode(final Version source);

    /**
     * Retrieve the terminal {@link Version} of the {@link Graph}.
     *
     * @return the terminal {@link Version} of the {@link Graph}
     */
    Version getTerminalVersion();
}
