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

import java.util.Map;
import java.util.WeakHashMap;

import com.vmware.upgrade.UpgradeContext;

/**
 * Utility class for convenience methods related to creation of {@link UpgradeLogger} instances.
 *
 * Intended to be used by {@link UpgradeContext} implementations.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class UpgradeLoggerHelper {
    private static Map<org.apache.log4j.Logger, UpgradeLogger> cache = new WeakHashMap<org.apache.log4j.Logger, UpgradeLogger>();

    /**
     * A trivial {@link UpgradeLogger} implementation which does nothing.
     */
    public static final UpgradeLogger NO_OP_LOGGER = new UpgradeLogger() {

        @Override
        public void warn(Throwable error, String text, Object... args) {
        }

        @Override
        public void warn(String text, Object... args) {
        }

        @Override
        public void trace(Throwable error, String text, Object... args) {
        }

        @Override
        public void trace(String text, Object... args) {
        }

        @Override
        public void security(Throwable error, String text, Object... args) {
        }

        @Override
        public void security(String text, Object... args) {
        }

        @Override
        public void log(LogLevel level, Throwable error, String text, Object... args) {
        }

        @Override
        public void log(LogLevel level, String text, Object... args) {
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public boolean isSecurityEnabled() {
            return false;
        }

        @Override
        public boolean isLogLevelEnabled(LogLevel level) {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public boolean isFatalEnabled() {
            return false;
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void info(Throwable error, String text, Object... args) {
        }

        @Override
        public void info(String text, Object... args) {
        }

        @Override
        public void fatal(Throwable error, String text, Object... args) {
        }

        @Override
        public void fatal(String text, Object... args) {
        }

        @Override
        public void error(Throwable error, String text, Object... args) {
        }

        @Override
        public void error(String text, Object... args) {
        }

        @Override
        public void debug(Throwable error, String text, Object... args) {
        }

        @Override
        public void debug(String text, Object... args) {
        }
    };

    /**
     * Return a {@link UpgradeLogger} corresponding to a supplied {@link org.apache.log4j.Logger}.
     * <p>
     * When convenient, this class may re-use a {@linkplain UpgradeLogger} instance created by a
     * previous call to this method for the same {@linkplain org.apache.log4j.Logger}.
     *
     * @param delegateLogger
     * @return the wrapped {@linkplain org.apache.log4j.Logger Logger}
     */
    public static UpgradeLogger asUpgradeLogger(org.apache.log4j.Logger delegateLogger) {
        UpgradeLogger logger = cache.get(delegateLogger);
        if (logger == null) {
            logger = new UpgradeLoggerImpl(new Log4jLoggerImpl(delegateLogger));

            // Synchronization would be unnecessary here as the goal is to simply minimize
            // construction cost (and not necessarily to guarantee the return of the same
            // UpgradeLogger every time).
            cache.put(delegateLogger, logger);
        }
        return logger;
    }
}
