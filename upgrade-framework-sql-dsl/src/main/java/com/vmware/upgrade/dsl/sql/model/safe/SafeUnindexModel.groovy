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

import com.vmware.upgrade.dsl.sql.model.defaults.DefaultUnindexModel
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory
import com.vmware.upgrade.sql.DatabaseType

/**
 * {@link SafeUnindexModel} extends the logic of {@link DefaultUnindexModel} by
 * wrapping the raw SQL string returned by {@link DefaultUnindexModel#get(DatabaseType)}
 * with a SQL entity existence check.
 * <p>
 * A check is performed to ensure the table exists before execution.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
class SafeUnindexModel extends DefaultUnindexModel {
    @Override
    public String get(final DatabaseType databaseType) {
        return SQLStatementFactory.format(
            SafeSQLStatementWrapper.TABLE_EXISTS.get(databaseType),
            databaseType,
            super.getTable(databaseType),
            super.get(databaseType))
    }
}
