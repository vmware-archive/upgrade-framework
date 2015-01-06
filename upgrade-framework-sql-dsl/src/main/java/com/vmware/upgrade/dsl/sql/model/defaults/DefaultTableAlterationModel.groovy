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

package com.vmware.upgrade.dsl.sql.model.defaults

import com.vmware.upgrade.dsl.sql.model.ConstraintModel
import com.vmware.upgrade.dsl.sql.model.TableAlterationModel
import com.vmware.upgrade.dsl.sql.syntax.ColumnType
import com.vmware.upgrade.dsl.sql.syntax.DataType
import com.vmware.upgrade.dsl.sql.util.NullAware
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.sql.util.ValidationUtil
import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement

/**
 * {@code DefaultTableAlterationModel} is the core implementation of {@link TableAlterationModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of table or column before execution.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class DefaultTableAlterationModel implements TableAlterationModel {
    private static final String DROP_CONSTRAINT_SQL = "ALTER TABLE %s DROP COLUMN %s"

    /*In MS_SQL all constraints must be removed before dropping a column.*/
    private static final String MS_DROP_CONSTRAINT_SQL = """DECLARE @DefaultConstraintName nvarchar(200)
        SELECT @DefaultConstraintName = Name FROM sys.default_constraints
        WHERE PARENT_OBJECT_ID = OBJECT_ID('%1\$s')
        AND PARENT_COLUMN_ID = (SELECT column_id FROM sys.columns
        WHERE NAME = N'%2\$s'
        AND object_id = OBJECT_ID(N'%1\$s'))
        IF @DefaultConstraintName IS NOT NULL
        EXEC('ALTER TABLE %1\$s DROP CONSTRAINT ' + @DefaultConstraintName)
        WHILE 1=1
        BEGIN
        DECLARE @ConstraintName nvarchar(200)
        SET @ConstraintName = (SELECT TOP 1 constraint_name FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE
        WHERE TABLE_NAME='%1\$s' AND COLUMN_NAME='%2\$s')
        IF @ConstraintName IS NULL BREAK
        EXEC('ALTER TABLE %1\$s DROP CONSTRAINT ' + @ConstraintName)
        END
        """

    public static enum AlterationType {
        ADD_COLUMN(
            SQLStatementFactory.create("ALTER TABLE %s ADD %s %s")
        ),
        DROP_COLUMN(
            SQLStatementFactory.create(
                [
                    ms_sql: DefaultTableAlterationModel.MS_DROP_CONSTRAINT_SQL +
                            DefaultTableAlterationModel.DROP_CONSTRAINT_SQL,
                    default: DefaultTableAlterationModel.DROP_CONSTRAINT_SQL
                ]
            )
        ),
        RENAME_COLUMN(
            SQLStatementFactory.create(
                [
                    ms_sql: "EXEC sp_rename '%s.[%s]', '%s', 'COLUMN'",
                    default: "ALTER TABLE %s RENAME COLUMN %s TO %s"
                ]
            )
        ),
        RETYPE_COLUMN(
            SQLStatementFactory.create(
                [
                    ms_sql: "ALTER TABLE %s ALTER COLUMN %s %s",
                    oracle: """
DECLARE
l_nullable VARCHAR(1);

BEGIN
  SELECT nullable INTO l_nullable FROM user_tab_columns WHERE table_name = UPPER('%1\$s') AND column_name = UPPER('%2\$s');

  IF l_nullable = 'N' THEN
    EXECUTE IMMEDIATE 'ALTER TABLE %1\$s MODIFY (%2\$s %3\$s %4\$s)';
  END IF;
  IF l_nullable = 'Y' THEN
    EXECUTE IMMEDIATE 'ALTER TABLE %1\$s MODIFY (%2\$s %3\$s %5\$s)';
  END IF;
END;
""",
/*Limit the retype to a column without any reference and data for vPostgres now.*/
                    postgres: "ALTER TABLE %1\$s DROP COLUMN %2\$s; ALTER TABLE %1\$s ADD COLUMN %2\$s %3\$s;"
                ]
            )
        ),
        ADD_OR_DROP_CONSTRAINT(
            SQLStatementFactory.create("ALTER TABLE %s %s")
        )

        private SQLStatement sql

        private AlterationType(sql) {
            this.sql = sql
        }

        public String getSql(DatabaseType databaseType) {
            sql.get(databaseType)
        }
    }

    private def columnType

    private def newColumnName

    protected AlterationType alterationType

    protected def columnName

    protected ConstraintModel constraintModel

    protected def tableName

    public DefaultTableAlterationModel(tableName) {
        this.tableName = tableName
    }

    @Override
    public void addColumn(name, type) {
        ValidationUtil.validateNotReserved(name)
        this.alterationType = AlterationType.ADD_COLUMN
        this.columnName = name
        this.columnType = type
    }

    @Override
    public void dropColumn(name) {
        this.alterationType = AlterationType.DROP_COLUMN
        this.columnName = name
    }

    @Override
    public void renameColumn(name, newName) {
        ValidationUtil.validateNotReserved(newName)
        this.alterationType = AlterationType.RENAME_COLUMN
        this.columnName = name
        this.newColumnName = newName
    }

    @Override
    public void retypeColumn(name, type) {
        this.alterationType = AlterationType.RETYPE_COLUMN
        this.columnName = name
        this.columnType = type
    }

    @Override
    public void setConstraint(ConstraintModel constraint) {
        this.alterationType = AlterationType.ADD_OR_DROP_CONSTRAINT
        this.constraintModel = constraint
    }

    @Override
    public String get(DatabaseType databaseType) {
        switch (alterationType) {
            case AlterationType.ADD_COLUMN:
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName, columnType)
            case AlterationType.DROP_COLUMN:
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName)
            case AlterationType.RENAME_COLUMN:
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName, newColumnName)
            case AlterationType.RETYPE_COLUMN:
                /*
                 *  In oracle, if the column having its type changed is already marked as "not null",
                 *  then it will complain if the ALTER TABLE MODIFY also contains "not null" saying
                 *  that it is already the case. This logic handles the appropriate placement of
                 *  "not null".
                 */
                if (databaseType.toString().toUpperCase().equals("ORACLE")) {
                    String simpleColumnType, existingColumnIsNullable, existingColumnIsNotNullable
                    String evaluatedColumnType = SQLStatementFactory.create(columnType).get(databaseType)

                    boolean nullable = (columnType in NullAware) ? columnType.getAllowNulls() : !evaluatedColumnType.contains("NOT NULL")
                    if (nullable) {
                        simpleColumnType = evaluatedColumnType.replace("NULL", "")
                        existingColumnIsNullable = "NULL"
                        existingColumnIsNotNullable = ""
                    } else {
                        simpleColumnType = evaluatedColumnType.replace("NOT NULL", "")
                        existingColumnIsNullable = ""
                        existingColumnIsNotNullable = "NOT NULL"
                    }

                    return SQLStatementFactory.format(
                        alterationType.getSql(databaseType),
                        databaseType,
                        tableName,
                        columnName,
                        simpleColumnType,
                        existingColumnIsNullable,
                        existingColumnIsNotNullable
                    )
                } else {
                    return SQLStatementFactory.format(
                        alterationType.getSql(databaseType),
                        databaseType,
                        tableName,
                        columnName,
                        columnType)
                }
            case AlterationType.ADD_OR_DROP_CONSTRAINT:
                tableName = SQLStatementFactory.create(tableName).get(databaseType)

                return SQLStatementFactory.format(
                    alterationType.getSql(databaseType),
                    databaseType,
                    tableName,
                    constraintModel.getAlterInline(tableName))
            default:
                throw new IllegalStateException("Unsupported alteration type")
        }
    }
}
