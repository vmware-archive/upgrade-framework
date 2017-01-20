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

package com.vmware.upgrade.dsl.sql.semantics;

import java.util.HashMap;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel;
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory;
import com.vmware.upgrade.sql.SQLStatement;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateSemanticsTest {
    @SuppressWarnings("serial")
    @DataProvider
    public Object[][] createStatements() {
        return new Object[][] {
                new Object[] {
                        "create 't1' columns { add 'a' storing 'char(1)' }",
                        SQLStatementFactory.create("CREATE TABLE t1 ( a char(1) )")
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing BOOL }",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( a TINYINT DEFAULT 0 NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( a NUMBER(1,0) DEFAULT 0 NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( a BOOLEAN DEFAULT FALSE NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing BOOL allowing null }",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( a TINYINT DEFAULT 0 NULL )");
                                    put("oracle", "CREATE TABLE t1 ( a NUMBER(1,0) DEFAULT 0 NULL )");
                                    put("postgres", "CREATE TABLE t1 ( a BOOLEAN DEFAULT FALSE NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { " +
                        "  add 'a' storing BOOL allowing null\n" +
                        "  add 'b' storing BOOL " +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( " +
                                            "a TINYINT DEFAULT 0 NULL, " +
                                            "b TINYINT DEFAULT 0 NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( " +
                                            "a NUMBER(1,0) DEFAULT 0 NULL, " +
                                            "b NUMBER(1,0) DEFAULT 0 NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( " +
                                            "a BOOLEAN DEFAULT FALSE NULL, " +
                                            "b BOOLEAN DEFAULT FALSE NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { " +
                        "  add 'a' storing NVARCHAR(16) allowing null\n" +
                        "  add 'b' storing NVARCHAR(16) " +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( " +
                                            "a NVARCHAR(16) NULL, " +
                                            "b NVARCHAR(16) NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( " +
                                            "a NVARCHAR2(16) NULL, " +
                                            "b NVARCHAR2(16) NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( " +
                                            "a VARCHAR(16) NULL, " +
                                            "b VARCHAR(16) NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { " +
                        "  add 'a' storing TEST_VARCHAR allowing null\n" +
                        "  add 'b' storing TEST_VARCHAR " +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( " +
                                            "a VARCHAR(128) NULL, " +
                                            "b VARCHAR(128) NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( " +
                                            "a VARCHAR2(128) NULL, " +
                                            "b VARCHAR2(128) NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( " +
                                            "a VARCHAR(128) NULL, " +
                                            "b VARCHAR(128) NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing NVARCHAR(16) }",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( a NVARCHAR(16) NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( a NVARCHAR2(16) NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( a VARCHAR(16) NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing NVARCHAR(16) allowing null }",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( a NVARCHAR(16) NULL )");
                                    put("oracle", "CREATE TABLE t1 ( a NVARCHAR2(16) NULL )");
                                    put("postgres", "CREATE TABLE t1 ( a VARCHAR(16) NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 't1' columns { add 'a' storing NVARCHAR(16) default_value 'foo' }",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "CREATE TABLE t1 ( a NVARCHAR(16) DEFAULT 'foo' NOT NULL )");
                                    put("oracle", "CREATE TABLE t1 ( a NVARCHAR2(16) DEFAULT 'foo' NOT NULL )");
                                    put("postgres", "CREATE TABLE t1 ( a VARCHAR(16) DEFAULT 'foo' NOT NULL )");
                                }}
                        )
                },
                new Object[] {
                        "create 'virtual_machine_record' columns {\n" +
                        "    add 'vm_id' storing LONG\n" +
                        "    add 'org_id' storing INTEGER\n" +
                        "    add 'machine_id' storing LONG\n" +
                        "    add 'custom' storing NVARCHAR(123)\n" +
                        "    add 'custom2' storing VARCHAR(234)\n" +
                        "    add 'created_at' storing DATE\n" +
                        "} constraints {\n" +
                        "    primary 'vm_id'\n" +
                        "    unique 'machine_id' and 'org_id'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "CREATE TABLE virtual_machine_record ( vm_id BIGINT NOT NULL, " +
                                            "org_id INT NOT NULL, machine_id BIGINT NOT NULL, " +
                                            "custom NVARCHAR(123) NOT NULL, custom2 VARCHAR(234) NOT NULL, " +
                                            "created_at DATETIME DEFAULT GETDATE() NOT NULL, " +
                                            "CONSTRAINT pk_virt_mach_rec_vm_id PRIMARY KEY ( vm_id ), " +
                                            "CONSTRAINT uq_virt_mach_rec_mac_id_org_id UNIQUE ( machine_id, org_id ) )"
                                    );
                                    put(
                                            "oracle",
                                            "CREATE TABLE virtual_machine_record ( vm_id NUMBER(19,0) NOT NULL, " +
                                            "org_id NUMBER(10,0) NOT NULL, machine_id NUMBER(19,0) NOT NULL, " +
                                            "custom NVARCHAR2(123) NOT NULL, custom2 VARCHAR2(234) NOT NULL, " +
                                            "created_at TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL, " +
                                            "CONSTRAINT pk_virt_mach_rec_vm_id PRIMARY KEY ( vm_id ), " +
                                            "CONSTRAINT uq_virt_mach_rec_mac_id_org_id UNIQUE ( machine_id, org_id ) )");
                                    put(
                                            "postgres",
                                            "CREATE TABLE virtual_machine_record ( vm_id BIGINT NOT NULL, " +
                                            "org_id INT NOT NULL, machine_id BIGINT NOT NULL, " +
                                            "custom VARCHAR(123) NOT NULL, custom2 VARCHAR(234) NOT NULL, " +
                                            "created_at TIMESTAMP (6) DEFAULT NOW() NOT NULL, " +
                                            "CONSTRAINT pk_virt_mach_rec_vm_id PRIMARY KEY ( vm_id ), " +
                                            "CONSTRAINT uq_virt_mach_rec_mac_id_org_id UNIQUE ( machine_id, org_id ) )");
                                }}
                        )
                },
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "createStatements")
    public void compareCreateStatements(String ddl, SQLStatement expected) {
        final UpgradeDefinitionModel upgrade = SemanticTestUtil.createUpgradeModel(ddl);

        SemanticTestUtil.compareModelToSQL(upgrade, expected);
    }
}
