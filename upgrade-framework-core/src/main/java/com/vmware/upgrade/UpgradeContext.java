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

import java.util.NoSuchElementException;

import com.vmware.upgrade.context.PersistenceContextHelper;
import com.vmware.upgrade.logging.UpgradeLogger;
import com.vmware.upgrade.logging.UpgradeLoggerHelper;
import com.vmware.upgrade.sequencing.Version;

/**
 * {@link UpgradeContext} encapsulates the environment within which the framework will operate.
 * <p>
 * Framework users will need to create an implementation of this interface which captures the
 * specifics of their use case.
 * <p>
 * Where appropriate, helper classes are provided to aid in the implementation of these methods.
 * See each method's documentation for more information.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface UpgradeContext {
    /**
     * Produce a {@link UpgradeLogger} instance suitable for use by the indicated class.
     *
     * @see UpgradeLoggerHelper
     *
     * @param clazz The {@link Class} which will use the returned logger.
     * @return a {@link UpgradeLogger} instance
     */
    UpgradeLogger getLogger(Class<?> clazz);

    /**
     * Return the current {@link Version} representing the state of the environment within which
     * upgrade is occurring.
     *
     * @return the current {@link Version}
     */
    Version getVersion();

    /**
     * Update the current {@link Version} representing the state of the environment within which
     * upgrade is occurring.
     *
     * @param version The new {@link Version}
     */
    void setVersion(Version version);

    /**
     * Return an instance of a utility class suitable for interacting with a specific type of
     * persistence mechanism, as indicated by the supplied {@link Class}.
     *
     * @see PersistenceContextHelper
     *
     * @param type the desired {@link Class}
     * @return an instance of the specified context {@link Class}, if one exists.
     * @throws NoSuchElementException if no instance is found
     */
    <T extends PersistenceContext> T getPersistenceContext(Class<T> type);

    /**
     * Return an instance of a utility class suitable suitable for interacting with a specific
     * type of persistence mechanism, as indicated by the supplied {@link Class} and
     * {@code qualifier} {@link String}.
     *
     * @see PersistenceContextHelper
     *
     * @param type the desired {@link Class}
     * @param qualifier the desired {@code qualifier} {@link String}
     * @return an instance of the specified context {@link Class} with the specified
     *          {@code qualifier} {@link String}, if one exists.
     * @throws NoSuchElementException if no instance is found
     */
    <T extends PersistenceContext> T getPersistenceContext(Class<T> type, String qualifier);
}
