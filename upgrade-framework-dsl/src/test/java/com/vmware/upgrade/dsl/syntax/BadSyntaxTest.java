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

package com.vmware.upgrade.dsl.syntax;

import groovy.lang.Closure;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.dsl.ManifestLoader;
import com.vmware.upgrade.dsl.Processor;
import com.vmware.upgrade.dsl.TaskResolver;
import com.vmware.upgrade.dsl.util.UpgradeLoader;

import org.codehaus.groovy.runtime.MethodClosure;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for syntax errors and the warnings they should generate.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class BadSyntaxTest {
    UpgradeCompilationException checkManifestError(String manifest) {
        try {
            ManifestLoader.loadInlineManifest(manifest);
            Assert.fail("no exception");
        } catch (UpgradeCompilationException e) {
            return e;
        }
        return null;
    }
    @Test(groups = { TestGroups.UNIT })
    public void unknownSyntaxKeywordTest() {
        UpgradeCompilationException e =  checkManifestError("blah {}\n");
        Assert.assertTrue(e instanceof UnknownKeywordException);
        Assert.assertEquals(((UnknownKeywordException) e).getName(), "blah");
    }

    @Test(groups = { TestGroups.UNIT })
    public void unknownUpgradeInManifestTest() {
        UpgradeCompilationException e = checkManifestError(
                "manifest {\n" +
                "    from '1.0.0' call foo\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof UnknownVariableException);
        Assert.assertEquals(((UnknownVariableException) e).getName(), "foo");
        e = checkManifestError(
                "manifest {\n" +
                "    from '1.0.0' see foo\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof UnknownVariableException);
        Assert.assertEquals(((UnknownVariableException) e).getName(), "foo");
    }

    @Test(groups = { TestGroups.UNIT })
    public void unknownUpgradeKeywordTest() {
        UpgradeCompilationException e = checkManifestError(
                "upgrade {\n" +
                "   blah 1.0\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof UnknownKeywordException);
        Assert.assertEquals(((UnknownKeywordException) e).getName(), "blah");
    }

    @Test(groups = { TestGroups.UNIT })
    public void duplicateUpgradeNameTest() {
        UpgradeCompilationException e = checkManifestError(
                "foo = upgrade {\n" +
                "}\n" +
                "foo = upgrade {\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof DuplicateVariableException);
        Assert.assertEquals(((DuplicateVariableException) e).getName(), "foo");
    }

    @Test(groups = { TestGroups.UNIT })
    public void lateMetadataTest() {
        UpgradeCompilationException e = checkManifestError(
                "upgrade {\n" +
                "   java \"com.vmware.upgrade.task.TrivialTask\"\n" +
                "   name \"oops\"\n" +
                "}\n"
        );

        Assert.assertEquals(e.getMessage(),
            "Attempted to set 'name' when metadata not allowed " +
                    "at inlineScript$_run_closure1.doCall(inlineScript:3)");
    }

    @Test(groups = { TestGroups.UNIT })
    public void unknownManifestKeywordTest() {
        UpgradeCompilationException e = checkManifestError(
                "dummy = upgrade {}\n" +
                "manifest {\n" +
                "    blah '1.0.0' baz dummy\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof UnknownKeywordException);
        Assert.assertEquals(((UnknownKeywordException) e).getName(), "blah");
    }

    @Test(groups = { TestGroups.UNIT })
    public void unknownManifestSubKeywordTest() {
        UpgradeCompilationException e = checkManifestError(
                "dummy = 5\n" +
                "manifest {\n" +
                "    from '1.0.0' baz dummy\n" +
                "}\n"
        );
        Assert.assertTrue(e instanceof UnknownKeywordException);
        Assert.assertEquals(((UnknownKeywordException) e).getName(), "baz");
    }

    @DataProvider
    public Object[][] invalidSyntaxStatements() {
        return new Object[][] {
                new Object[] {
                        "outerKeyword 'foo' middleKeyword 'bar'",
                        IllegalArgumentException.class,
                        "Keyword 'innerKeyword' is required",
                        new RequiredKeywordProcessor()
                },
                new Object[] {
                        "outerKeyword 'foo' wrongKeyword 'bar'",
                        UnknownKeywordException.class,
                        "Unknown keyword: wrongKeyword",
                        new UnknownKeywordProcessor()
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalidSyntaxStatements")
    public void missingKeywordTest(String ddl, Class<?> clazz, String expectedMsg, Processor processor) {
        verifyException(ddl, clazz, expectedMsg, processor);
    }

    class UnknownKeywordProcessor implements Processor {
        public Map<String, Closure<?>> outer(String arg) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Closure<?>> getKeywordProcessors() {
            final Map<String, Closure<?>> outer = new HashMap<>();
            outer.put("outerKeyword", new MethodClosure(this, "outer"));
            return outer;
        }

        @Override
        public List<?> getPropertyProcessors() {
            return Collections.emptyList();
        }
    }

    class TestTaskResolver implements TaskResolver {
        @Override
        public Task resolve(UpgradeContext context, Class<?> taskClass, String name, List<?> args) {
            return null;
        }

        @Override
        public Task combine(UpgradeContext context, List<Task> tasks, String name) {
            return null;
        }
    }

    private void verifyException(String ddl, Class<?> clazz, String expectedMsg, Processor processor) {
        final String wrappedDdl = String.format("upgrade { %s }", ddl);
        try {
            UpgradeLoader.loadDefinitionInline(wrappedDdl, new TestTaskResolver(), processor);
            Assert.fail(clazz.getSimpleName()+" should have been thrown containing message \""+expectedMsg+"\"");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), clazz);
            Assert.assertTrue(e.getMessage().contains(expectedMsg));
        }
    }
}
