/* ****************************************************************************
 * Copyright (c) 2013-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.syntax

import groovy.mock.interceptor.MockFor

import com.vmware.upgrade.TestGroups
import com.vmware.upgrade.dsl.Loader
import com.vmware.upgrade.dsl.util.FinalVariableBinding
import com.vmware.upgrade.dsl.util.NoopProcessor
import com.vmware.upgrade.dsl.util.NoopTaskResolver

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * A test class to verify the resource-inclusion related behavior of {@link ScriptSyntax}.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class ResourceInclusionTest {
    @DataProvider
    public Object[][] resolvePaths() {
        [
            ["/Sub/Dir/Example.groovy", "[/Sub/Dir] | Example.groovy"],
            ["/Example.groovy", "[/] | Example.groovy"],
            ["Example.groovy", "[] | Example.groovy"]
        ]
    }

    @Test(groups=[TestGroups.UNIT], dataProvider = "resolvePaths")
    public void fromResolvesCorrectBaseAndSource(path, expected) {
        def mocker = new MockFor(Loader.class)
        mocker.demand.loadManifest(1) { source, mapper, taskResolver, processor, binding ->
            // Assert that mapper has been curried with the correct base.
            Assert.assertEquals(mapper(source), expected)
        }

        mocker.use {
            def mapper = { String name, Object[] base = [ "" ] ->
                "${base} | ${name}"
            }
            def caller = new ScriptSyntax(mapper, new FinalVariableBinding(), new NoopTaskResolver(), new NoopProcessor())

            caller.from(path)
        }
    }
}
