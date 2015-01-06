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

package com.vmware.upgrade.dsl.sql.semantics;

import java.util.HashMap;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel;
import com.vmware.upgrade.dsl.sql.semantics.SemanticTestUtil.TestDatabaseTypes;
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory;
import com.vmware.upgrade.sql.SQLStatement;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("serial")
public class SafeSemanticsTest {
    private static final SQLStatement TABLE_WRAPPER =
        SQLStatementFactory.create(
                new HashMap<String, String>() {{
                    put(
                            "ms_sql",
                            "\nIF OBJECT_ID(N'table1', N'U') IS %sNULL\n" + // Fill in either "" or "NOT "
                            "  BEGIN\n%s\n  END\n" // Fill in SQL to execute
                    );
                    put(
                            "oracle",
                            "\nDECLARE\n" +
                            "  t INT;\n" +
                            "\n" +
                            "  BEGIN\n" +
                            "    SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = UPPER('table1');\n" +
                            "\n" +
                            "    IF t = %s THEN\n" + // Fill in either a 0 or 1
                            "      EXECUTE IMMEDIATE '%s';\n" + // Fill in SQL to execute
                            "    END IF;\n" +
                            "  END;\n"
                    );
                    put(
                            "postgres",
                            "\nDO $$\n" +
                            "DECLARE\n" +
                            "  t INT;\n" +
                            "\n" +
                            "  BEGIN\n" +
                            "    SELECT COUNT(*) INTO t FROM pg_class WHERE relname = LOWER('table1') AND relkind = 'r';\n" +
                            "    IF t = %s THEN\n" + // Fill in either a 0 or 1
                            "      EXECUTE '%s';\n" + // Fill in SQL to execute
                            "    END IF;\n" +
                            "  END$$;\n"
                    );
                }}
        );

    private static final SQLStatement TABLE_COLUMN_WRAPPER =
        SQLStatementFactory.create(
                new HashMap<String, String>() {{
                    put(
                            "ms_sql",
                            "\nIF EXISTS (SELECT * FROM sys.columns WHERE OBJECT_ID = OBJECT_ID(N'table1', N'U') AND Name = 'name')\n" +
                            "  BEGIN\n%s\n  END\n" // Fill in SQL to execute
                    );
                    put(
                            "oracle",
                            "\nDECLARE\n" +
                            "  t INT;\n" +
                            "  c INT;\n" +
                            "\n" +
                            "  BEGIN\n" +
                            "    SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = UPPER('table1');\n" +
                            "    SELECT COUNT(*) INTO c FROM user_tab_cols WHERE table_name = UPPER('table1') AND column_name = UPPER('name');\n" +
                            "\n" +
                            "    IF t = 1 AND c = 1 THEN\n" +
                            "      EXECUTE IMMEDIATE '%s';\n" + // Fill in SQL to execute
                            "    END IF;\n" +
                            "  END;\n"
                    );
                    put(
                            "postgres",
                            "\nDO $$\n" +
                            "DECLARE\n" +
                            "  t INT;\n" +
                            "\n" +
                            "  BEGIN\n" +
                            "    SELECT COUNT(*) INTO t FROM pg_class tables\n" +
                            "       JOIN pg_attribute columns ON columns.attrelid = tables.oid\n" +
                            "       WHERE tables.relname = LOWER('table1') AND tables.relkind = 'r' AND columns.attname = LOWER('name');\n" +
                            "    IF t = 1 THEN\n" +
                            "      EXECUTE '%s';\n" + // Fill in SQL to execute
                            "    END IF;\n" +
                            "  END$$;\n"
                    );
                }}
        );

    @DataProvider
    public Object[][] safeStatements() {
        return new Object[][] {
                new Object[] {
                        "safe('table creation') {\n" +
                        "    create 'table1' columns { add 'id' storing LONG } constraints { primary 'id' }\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "",
                                                    "CREATE TABLE table1 ( id BIGINT NOT NULL, CONSTRAINT pk_table1_id PRIMARY KEY ( id ) )"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "0",
                                                    "CREATE TABLE table1 ( id NUMBER(19,0) NOT NULL, CONSTRAINT pk_table1_id PRIMARY KEY ( id ) )"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "0",
                                                    "CREATE TABLE table1 ( id BIGINT NOT NULL, CONSTRAINT pk_table1_id PRIMARY KEY ( id ) )"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('add column') {\n" +
                        "    alter 'table1' add 'name' storing NVARCHAR(128)\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "ALTER TABLE table1 ADD name NVARCHAR(128) NOT NULL"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "ALTER TABLE table1 ADD name NVARCHAR2(128) NOT NULL"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "ALTER TABLE table1 ADD name VARCHAR(128) NOT NULL"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('rename column') {\n" +
                        "    alter 'table1' rename 'name' to 'new_name'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "EXEC sp_rename 'table1.[name]', 'new_name', 'COLUMN'"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "ALTER TABLE table1 RENAME COLUMN name TO new_name"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "ALTER TABLE table1 RENAME COLUMN name TO new_name"
                                            )
                                    );
                                }}
                        )
                },

                new Object[] {
                        "safe('retype column') {\n" +
                        "    alter 'table1' retype 'name' to NVARCHAR(256)\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "ALTER TABLE table1 ALTER COLUMN name NVARCHAR(256) NOT NULL"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "\nDECLARE\n" +
                                                    "l_nullable VARCHAR(1);\n" +
                                                    "\n" +
                                                    "BEGIN\n" +
                                                    "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('table1') AND column_name = UPPER('name');\n" +
                                                    "\n" +
                                                    "  IF l_nullable = 'N' THEN\n" +
                                                    "    EXECUTE IMMEDIATE 'ALTER TABLE table1 MODIFY (name NVARCHAR2(256)  )';\n" +
                                                    "  END IF;\n" +
                                                    "  IF l_nullable = 'Y' THEN\n" +
                                                    "    EXECUTE IMMEDIATE 'ALTER TABLE table1 MODIFY (name NVARCHAR2(256)  NOT NULL)';\n" +
                                                    "  END IF;\n" +
                                                    "END;\n"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "ALTER TABLE table1 DROP COLUMN name; ALTER TABLE table1 ADD COLUMN name VARCHAR(256) NOT NULL;"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop column') {\n" +
                        "    alter 'table1' drop 'name'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "DECLARE @DefaultConstraintName nvarchar(200)\n" +
                                                    "        SELECT @DefaultConstraintName = Name FROM sys.default_constraints\n" +
                                                    "        WHERE PARENT_OBJECT_ID = OBJECT_ID('table1')\n" +
                                                    "        AND PARENT_COLUMN_ID = (SELECT column_id FROM sys.columns\n" +
                                                    "        WHERE NAME = N'name'\n" +
                                                    "        AND object_id = OBJECT_ID(N'table1'))\n" +
                                                    "        IF @DefaultConstraintName IS NOT NULL\n" +
                                                    "        EXEC('ALTER TABLE table1 DROP CONSTRAINT ' + @DefaultConstraintName)\n" +
                                                    "        WHILE 1=1\n" +
                                                    "        BEGIN\n" +
                                                    "        DECLARE @ConstraintName nvarchar(200)\n" +
                                                    "        SET @ConstraintName = (SELECT TOP 1 constraint_name FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE\n" +
                                                    "        WHERE TABLE_NAME='table1' AND COLUMN_NAME='name')\n" +
                                                    "        IF @ConstraintName IS NULL BREAK\n" +
                                                    "        EXEC('ALTER TABLE table1 DROP CONSTRAINT ' + @ConstraintName)\n" +
                                                    "        END\n" +
                                                    "        ALTER TABLE table1 DROP COLUMN name"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "ALTER TABLE table1 DROP COLUMN name"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "ALTER TABLE table1 DROP COLUMN name"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop primary key') {\n" +
                        "    alter 'table1' drop_primary 'id'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "ALTER TABLE table1 DROP CONSTRAINT pk_table1_id"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT pk_table1_id"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT pk_table1_id"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop unique key') {\n" +
                        "    alter 'table1' drop_unique 'example'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "ALTER TABLE table1 DROP CONSTRAINT uq_table1_example"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT uq_table1_example"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT uq_table1_example"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('add foreign key') {\n" +
                        "    reference 'id' of 'table2' from 'id' of 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "ALTER TABLE table1 ADD CONSTRAINT fk_table12table2 FOREIGN KEY ( id ) REFERENCES table2 ( id )"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "ALTER TABLE table1 ADD CONSTRAINT fk_table12table2 FOREIGN KEY ( id ) REFERENCES table2 ( id )"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "ALTER TABLE table1 ADD CONSTRAINT fk_table12table2 FOREIGN KEY ( id ) REFERENCES table2 ( id )"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop foreign key') {\n" +
                        "    unreference 'table2' from 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "ALTER TABLE table1 DROP CONSTRAINT fk_table12table2"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT fk_table12table2"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "ALTER TABLE table1 DROP CONSTRAINT fk_table12table2"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('table comment') {\n" +
                        "    comment 'good comment' on 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "\nDECLARE @schema sysname\n" +
                                                    "SELECT @schema = SCHEMA_NAME()\n" +
                                                    "Exec sp_addextendedproperty \"MS_Description\", \"good comment\", \"SCHEMA\", @schema, \"TABLE\", \"table1\"\n"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "COMMENT ON TABLE table1 IS ''good comment''"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "COMMENT ON TABLE table1 IS ''good comment''"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('column comment') {\n" +
                        "    comment 'good comment' on 'name' of 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "\nDECLARE @schema sysname\n" +
                                                    "SELECT @schema = SCHEMA_NAME()\n" +
                                                    "Exec sp_addextendedproperty \"MS_Description\", \"good comment\", \"SCHEMA\", @schema, \"TABLE\", \"table1\", \"COLUMN\", \"name\"\n"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "COMMENT ON COLUMN table1.name IS ''good comment''"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_COLUMN_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "COMMENT ON COLUMN table1.name IS ''good comment''"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('create index') {\n" +
                        "    index 'a' of 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "CREATE INDEX ix_a ON table1 (a)"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "CREATE INDEX ix_a ON table1 (a)"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "CREATE INDEX ix_a ON table1 (a)"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop index') {\n" +
                        "    unindex 'ix_a' of 'table1'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.MS_SQL),
                                                    "NOT ",
                                                    "DROP INDEX ix_a ON table1"
                                            )
                                    );
                                    put(
                                            "oracle",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.ORACLE),
                                                    "1",
                                                    "DROP INDEX ix_a"
                                            )
                                    );
                                    put(
                                            "postgres",
                                            String.format(
                                                    TABLE_WRAPPER.get(TestDatabaseTypes.POSTGRES),
                                                    "1",
                                                    "DROP INDEX ix_a"
                                            )
                                    );
                                }}
                        )
                },
                new Object[] {
                        "safe('drop view') {\n" +
                        "    drop_view 'view_name'\n" +
                        "}",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nIF OBJECT_ID(N'view_name', N'V') IS NOT NULL\n" +
                                            "  DROP VIEW view_name\n"
                                    );
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "  v INT;\n" +
                                            "\n" +
                                            "  BEGIN\n" +
                                            "    SELECT COUNT(*) INTO v FROM user_views WHERE view_name = UPPER('view_name');\n" +
                                            "\n" +
                                            "    IF v = 1 THEN\n" +
                                            "      EXECUTE IMMEDIATE 'DROP VIEW view_name';\n" +
                                            "    END IF;\n" +
                                            "  END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "\nDO $$\n" +
                                            "DECLARE\n" +
                                            "  v INT;\n" +
                                            "\n" +
                                            "  BEGIN\n" +
                                            "    SELECT COUNT(*) INTO v FROM pg_class WHERE relname = LOWER('view_name') AND relkind = 'v';\n" +
                                            "    IF v = 1 THEN\n" +
                                            "      EXECUTE 'DROP VIEW view_name';\n" +
                                            "    END IF;\n" +
                                            "  END$$;\n"
                                    );
                                }}
                        )
                },
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "safeStatements")
    public void compareSafeStatements(String ddl, SQLStatement expected) {
        final UpgradeDefinitionModel upgrade = SemanticTestUtil.createUpgradeModel(ddl);

        SemanticTestUtil.compareModelToSQL(upgrade, expected);
    }
}
