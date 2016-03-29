/* ****************************************************************************
 * Copyright (c) 2009-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.logging;

/**
 * <code>LogLevel</code> enumeration contains all possible log severities
 *
 * @author Vassil Popovski vpopovski@vmware.com
 * @version 1.0
 * @since 1.0
 */
public enum LogLevel {
    /**
     * very severe error events that will presumably lead the application to
     * abort.
     */
    FATAL,

    /**
     * error events that might still allow the application to continue running.
     */
    ERROR,

    /**
     * potentially harmful situations.
     */
    WARN,

    /**
     * security related events such as invalid logins
     */
    SECURITY,

    /**
     * informational messages that highlight the progress of the application at
     * coarse-grained level.
     */
    INFO,

    /**
     * fine-grained informational events that are most useful to debug an
     * application.
     */
    DEBUG,

    /**
     * finer-grained informational events than the DEBUG.
     */
    TRACE
}
