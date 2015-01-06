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

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.logging.UpgradeLoggerHelper;
import com.vmware.upgrade.sql.DatabasePersistenceContext;
import com.vmware.upgrade.task.TrivialTask;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link TransactionTask}.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
@Test(groups = {TestGroups.UNIT})
public class TransactionTaskTest {
    private final IMocksControl control = EasyMock.createControl();
    private UpgradeContext context;
    private DatabasePersistenceContext databaseContext;
    private Connection connection;

    @BeforeClass
    public void createMocks() {
        context = control.createMock(UpgradeContext.class);
        databaseContext = control.createMock(DatabasePersistenceContext.class);
        connection = control.createMock(Connection.class);
    }

    @BeforeMethod
    public void wireMocks() {
        EasyMock.expect(context.getPersistenceContext(DatabasePersistenceContext.class)).andReturn(databaseContext).anyTimes();
        EasyMock.expect(context.getLogger(TransactionTask.class)).andReturn(UpgradeLoggerHelper.NO_OP_LOGGER).anyTimes();
        EasyMock.expect(databaseContext.isConnected()).andReturn(true).anyTimes();
        EasyMock.expect(databaseContext.getConnection()).andReturn(connection).anyTimes();
    }

    @AfterMethod
    public void resetMocks() {
        control.reset();
    }

    @Test
    public void testCommit() throws Exception {
        // General Expectations
        connection.setAutoCommit(false); EasyMock.expectLastCall();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(true); EasyMock.expectLastCall();

        // Commit Expectations
        connection.commit(); EasyMock.expectLastCall();

        control.replay();

        TransactionTask t = new TransactionTask("test transaction", new TrivialTask("test runnable", new Runnable() {
            @Override
            public void run() {
            }
        }), context);

        t.call();

        control.verify();
    }

    @Test
    public void testRollback() throws Exception {
        // General Expectations
        connection.setAutoCommit(false); EasyMock.expectLastCall();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(true); EasyMock.expectLastCall();

        // Commit Expectations
        connection.rollback(); EasyMock.expectLastCall();

        control.replay();

        TransactionTask t = new TransactionTask("test transaction", new TrivialTask("test runnable", new Runnable() {
            @Override
            public void run() {
                throw new ExpectedException();
            }
        }), context);

        try {
            t.call();
        } catch(ExpectedException e) {
            // expected.
        }

        control.verify();
    }

    @SuppressWarnings("serial")
    private class ExpectedException extends RuntimeException {}
}
