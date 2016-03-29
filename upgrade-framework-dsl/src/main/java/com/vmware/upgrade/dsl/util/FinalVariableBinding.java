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

package com.vmware.upgrade.dsl.util;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;

import java.util.HashSet;
import java.util.Set;

/**
 * This class serves as an extension of {@link Binding} which does not allow the value of a
 * variable to be changed once it is set.
 *
 * This class uses more memory than {@code Binding} as the set of variable names is duplicated.
 *
 * @author Zach Shepherd shepherdz@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class FinalVariableBinding extends Binding {
    /**
     * An explicit subclass of {@code IllegalArgumentException} to allow for more careful handling.
     */
    @SuppressWarnings("serial")
    public static final class DuplicateVariableBindingException extends IllegalArgumentException {
        private DuplicateVariableBindingException(String name) {
            super(name);
        }
    }

    /**
     * The set of variables which have been set.
     *
     * This is maintained separately because calling {@link Binding#getVariable(String)} throws an
     * {@link MissingPropertyException} if the variable is not set, which would be the common case
     * as this class would not allow setting the variable in any other case.
     */
    private final Set<String> variables = new HashSet<String>();

    /**
     * Sets the value of the given variable assuming no value has been set.
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     * @throws DuplicateVariableBindingException if the variable has already been set.
     */
    @Override
    public void setVariable(String name, Object value) {
        if (variables.contains(name)) {
            throw new DuplicateVariableBindingException(name);
        }

        variables.add(name);

        super.setVariable(name, value);
    }
}
