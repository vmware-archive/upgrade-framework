/* ****************************************************************************
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.sql.util;

import groovy.lang.MetaProperty;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.testng.annotations.Test;
import org.testng.Assert;

import com.vmware.upgrade.TestGroups;

/**
*
* @author Matthew Frost mfrost@vmware.com
* @version 1.0
* @since 1.0
*/
public class ClassUtilTest {
    @Test(groups = { TestGroups.UNIT })
    public void hasFieldTest() {
        class TestClass {
            @SuppressWarnings("unused")
            public Object a = new Object();
        }

        Assert.assertTrue(DefaultGroovyMethods.hasProperty(TestClass.class, "a") == null);
        Assert.assertTrue(ClassUtil.hasField(TestClass.class, "a") instanceof MetaProperty);

        Assert.assertFalse(ClassUtil.hasField(TestClass.class, "b") instanceof MetaProperty);
        Assert.assertTrue(ClassUtil.hasField(TestClass.class, "b") == null);
    }
}
