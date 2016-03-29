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

package com.vmware.upgrade.dsl.sql.model

import com.vmware.upgrade.sql.SQLStatement;

/**
 * {@link SQLStatementProxy} allows an {@link SQLStatement} model's various
 * implementations to be instantiated appropriately.
 *
 * @see AbstractSQLStatementProxy
 * @see DefaultSQLStatementProxy
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface SQLStatementProxy {
    /**
     * Returns an implementation of {@link TableCreationModel}.
     *
     * @param table table name
     * @return a new {@link TableCreationModel}
     */
    public TableCreationModel create(table)

    /**
     * Returns an implementation of {@link TableAlterationModel}.
     *
     * @param table table name
     * @return a new {@link TableAlterationModel}
     */
    public TableAlterationModel alter(table)

    /**
     * Returns an implementation of {@link ReferenceModel}.
     *
     * @return a new {@link ReferenceModel}
     */
    public ReferenceModel reference()

    /**
     * Returns an implementation of {@link UnreferenceModel}.
     *
     * @return a new {@link UnreferenceModel}
     */
    public UnreferenceModel unreference()

    /**
     * Returns an implementation of {@link IndexModel}.
     *
     * @return a new {@link IndexModel}
     */
    public IndexModel index()

    /**
     * Returns an implementation of {@link UnindexModel}.
     *
     * @return a new {@link UnindexModel}
     */
    public UnindexModel unindex()

    /**
     * Returns an implementation of {@link CommentModel}.
     *
     * @return a new {@link CommentModel}
     */
    public CommentModel comment()

    /**
     * Returns an implementation of {@link SQLStatement}.
     * <p>
     * The drop view keyword implementation is simple enough that its model
     * would contain zero methods. As such, any implementation will just
     * implement {@link SQLStatement} directly.
     *
     * @param view view name
     * @return a new {@link SQLStatement}
     */
    public SQLStatement dropView(view)
}
