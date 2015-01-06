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
 * A test class to verify the behavior of {@link TableCreationSyntax}.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class CreateSyntaxTest {
    @DataProvider
    public Object[][] validCreateStatements() {
        return new Object[][] {
                new Object[] {
                        "create 't1' columns { add 'a' storing 'char(1)' }"
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing BOOL }"
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing NVARCHAR(16) }"
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing BOOL allowing null }"
                },
                new Object[] {
                        "create 'virtual_machine_record' columns {\n" +
                        "    add 'vm_id' storing LONG\n" +
                        "    add 'org_id' storing INTEGER\n" +
                        "    add 'machine_id' storing LONG\n" +
                        "    add 'created_at' storing DATE\n" +
                        "    add 'custom' storing NVARCHAR(123)\n" +
                        "} constraints {\n" +
                        "    primary 'vm_id'\n" +
                        "    unique 'machine_id' and 'org_id'\n" +
                        "}"
                }
        };
    }

    @DataProvider
    public Object[][] invalidCreateStatements() {
        return new Object[][] {
                new Object[] {
                        "create 't1' columns { add 'a' storing od }",
                        UnknownKeywordException.class,
                        "Unknown column type"
                },
                new Object[] {
                        "create 't1' columns {\n" +
                        "    add 'a' storing INTEGER\n" +
                        "} constraints {\n" +
                        "    and 'a'\n" +
                        "}",
                        IllegalStateException.class,
                        "'and' keyword used before 'primary' or 'unique'"
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing BOOL allowing 'foo' }",
                        IllegalArgumentException.class,
                        "expected null following 'allowing' but found 'foo'"
                },
                new Object[] {
                        "create 'TABLE' columns { add 'a' storing LONG }",
                        IllegalArgumentException.class,
                        "'TABLE' is a reserved keyword in: [MS_SQL, ORACLE, POSTGRES]"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validCreateStatements")
    public void verifyValidCreateSyntax(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidCreateStatements")
    public void verifyInvalidCreateSyntax(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }
}
