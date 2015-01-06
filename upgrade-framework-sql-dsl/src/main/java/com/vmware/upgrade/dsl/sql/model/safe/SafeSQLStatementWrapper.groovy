/* ****************************************************************************
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
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

import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.sql.SQLStatement

/**
 * Provides wrapping a raw SQL string with SQL entity existence checks.
 *
 * @author Matthew Frost <mfrost@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class SafeSQLStatementWrapper {
    public static final SQLStatement TABLE_EXISTS = SQLStatementFactory.create(
        [
            ms_sql: """
IF OBJECT_ID(N'%s', N'U') IS NOT NULL
  BEGIN
%s
  END
""",
            oracle: """
DECLARE
  t INT;

  BEGIN
    SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = UPPER('%s');

    IF t = 1 THEN
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
    IF t = 1 THEN
      EXECUTE '%s';
    END IF;
  END\$\$;
"""
        ])

    public static final SQLStatement TABLE_AND_COLUMN_EXISTS = SQLStatementFactory.create(
        [
            ms_sql: """
IF EXISTS (SELECT * FROM sys.columns WHERE OBJECT_ID = OBJECT_ID(N'%s', N'U') AND Name = '%s')
  BEGIN
%s
  END
""",
            oracle: """
DECLARE
  t INT;
  c INT;

  BEGIN
    SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = UPPER('%s');
    SELECT COUNT(*) INTO c FROM user_tab_cols WHERE table_name = UPPER('%1\$s') AND column_name = UPPER('%s');

    IF t = 1 AND c = 1 THEN
      EXECUTE IMMEDIATE '%s';
    END IF;
  END;
""",
            postgres:"""
DO \$\$
DECLARE
  t INT;

  BEGIN
    SELECT COUNT(*) INTO t FROM pg_class tables
       JOIN pg_attribute columns ON columns.attrelid = tables.oid
       WHERE tables.relname = LOWER('%s') AND tables.relkind = 'r' AND columns.attname = LOWER('%s');
    IF t = 1 THEN
      EXECUTE '%s';
    END IF;
  END\$\$;
"""
        ]
    )
}
