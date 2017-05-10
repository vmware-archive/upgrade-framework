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

package com.vmware.upgrade.dsl.sql.syntax;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link CommentSyntax}.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class CommentSyntaxTest {
    private final String longComment = StringUtils.repeat("x", 129);

    @DataProvider
    public Object[][] validCommentStatements() {
        return new Object[][] {
                new Object[] { "comment 'test' on 'column' of 'table'" },
                new Object[] { "comment 'test' on 'table'" },
                new Object[] { "comment oracle: 'test', ms_sql: 'other test', postgres: 'ps test' on 'table'" },
                new Object[] { "comment oracle: '" + longComment + "', ms_sql: 'test', postgres: '" + longComment + "' on 'table'" }
        };
    }

    @DataProvider
    public Object[][] invalidCommentStatements() {
        return new Object[][] {
                new Object[] { "comment '" + longComment + "' on 'column' of 'table'" },
                new Object[] { "comment '" + longComment + "' on 'table'" },
                new Object[] { "comment ms_sql: '" + longComment + "'" }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validCommentStatements")
    public void verifyValidCommentSyntax(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidCommentStatements")
    public void verifyInvalidCommentSyntax(String ddl) {
        try {
            UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

            SyntaxTestUtil.basicVerification(upgrade);
            Assert.fail("IllegalArgumentException not thrown for invalid comments");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
