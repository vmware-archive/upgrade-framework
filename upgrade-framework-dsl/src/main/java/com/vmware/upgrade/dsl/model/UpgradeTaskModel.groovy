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

package com.vmware.upgrade.dsl.model;

import com.vmware.upgrade.Task
import com.vmware.upgrade.UpgradeContext
import com.vmware.upgrade.sequencing.Graph
import com.vmware.upgrade.sequencing.Version

/**
 * A DSL model object representing a {@link Graph.Edge} within the {@link Graph} represented
 * by the {@link ManifestModel}.
 * <p>
 * This class represents the association between a concrete {@link UpgradeDefinitionModel} and the
 * version information related to that process (the required source version and the resulting
 * version assuming successful execution).
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
class UpgradeTaskModel implements Graph.Edge {
    Version source
    Version target
    UpgradeDefinitionModel definition

    @Override
    public Task createTask(UpgradeContext context) {
        return definition.createTask(context);
    }

    @Override
    public Version getSource() {
        return source;
    }

    @Override
    public Version getTarget() {
        return target;
    }
}
