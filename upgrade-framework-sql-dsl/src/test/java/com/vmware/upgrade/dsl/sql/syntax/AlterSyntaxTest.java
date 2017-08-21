/* ****************************************************************************
 * Copyright (c) 2012-2017 VMware, Inc. All Rights Reserved.
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
 * A test class to verify the behavior of {@link TableAlterationSyntax}.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class AlterSyntaxTest {
    @DataProvider
    public Object[][] validAlterStatements() {
        return new Object[][] {
                new Object[] { "alter 't' add 'a' storing 'char(1)'" },
                new Object[] { "alter 't' add 'a' storing BLOB" },
                new Object[] { "alter 't' add 'a' storing CLOB" },
                new Object[] { "alter 't' add 'a' storing BOOL" },
                new Object[] { "alter 't' add 'a' storing BOOL default_sql '1'" },
                new Object[] { "alter 't' add 'a' storing DATE" },
                new Object[] { "alter 't' add 'a' storing DATE default_sql ms_sql: 'GETDATE()', oracle: 'SYSTIMESTAMP', postgres: 'NOW()'" },
                new Object[] { "alter 't' add 'a' storing NVARCHAR(16)" },
                new Object[] { "alter 't' add 'a' storing LONG" },
                new Object[] { "alter 't' add 'a' storing BOOL allowing null" },
                new Object[] { "alter 't' add 'a' storing LONG initial_value 12345" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) initial_value 'i'" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) default_value 'j'" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) allowing null initial_value 'i'" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) initial_value 'i' allowing null" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) allowing null default_value 'j'" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) allowing null default_value null" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) default_value 'j' allowing null" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) allowing null initial_value 'i' default_value 'j'" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) initial_value 'i' default_value 'j' allowing null" },
                new Object[] { "alter 't' add 'a' storing VARCHAR(16) default_value 'j' initial_value 'i' allowing null" },
                new Object[] { "alter oracle: 't', ms_sql: 'x', postgres: 'p' add 'a' storing 'char(1)'" },
                new Object[] { "alter 't' add oracle: 'a1', ms_sql: 'a2', postgres: 'a3' storing 'char(1)'" },
                new Object[] { "alter 't' add oracle: 'ANALYZE', ms_sql: 'ACCESS', postgres: 'ADD' storing 'char(1)'" },
                new Object[] { "alter 't' add 'a' storing oracle: 'nvarchar2(1)', ms_sql: 'nvarchar(1)', postgres: 'varchar(1)'" },
                new Object[] { "alter 't' drop 'c'" },
                new Object[] { "alter 't' rename 'a' to 'b'" },
                new Object[] { "alter 't' add_primary 'a'" },
                new Object[] { "alter 't' add_primary 'a' and 'b' and 'c'" },
                new Object[] { "alter 't' drop_primary 'a'" },
                new Object[] { "alter 't' drop_primary 'a' and 'b' and 'c'" },
                new Object[] { "alter 't' add_unique 'a'" },
                new Object[] { "alter 't' add_unique 'a' and 'b' and 'c'" },
                new Object[] { "alter 't' drop_unique 'a'" },
                new Object[] { "alter 't' drop_unique 'a' and 'b' and 'c'" }
        };
    }

    @DataProvider
    public Object[][] invalidAlterStatements() {
        return new Object[][] {
                new Object[] {
                        "alter 't' add 'a' storing od",
                        UnknownKeywordException.class,
                        "Unknown keyword 'od'"
                },
                new Object[] {
                        "alter 't' and 'b'",
                        IllegalStateException.class,
                        "'and' keyword used before 'drop_primary' or 'drop_unique'"
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL allowing foo",
                        UnknownKeywordException.class,
                        "Unknown keyword 'foo'"
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL allowing 'foo'",
                        IllegalArgumentException.class,
                        "expected null following 'allowing' but found 'foo'"
                },
                new Object[] {
                        "alter 't' add 'ADD' storing 'char(1)'",
                        IllegalArgumentException.class,
                        "'ADD' is a reserved keyword in: [MS_SQL, ORACLE]"
                },
                new Object[] {
                        "alter 't' add 'add' storing 'char(1)'",
                        IllegalArgumentException.class,
                        "'add' is a reserved keyword in: [MS_SQL, ORACLE]"
                },
                new Object[] {
                        "alter 't' add 'COLUMN_WITH_A_NAME_MORE_THAN_MAX_LENGTH' storing BOOL",
                        IllegalArgumentException.class,
                        "'COLUMN_WITH_A_NAME_MORE_THAN_MAX_LENGTH' exceeds maximum length of 30 characters"
                },
                new Object[] {
                        "alter 't' rename 'a' to 'COLUMN_WITH_A_NAME_MORE_THAN_MAX_LENGTH'",
                        IllegalArgumentException.class,
                        "'COLUMN_WITH_A_NAME_MORE_THAN_MAX_LENGTH' exceeds maximum length of 30 characters"
                },
                new Object[] {
                        "alter oracle: 'o', ms_sql: 'm', postgres: 'p' add 'ADD' storing 'char(1)'",
                        IllegalArgumentException.class,
                        "'ADD' is a reserved keyword in: [MS_SQL, ORACLE]"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER allowing null allowing null",
                        IllegalArgumentException.class,
                        "'allowing null' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER allowing null initial_value 0 allowing null",
                        IllegalArgumentException.class,
                        "'allowing null' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER initial_value 0 initial_value 0",
                        IllegalArgumentException.class,
                        "'initial_value' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER initial_value 0 allowing null initial_value 0",
                        IllegalArgumentException.class,
                        "'initial_value' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER default_value 0 default_value 0",
                        IllegalArgumentException.class,
                        "'default_value' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER default_value 0 allowing null default_value 0",
                        IllegalArgumentException.class,
                        "'default_value' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing DATE default_sql 'NOW()' allowing null default_sql 'NOW()'",
                        IllegalArgumentException.class,
                        "'default_sql' has already been specified"
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER default_value null",
                        IllegalArgumentException.class,
                        "Default cannot be null unless 'allowing null' was specified ('default_value null' may be provided with 'allowing null' but is not necessary)"
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL default_sql 1",
                        IllegalArgumentException.class,
                        "Expected a String or Map of Strings following 'default_sql'"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "validAlterStatements")
    public void verifyValidAlterSyntax(String ddl) {
        UpgradeDefinitionModel upgrade = SyntaxTestUtil.loadInline(ddl);

        SyntaxTestUtil.basicVerification(upgrade);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidAlterStatements")
    public void verifyInvalidAlterSyntax(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }
}
