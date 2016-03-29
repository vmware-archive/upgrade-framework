/* ****************************************************************************
 * Copyright (c) 2011-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.factory;

import java.io.IOException;

import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.UpgradeDefinition;
import com.vmware.upgrade.sequencing.Version;

/**
 * A factory which will create {@link UpgradeDefinition}s for a specified {@link UpgradeContext}.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface UpgradeDefinitionFactory {
    /**
     * Determine whether an {@link UpgradeDefinition} can be produced for the given context.
     *
     * @param context The {@link UpgradeContext} within which the upgrade would run.
     * @return true if the factory should be able to produce an upgrade for that context
     * @throws IOException if an IO error occurs when reading files defining the upgrade.
     */
    boolean isUpgradeSupported (final UpgradeContext context) throws IOException;

    /**
     * Generate an {@link UpgradeDefinition} which, when executed, will result in upgrade from the
     * {@link UpgradeContext#getVersion() current version} to the
     * {@link #getTargetVersion() highest reachable version}.
     *
     * @param context The {@link UpgradeContext} within which the upgrade should run.
     * @return the created {@link UpgradeDefinition}
     * @throws IOException if an IO error occurs when reading files defining the upgrade.
     */
    UpgradeDefinition create(final UpgradeContext context) throws IOException;

    /**
     * Return the highest {@link Version} reachable via execution of an {@link UpgradeDefinition}
     * produced by this factory.
     *
     * @return the maximum reachable {@link Version}
     */
    Version getTargetVersion();
}
