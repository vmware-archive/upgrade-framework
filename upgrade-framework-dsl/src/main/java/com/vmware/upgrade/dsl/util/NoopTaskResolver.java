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

package com.vmware.upgrade.dsl.util;

import java.util.List;
import java.util.concurrent.Callable;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.dsl.TaskResolver;
import com.vmware.upgrade.task.SerialAggregateTask;
import com.vmware.upgrade.task.TrivialTask;

/**
 * A default {@link TaskResolver} implementation which {@linkplain #resolve resolves} all
 * {@link Class}es into {@link Task}s which do nothing.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class NoopTaskResolver implements TaskResolver {
    @Override
    public Task resolve(UpgradeContext context, Class<?> taskClass, String name, List<?> args) {
        return new TrivialTask(
            name,
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    return null;
                }
            }
        );
    }

    @Override
    public Task combine(UpgradeContext context, List<Task> tasks, String name) {
        return new SerialAggregateTask(context, name, tasks);
    }
}
