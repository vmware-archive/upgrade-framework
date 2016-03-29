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

package com.vmware.upgrade.logging;

import java.text.MessageFormat;

/**
 * An internal {@link UpgradeLogger} implementation leveraged by {@link UpgradeLoggerHelper}.
 *
 * @author Stephen Evanchik evanchik@vmware.com
 * @version 1.0
 * @since 1.0
 */
class UpgradeLoggerImpl implements UpgradeLogger {
    private static final String TRACE_UPGRADE_LOGGER_PREFIX = "{0}";

    private static final String DEBUG_UPGRADE_LOGGER_PREFIX = "{0}";

    private static final String INFO_UPGRADE_LOGGER_PREFIX = "{0}";


    private final Logger logger;

    /**
     * Constructor.
     *
     * @param logger
     *            the logger that all log information ultimately goes to; this
     *            cannot be {@code null}
     * @throws NullPointerException
     *             if the logger is {@code null}
     */
    public UpgradeLoggerImpl(final Logger logger) {
        if (logger == null) {
            throw new NullPointerException("logger");
        }

        this.logger = logger;
    }

    @Override
    public void debug(String text, Object... args) {
        if (logger.isDebugEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.DEBUG, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void debug(Throwable error, String text, Object... args) {
        if (logger.isDebugEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.DEBUG, error, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void error(String text, Object... args) {
        if (logger.isErrorEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.ERROR, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void error(Throwable error, String text, Object... args) {
        if (logger.isErrorEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.ERROR, error, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void fatal(String text, Object... args) {
        if (logger.isFatalEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.FATAL, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void fatal(Throwable error, String text, Object... args) {
        if (logger.isFatalEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.FATAL, error, DEBUG_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void info(String text, Object... args) {
        if (logger.isInfoEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.INFO, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void info(Throwable error, String text, Object... args) {
        if (logger.isInfoEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.INFO, error, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isLogLevelEnabled(LogLevel level) {
        return logger.isLogLevelEnabled(level);
    }

    @Override
    public boolean isSecurityEnabled() {
        return logger.isSecurityEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void log(LogLevel level, String text, Object... args) {
        if (logger.isLogLevelEnabled(level)) {
            final String userText = renderUserMessage(text, args);
            logger.log(level, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void log(LogLevel level, Throwable error, String text, Object... args) {
        if (logger.isLogLevelEnabled(level)) {
            final String userText = renderUserMessage(text, args);
            logger.log(level, error, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void security(String text, Object... args) {
        if (logger.isSecurityEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.SECURITY, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void security(Throwable error, String text, Object... args) {
        if (logger.isSecurityEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.SECURITY, error, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void trace(String text, Object... args) {
        if (logger.isTraceEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.TRACE, TRACE_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void trace(Throwable error, String text, Object... args) {
        if (logger.isTraceEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.TRACE, error, TRACE_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void warn(String text, Object... args) {
        if (logger.isWarnEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.WARN, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    @Override
    public void warn(Throwable error, String text, Object... args) {
        if (logger.isWarnEnabled()) {
            final String userText = renderUserMessage(text, args);
            logger.log(LogLevel.WARN, error, INFO_UPGRADE_LOGGER_PREFIX, userText);
        }
    }

    /**
     * Helper method that renders the user supplied message
     *
     * @param text
     *            the text of the message
     * @param args
     *            arguments to the message
     * @return the fully rendered message
     */
    private String renderUserMessage(String text, Object... args) {
        return MessageFormat.format(text, args);
    }
}
