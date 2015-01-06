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

package com.vmware.upgrade.dsl.syntax

import com.vmware.upgrade.sequencing.Version

/**
 * An exception class for errors encountered during parsing of Upgrade Definitions and Manifests.
 *
 * @author Emil Sit <sit@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class UpgradeCompilationException extends Exception {
    private static final long serialVersionUID = 1

    static private def filterStackTrace(final StackTraceElement[] trace) {
        // Anything that's part of Java, Groovy, or the DSL implementation is not interesting.
        trace.findAll { frame ->
            ! ["com.vmware", "sun", "java", "groovy", "org.codehaus"].any { frame.className.startsWith(it) }
        }
    }

    static private def annotateMessage(s, StackTraceElement[] trace) {
        final StackTraceElement[] filteredStackTrace = filterStackTrace(trace)
        String f = filteredStackTrace?.getAt(0)?.toString()
        s + " at " + (f == null ? "unknown location" : f)
    }

    static private def annotateMessage(s) {
        annotateMessage(s, Thread.currentThread().getStackTrace())
    }

    public UpgradeCompilationException(message, Throwable cause) {
        // Make sure the message reflects where the throwable came from, not where
        // we are catching it.
        super(String.valueOf(annotateMessage(message, cause.getStackTrace())), cause)
    }

    public UpgradeCompilationException(message) {
        super(String.valueOf(annotateMessage(message)))
    }
}

class UnknownKeywordException extends UpgradeCompilationException {
    private static final long serialVersionUID = 1
    final String name
    final Object[] args

    public UnknownKeywordException(String msg) {
        super(msg)
    }

    public UnknownKeywordException(String name, Object[] args) {
        super("Unknown keyword: '${name}' with args '${args}'")
        this.name = name
        this.args = args
    }

    public UnknownKeywordException(MissingMethodException e) {
        super("Unknown keyword: '${e.method}' with args '${e.arguments}'", e)
        this.name = e.method
        this.args = e.arguments
    }
}

class DuplicateSourceException extends UpgradeCompilationException {
    private static final long serialVersionUID = 1
    final Version sourceVersion

    public DuplicateSourceException(Version sourceVersion) {
        super("Duplicate source version: '${sourceVersion}'")
        this.sourceVersion = sourceVersion
    }
}

class UnknownVariableException extends UpgradeCompilationException {
    private static final long serialVersionUID = 1
    final String name

    public UnknownVariableException(MissingPropertyException e) {
        super("Unknown variable: '${e.property}'", e)
        this.name = e.property
    }
}

class DuplicateVariableException extends UpgradeCompilationException {
    private static final long serialVersionUID = 1
    final String name

    public DuplicateVariableException(IllegalArgumentException e) {
        super("Duplicate variable: '${e.getMessage()}'")
        this.name = e.getMessage()
    }
}

class MissingFileException extends UpgradeCompilationException {
    private static final long serialVersionUID = 1
    final String name

    public MissingFileException(String f) {
        super("Missing upgrade file: '${f}'")
        this.name = f
    }
}

