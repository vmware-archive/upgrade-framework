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

package com.vmware.upgrade.dsl.sql.semantics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import com.vmware.upgrade.dsl.Processor;
import com.vmware.upgrade.dsl.TaskResolver;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel.TaskDescriptor;
import com.vmware.upgrade.dsl.sql.util.UpgradeLoader;
import com.vmware.upgrade.sql.DatabaseType;
import com.vmware.upgrade.sql.SQLStatement;

/**
 * A utility for upgrade DDL semantic tests to use.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class SemanticTestUtil {
    static enum TestDatabaseTypes implements DatabaseType {
        ORACLE, MS_SQL, POSTGRES;

        @Override
        public String load(String scriptName) throws IOException {
            return "";
        }
    }

    private static final String UPGRADE_DEFINITION_WRAPPER = "upgrade { %s }";

    private static String wrapUpgrade(String ddl) {
        return String.format(UPGRADE_DEFINITION_WRAPPER, ddl);
    }

    /**
     * Wraps the given {@code DDL} in an {@code upgrade} keyword, loads it,
     * and returns an {@link UpgradeDefinitionModel}.
     *
     * @param ddl
     */
    public static UpgradeDefinitionModel createUpgradeModel(String ddl) {
        return UpgradeLoader.loadDefinitionInline(wrapUpgrade(ddl));
    }

    /**
     * Wraps the given {@code DDL} in an {@code upgrade} keyword, loads it,
     * and returns an {@link UpgradeDefinitionModel}.
     *
     * @param ddl
     * @param taskResolver
     * @param processor
     */
    public static UpgradeDefinitionModel createUpgradeModel(String ddl, TaskResolver taskResolver, Processor processor) {
        return UpgradeLoader.loadDefinitionInline(wrapUpgrade(ddl), taskResolver, processor);
    }

    /**
     * Assert that the raw SQL the given {@link UpgradeDefinitionModel}
     * represents equals the {@code expected} {@link SQLStatement} for all
     * {@link DatabaseType}s.
     * <p>
     * If a raw SQL string can not be retrieved from {@code expected} for a
     * particular {@link DatabaseType} because it has no representation in its
     * internal map, then it is ignored. This will not throw an exception or
     * cause the equality assertion to fail.
     *
     * @param upgrade
     * @param expected
     */
    public static void compareModelToSQL(UpgradeDefinitionModel upgrade, SQLStatement expected) {
        for (DatabaseType databaseType : TestDatabaseTypes.values()) {
            try {
                final SQLStatement sql = findSQLStatement(upgrade.getTasks());

                Assert.assertEquals(sql.get(databaseType), expected.get(databaseType));
            } catch (IllegalArgumentException e) {
                if (!e.getMessage().startsWith("map does not contain a record for database type")) {
                    throw e;
                }
            }
        }
    }

    private static SQLStatement findSQLStatement(List<TaskDescriptor> tasks) {
        final Object o = ((List<?>) tasks.get(0).getArgs()).get(0);

        if (o instanceof SQLStatement) {
            return (SQLStatement) o;
        } else if (o instanceof List) {
            final List<?> list = (List<?>) o;
            final List<TaskDescriptor> typedList = new ArrayList<TaskDescriptor>(list.size());
            for (Object item : list) {
                if (item instanceof TaskDescriptor) {
                    typedList.add((TaskDescriptor) item);
                }
            }

            return findSQLStatement(typedList);
        } else {
            throw new IllegalArgumentException("Can not find an SQLStatement in the upgrade model's task list");
        }
    }
}
