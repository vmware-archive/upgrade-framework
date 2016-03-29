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

package com.vmware.upgrade.dsl.syntax;

import java.util.Collection;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.ManifestLoader;
import com.vmware.upgrade.dsl.model.ManifestModel;
import com.vmware.upgrade.dsl.model.UpgradeTaskModel;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * Test cases for the "namespace" keyword.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class NamespaceKeywordTest {
    @Test(groups = { TestGroups.UNIT }, dataProvider = "namespace_script_generator")
    public void namespaceTest (final String script, final int count) throws UpgradeCompilationException {
        final ManifestModel parsedManifest = ManifestLoader.loadInlineManifest(script);

        verifyUpgrades(parsedManifest.getUpgrades(), count);
    }

    @DataProvider(name = "namespace_script_generator")
    public Object[][] generateValidNamespaceUpgradeScripts() {
        final int MAX_UPGRADE_TESTS = 3;
        final Object[][] result = new Object[MAX_UPGRADE_TESTS][2];

        for (int i = 0; i < MAX_UPGRADE_TESTS; ++i) {
            final String[] upgradeNames = new String[i];
            for (int j = 0; j < i; ++j) {
                upgradeNames[j] = "upgrade" + j;
            }

            result[i][0] = generateScriptForManyUpgradeNames(upgradeNames);
            result[i][1] = Integer.valueOf(i);
        }

        return result;
    }


    @Test(groups = { TestGroups.UNIT })
    public void sameUpgradeAndNamespaceNamesTest() throws UpgradeCompilationException {
        final String script =
            "foo = namespace {\n" +
            "foo = upgrade {}\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call foo.foo\n" +
            "}\n";

        final ManifestModel parsedManifest = ManifestLoader.loadInlineManifest(script);

        verifyUpgrades(parsedManifest.getUpgrades(), 1);
    }


    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { DuplicateVariableException.class })
    public void duplicateNamespaceTest() throws UpgradeCompilationException {
        final String script =
            "bar = namespace {\n" +
            "foo = upgrade {}\n" +
            "}\n" +
            "bar = namespace {\n" +
            "fu  = upgrade {}\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call bar.foo\n" +
            "from \"1.0.1\" call bar.foo\n" +
            "}\n";

        ManifestLoader.loadInlineManifest(script);
    }

    @Test(groups = { TestGroups.UNIT } )
    public void sameUpgradeNameInDifferentNamespacesTest() throws UpgradeCompilationException {
        final String script =
            "bar = namespace {\n" +
            "foo = upgrade {}\n" +
            "}\n" +
            "baz = namespace {\n" +
            "foo = upgrade {}\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call bar.foo\n" +
            "from \"1.0.1\" call baz.foo\n" +
            "}";

        final ManifestModel parsedManifest = ManifestLoader.loadInlineManifest(script);
        verifyUpgrades(parsedManifest.getUpgrades(), 2);
    }

    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { DuplicateVariableException.class })
    public void sameUpgradeNameInSameNamespacesTest() throws UpgradeCompilationException {
        final String script =
            "foo = namespace {\n" +
            "bar = upgrade {}\n" +
            "bar = upgrade {}\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call foo.bar\n" +
            "from \"1.0.1\" call foo.bar\n" +
            "}\n";

        ManifestLoader.loadInlineManifest(script);
    }

    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { UpgradeCompilationException.class })
    public void nestedNamespacesTest() throws UpgradeCompilationException {
        final String script =
            "foo = namespace {\n" +
            "bar = namespace {\n" +
            "baz = upgrade {}\n" +
            "} // namespace bar\n" +
            "} // namespace foo\n" +
            "manifest {\n" +
            "from \"1.0.0\" call foo.bar.baz\n" +
            "}\n";

        ManifestLoader.loadInlineManifest(script);
    }

    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { UnknownVariableException.class })
    public void incorrectNamespacesTest() throws UpgradeCompilationException {
        final String script =
            "bar = namespace {\n" +
            "foo = upgrade {}\n" +
            "}\n" +
            "baz = namespace {\n" +
            "foo2 = upgrade {}\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call baz.foo\n" +
            "}\n";

        ManifestLoader.loadInlineManifest(script);
    }

    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { UnknownVariableException.class })
    public void undefinedNamespacesTest() throws UpgradeCompilationException {
        final String script =
            "foo = namespace {\n" +
            "}\n" +
            "manifest {\n" +
            "from \"1.0.0\" call bar.bar\n" +
            "}\n";

        ManifestLoader.loadInlineManifest(script);
    }

   private void verifyUpgrades(Collection<UpgradeTaskModel> upgrades, int count) {
        Assert.assertEquals(upgrades.size(), count);

        UpgradeTaskModel previousUpgrade = null;
        for (final UpgradeTaskModel upgrade : upgrades) {
            Assert.assertNotNull(upgrade);
            Assert.assertNotSame(upgrade, previousUpgrade);
        }
    }

    private String generateScriptForManyUpgradeNames(String... upgradeNames) {
        final String namespaceName = "foo";
        final StringBuffer namespaceScriptSnippet = new StringBuffer("");
        final StringBuffer manifestScriptSnippet = new StringBuffer("");

        int count = 0;
        namespaceScriptSnippet.append(namespaceName).append(" = namespace {\n");

        for (final String upgradeName : upgradeNames) {
            namespaceScriptSnippet.append(upgradeName).append(" = upgrade {}\n");

            manifestScriptSnippet.
                append("from ").
                append("\"1.0.").append(count++).append("\""). // version no.
                append(" call ").
                append(namespaceName).append(".").append(upgradeName).
                append("\n");
        }

        namespaceScriptSnippet.append("}\n");

        final String script =
                namespaceScriptSnippet.toString() + "manifest {\n" + manifestScriptSnippet + "}\n";

        return script;
    }
}
