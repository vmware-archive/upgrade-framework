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
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link ReferenceSyntax} and {@link UnreferenceSyntax}.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class ReferenceSyntaxTest {
    @DataProvider
    public Object[][] validReferenceStatements() {
        return new Object[][] {
                new Object[] { "reference 'p_id' of 'person' from 'person_id' of 'virtual_machine_library'" },
                new Object[] { "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2'" },
                new Object[] { "reference 'c' and 'd' and 'e' of 't' from 'c2' and 'd2' and 'e2' of 't2'" },
                new Object[] { "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete cascade" },
                new Object[] { "unreference 'person' from 'virtual_machine_library'" }
        };
    }

    @DataProvider
    public Object[][] invalidReferenceStatements() {
        return new Object[][] {
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' of 't2'",
                        IllegalStateException.class,
                        "Number of source columns does not equal number of target columns"

                },
                new Object[] {
                        "reference 'c' of 't' from 'c2' and 'd2' of 't2'",
                        IllegalStateException.class,
                        "Number of source columns does not equal number of target columns"
                },
                new Object[] {
                        "reference 'c' from 'c2' of 't2'",
                        IllegalStateException.class,
                        "No target table specified. An 'of' keyword is probably missing"
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete",
                        UnknownKeywordException.class,
                        "Missing keyword following 'on_delete'"
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' k",
                        UnknownKeywordException.class,
                        "Unknown keyword 'k'"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validReferenceStatements")
    public void verifyValidReferenceSyntax(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidReferenceStatements")
    public void verifyInvalidReferenceSyntax(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }
}
