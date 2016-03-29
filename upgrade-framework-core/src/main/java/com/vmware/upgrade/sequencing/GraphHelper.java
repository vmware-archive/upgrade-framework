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

import java.util.LinkedList;
import java.util.List;

import com.vmware.upgrade.sequencing.Graph.Edge;

/**
 * Utility class for convenience methods related to use of {@link Graph} instances.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class GraphHelper {
    public static List<Edge> extractPath(final Graph graph, final Version version) {
        final List<Edge> upgrades = new LinkedList<Edge>();

        Version runningSourceVersionPart = version;

        Edge nextModel = graph.getEdge(runningSourceVersionPart);
        while (nextModel != null) {
            final Version nextSourceVersionPart = nextModel.getTarget();

            // This should never happen as it is checked elsewhere, but is double-checked here to
            // be safe (i.e. prevent a potential infinite loop).
            if (runningSourceVersionPart.compareTo(nextSourceVersionPart) > 0) {
                throw new AssertionError(runningSourceVersionPart);
            }

            upgrades.add(nextModel);

            runningSourceVersionPart = nextSourceVersionPart;
            nextModel = graph.getEdge(runningSourceVersionPart);
        }

        return upgrades;
    }
}
