/* ****************************************************************************
 * Copyright (c) 2011-2015 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.sequencing;

import com.vmware.upgrade.TestGroups;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Ensure that {@link Version} correctly identifies valid versions and their transition state.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class VersionTest {
    @Test(groups = { TestGroups.UNIT })
    public void testLookupWithConstant() throws Exception {
        String[] hasConstant = {"1.0", "1.5", "2.0.0"};
        for (String s : hasConstant) {
            Assert.assertNotNull(Version.lookup(s));
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testLookupWithoutConstant() throws Exception {
        String[] noConstant = {"0.9", "1.3", "3"};
        for (String s : noConstant) {
            Assert.assertNotNull(Version.lookup(s));
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testInvalidLookup() throws Exception {
        String[] invalid = {"1.3.4.5.6", "foobarbaz"};
        for (String s : invalid) {
            Assert.assertNull(Version.lookup(s));
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testNotIsTransition() {
        String[] notTransition = {"1.0", "1.5", "2.0.0"};
        for (String s : notTransition) {
            Assert.assertFalse(Version.lookup(s).isTransition());
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testIsTransition() throws Exception {
        String[] transition = {/* "In Transition 1.0->1.5",*/ "2.0.0.TRANSITION"};
        for (String s : transition) {
            Assert.assertTrue(Version.lookup(s).isTransition());
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testSimpleLookupToString() throws Exception {
        String[] valid = {"1.0.0", "1.5.0", "2.0.0", "1.5.0.TRANSITION", "2.0.0.TRANSITION"};
        for (String s : valid) {
            Assert.assertEquals(Version.lookup(s).toString(), "\"" + s + "\"");
        }
    }

    @Test(groups = { TestGroups.UNIT })
    public void testComplexLookupToString() throws Exception {
        String[] valid = {"[ \"1.0.0\", \"2.0.1\" ]", "[ \"2.0.1\", \"6.0.1\" ]"};
        for (String s : valid) {
            Assert.assertEquals(Version.lookup(s).toString(), s);
        }
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "transitions")
    public void testGetTransition(Version source, Version destination, Version expected) {
        Assert.assertEquals(source.getTransition(destination), expected);
    }

    @DataProvider(name = "transitions")
    public Object[][] provider() {
        Object[][] getTransitionTests = {
                createTestGetTransitionArgs("1.0.0", "1.0.1", "1.0.0.transition"),
                createTestGetTransitionArgs("1.0.0", "2.0.0", "1.0.0.transition"),
                createTestGetTransitionArgs("1.0.0.foo", "1.0.1.foo", "1.0.0.foo-transition")
        };

        return getTransitionTests;
    }

    private Version[] createTestGetTransitionArgs(String src, String dst, String exp) {
        Version source = Version.lookup(src);
        Version destination = Version.lookup(dst);
        Version expected = Version.lookup(exp);

        return new Version[] { source, destination, expected };
    }

    @Test(groups = { TestGroups.UNIT })
    public void testGet() {
        Assert.assertEquals(Version.lookup("1.0.0").get(1).toString(), "\"0.0.0\"");
        Assert.assertEquals(Version.lookup("[\"1.0.0\",\"2.0.0\"]").get(1).toString(), "\"2.0.0\"");
        Assert.assertEquals(Version.lookup("[ \"1.0.0\", \"2.0.0\" ]").get(3).toString(), "\"0.0.0\"");
    }

    @DataProvider
    public Object[][] replaceVersionByIndex() {
        return new Object[][] {
                new Object[] { "[ \"1.0.0\", \"6.0.1\" ]", 0, "2.0.1", "[ \"2.0.1\", \"6.0.1\" ]" },
                new Object[] { "[ \"2.0.1\", \"6.0.2\" ]", 1, "6.0.3", "[ \"2.0.1\", \"6.0.3\" ]" },
                new Object[] { "[ \"2.0.1\", \"6.0.2\" ]", 1, "6.0.3", "[ \"2.0.1\", \"6.0.3\" ]" },
                new Object[] { "[ \"1.0.0\", \"2.0.0\" ]", 2, "0.0.0", "[ \"1.0.0\", \"2.0.0\", \"0.0.0\" ]" },
                new Object[] { "1.0.0", 1, "{ \"foo\": \"0.0.0\" }", "[ \"1.0.0\", { \"foo\": \"0.0.0\" } ]" },
                new Object[] { "1.0.0", 1, "2.0.0", "[ \"1.0.0\", \"2.0.0\" ]" }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "replaceVersionByIndex")
    public void testReplaceByIndex(String initial, int replaceIndex, String replacement, String result) {
        Assert.assertEquals(
                Version.lookup(initial).replace(replaceIndex, Version.lookup(replacement)).toString(),
                result
        );
    }

    @DataProvider
    public Object[][] replaceVersionByKey() {
        return new Object[][] {
                new Object[] { "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.0\" }", "foo", "1.0.1", "{ \"foo\": \"1.0.1\", \"bar\": \"2.0.0\" }" },
                new Object[] { "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.0\" }", "bar", "2.0.1", "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.1\" }" },
                new Object[] { "0.0.0", "test", "1.0.0", "{ \"test\": \"1.0.0\" }" },
                new Object[] { "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.0\" }", "baz", "0.0.0", "{ \"foo\": \"1.0.0\", \"bar\": \"2.0.0\", \"baz\": \"0.0.0\" }" }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "replaceVersionByKey")
    public void testReplaceByMap(String initial, String replaceKey, String replacement, String result) {
        Assert.assertEquals(
                Version.lookup(initial).replace(replaceKey, Version.lookup(replacement)).toString(),
                result
        );
    }

    @DataProvider
    public Object[][] negativeCompareToVersions() {
        return new Object[][] {
                new Object[] { "[\"2.0.0\",\"6.0.1\"]", "2.0.1" },
                new Object[] { "2.0.1", "[\"2.0.0\",\"6.0.1\"]" },
                new Object[] { "2.0.0", "[\"1.0.0\",\"1.0.0\"]" },
                new Object[] { "[\"1.0.0\",\"2.0.0\"]", "[\"2.0.0\",\"1.0.0\",\"1.0.0\"]" },
                new Object[] { "[\"2.0.0\",\"1.0.0\",\"1.0.0\"]", "[\"1.0.0\",\"2.0.0\"]" },
                new Object[] {
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"2.0.0\" } ]",
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"2.0.1\" } ]"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "negativeCompareToVersions", expectedExceptions = { IllegalArgumentException.class })
    public void testNegativeCompareTo(String a, String b) {
        Version.lookup(a).compareTo(Version.lookup(b));
    }

    @DataProvider
    public Object[][] compareToVersions() {
        return new Object[][] {
                new Object[] { "[\"2.0.1\",\"6.0.1\"]", "[\"2.0.1\",\"6.0.2\"]", -1 },
                new Object[] { "[\"2.0.1\",\"6.0.1\"]", "[\"2.0.0\",\"6.0.1\"]", 1 },
                new Object[] { "[\"2.0.1\",\"6.0.1\"]", "[\"2.0.1\",\"6.0.1\"]", 0 },
                new Object[] {
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.0\" } ]",
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.1\" } ]",
                        -1
                },
                new Object[] {
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.1\" } ]",
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.0\" } ]",
                        1
                },
                new Object[] {
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.0\" } ]",
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.0\" } ]",
                        0
                },
                new Object[] {
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.1\" } ]",
                        "[ \"2.0.1\", \"6.0.1\", { \"foo\": \"1.0.0\", \"bar\": \"1.0.1\", \"baz\": \"1.0.0\" } ]",
                        -1
                },
                new Object[] {
                        "[ \"1.0.0\" ]",
                        "[ [ \"1.0.0\", \"2.0.0\"] ]",
                        -1
                },
                new Object[] {
                        "[ [ \"1.0.0\", \"2.0.0\"] ]",
                        "[ \"1.0.0\" ]",
                        1
                },
                new Object[] {
                        "[ \"1.0.0\", \"2.0.0\", \"3.0.0\", [ \"1.0.0\", \"2.0.0\", [ \"1.0.0\", \"2.0.0\" ], \"3.0.0\" ], \"9.0.0\" ]",
                        "[ \"1.0.0\", \"2.0.0\", \"3.0.0\", [ \"1.0.0\", \"2.0.0\", [ \"1.0.0\", \"2.0.0\", \"1.0.0\" ], \"3.0.0\" ], \"9.0.0\" ]",
                        -1
                },
                new Object[] {
                        "[ \"1.0.0\", \"2.0.0\", \"3.0.0\", [ \"1.0.0\", \"2.0.0\", [ \"1.0.0\", \"2.0.0\", \"1.0.0\" ], \"3.0.0\" ], \"9.0.0\" ]",
                        "[ \"1.0.0\", \"2.0.0\", \"3.0.0\", [ \"1.0.0\", \"2.0.0\", [ \"1.0.0\", \"2.0.0\" ], \"3.0.0\" ], \"9.0.0\" ]",
                        1
                },
                new Object[] {
                        "2.0.1",
                        "[\"2.0.1\",\"6.0.1\"]",
                        -1
                },
                new Object[] {
                        "[\"2.0.1\",\"6.0.1\"]",
                        "2.0.1",
                        1
                },
                new Object[] {
                        "[\"2.0.1\"]",
                        "2.0.1",
                        0
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "compareToVersions")
    public void testCompareTo(String a, String b, int rc) throws Exception {
        Assert.assertEquals(Math.signum(Version.lookup(a).compareTo(Version.lookup(b))), Math.signum(rc));
    }
}
