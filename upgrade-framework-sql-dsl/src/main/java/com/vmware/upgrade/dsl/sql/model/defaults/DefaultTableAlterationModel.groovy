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

package com.vmware.upgrade.dsl.sql.model.defaults

import com.vmware.upgrade.dsl.sql.model.ConstraintModel
import com.vmware.upgrade.dsl.sql.model.TableAlterationModel
import com.vmware.upgrade.dsl.sql.util.InitialAware
import com.vmware.upgrade.dsl.sql.util.NullAware
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.dsl.sql.util.ValidationUtil
import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement
import com.vmware.upgrade.transformation.Transformation

/**
 * {@code DefaultTableAlterationModel} is the core implementation of {@link TableAlterationModel}.
 * <p>
 * The raw SQL returned by {@link #get(DatabaseType)} will not check for the
 * existence of table or column before execution.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
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
                    postgres: "ALTER TABLE %1\$s ALTER COLUMN %2\$s TYPE %3\$s;" +
                              "ALTER TABLE %1\$s ALTER COLUMN %2\$s %4\$s NOT NULL"
                ]
            )
        ),
        ADD_OR_DROP_CONSTRAINT(
            SQLStatementFactory.create("ALTER TABLE %s %s")
        ),
        ADD_COLUMN_WITH_INITIAL(
            SQLStatementFactory.create("""
BEGIN
ALTER TABLE %1\$s ADD %2\$s %3\$s
UPDATE %1\$s SET %2\$s = %4\$s
%5\$s
END
""")
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
        ValidationUtil.validateEntityName(name)

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
        ValidationUtil.validateEntityName(newName)

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

    private String getAddColumnWithInitial(DatabaseType databaseType) {
        boolean isNullable = (columnType in NullAware) ? columnType.isNullable() : true
        Object nullableType = (isNullable) ? columnType : columnType.makeNullableCopy()
        String set = (databaseType.toString().equalsIgnoreCase("POSTGRES")) ? "SET" : ""
        String setNullability =
            (isNullable) ? "" : "ALTER TABLE ${tableName} ALTER COLUMN ${columnName} ${set} NOT NULL"
        Object initialValue = columnType.getInitialValue()

        return SQLStatementFactory.format(AlterationType.ADD_COLUMN_WITH_INITIAL.getSql(databaseType),
            databaseType, tableName, columnName, nullableType, initialValue, setNullability)
    }

    @Override
    public String get(DatabaseType databaseType) {
        switch (alterationType) {
            case AlterationType.ADD_COLUMN:
                if (columnType in InitialAware && columnType.getInitialValue() != null) {
                    return getAddColumnWithInitial(databaseType)
                }
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName, columnType)
            case AlterationType.DROP_COLUMN:
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName)
            case AlterationType.RENAME_COLUMN:
                return SQLStatementFactory.format(alterationType.getSql(databaseType), databaseType, tableName, columnName, newColumnName)
            case AlterationType.RETYPE_COLUMN:
                String simpleColumnType
                String evaluatedColumnType = SQLStatementFactory.create(columnType).get(databaseType)
                boolean nullable = (columnType in NullAware) ? columnType.isNullable() : !evaluatedColumnType.contains("NOT NULL")

                /*
                 *  In oracle, if the column having its type changed is already marked as "not null",
                 *  then it will complain if the ALTER TABLE MODIFY also contains "not null" saying
                 *  that it is already the case. This logic handles the appropriate placement of
                 *  "not null".
                 */
                if (databaseType.toString().equalsIgnoreCase("ORACLE")) {
                    String existingColumnIsNullable, existingColumnIsNotNullable

                    if (nullable) {
                        simpleColumnType = evaluatedColumnType.replace("NULL", "")
                        existingColumnIsNotNullable = "NULL"
                        existingColumnIsNullable = ""
                    } else {
                        simpleColumnType = evaluatedColumnType.replace("NOT NULL", "")
                        existingColumnIsNotNullable = ""
                        existingColumnIsNullable = "NOT NULL"
                    }

                    return SQLStatementFactory.format(
                        alterationType.getSql(databaseType),
                        databaseType,
                        tableName,
                        columnName,
                        simpleColumnType,
                        existingColumnIsNotNullable,
                        existingColumnIsNullable
                    )
                /*
                 * In Postgres the nullability isn't included in the type for the alter statement.
                 * Dropping/setting NOT NULL is used in a subsequent ALTER statement instead.
                 */
                } else if (databaseType.toString().equalsIgnoreCase("POSTGRES")) {
                    simpleColumnType = evaluatedColumnType.replaceAll("(?i)\\s?(NOT\\s)?NULL", "")
                    String nullability = (nullable) ? "DROP" : "SET"

                    return SQLStatementFactory.format(
                        alterationType.getSql(databaseType),
                        databaseType,
                        tableName,
                        columnName,
                        simpleColumnType,
                        nullability)
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

    @Override
    public Transformation getTransformation() {
        Transformation.TransformationType transformationAlterationType
        switch (alterationType) {
        case AlterationType.ADD_COLUMN:
            transformationAlterationType = (columnType in NullAware && columnType.isNullable())
                ? Transformation.TransformationType.ADD_COLUMN_NULL
                : Transformation.TransformationType.ADD_COLUMN_NOT_NULL
            break
        case AlterationType.DROP_COLUMN:
            transformationAlterationType = Transformation.TransformationType.DROP_COLUMN
            break
        case AlterationType.RENAME_COLUMN:
            transformationAlterationType = Transformation.TransformationType.RENAME_COLUMN
            break
        case AlterationType.RETYPE_COLUMN:
            transformationAlterationType = Transformation.TransformationType.RETYPE_COLUMN
            break
        case AlterationType.ADD_OR_DROP_CONSTRAINT:
            if (constraintModel.type == ConstraintModel.ConstraintType.ADD_PRIMARY_KEY ||
                constraintModel.type == ConstraintModel.ConstraintType.ADD_UNIQUE_KEY) {
                transformationAlterationType = Transformation.TransformationType.ADD_CONSTRAINT
            } else {
                transformationAlterationType = Transformation.TransformationType.DROP_CONSTRAINT
            }
            break
        }

        return new Transformation(tableName, columnName, transformationAlterationType)
    }
}
