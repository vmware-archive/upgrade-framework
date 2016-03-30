/* ****************************************************************************
 * Copyright (c) 2016 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.sql.util

import java.util.concurrent.atomic.AtomicBoolean

import com.vmware.upgrade.Task
import com.vmware.upgrade.UpgradeContext
import com.vmware.upgrade.dsl.TaskResolver
import com.vmware.upgrade.dsl.sql.model.TransformingModel
import com.vmware.upgrade.sql.task.RawSQLTask
import com.vmware.upgrade.task.AbstractSimpleTask
import com.vmware.upgrade.task.SerialAggregateTask
import com.vmware.upgrade.transformation.Transformation

/**
 * A {@link TaskResolver} containing the transformations that would occur
 * for an upgrade, but will <b>not</b> result in any transformations
 * itself, i.e. executing {@code call} on any {@link Task} created by
 * this resolver is guaranteed to be a NO-OP.
 * <p>
 * This can be used to determine the safety of an upgrade, e.g. you could
 * call {@code hasUnknownTransformations} and warn the user if the upgrade
 * contains transformations that use raw SQL.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class TransformingModelTaskResolver implements TaskResolver {

    private AtomicBoolean hasUnknownTransformations = new AtomicBoolean(false)
    private List<Transformation> transformations = new ArrayList<>()

    /**
     * A {@link Task} representing a {@link SQLStatement} where the transformation is
     * unknown (i.e. does not implement {@link TransformingModel}).
     */
    private class TransformationUnknownTask extends AbstractSimpleTask {
        public TransformationUnknownTask(String name) {
            super(name)
        }

        @Override
        public Void call() throws Exception {
            return null
        }
    }

    /**
     * A {@link Task} that performs no action itself but represents one that would
     * transform the database.
     */
    private class TransformingTask extends AbstractSimpleTask {

        private final Transformation transformation

        public TransformingTask(String name, Transformation transformation) {
            super(name)
            this.transformation = transformation
        }

        @Override
        public Void call() throws Exception {
            return null
        }

        @Override
        public String toString() {
            return String.format("Task representing the transformation: %s [%s]",
                    transformation, super.getName())
        }
    }

    public boolean hasUnknownTransformations() {
        return hasUnknownTransformations.get()
    }

    public List<Transformation> getTransformations() {
        return transformations
    }

    @Override
    public Task resolve(UpgradeContext context, Class<?> taskClass, String name, List<?> args) {
        Task task

        if (taskClass.equals(RawSQLTask) && args[0] instanceof TransformingModel) {
            TransformingModel model = (TransformingModel) args[0]
            Transformation transformation = model.getTransformation()
            transformations.add(transformation)
            task = new TransformingTask(name, transformation)
        } else {
            hasUnknownTransformations.set(true)
            task = new TransformationUnknownTask(name)
        }

        return task
    }

    @Override
    public Task combine(UpgradeContext context, List<Task> tasks, String name) {
        return new SerialAggregateTask(context, name, tasks)
    }

}
