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

public class AlterSemanticsTest {
    @SuppressWarnings("serial")
    @DataProvider
    public Object[][] alterStatements() {
        return new Object[][] {
                new Object[] {
                        "alter 't' add 'a' storing BOOL",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a TINYINT DEFAULT 0 NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(1,0) DEFAULT 0 NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a BOOLEAN DEFAULT FALSE NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a TINYINT DEFAULT 0 NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(1,0) DEFAULT 0 NULL");
                                    put("postgres", "ALTER TABLE t ADD a BOOLEAN DEFAULT FALSE NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL allowing null initial_value postgres: 'true', default: '1'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a TINYINT DEFAULT 0 NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''1''');\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a NUMBER(1,0) DEFAULT 0 NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = '1']';\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a BOOLEAN DEFAULT FALSE NULL;\n" +
                                            "UPDATE t SET a = 'true';\n" +
                                            ";\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL default_sql '1'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a TINYINT DEFAULT 1 NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(1,0) DEFAULT 1 NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a BOOLEAN DEFAULT 1 NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing DATE",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a DATETIME DEFAULT GETDATE() NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a TIMESTAMP (6) DEFAULT NOW() NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing DATE default_sql ms_sql: 'GETDATE()', oracle: 'SYSTIMESTAMP', postgres: 'NOW()'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a DATETIME DEFAULT GETDATE() NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a TIMESTAMP (6) DEFAULT NOW() NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing NVARCHAR(16)",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a NVARCHAR(16) NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NVARCHAR2(16) NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing NVARCHAR(16) allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a NVARCHAR(16) NULL");
                                    put("oracle", "ALTER TABLE t ADD a NVARCHAR2(16) NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16)",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a VARCHAR(16) NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a VARCHAR2(16) NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16) allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a VARCHAR(16) NULL");
                                    put("oracle", "ALTER TABLE t ADD a VARCHAR2(16) NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16) initial_value 'foobar'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''foobar''');\n" +
                                            "EXEC('ALTER TABLE t ALTER COLUMN a VARCHAR(16) NOT NULL');\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a VARCHAR2(16) NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = 'foobar']';\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t MODIFY a VARCHAR2(16) NOT NULL]';\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "UPDATE t SET a = 'foobar';\n" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL;\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16) initial_value ms_sql: 'foo', default: 'bar'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''foo''');\n" +
                                            "EXEC('ALTER TABLE t ALTER COLUMN a VARCHAR(16) NOT NULL');\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a VARCHAR2(16) NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = 'bar']';\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t MODIFY a VARCHAR2(16) NOT NULL]';\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "UPDATE t SET a = 'bar';\n" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL;\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16) allowing null initial_value 'foobar'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''foobar''');\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a VARCHAR2(16) NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = 'foobar']';\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "UPDATE t SET a = 'foobar';\n" +
                                            ";\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing VARCHAR(16) initial_value 'foobar' allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''foobar''');\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a VARCHAR2(16) NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = 'foobar']';\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a VARCHAR(16) NULL;\n" +
                                            "UPDATE t SET a = 'foobar';\n" +
                                            ";\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER initial_value 1701",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a INT NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''1701''');\n" +
                                            "EXEC('ALTER TABLE t ALTER COLUMN a INT NOT NULL');\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a NUMBER(10,0) NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = '1701']';\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t MODIFY a NUMBER(10,0) NOT NULL]';\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a INT NULL;\n" +
                                            "UPDATE t SET a = '1701';\n" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL;\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER initial_value 1 default_value 2",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a INT DEFAULT '2' NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''1''');\n" +
                                            "EXEC('ALTER TABLE t ALTER COLUMN a INT NOT NULL');\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a NUMBER(10,0) DEFAULT '2' NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = '1']';\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t MODIFY a NUMBER(10,0) DEFAULT '2' NOT NULL]';\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a INT DEFAULT '2' NULL;\n" +
                                            "UPDATE t SET a = '1';\n" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL;\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER initial_value 1 default_value 2 allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "ALTER TABLE t ADD a INT DEFAULT '2' NULL;\n" +
                                            "EXEC('UPDATE t SET a = ''1''');\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "oracle",
                                            "\nBEGIN\n" +
                                            "EXECUTE IMMEDIATE q'[ALTER TABLE t ADD a NUMBER(10,0) DEFAULT '2' NULL]';\n" +
                                            "EXECUTE IMMEDIATE q'[UPDATE t SET a = '1']';\n" +
                                            "\n" +
                                            "END;\n");
                                    put(
                                            "postgres",
                                            "\nBEGIN;\n" +
                                            "ALTER TABLE t ADD a INT DEFAULT '2' NULL;\n" +
                                            "UPDATE t SET a = '1';\n" +
                                            ";\n" +
                                            "END;\n");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing BOOL default_value default: '1', postgres: 'true'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a TINYINT DEFAULT '1' NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(1,0) DEFAULT '1' NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a BOOLEAN DEFAULT 'true' NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing NVARCHAR(16) default_value 'foo'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a NVARCHAR(16) DEFAULT 'foo' NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NVARCHAR2(16) DEFAULT 'foo' NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) DEFAULT 'foo' NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER default_value 1701",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a INT DEFAULT '1701' NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(10,0) DEFAULT '1701' NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a INT DEFAULT '1701' NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing NVARCHAR(16) allowing null default_value 'foo'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a NVARCHAR(16) DEFAULT 'foo' NULL");
                                    put("oracle", "ALTER TABLE t ADD a NVARCHAR2(16) DEFAULT 'foo' NULL");
                                    put("postgres", "ALTER TABLE t ADD a VARCHAR(16) DEFAULT 'foo' NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER allowing null default_value 1701",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a INT DEFAULT '1701' NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(10,0) DEFAULT '1701' NULL");
                                    put("postgres", "ALTER TABLE t ADD a INT DEFAULT '1701' NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing INTEGER default_value ms_sql: 1, oracle: 2, postgres: 3",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ADD a INT DEFAULT '1' NOT NULL");
                                    put("oracle", "ALTER TABLE t ADD a NUMBER(10,0) DEFAULT '2' NOT NULL");
                                    put("postgres", "ALTER TABLE t ADD a INT DEFAULT '3' NOT NULL");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add 'a' storing 'char(1)'",
                        SQLStatementFactory.create("ALTER TABLE t ADD a char(1)")
                },
                new Object[] {
                        "alter 't' drop 'c'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "DECLARE @DefaultConstraintName nvarchar(200)\n" +
                                            "        SELECT @DefaultConstraintName = Name FROM sys.default_constraints\n" +
                                            "        WHERE PARENT_OBJECT_ID = OBJECT_ID('t')\n" +
                                            "        AND PARENT_COLUMN_ID = (SELECT column_id FROM sys.columns\n" +
                                            "        WHERE NAME = N'c'\n" +
                                            "        AND object_id = OBJECT_ID(N't'))\n" +
                                            "        IF @DefaultConstraintName IS NOT NULL\n" +
                                            "        EXEC('ALTER TABLE t DROP CONSTRAINT ' + @DefaultConstraintName)\n" +
                                            "        WHILE 1=1\n" +
                                            "        BEGIN\n" +
                                            "        DECLARE @ConstraintName nvarchar(200)\n" +
                                            "        SET @ConstraintName = (SELECT TOP 1 constraint_name FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE\n" +
                                            "        WHERE TABLE_NAME='t' AND COLUMN_NAME='c')\n" +
                                            "        IF @ConstraintName IS NULL BREAK\n" +
                                            "        EXEC('ALTER TABLE t DROP CONSTRAINT ' + @ConstraintName)\n" +
                                            "        END\n" +
                                            "        ALTER TABLE t DROP COLUMN c"
                                    );
                                    put("default", "ALTER TABLE t DROP COLUMN c");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' rename 'a' to 'b'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "EXEC sp_rename 't.[a]', 'b', 'COLUMN'");
                                    put("oracle", "ALTER TABLE t RENAME COLUMN a TO b");
                                    put("postgres", "ALTER TABLE t RENAME COLUMN a TO b");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' rename 'a' to 'b'\nalter 't' rename 'c' to 'd'",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "EXEC sp_rename 't.[a]', 'b', 'COLUMN';EXEC sp_rename 't.[c]', 'd', 'COLUMN'");
                                    put("oracle", "ALTER TABLE t RENAME COLUMN a TO b;ALTER TABLE t RENAME COLUMN c TO d");
                                    put("postgres", "ALTER TABLE t RENAME COLUMN a TO b;ALTER TABLE t RENAME COLUMN c TO d");
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to VARCHAR(128)",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ALTER COLUMN a VARCHAR(128) NOT NULL");
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  )';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  NOT NULL)';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE VARCHAR(128);" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to VARCHAR(128) allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ALTER COLUMN a VARCHAR(128) NULL");
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  NULL)';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  )';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE VARCHAR(128);" +
                                            "ALTER TABLE t ALTER COLUMN a DROP NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to INTEGER",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ALTER COLUMN a INT NOT NULL");
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(10,0)  )';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(10,0)  NOT NULL)';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE INT;" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to INTEGER allowing null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql", "ALTER TABLE t ALTER COLUMN a INT NULL");
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(10,0)  NULL)';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(10,0)  )';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE INT;" +
                                            "ALTER TABLE t ALTER COLUMN a DROP NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to BOOL",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put(
                                            "ms_sql",
                                            "\nBEGIN\n" +
                                            "DECLARE @DefaultConstraintName nvarchar(200)\n" +
                                            "        SELECT @DefaultConstraintName = Name FROM sys.default_constraints\n" +
                                            "        WHERE PARENT_OBJECT_ID = OBJECT_ID('t')\n" +
                                            "        AND PARENT_COLUMN_ID = (SELECT column_id FROM sys.columns\n" +
                                            "        WHERE NAME = N'a'\n" +
                                            "        AND object_id = OBJECT_ID(N't'))\n" +
                                            "        IF @DefaultConstraintName IS NOT NULL\n" +
                                            "        EXEC('ALTER TABLE t DROP CONSTRAINT ' + @DefaultConstraintName)\n" +
                                            "        WHILE 1=1\n" +
                                            "        BEGIN\n" +
                                            "        DECLARE @ConstraintName nvarchar(200)\n" +
                                            "        SET @ConstraintName = (SELECT TOP 1 constraint_name FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE\n" +
                                            "        WHERE TABLE_NAME='t' AND COLUMN_NAME='a')\n" +
                                            "        IF @ConstraintName IS NULL BREAK\n" +
                                            "        EXEC('ALTER TABLE t DROP CONSTRAINT ' + @ConstraintName)\n" +
                                            "        END\n" +
                                            "        ;\n" +
                                            "ALTER TABLE t ALTER COLUMN a TINYINT NOT NULL;\n" +
                                            "ALTER TABLE t ADD DEFAULT '0' FOR a;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(1,0) DEFAULT 0  )';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a NUMBER(1,0) DEFAULT 0  NOT NULL)';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE BOOLEAN DEFAULT FALSE;" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' retype 'a' to TEST_VARCHAR allowing null\n" +
                        "alter 't' retype 'a' to TEST_VARCHAR",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("ms_sql",
                                            "ALTER TABLE t ALTER COLUMN a VARCHAR(128) NULL;ALTER TABLE t ALTER COLUMN a VARCHAR(128) NOT NULL");
                                    put(
                                            "oracle",
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  NULL)';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  )';\n" +
                                            "  END IF;\n" +
                                            "END;\n" +
                                            ";" +
                                            "\nDECLARE\n" +
                                            "l_nullable VARCHAR(1);\n" +
                                            "\n" +
                                            "BEGIN\n" +
                                            "  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('t') AND column_name = UPPER('a');\n" +
                                            "\n" +
                                            "  IF l_nullable = 'N' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  )';\n" +
                                            "  END IF;\n" +
                                            "  IF l_nullable = 'Y' THEN\n" +
                                            "    EXECUTE IMMEDIATE 'ALTER TABLE t MODIFY (a VARCHAR2(128)  NOT NULL)';\n" +
                                            "  END IF;\n" +
                                            "END;\n"
                                    );
                                    put(
                                            "postgres",
                                            "ALTER TABLE t ALTER COLUMN a TYPE VARCHAR(128);" +
                                            "ALTER TABLE t ALTER COLUMN a DROP NOT NULL;" +
                                            "ALTER TABLE t ALTER COLUMN a TYPE VARCHAR(128);" +
                                            "ALTER TABLE t ALTER COLUMN a SET NOT NULL"
                                    );
                                }}
                        )
                },
                new Object[] {
                        "alter 't' add_primary 'a' and 'b' and 'c'",
                        SQLStatementFactory.create("ALTER TABLE t ADD CONSTRAINT pk_t_a_b_c PRIMARY KEY ( a, b, c )")
                },
                new Object[] {
                        "alter 't' add_unique 'a' and 'b' and 'c'",
                        SQLStatementFactory.create("ALTER TABLE t ADD CONSTRAINT uq_t_a_b_c UNIQUE ( a, b, c )")
                },
                new Object[] {
                        "alter 't' drop_primary 'a' and 'b' and 'c'",
                        SQLStatementFactory.create("ALTER TABLE t DROP CONSTRAINT pk_t_a_b_c")
                },
                new Object[] {
                        "alter 't' drop_unique 'a' and 'b' and 'c'",
                        SQLStatementFactory.create("ALTER TABLE t DROP CONSTRAINT uq_t_a_b_c")
                }

        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "alterStatements")
    public void compareAlterStatements(String ddl, SQLStatement expected) {
        final UpgradeDefinitionModel upgrade = SemanticTestUtil.createUpgradeModel(ddl);

        SemanticTestUtil.compareModelToSQL(upgrade, expected);
    }
}
