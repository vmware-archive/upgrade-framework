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
 * Core interface for logging. All vCloud services must use this interface for
 * all logging purposes.
 *
 * @author Vassil Popovski <vpopovski@vmware.com>
 * @version 1.0
 * @since 1.0
 */
interface Logger {

    /**
     * Logs a fatal event, along with some arguments.
     *
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void fatal(String text, Object... args);

    /**
     * Logs a fatal event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */

    void fatal(Throwable error, String text, Object... args);

    /**
     * Checks whether FATAL log level is enabled.
     *
     * @return true if FATAL log level is enabled, false otherwise
     */
    boolean isFatalEnabled();

    /**
     * Logs an error event, along with some arguments.
     *
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void error(String text, Object... args);

    /**
     * Logs a error event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void error(Throwable error, String text, Object... args);

    /**
     * Checks whether ERROR log level is enabled.
     *
     * @return true if ERROR log level is enabled, false otherwise
     */
    boolean isErrorEnabled();

    /**
     * Logs a warn event, along with some arguments.
     *
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void warn(String text, Object... args);

    /**
     * Logs a warn event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void warn(Throwable error, String text, Object... args);

    /**
     * Checks whether WARN log level is enabled.
     *
     * @return true if WARN log level is enabled, false otherwise
     */
    boolean isWarnEnabled();

    /**
     * Logs a security event, along with some arguments.
     *
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void security(String text, Object... args);

    /**
     * Logs a security event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void security(Throwable error, String text, Object... args);

    /**
     * Checks whether SECURITY log level is enabled.
     *
     * @return true if SECURITY log level is enabled, false otherwise
     */
    boolean isSecurityEnabled();

    /**
     * Logs an info event, along with some arguments.
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void info(String text, Object... args);

    /**
     * Logs an info event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void info(Throwable error, String text, Object... args);

    /**
     * Checks whether INFO log level is enabled.
     *
     * @return true if INFO log level is enabled, false otherwise
     */
    boolean isInfoEnabled();

    /**
     * Logs a debug event, along with some arguments.
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void debug(String text, Object... args);

    /**
     * Logs a debug event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void debug(Throwable error, String text, Object... args);

    /**
     * Checks whether DEBUG log level is enabled.
     *
     * @return true if DEBUG log level is enabled, false otherwise
     */
    boolean isDebugEnabled();

    /**
     * Logs a trace event, along with some arguments.
     *
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void trace(String text, Object... args);

    /**
     * Logs a trace event, along with some arguments.
     *
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void trace(Throwable error, String text, Object... args);

    /**
     * Checks whether TRACE log level is enabled.
     *
     * @return true if TRACE log level is enabled, false otherwise
     */
    boolean isTraceEnabled();

    /**
     * Logs an event with specified log level, along with some arguments.
     *
     * @param level the log level
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void log(LogLevel level, String text, Object... args);

    /**
     * Logs an event with specified log level, along with some arguments.
     *
     * @param level the log level
     * @param error Throwable to log
     * @param text message of the event
     * @param args arguments that will be replaced in the <code>text</code> as
     *                specified in {@link java.text.MessageFormat}
     */
    void log(LogLevel level, Throwable error, String text, Object... args);

    /**
     * Checks if given log level is enabled or not.
     *
     * @param level log level to check
     * @return true if given log level is enabled, false otherwise
     */
    boolean isLogLevelEnabled(LogLevel level);
}
