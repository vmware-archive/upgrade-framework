/* ****************************************************************************
 * Copyright (c) 2011-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.sql.task;

import java.sql.SQLException;
import java.sql.Statement;

import com.vmware.upgrade.PersistenceContext;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.logging.UpgradeLogger;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.sql.DatabasePersistenceContext;
import com.vmware.upgrade.sql.SQLStatement;
import com.vmware.upgrade.task.AbstractSimpleTask;

import org.apache.commons.lang3.StringUtils;

/**
 * Task that encapsulates a raw statement {@link String} or {@link SQLStatement} to be executed at
 * runtime against an {@link UpgradeContext} containing a {@link DatabasePersistenceContext}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public final class RawSQLTask extends AbstractSimpleTask {
    private final UpgradeLogger logger;

    private static final int MAX_PROGRESS = 1;

    private final DatabasePersistenceContext databaseContext;

    private final String sql;

    /**
     * Construct a task with the given name, raw sql statement and the database context
     *
     * @param name
     *            name for the task
     * @param context
     *            {@link UpgradeContext} containing the {@link PersistenceContext} for the
     *            database to communicate with
     * @param sqlStatement
     *            sql statement to execute
     */
    public RawSQLTask(String name, UpgradeContext context, String sqlStatement) {
        super(name, MAX_PROGRESS);

        this.logger = context.getLogger(getClass());
        this.databaseContext = context.getPersistenceContext(DatabasePersistenceContext.class);
        this.sql = sqlStatement;
    }

    /**
     * Construct a task with the given name, a raw SQL statement string based on the
     * database context, and the database context.
     *
     * @param name
     *            name for the task
     * @param upgradeContext
     *            {@link UpgradeContext} containing the {@link PersistenceContext} for the
     *            database to communicate with
     * @param statement
     *            sql statement to execute
     * @throws IllegalArgumentException
     *             if a sql statement is not provided for all supported databases
     */
    public RawSQLTask(String name, UpgradeContext upgradeContext, SQLStatement statement) {
        super(name, MAX_PROGRESS);

        this.logger = upgradeContext.getLogger(getClass());
        this.databaseContext = upgradeContext.getPersistenceContext(DatabasePersistenceContext.class);

        try {
            this.sql = statement.get(databaseContext.getDatabaseType());
        } catch (IllegalArgumentException iae) {
            logger.error("Error creating Raw SQL Task: ''{0}'' due to exception: {1}", name, iae);
            throw new IllegalArgumentException("Error creating Raw SQL Task: " + name, iae);
        }
    }

    /**
     * Executes the supplied SQL statement. No response is captured.
     *
     * @throws SQLException
     *             if an {@code SQLException} is encountered while executing the procedure
     * @throws IllegalStateException
     *             if the database is not connected
     */
    @Override
    public Void call() throws SQLException {
        setState(ExecutionState.RUNNING);

        if (!databaseContext.isConnected()) {
            setState(ExecutionState.FAILED);
            throw new IllegalStateException("Database not connected");
        }

        Statement stmt = null;
        try {
            if (!StringUtils.isEmpty(sql)) {
                stmt = databaseContext.getConnection().createStatement();

                logger.debug("Executing sql ''{0}''", sql);
                stmt.execute(sql);
            }

            incrementProgress();
            setState(ExecutionState.COMPLETED);
        } catch (SQLException sqle) {
            setState(ExecutionState.FAILED);
            throw sqle;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.trace(e, "Exception encountered when closing a statement.");
                }
            }
        }

        return null;
    }

    public String getSQL() {
        return sql;
    }

    @Override
    public String toString() {
        return "RawSQLTask [" + sql + "]";
    }

    /**
     * Computes hashCode based on the raw SQL to be executed and the task name.
     *
     * Ignores DatabaseContext.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((sql == null) ? 0 : sql.hashCode());
        return result;
    }

    /**
    * Computes equality based on the raw SQL to be executed and the task name.
    *
    * Ignores DatabaseContext.
    */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RawSQLTask)) {
            return false;
        }
        RawSQLTask other = (RawSQLTask) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (sql == null) {
            if (other.sql != null) {
                return false;
            }
        } else if (!sql.equals(other.sql)) {
            return false;
        }
        return true;
    }
}
