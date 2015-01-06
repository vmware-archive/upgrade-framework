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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SafeSyntaxTest {
    @DataProvider
    public Object[][] validSafeStatements() {
        return new Object[][] {
                new Object[] {
                        "safe('table creation') {\n" +
                        "    create 'table1' columns { add 'id' storing LONG } constraints { primary 'id' }\n" +
                        "}"
                },
                new Object[] {
                        "safe('alter table') {\n" +
                        "    alter 'table1' add 'ship_date' storing DATE\n" +
                        "    alter 'table1' rename 'name' to 'new_name'\n" +
                        "    alter 'table1' retype 'new_name' to VARCHAR(128)\n" +
                        "    alter 'table1' drop 'name'\n" +
                        "    alter 'table1' drop_primary 'id'\n" +
                        "    alter 'table1' drop_unique 'example'\n" +
                        "}"
                },
                new Object[] {
                        "safe('foreign keys') {\n" +
                        "    reference 'id' of 'table2' from 'id' of 'table1'\n" +
                        "    unreference 'id' from 'table'\n" +
                        "}"
                },
                new Object[] {
                        "safe('column comment') {\n" +
                        "    comment 'good comment' on 'column' of 'table1'\n" +
                        "}"
                },
                new Object[] {
                        "safe('table comment') {\n" +
                        "    comment 'good comment' on 'table1'\n" +
                        "}"
                },
                new Object[] {
                        "safe('create index') {\n" +
                        "    index 'a' of 't'\n" +
                        "}"
                },
                new Object[] {
                        "safe('drop index') {\n" +
                        "    unindex 'a_idx' of 't'\n" +
                        "}"
                },
                new Object[] {
                        "safe('drop view') {\n" +
                        "    drop_view 'view'\n" +
                        "}"
                }
        };
    }

    @DataProvider
    public Object[][] invalidSafeStatements() {
        return new Object[][] {
                new Object[] {
                        "safe { drop_view 'view_name' }",
                        IllegalArgumentException.class,
                        "When using the 'safe' keyword"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validSafeStatements")
    public void verifyValidSafeStatements(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidSafeStatements")
    public void verifyInvalidSafeStatements(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }
}
