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

package com.vmware.upgrade.dsl.sql.model.safe

import com.vmware.upgrade.dsl.sql.model.defaults.DefaultTableCreationModel
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.sql.DatabaseType
import com.vmware.upgrade.sql.SQLStatement

/**
 * {@link SafeTableCreationModel} extends the logic of {@link DefaultTableCreationModel} by
 * wrapping the raw SQL string returned by {@link DefaultTableCreationModel#get(DatabaseType)}
 * with an SQL entity existence check.
 * <p>
 * A check is performed to ensure the table being created doesn't already exist.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class SafeTableCreationModel extends DefaultTableCreationModel {
    private static final SQLStatement TABLE_NOT_EXISTS_WRAPPER = SQLStatementFactory.create(
        [
            ms_sql: """
IF OBJECT_ID(N'%s', N'U') IS NULL
  BEGIN
%s
  END
""",
            oracle: """
DECLARE
  t INT;

  BEGIN
    SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = UPPER('%s');

    IF t = 0 THEN
      EXECUTE IMMEDIATE '%s';
    END IF;
  END;
""",
            postgres:"""
DO \$\$
DECLARE
  t INT;

  BEGIN
    SELECT COUNT(*) INTO t FROM pg_class WHERE relname = LOWER('%s') AND relkind = 'r';
    IF t = 0 THEN
      EXECUTE '%s';
    END IF;
  END\$\$;
"""
        ])

    public SafeTableCreationModel(tableName) {
        super(tableName)
    }

    @Override
    public String get(final DatabaseType databaseType) {
        return SQLStatementFactory.format(TABLE_NOT_EXISTS_WRAPPER.get(databaseType), databaseType, super.tableName, super.get(databaseType));
    }
}
