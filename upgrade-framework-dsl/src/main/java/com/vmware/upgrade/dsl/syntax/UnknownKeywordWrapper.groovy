/* ****************************************************************************
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
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

/**
 * A wrapper for DSL closures to provide improved error handling for invalid keywords.
 * Wrapped delegates will throw an {@link UnknownKeywordException} (with a message noting
 * the unknown keyword) when an undefined keyword is encountered. This functions as an
 * improvement over the default behavior of a generic {@link MissingMethodException}.
 * <p>
 * The improved error handling applies only if the delegate is a map [this wrapper is safe
 * to use if delegate is <i>not</i> a map, but it will not provide any benefit].
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class UnknownKeywordWrapper {
    private Map<String, Closure<?>> keywords

    static def wrap(delegate) {
        return (delegate instanceof Map) ? new UnknownKeywordWrapper(delegate) :  delegate
    }

    private def UnknownKeywordWrapper(Map<String, Closure<?>> keywords) {
        this.keywords = keywords
    }

    def methodMissing(String name, args) {
        if (name in keywords.keySet()) {
            def processor = keywords.get(name)
            processor.delegate = this
            return wrap(processor.call(*args))
        } else {
            throw new UnknownKeywordException("Unknown keyword: " + name)
        }
    }
}
