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

/**
 * Generally applicable implementations of the {@link com.vmware.upgrade.Task} interface.
 * <p>
 * Provides three canonical abstract implementations from which most conceivable
 * {@link com.vmware.upgrade.Task} implementations could be built.
 * <dl>
 * <dt>{@link com.vmware.upgrade.task.AbstractTask}</dt>
 * <dd>A base class on which other abstract classes are based.</dd>
 * <dd>
 *   <dl>
 *     <dt>{@link com.vmware.upgrade.task.AbstractSimpleTask}</dt>
 *     <dd>A base class from which {@link com.vmware.upgrade.Task}s which perform a single logical
 *          operation can be built.</dd>
 *     <dd>
 *       <dl>
 *         <dt>{@link com.vmware.upgrade.task.TrivialTask}</dt>
 *         <dd>Wraps a {@link java.lang.Runnable} or {@link java.util.concurrent.Callable}.</dd>
 *       </dl>
 *     </dd>
 *     <dt>{@link com.vmware.upgrade.task.AbstractAggregateTask}</dt>
 *     <dd>A base class from which {@link com.vmware.upgrade.Task}s which wrap multiple
 *          {@link com.vmware.upgrade.Task}s can be built.</dd>
 *     <dd>
 *       <dl>
 *         <dt>{@link com.vmware.upgrade.task.SerialAggregateTask}</dt>
 *         <dd>Executes a {@link java.util.List} of {@link com.vmware.upgrade.Task}s
 *              sequentially.</dd>
 *         <dt>{@link com.vmware.upgrade.task.ParallelAggregateTask}</dt>
 *         <dd>Executes a {@link java.util.List} of {@link com.vmware.upgrade.Task}s in
 *              parallel.</dd>
 *       </dl>
 *     </dd>
 *     <dt>{@link com.vmware.upgrade.task.AbstractDelegatingTask}</dt>
 *     <dd>A base class from which {@link com.vmware.upgrade.Task}s which perform wrap another
 *          {@link com.vmware.upgrade.Task} (e.g. in order to add functionality) can be built.</dd>
 *   </dl>
 * </dd>
 * </dl>
 *
 * @since 1.0
 */
package com.vmware.upgrade.task;
