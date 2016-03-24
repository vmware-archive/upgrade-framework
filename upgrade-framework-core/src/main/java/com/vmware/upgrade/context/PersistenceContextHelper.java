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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import com.vmware.upgrade.PersistenceContext;
import com.vmware.upgrade.UpgradeContext;

/**
 * Utility class for persistence-related convenience methods to facilitate the implementation of
 * {@link UpgradeContext}s.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class PersistenceContextHelper {
    private final Collection<PersistenceContextReference<?>> persistenceContextReferences;

    public PersistenceContextHelper(Collection<PersistenceContextReference<?>> persistenceContextReferences) {
        this.persistenceContextReferences = persistenceContextReferences;
    }

    public PersistenceContextHelper(PersistenceContextReference<?> ... persistenceContextReferences) {
        this(Arrays.asList(persistenceContextReferences));
    }

    /**
     * Return a {@link PersistenceContext} of the specified {@link Class}, if one exists.
     *
     * @see UpgradeContext#getPersistenceContext(Class)
     *
     * @param <T> extends {@link PersistenceContext}
     * @param type the {@link Class} of {@link PersistenceContext} to return.
     * @return a {@link PersistenceContext} of the specified {@link Class}.
     * @throws NoSuchElementException if no {@link PersistenceContext} of the specified
     *          {@link Class} is found.
     */
    public <T extends PersistenceContext> T getPersistenceContext(Class<T> type) {
        final List<PersistenceContextReference<T>> candidates = getCandidates(type);

        if (candidates.isEmpty()) {
            throw new NoSuchElementException(); // Can we do better?
        }

        return candidates.get(0).getTarget();
    }

    /**
     * Return a {@link PersistenceContext} of the specified {@link Class} identified by the specified
     * {@link String}, if one exists.
     *
     * @see UpgradeContext#getPersistenceContext(Class, String)
     *
     * @param <T> extends {@link PersistenceContext}
     * @param type the {@link Class} of {@link PersistenceContext} to return.
     * @param qualifier a {@link String} identifying the {@link PersistenceContext} to return.
     * @return a {@linkplain PersistenceContext} of the specified {@linkplain Class}.
     * @throws NoSuchElementException if no {@link PersistenceContext} of the specified
     *          {@link Class} with the specified {@code qualifier} is found.
     */
    public <T extends PersistenceContext> T getPersistenceContext(Class<T> type, String qualifier) {
        final List<PersistenceContextReference<T>> candidates = getCandidates(type);

        for (PersistenceContextReference<T> candidate : candidates) {
            if (candidate.getQualifier().equals(qualifier)) {
                return candidate.getTarget();
            }
        }

        throw new NoSuchElementException(); // Can we do better?
    }

    private <T extends PersistenceContext> List<PersistenceContextReference<T>> getCandidates(Class<T> type) {
        final List<PersistenceContextReference<T>> candidates = new ArrayList<PersistenceContextReference<T>>();

        for (PersistenceContextReference<?> persistenceContextReference : persistenceContextReferences) {
            if (type.isAssignableFrom(persistenceContextReference.getType())) {
                @SuppressWarnings("unchecked") // We checked
                final PersistenceContextReference<T> candidate = (PersistenceContextReference<T>) persistenceContextReference;

                candidates.add(candidate);
            }
        }

        return candidates;
    }
}
