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

package com.vmware.upgrade.task;

import java.util.List;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.logging.UpgradeLogger;

/**
 * A {@link Task} which aggregates other {@link Task}s and executes them in series
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public final class SerialAggregateTask extends AbstractAggregateTask {
    private final UpgradeLogger logger;

    /**
     * Constructs a named task which aggregates the supplied tasks.
     *
     * @param name
     *          see {@link Task#getName()}
     * @param children
     *          the children to execute
     * @throws IllegalArgumentException
     *          if {@code children} is {@code null}
     */
    public SerialAggregateTask(final UpgradeContext context, final String name, final List<Task> children) {
        super(name, children);
        logger = context.getLogger(getClass());
    }

    @Override
    public Void call() throws Exception {
        logger.trace("{0}: Beginning execution", getName());

        try {
            for (final Task child : getChildren()) {
                logger.debug("{0}: Beginning execution of task {1}", getName(), child.getName());
                child.call();
            }
        } catch (Exception e) {
            logger.warn(e, "{0}: Task failed due to uncaught exception", getName());
            getReporter().terminateWithFailure();
            throw e;
        }

        logger.trace("{0}: Completed successfully", getName());

        return null;
    }
}

