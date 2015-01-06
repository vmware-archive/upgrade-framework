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

/**
 * The core upgrade framework.
 * <p>
 * The upgrade framework exists to generally describe the process of performing a set of actions,
 * represented as {@link com.vmware.upgrade.Task} objects, to transition one or more persistence
 * mechanisms, described by {@link com.vmware.upgrade.PersistenceContext} objects encapsulated
 * within an overall {@link com.vmware.upgrade.UpgradeContext}, from one
 * {@link com.vmware.upgrade.sequencing.Version} to another later
 * {@link com.vmware.upgrade.sequencing.Version}.
 * <p>
 * In addition to encapsulating the {@linkplain com.vmware.upgrade.PersistenceContext} objects, the
 * {@link com.vmware.upgrade.UpgradeContext} is responsible for capturing information about
 * the environment within which the upgrade is occurring, including the current
 * {@link com.vmware.upgrade.sequencing.Version} of the environment and the process for
 * {@linkplain com.vmware.upgrade.logging.UpgradeLogger logging} the changes being made.
 *
 * @since 1.0
 */
package com.vmware.upgrade;
