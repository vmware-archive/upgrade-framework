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

import com.vmware.upgrade.dsl.sql.model.CommentModel
import com.vmware.upgrade.dsl.sql.model.IndexModel
import com.vmware.upgrade.dsl.sql.model.ReferenceModel
import com.vmware.upgrade.dsl.sql.model.SQLStatementProxy
import com.vmware.upgrade.dsl.sql.model.TableAlterationModel
import com.vmware.upgrade.dsl.sql.model.TableCreationModel
import com.vmware.upgrade.dsl.sql.model.UnindexModel
import com.vmware.upgrade.dsl.sql.model.UnreferenceModel
import com.vmware.upgrade.sql.SQLStatement

/**
 * {@code DefaultSQLStatementProxy} provides methods for instantiating
 * {@link SQLStatement} models such that when their raw SQL is retrieved, no
 * object existence checks are performed.
 * <p>
 * For example, if a table is trying to be created and one already exists with
 * the specified name, the operation will fail with an {@code SQLException}.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class DefaultSQLStatementProxy implements SQLStatementProxy {
    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultTableCreationModel}
     */
    @Override
    public TableCreationModel create(table) {
        return new DefaultTableCreationModel(table)
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultTableAlterationModel}
     */
    @Override
    public TableAlterationModel alter(table) {
        return new DefaultTableAlterationModel(table)
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultReferenceModel}
     */
    @Override
    public ReferenceModel reference() {
        return new DefaultReferenceModel()
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultUnreferenceModel}
     */
    @Override
    public UnreferenceModel unreference() {
        return new DefaultUnreferenceModel()
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultIndexModel}
     */
    @Override
    public IndexModel index() {
        return new DefaultIndexModel()
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultUnindexModel}
     */
    @Override
    public UnindexModel unindex() {
        return new DefaultUnindexModel()
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultCommentModel}
     */
    @Override
    public CommentModel comment() {
        return new DefaultCommentModel()
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@link DefaultDropViewModel}
     */
    @Override
    public SQLStatement dropView(view) {
        return new DefaultDropViewModel(view)
    }
}
