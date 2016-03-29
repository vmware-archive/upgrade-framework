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

package com.vmware.upgrade.dsl.sql.syntax

import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel
import com.vmware.upgrade.dsl.sql.util.UpgradeLoader

import org.testng.Assert

/**
 * A utility class for upgrade DDL syntax tests to use.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
class SyntaxTestUtil {
    private static String UPGRADE_DEFINITION_WRAPPER = "upgrade { %s }";

    /**
     * Wraps the given {@code DDL} in an {@code upgrade} keyword, loads it,
     * and returns an {@link UpgradeDefinitionModel}.
     *
     * @param ddl
     */
    public static UpgradeDefinitionModel loadInline(String ddl) {
        final String wrappedUpgrade = String.format(UPGRADE_DEFINITION_WRAPPER, ddl);

        return UpgradeLoader.loadDefinitionInline(wrappedUpgrade)
    }

    /**
     * Provides assertions for basic truths about an {@link UpgradeDefinitionModel}
     * such as verifying it is not null and that it manages exactly one task.
     *
     * @param upgrade
     * @param enableDryRun an optional parameter to enable logging of the
     *        model's raw SQL for all {@link DatabaseType}s. This is
     *        {@code false} by default.
     */
    public static void basicVerification(UpgradeDefinitionModel upgrade) {
        Assert.assertNotNull(upgrade)
        Assert.assertNotNull(upgrade.tasks)
        Assert.assertEquals(upgrade.tasks.size(), 1)
    }

    /**
     * Loads the given {@code DDL} using {@link SyntaxTestUtil#loadInline(String)}
     * which is expected to fail with the exception specified by {@code clazz}. This
     * exception's message is also expected to {@link String#contains(CharSequence) contain}
     * the specified message, {@code expectedMsg}.
     *
     * @param ddl upgrade DDL to load
     * @param clazz expected exception class
     * @param expectedMsg expected exception's message must contain this string
     */
    public static void verifyException(String ddl, Class<?> clazz, String expectedMsg) {
        try {
            loadInline(ddl);

            Assert.fail("${clazz.getSimpleName()} should have been thrown containing message \"${expectedMsg}\"");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), clazz);
            Assert.assertTrue(e.getMessage().contains(expectedMsg));
        }
    }
}
