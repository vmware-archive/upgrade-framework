/* ****************************************************************************
 * Copyright (c) 2014-2017 VMware, Inc. All Rights Reserved.
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

/**
 * Interface to control whether or not to allow nulls.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface NullAware extends HasClosureMap {
    /**
     * Set the allowing of null values.
     *
     * @param arg the {@link Object} used to determine allowing of null values
     * @return Object
     */
    public Object makeNullable(Object arg);

    /**
     * Get the allowing or disallowing of null values.
     *
     * @return true if nulls are allowed, false otherwise
     */
    public boolean isNullable();

    /**
     * Make a copy of this object
     *
     * @return a copy of this {@link NullAware}
     */
    public NullAware makeCopy();

    /**
     * Make a copy of this object that allows nulls, regardless of the nullability
     * of the original object.
     *
     * @return a copy of this {@link NullAware} that will allow nulls
     */
    public NullAware makeNullableCopy();
}
