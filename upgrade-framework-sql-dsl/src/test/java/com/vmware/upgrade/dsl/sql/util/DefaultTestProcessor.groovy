
/* ****************************************************************************
 * Copyright (c) 2015 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.sql.util

import static com.vmware.upgrade.dsl.sql.syntax.ColumnType.*

/**
 * A {@link Processor} for testing purposes.
 *
 * @author Matthew Frost mfrost@vmware.com
 * @version 1.0
 * @since 1.0
 */
class DefaultTestProcessor extends AgnosticSqlProcessor {
    List<Object> propertyProcessors = [new AdditionalColumnTypes()]

    @Override
    public Map<String, Closure<?>> getKeywordProcessors() {
        return super.getKeywordProcessors()
    }

    @Override
    public List<?> getPropertyProcessors() {
        return super.getPropertyProcessors() + propertyProcessors
    }

    class AdditionalColumnTypes {
        public static def TEST_VARCHAR = VARCHAR(128)

        public MetaProperty hasProperty(String name) {
            return ClassUtil.hasField(AdditionalColumnTypes.class, name)
        }

        def propertyMissing(String name) {
            try {
                return Type.valueOf(name)
            } catch (IllegalArgumentException e) {
                throw new com.vmware.upgrade.dsl.syntax.UnknownKeywordException("Unknown column type '${name}'")
            }
        }
    }
}
