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

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

/**
 * A log4j log level for security events.<br>
 * <br>
 * This level should be used when security related events are sent to the logs.
 * Example events include failed logins, max password retries and any other
 * event that would be helpful in a security incident response.
 *
 * @see Level
 *
 * @author Stephen Evanchik evanchik@vmware.com
 * @version 1.0
 * @since 1.0
 */
class SecurityLevel extends Level {

    private static final long serialVersionUID = 1L;

    /**
     * String token for this log level
     */
    private static final String SECURITY_STRING = "SECURITY";

    /**
     * Value of security level. This value is slightly higher than
     * {@link org.apache.log4j.Priority#INFO_INT}.
     */
    public static final int SECURITY_LEVEL_INT = Priority.INFO_INT + 500;

    /**
     * Syslog level equivalent
     */
    public static final int SECURITY_LEVEL_SYSLOG_EQUIVALENT = 5;

    /**
     * {@link Level} singleton representing security log level
     */
    public static final Level SECURITY = new SecurityLevel(SECURITY_LEVEL_INT,
            SECURITY_STRING, SECURITY_LEVEL_SYSLOG_EQUIVALENT);

    /**
     * Default constructor.
     *
     * @param level the integer value of the log level
     * @param levelString the string representation of the log level
     * @param syslogEquivalent the syslog equivalent level
     */
    protected SecurityLevel(int level, String levelString, int syslogEquivalent) {
        super(level, levelString, syslogEquivalent);

    }

    public static Level toLevel(int val) {
        if (val == SECURITY_LEVEL_INT) {
            return SECURITY;
        }

        return toLevel(val, Level.INFO);
    }

    public static Level toLevel(int val, Level defaultLevel) {
        if (val == SECURITY_LEVEL_INT) {
            return SECURITY;
        }

        return Level.toLevel(val, defaultLevel);
    }

    public static Level toLevel(String sArg) {
        if (sArg != null && sArg.toUpperCase().equals(SECURITY_STRING)) {
            return SECURITY;
        }
        return toLevel(sArg, Level.INFO);
    }

    public static Level toLevel(String sArg, Level defaultLevel) {
        if (sArg != null && sArg.toUpperCase().equals(SECURITY_STRING)) {
            return SECURITY;
        }

        return Level.toLevel(sArg, defaultLevel);
    }

}
