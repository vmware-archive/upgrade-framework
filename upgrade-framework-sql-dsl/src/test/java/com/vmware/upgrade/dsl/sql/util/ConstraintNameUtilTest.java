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

package com.vmware.upgrade.dsl.sql.util;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.sql.util.ConstraintNameUtil;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link ReferenceSyntax} and {@link ConstraintNameUtil}.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class ConstraintNameUtilTest {
    @DataProvider
    public Object[][] constraints() {
        return new Object[][] {
                new Object[] { "abc", 0, "" },
                new Object[] { "abc", 1, "a" },
                new Object[] { "a_b_c", 1, "a" },
                new Object[] { "a_b_c", 2, "a" },
                new Object[] { "a_b_c", 3, "a_b" },
                new Object[] { "ab_cd_ef", 2, "ab" },
                new Object[] { "ab_cd_ef", 5, "a_c_e" },
                new Object[] { "abc_def_ghi", 6, "ab_d_g" },
                new Object[] { "abc_def_ghi", 11, "abc_def_ghi" },
                new Object[] { "abc_def_ghi", 12, "abc_def_ghi" },
                new Object[] { "vshield_manager", 13, "vshiel_manage" },
                new Object[] { "vcenter_supported_builds", 13, "vcen_supp_bui" },
                new Object[] { "network_appliance_pvdc_mview", 13, "net_app_pv_mv"},
                new Object[] { "network_appliance_pvdc_m", 13, "net_app_pvd_m"}
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "constraints")
    public void abbreviate(String name, int maxLength, String expected) {
        final String abbreviatedName = ConstraintNameUtil.abbreviate(name, maxLength);

        Assert.assertEquals(abbreviatedName, expected);
    }
}
