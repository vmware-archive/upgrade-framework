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

import java.util.Objects;

import com.vmware.upgrade.PersistenceContext;

/**
 * A {@link PersistenceContextReference} serves as a doubly-keyed map entry for use with
 * {@link PersistenceContextHelper}.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public interface PersistenceContextReference<T extends PersistenceContext> {
    /**
     * Accessor for the {@link Class} of the referenced {@link PersistenceContext}.
     *
     * @return the {@link Class} of the {@link PersistenceContext}.
     */
    Class<T> getType();

    /**
     * Accessor for a {@link String} identifying the {@link PersistenceContext}.
     *
     * @return an identifying {@link String}.
     */
    String getQualifier();

    /**
     * Accessor for the referenced {@link PersistenceContext}.
     *
     * @return the referenced {@link PersistenceContext}.
     */
    T getTarget();

    /**
     * Default implementation of {@link PersistenceContextReference}.
     * <p>
     * This class provides a very basic implementation of the {@link PersistenceContextReference}
     * which assumes all information is available at construction time and provides no ability to
     * subsequently modify the object.
     * <p>
     * It is expected that this implementation will be sufficient for most basic use cases
     * (including tests).
     */
    static class ImmutablePersistenceContextReference<T extends PersistenceContext> implements PersistenceContextReference<T> {
        private final T target;
        private final Class<T> type;
        private final String qualifier;

        public ImmutablePersistenceContextReference(final Class<T> type, final T target) {
            this(type, target, "");
        }

        public ImmutablePersistenceContextReference(final Class<T> type, final T target, final String qualifier) {
            this.type = type;
            this.target = target;
            this.qualifier = qualifier;
        }

        @Override
        public Class<T> getType() {
            return type;
        }

        @Override
        public String getQualifier() {
            return qualifier;
        }

        @Override
        public T getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImmutablePersistenceContextReference)) {
                return false;
            }

            final ImmutablePersistenceContextReference<?> o = (ImmutablePersistenceContextReference<?>) obj;

            return Objects.equals(type, o.type) &&
                    Objects.equals(qualifier, o.qualifier) &&
                    Objects.equals(target, o.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, qualifier, target);
        }

        @Override
        public String toString() {
            return super.toString() + ": {type:" + type + ", qualifier: " + qualifier + "}";
        }
    }
}
