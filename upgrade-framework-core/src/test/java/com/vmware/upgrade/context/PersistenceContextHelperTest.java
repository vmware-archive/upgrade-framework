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

package com.vmware.upgrade.context;

import java.util.NoSuchElementException;

import com.vmware.upgrade.PersistenceContext;
import com.vmware.upgrade.TestGroups;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test class to verify the behavior of {@link PersistenceContextHelper}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class PersistenceContextHelperTest {
    private abstract class BasePersistenceContext implements PersistenceContext {
        @Override
        public boolean isConnected() {
            return false;
        }
    }

    private class TypeAPersistenceContext extends BasePersistenceContext {}
    private class SubtypeOfTypeAPersistenceContext extends TypeAPersistenceContext {}
    private class TypeBPersistenceContext extends BasePersistenceContext {}

    private PersistenceContextReference<TypeAPersistenceContext> createTypeAReference() {
        return new PersistenceContextReference.ImmutablePersistenceContextReference<TypeAPersistenceContext>(TypeAPersistenceContext.class, new TypeAPersistenceContext());
    }
    private PersistenceContextReference<TypeAPersistenceContext> createTypeAReference(final String qualifier) {
        return new PersistenceContextReference.ImmutablePersistenceContextReference<TypeAPersistenceContext>(TypeAPersistenceContext.class, new TypeAPersistenceContext(), qualifier);
    }
    private PersistenceContextReference<SubtypeOfTypeAPersistenceContext> createSubtypeOfAReference() {
        return new PersistenceContextReference.ImmutablePersistenceContextReference<SubtypeOfTypeAPersistenceContext>(SubtypeOfTypeAPersistenceContext.class, new SubtypeOfTypeAPersistenceContext());
    }
    private PersistenceContextReference<TypeBPersistenceContext> createTypeBReference() {
        return new PersistenceContextReference.ImmutablePersistenceContextReference<TypeBPersistenceContext>(TypeBPersistenceContext.class, new TypeBPersistenceContext());
    }
    private PersistenceContextReference<TypeBPersistenceContext> createTypeBReference(final String qualifier) {
        return new PersistenceContextReference.ImmutablePersistenceContextReference<TypeBPersistenceContext>(TypeBPersistenceContext.class, new TypeBPersistenceContext(), qualifier);
    }

    @Test(groups = TestGroups.UNIT)
    public void baseTest() {
        final PersistenceContextHelper helper = new PersistenceContextHelper(createTypeAReference());

        Assert.assertNotNull(helper.getPersistenceContext(TypeAPersistenceContext.class));
    }

    @Test(groups = TestGroups.UNIT, expectedExceptions = { NoSuchElementException.class })
    public void emptyTest() {
        final PersistenceContextHelper helper = new PersistenceContextHelper();

        helper.getPersistenceContext(TypeAPersistenceContext.class);
    }

    @Test(groups = TestGroups.UNIT)
    public void retrieveByTypeTest() {
        final PersistenceContextReference<?> a = createTypeAReference();
        final PersistenceContextReference<?> b = createTypeBReference();

        Assert.assertNotEquals(a, b);

        final PersistenceContextHelper helper = new PersistenceContextHelper(a, b);

        Assert.assertEquals(helper.getPersistenceContext(TypeAPersistenceContext.class), a.getTarget());
        Assert.assertEquals(helper.getPersistenceContext(TypeBPersistenceContext.class), b.getTarget());
    }

    @Test(groups = TestGroups.UNIT)
    public void retrieveBySuperTypeTest() {
        final PersistenceContextReference<?> subA = createSubtypeOfAReference();

        final PersistenceContextHelper helper = new PersistenceContextHelper(subA);

        Assert.assertEquals(helper.getPersistenceContext(TypeAPersistenceContext.class), subA.getTarget());
    }

    @Test(groups = TestGroups.UNIT)
    public void retrieveExactTypeTest() {
        final PersistenceContextReference<?> a = createTypeAReference();
        final PersistenceContextReference<?> subA = createSubtypeOfAReference();

        final PersistenceContextHelper helper = new PersistenceContextHelper(a, subA);

        Assert.assertEquals(helper.getPersistenceContext(TypeAPersistenceContext.class), a.getTarget());
        Assert.assertEquals(helper.getPersistenceContext(SubtypeOfTypeAPersistenceContext.class), subA.getTarget());
    }

    @Test(groups = TestGroups.UNIT, expectedExceptions = { NoSuchElementException.class })
    public void missingTypeTest() {
        final PersistenceContextHelper helper = new PersistenceContextHelper(createTypeAReference());

        helper.getPersistenceContext(TypeBPersistenceContext.class);
    }

    @Test(groups = TestGroups.UNIT)
    public void retrieveByTypeAndQualifierTest() {
        final PersistenceContextReference<?> unnamed = createTypeAReference();
        final PersistenceContextReference<?> named = createTypeAReference("named");

        Assert.assertNotEquals(unnamed, named);

        final PersistenceContextHelper helper = new PersistenceContextHelper(unnamed, named);

        Assert.assertEquals(helper.getPersistenceContext(TypeAPersistenceContext.class, "named"), named.getTarget());
        Assert.assertNotEquals(helper.getPersistenceContext(TypeAPersistenceContext.class, "named"), unnamed.getTarget());
    }

    @Test(groups = TestGroups.UNIT, expectedExceptions = { NoSuchElementException.class })
    public void missingQualifierTest() {
        final PersistenceContextReference<?> named = createTypeAReference("named");

        final PersistenceContextHelper helper = new PersistenceContextHelper(named);

        helper.getPersistenceContext(TypeAPersistenceContext.class, "");
    }

    @Test(groups = TestGroups.UNIT, expectedExceptions = { NoSuchElementException.class })
    public void missingTypeAndQualifierTest() {
        final PersistenceContextHelper helper = new PersistenceContextHelper();

        helper.getPersistenceContext(TypeAPersistenceContext.class, "named");
    }

    @Test(groups = TestGroups.UNIT)
    public void retrieveByTypeAndQualifierWithSimilarContextTest() {
        final PersistenceContextReference<?> unnamed = createTypeAReference();
        final PersistenceContextReference<?> named = createTypeAReference("named");
        final PersistenceContextReference<?> other = createTypeBReference("named");

        final PersistenceContextHelper helper = new PersistenceContextHelper(unnamed, named, other);

        Assert.assertEquals(helper.getPersistenceContext(TypeAPersistenceContext.class, "named"), named.getTarget());
        Assert.assertNotEquals(helper.getPersistenceContext(TypeAPersistenceContext.class, "named"), other.getTarget());
    }
}
