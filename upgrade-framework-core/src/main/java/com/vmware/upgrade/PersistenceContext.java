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

package com.vmware.upgrade;

/**
 * A {@link PersistenceContext} encapsulates the information about a particular persistence store
 * and specific implementations act as entry points suitable for interacting with specific types of
 * persistence mechanisms.
 *
 * @see UpgradeContext#getPersistenceContext(Class)
 * @see UpgradeContext#getPersistenceContext(Class, String)
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public interface PersistenceContext {
    /**
     * Returns {@code true} if the context is in a usable state.
     *
     * @return Returns {@code true} if the context can be safely used. Returns {@code false}
     *          otherwise.
     */
    public boolean isConnected();
}
