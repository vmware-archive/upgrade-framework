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

import java.text.MessageFormat;

import org.apache.log4j.Level;

/**
 * {@link Logger} implementation.
 *
 * @author Vassil Popovski <vpopovski@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class Log4jLoggerImpl implements Logger {
    // log4j logger
    private final org.apache.log4j.Logger logger;

    // we have to pass this to log4j, so log4j can properly identify location information
    private final static String FQCN = Log4jLoggerImpl.class.getName();

    // we use this to indicate a NULL Throwable. Used to increase the readability of the code
    private final static Throwable NO_THROWABLE = null;

    /**
     * Constructor.
     *
     * @param name the name of the logger. Name is used in configuration files
     *                to configure class/module specific logging settings
     */
    public Log4jLoggerImpl(final String name) {
        this.logger = org.apache.log4j.Logger.getLogger(name);
    }

    /**
     * Constructor used for unit testing and allowing injection of mock objects.
     *
     * @param logger mock object to be injected
     */
    protected Log4jLoggerImpl(final org.apache.log4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void fatal(final String text, final Object... args) {
        fatal(NO_THROWABLE, text, args);
    }

    @Override
    public void fatal(final Throwable error, final String text, final Object... args) {
        log(LogLevel.FATAL, error, text, args);
    }

    @Override
    public boolean isFatalEnabled() {
        return isLogLevelEnabled(LogLevel.FATAL);
    }

    @Override
    public void error(final String text, final Object... args) {
        error(NO_THROWABLE, text, args);
    }

    @Override
    public void error(final Throwable error, final String text, final Object... args) {
        log(LogLevel.ERROR, error, text, args);
    }

    @Override
    public boolean isErrorEnabled() {
        return isLogLevelEnabled(LogLevel.ERROR);
    }

    @Override
    public void info(final String text, final Object... args) {
        info(NO_THROWABLE, text, args);
    }

    @Override
    public void info(final Throwable error, final String text, final Object... args) {
        log(LogLevel.INFO, error, text, args);
    }

    @Override
    public boolean isInfoEnabled() {
        return isLogLevelEnabled(LogLevel.INFO);
    }

    @Override
    public void debug(final String text, final Object... args) {
        debug(NO_THROWABLE, text, args);
    }

    @Override
    public void debug(final Throwable error, final String text, final Object... args) {
        log(LogLevel.DEBUG, error, text, args);
    }

    @Override
    public boolean isDebugEnabled() {
        return isLogLevelEnabled(LogLevel.DEBUG);
    }

    @Override
    public void warn(final String text, final Object... args) {
        warn(NO_THROWABLE, text, args);
    }

    @Override
    public void warn(final Throwable error, final String text, final Object... args) {
        log(LogLevel.WARN, error, text, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return isLogLevelEnabled(LogLevel.WARN);
    }

    @Override
    public void security(final String text, final Object... args) {
        security(NO_THROWABLE, text, args);
    }

    @Override
    public void security(final Throwable error, final String text, final Object... args) {
        log(LogLevel.SECURITY, error, text, args);
    }

    @Override
    public boolean isSecurityEnabled() {
        return isLogLevelEnabled(LogLevel.SECURITY);
    }

    @Override
    public void trace(final String text, final Object... args) {
        trace(NO_THROWABLE, text, args);
    }

    @Override
    public void trace(final Throwable error, final String text, final Object... args) {
        log(LogLevel.TRACE, error, text, args);
    }

    @Override
    public boolean isTraceEnabled() {
        return isLogLevelEnabled(LogLevel.TRACE);
    }

    @Override
    public void log(final LogLevel level, final String text, final Object... args) {
        log(level, NO_THROWABLE, text, args);
    }

    @Override
    public void log(final LogLevel level, final Throwable error, final String text, final Object... args) {
        Level log4jLevel = convertsLogLevel(level);

        if (logger.isEnabledFor(log4jLevel)) {

            try {
                String messageToLog = MessageFormat.format(text, args);
                logger.log(FQCN, log4jLevel, messageToLog, error);
            } catch (IllegalArgumentException e){
                String messageToLog = MessageFormat.format("Cannot format message: {0}", text);
                logger.log(FQCN, Level.ERROR, messageToLog, e);
                logger.log(FQCN, log4jLevel, text, error);
            }
        }
    }

    @Override
    public boolean isLogLevelEnabled(final LogLevel level) {
        return logger.isEnabledFor(convertsLogLevel(level));
    }

    private Level convertsLogLevel(final LogLevel level) {
        switch (level) {
        case FATAL:
            return Level.FATAL;
        case ERROR:
            return Level.ERROR;
        case WARN:
            return Level.WARN;
        case SECURITY:
            return SecurityLevel.SECURITY;
        case INFO:
            return Level.INFO;
        case DEBUG:
            return Level.DEBUG;
        case TRACE:
            return Level.TRACE;
        }
        return Level.ALL;
    }
}
