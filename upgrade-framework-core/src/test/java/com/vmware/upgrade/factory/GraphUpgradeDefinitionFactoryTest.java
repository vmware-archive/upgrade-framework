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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vmware.upgrade.DummyUpgradeContext;
import com.vmware.upgrade.Task;
import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.UpgradeDefinition;
import com.vmware.upgrade.sequencing.AbstractGraph;
import com.vmware.upgrade.sequencing.Version;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link GraphUpgradeDefinitionFactory}.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class GraphUpgradeDefinitionFactoryTest {
    @Test(groups = { TestGroups.UNIT })
    public void verifyEmpty() throws IOException {
        final UpgradeDefinitionFactory empty = new GraphUpgradeDefinitionFactory(new AbstractGraph() {
            @Override
            protected Map<Version, Edge> getEdges() {
                return Collections.emptyMap();
            }
        });

        final UpgradeDefinition definition = empty.create(new DummyUpgradeContext());

        Assert.assertEquals(definition.getUpgradeTasks().size(), 0);
    }

    @Test(groups = { TestGroups.UNIT })
    public void verifyNoOp() throws IOException {
        final UpgradeDefinitionFactory empty = new GraphUpgradeDefinitionFactory(new AbstractGraph() {
            @Override
            protected Map<Version, Edge> getEdges() {
                final Map<Version, Edge> map = new HashMap<Version, Edge>();
                map.put(Version.INITIAL, new ImmutableEdge(Version.INITIAL, Version.INITIAL.getNext(), EasyMock.createMock(Task.class)));
                return map;
            }
        });

        final UpgradeContext context = new DummyUpgradeContext() {
            @Override
            public Version getVersion() {
                return Version.INITIAL.getNext();
            }
        };

        Assert.assertTrue(empty.isUpgradeSupported(context));

        final UpgradeDefinition definition = empty.create(context);

        Assert.assertEquals(definition.getUpgradeTasks().size(), 0);
    }
}
