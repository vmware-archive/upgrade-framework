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

package com.vmware.upgrade.sql;

import java.io.IOException;
import java.sql.Connection;

import com.vmware.upgrade.PersistenceContext;
import com.vmware.upgrade.sql.script.SQLParsedDataAggregator;

/**
 * A {@link PersistenceContext} suitable for interacting with a relational database via JDBC.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public interface DatabasePersistenceContext extends PersistenceContext {
    /**
     * Retrieves the raw JDBC {@link Connection}.
     *
     * @return The {@link Connection} to the database or {@code null} if the context is not
     *          {@linkplain #isConnected() connected}.
     *          <p>
     *          The returned connection is not guaranteed to be connected.
     */
    public Connection getConnection();

    /**
     * Retrieves the {@link DatabaseType} that describes this context.
     *
     * @return The {@link DatabaseType} instance describing this context.
     *         This method never returns {@code null}.
     */
    public DatabaseType getDatabaseType();

    /**
     * Parses the provided script using the provided aggregator.
     *
     * @param script
     *            the script to parse
     * @param aggregator
     *            the aggregator to add results to
     * @return the results of calling {@code getParsedData} on the supplied aggregator
     * @throws IOException
     *             if there is an error reading the file
     */
    public <T> T parseWithAggregator(String script, SQLParsedDataAggregator<T> aggregator) throws IOException;

    /**
     * Returns true if the context has been connected, has not been disconnected, and no fatal
     * errors have occurred.
     *
     * @see Connection#isClosed()
     *
     * @return Returns {@code false} if the connection has never been connected, has been
     *          disconnected, or has been disconnected as a result of a fatal error. Returns
     *          {@code true} otherwise.
     */
    @Override
    public boolean isConnected();
}
