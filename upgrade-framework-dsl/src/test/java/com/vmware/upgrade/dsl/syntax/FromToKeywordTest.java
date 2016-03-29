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

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.ManifestLoader;
import com.vmware.upgrade.dsl.model.ManifestModel;
import com.vmware.upgrade.dsl.model.UpgradeTaskModel;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for the "from" and "to" keywords.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class FromToKeywordTest {
    @Test(groups = { TestGroups.UNIT }, expectedExceptions = { DuplicateSourceException.class })
    public void duplicateSourceVersionManifestTest() throws UpgradeCompilationException {
        ManifestLoader.loadInlineManifest(
                "foo = upgrade {}\n" +
                "manifest {\n" +
                "from '1.0.0' call foo\n" +
                "from '1.0.0' call foo\n" +
                "}");
    }

    @Test(groups = { TestGroups.UNIT })
    public void duplicateTargetVersionManifestTest() throws UpgradeCompilationException {
        ManifestModel m = ManifestLoader.loadInlineManifest(
                "foo = upgrade {}\n" +
                "manifest {\n" +
                "from '1.0.0' to '1.0.1' call foo\n" +
                "from '1.0.0.fixup' to '1.0.1' call foo\n" +
                "}");
        Assert.assertNotNull(m);
        Assert.assertEquals(m.getUpgrades().size(), 2);
    }

    @Test(groups = { TestGroups.UNIT })
    public void defaultToVersionTest() throws UpgradeCompilationException {
        ManifestModel m = ManifestLoader.loadInlineManifest(
                "foo = upgrade {}\n" +
                "manifest {\n" +
                "from '1.0.0.fixup' call foo\n" +
                "}");
        Assert.assertNotNull(m);
        Assert.assertEquals(m.getUpgrades().size(), 1);
        for (UpgradeTaskModel upgradeEntry: m.getUpgrades()) {
            Assert.assertEquals(upgradeEntry.getTarget().toString(), "\"1.0.1.fixup\"");
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void fromAnyVersionToSpecifiedVersionTest() throws UpgradeCompilationException {
        ManifestModel m = ManifestLoader.loadInlineManifest(
                "foo = upgrade {}\n" +
                "manifest {\n" +
                "to 2 call foo\n" +
                "}");
        Assert.assertNotNull(m);

        final UpgradeTaskModel[] models = m.getUpgrades().toArray(new UpgradeTaskModel[0]);
        Assert.assertEquals(models.length, 2);

        for (int i = 0; i < models.length; i++) {
            Assert.assertEquals(models[i].getSource().toString(), "\"" + i + ".0.0\"");
            Assert.assertEquals(models[i].getTarget().toString(), "\"2.0.0\"");
        }
    }
}
