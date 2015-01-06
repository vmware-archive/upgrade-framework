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

import java.sql.Connection;
import java.sql.SQLException;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.logging.UpgradeLogger;
import com.vmware.upgrade.sql.DatabasePersistenceContext;
import com.vmware.upgrade.task.AbstractDelegatingTask;

/**
 * Task that runs the provided task within a database transaction.
 *
 * @author Ankit Shah <ankitsha@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public final class TransactionTask extends AbstractDelegatingTask {
    private final UpgradeLogger logger;
    private static final int TICKS = 2;

    private final DatabasePersistenceContext databaseContext;

    /**
     * Constructor
     *
     * @param name
     *            name of the task
     * @param task
     *            {@link Task} that must be run within a database transaction
     * @param context
     *            {@link UpgradeContext} containing the {@link DatabasePersistenceContext} for
     *            the database to communicate with.
     */
    public TransactionTask(String name, Task task, UpgradeContext context) {
        super(name, task, TICKS);

        this.logger = context.getLogger(getClass());
        this.databaseContext = context.getPersistenceContext(DatabasePersistenceContext.class);
    }

    /**
     * Executes the delegate task and then commits the transaction if the task completes
     * successfully (does not thrown an exception) or rolls back if the task failed to complete.
     *
     * @throws SQLException
     *             if there is a sql exception when committing or rolling back the exception or if
     *             the delegate task throws a SQL exception
     * @throws IllegalStateException
     *             if the database context is not connected
     * @throws Exception
     *             passes along any exception that may be thrown by the delegate task
     */
    @Override
    public void doCall() throws Exception {
        if (!databaseContext.isConnected()) {
            throw new IllegalStateException("Database context not connected");
        }

        final Connection connection = databaseContext.getConnection();

        final boolean autoCommitStatus;

        // transaction preparation
        autoCommitStatus = connection.getAutoCommit();
        connection.setAutoCommit(false);

        advance();

        try {
            try {
                super.doCall();
            } catch (Exception e) {
                logger.debug(e, "Transaction for task ''{0}'' will rollback", getDelegateTask());

                try {
                    // handle transaction rollback
                    connection.rollback();
                } catch (SQLException se) {
                    logger.warn(se, "Rollback for task ''{0}'' failed", getDelegateTask());
                }
                throw e;
            }

            logger.debug("Transaction bound task ''{0}'' completed. Committing transation", getDelegateTask());

            // handle transaction committing
            connection.commit();
        } finally {
            try {
                connection.setAutoCommit(autoCommitStatus);
            } catch (SQLException se) {
                logger.warn(se, "Resetting auto commit for task ''{0}'' failed", getDelegateTask());
            }
        }

        advance();
    }

    @Override
    public String toString() {
        return "TransactionTask wrapper around task: " + getDelegateTask();
    }
}
