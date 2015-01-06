/* **********************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
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
 * *********************************************************************/
package com.vmware.upgrade.dsl.syntax

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.ManifestLoader
import com.vmware.upgrade.dsl.model.ManifestModel
import com.vmware.upgrade.dsl.util.FinalVariableBinding
import com.vmware.upgrade.dsl.util.NoopProcessor
import com.vmware.upgrade.dsl.util.NoopTaskResolver
import com.vmware.upgrade.factory.UpgradeDefinitionFactory

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VersionKeywordTest {
    static ManifestModel fooManifest = ManifestLoader.loadInlineManifest("""
            noop = upgrade {}
            foo = manifest {
                from ''  to '1' call noop
            }"""
    )

    static ManifestModel barManifest = ManifestLoader.loadInlineManifest("""
            noop = upgrade {}
            bar = manifest {
                from ''  to '1' call noop
                from '1' to '2' call noop
            }"""
    )

    @DataProvider
    public Object[][] manifestProvider() {
        [
            [fooManifest, "\"1.0.0\""],
            [[fooManifest], "[ \"1.0.0\" ]"],
            [[fooManifest, barManifest], "[ \"1.0.0\", \"2.0.0\" ]"],
            [[fooManifest, [barManifest]], "[ \"1.0.0\", [ \"2.0.0\" ] ]"],
            [[fooManifest, [barManifest, fooManifest]], "[ \"1.0.0\", [ \"2.0.0\", \"1.0.0\" ] ]"],
            [[foo: fooManifest], "{ \"foo\": \"1.0.0\" }"],
            [[foo: fooManifest, bar: barManifest], "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.0\" }"],
            [[fooManifest, [foo: fooManifest, bar: barManifest], barManifest],  "[ \"1.0.0\", { \"foo\": \"1.0.0\", \"bar\": \"2.0.0\" }, \"2.0.0\" ]"]
        ]
    }

    @Test(groups = [ TestGroups.UNIT ], dataProvider = "manifestProvider")
    public void verifyUpgradeDefinitionFactoryIsCreatedWithExpectedTargetVersion(def manifests, String expectedTarget) {
        def caller = new ScriptSyntax({ it -> it }, new FinalVariableBinding(), new NoopTaskResolver(), new NoopProcessor())

        UpgradeDefinitionFactory factory = caller.version(manifests)

        String actual = factory.getTargetVersion().toString()

        Assert.assertEquals(actual, expectedTarget)
    }
}
