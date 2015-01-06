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

package com.vmware.upgrade.dsl

import com.vmware.upgrade.TestGroups
import com.vmware.upgrade.dsl.model.ManifestModel
import com.vmware.upgrade.dsl.syntax.MissingFileException
import com.vmware.upgrade.dsl.syntax.UpgradeCompilationException
import com.vmware.upgrade.dsl.util.NoopProcessor
import com.vmware.upgrade.dsl.util.NoopTaskResolver

import org.testng.Assert
import org.testng.annotations.Test

public class LoaderTest {
    @Test(groups=[TestGroups.UNIT])
    public void loadManifestTest() throws UpgradeCompilationException {
        def resourceMapper = { String name ->
            URL u = getClass().getResource("/upgrade/${name}")
            if (u == null) {
                throw new MissingFileException(name)
            }
            new GroovyCodeSource(u)
        }

        ManifestModel m = Loader.loadManifest("nullManifest.groovy", resourceMapper, new NoopTaskResolver(), new NoopProcessor());
        Assert.assertNotNull(m);
        Assert.assertEquals(m.getUpgrades().size(), 0);
    }
}
