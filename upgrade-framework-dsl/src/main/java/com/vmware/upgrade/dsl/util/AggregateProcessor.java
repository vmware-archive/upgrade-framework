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

package com.vmware.upgrade.dsl.util;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.upgrade.dsl.Processor;

/**
 * Aggregates zero or more {@link Processor} implementations.
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class AggregateProcessor implements Processor {
    private final Map<String, Closure<?>> keywordProcessors;
    private final List<Object> propertyProcessors;

    /**
     * Creates an {@link AggregateProcessor} based on the state of the delegates <strong>at
     * construction time</strong>
     *
     * @param processors
     */
    public AggregateProcessor(Processor ... processors) {
        final Map<String, Closure<?>> keywordProcessors = new HashMap<String, Closure<?>>();
        final List<Object> propertyProcessors = new ArrayList<Object>();
        for (Processor processor : processors) {
            keywordProcessors.putAll(processor.getKeywordProcessors());
            propertyProcessors.addAll(processor.getPropertyProcessors());
        }
        this.keywordProcessors = Collections.unmodifiableMap(keywordProcessors);
        this.propertyProcessors = Collections.unmodifiableList(propertyProcessors);
    }

    @Override
    public Map<String, Closure<?>> getKeywordProcessors() {
        return keywordProcessors;
    }

    @Override
    public List<?> getPropertyProcessors() {
        return propertyProcessors;
    }
}
