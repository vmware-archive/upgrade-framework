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

package com.vmware.upgrade.dsl.util

import com.vmware.upgrade.dsl.Processor
import com.vmware.upgrade.dsl.TaskResolver
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel
import com.vmware.upgrade.dsl.syntax.ScriptSyntax
import com.vmware.upgrade.dsl.syntax.UpgradeDefinitionSyntax

import org.codehaus.groovy.GroovyException

/**
 * A test utility for loading upgrade definitions.
 *
 * @author Emil Sit sit@vmware.com
 * @version 1.0
 * @since 1.0
 */
class UpgradeLoader {
    /**
     * Parse a string as script and return an UpgradeModel.
     * This class is intended for use in testing only.
     *
     * @param script a String representing the body of an upgrade
     * @return the UpgradeModel from the provided script
     */
    static UpgradeDefinitionModel loadInline(String script, TaskResolver taskResolver, Processor processor) {
        def upgradeSyntax = new UpgradeDefinitionSyntax(taskResolver, processor)

        load(script, upgradeSyntax)

        def upgradeModel = upgradeSyntax.upgrade

        if (upgradeModel instanceof UpgradeDefinitionModel) {
            return upgradeModel
        }
        return null
    }

    /**
     * Parse a string as a complete upgrade definition (including {@code upgrade &#123; ... &#125;})
     * This class is intended for use in testing only.
     *
     * @param script a String representing a complete upgrade definition
     * @return the UpgradeModel from the provided script
     */
    static UpgradeDefinitionModel loadDefinitionInline(String script, TaskResolver taskResolver, Processor processor) {
        def scriptSyntax = new ScriptSyntax({ it -> it }, new FinalVariableBinding(), taskResolver, processor)

        def parsedUpgrade = load(script, scriptSyntax)

        if (parsedUpgrade instanceof UpgradeDefinitionModel) {
            return parsedUpgrade
        }
        return null
    }

    /**
     * Compiles {@code script} as groovy code and uses {@code syntax} as a
     * source for methods to invoke.
     *
     * @param script groovy source code
     * @param syntax source of methods to invoke on {@code script}
     * @return a resulting object as defined in {@code syntax}
     * @throws GroovyException if an exception occurs while executing
     *         {@code script}
     */
    private static def load(script, syntax) {
        def source = new GroovyCodeSource(script, "customScript", "inline.groovy")

        def raw = new groovy.lang.GroovyClassLoader().parseClass(source).newInstance(new Binding()) as Script

        raw.metaClass.methodMissing = { name, args -> syntax.invokeMethod(name, args) }

        try {
            return raw.run()
        } catch(GroovyException e) {
            println e
            throw e
        }
    }


}
