/* ****************************************************************************
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
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
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link IndexSyntax}.
 *
 * @author Matthew Frost <mfrost@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class IndexSyntaxTest {
    @DataProvider
    public Object[][] validIndexStatements() {
        return new Object[][] {
                new Object[] { "index 'a' of 't'" },
                new Object[] { "index default: 'a', oracle: 'b' of 't'" },
                new Object[] { "index 'a' and 'b' of 't'" },
                new Object[] { "unindex 'a' of 't'" },
                new Object[] { "unindex 'a' and 'b' of 't'" },
                new Object[] { "unindex 'ix_a' of 't'" }
        };
    }

    @DataProvider
    public Object[][] invalidIndexStatements() {
        return new Object[][] {
                new Object[] {
                        "index 'a' of",
                        UnknownKeywordException.class,
                        "Missing keyword following 'of'"
                },
                new Object[] {
                        "index a of 't'",
                        UnknownKeywordException.class,
                        "Unknown keyword 'a'"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validIndexStatements")
    public void verifyValidIndexSyntax(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidIndexStatements")
    public void verifyInvalidIndexSyntax(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }

}
