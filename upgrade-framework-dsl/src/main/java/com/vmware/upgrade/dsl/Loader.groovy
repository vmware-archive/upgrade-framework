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

package com.vmware.upgrade.dsl

import com.vmware.upgrade.dsl.model.ManifestModel
import com.vmware.upgrade.dsl.syntax.DuplicateVariableException
import com.vmware.upgrade.dsl.syntax.ScriptSyntax
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException
import com.vmware.upgrade.dsl.syntax.UnknownVariableException
import com.vmware.upgrade.dsl.syntax.UpgradeCompilationException
import com.vmware.upgrade.dsl.util.FinalVariableBinding
import com.vmware.upgrade.dsl.util.FinalVariableBinding.DuplicateVariableBindingException
import com.vmware.upgrade.factory.UpgradeDefinitionFactory

import org.codehaus.groovy.GroovyException

/**
 * Main entry point for loading upgrade manifests and definitions.
 *
 * @author Emil Sit <sit@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class Loader {
    /**
     * Load a manifest file and return the parsed manifest, if one is present.
     * Notably, a manifest file that only defines upgrades will return null. The results may
     * still potentially be accessed via binding, if one is provided.  This use case is
     * intended for recursive invocations to include other files.
     *
     * @see ScriptSyntax
     * @see ManifestSyntax
     * @see NamespaceSyntax
     * @see UpgradeSyntax
     *
     * @param source the file/resource path to load
     * @param mapper a closure that translates source to something suitable for {@link GroovyClassLoader#parseClass}
     * @param binding a {@link Binding} (optional) for recursive calls
     * @return the last manifest parsed from source or null if none parsed.
     * @throws UpgradeCompilationException if there is an error in the source file
     */
    static ManifestModel loadManifest(source, mapper, taskResolver, processor, binding = new FinalVariableBinding()) throws UpgradeCompilationException {
        def parsedManifest = loadScript(source, mapper, taskResolver, processor, binding)

        if (parsedManifest instanceof ManifestModel) {
            return parsedManifest
        }

        return null
    }

    /**
     * Load a master manifest and return the instantiated {@link UpgradeDefinitionFactory}.
     *
     * @see Loader#loadManifest()
     */
    static UpgradeDefinitionFactory createUpgradeDefinitionFactory(source, mapper, taskResolver, processor, binding = new FinalVariableBinding()) throws UpgradeCompilationException {
        def parsedFactory = loadScript(source, mapper, taskResolver, processor, binding)

        if (parsedFactory instanceof UpgradeDefinitionFactory) {
            return parsedFactory
        }

        return null
    }

    private static def loadScript(source, mapper, taskResolver, processor, binding) {
        def parsedScript

        def rawScript = new groovy.lang.GroovyClassLoader().parseClass(mapper(source)).newInstance(binding) as Script

        def dsl = new ScriptSyntax(mapper, binding, taskResolver, processor)
        rawScript.metaClass.methodMissing = { name, args -> dsl.dispatchKeyword(name, args) }

        try {
            parsedScript = rawScript.run()
        } catch (MissingMethodException e) {
            // This catches MME's from Java classes such as a LinkedHashMap which we use
            // in UpgradeSyntax.  There doesn't seem to be a great way to inject a
            // missingMethod into the LHM at that point. e.g., GROOVY-3867
            throw new UnknownKeywordException(e)
        } catch (MissingPropertyException e) {
            // This catches MPE's from when we refer to a variable that is not a property
            // or binding (or caught somewhere else).
            throw new UnknownVariableException(e)
        } catch (DuplicateVariableBindingException e) {
            // This catches IAE's from FinalVariableBinding
            throw new DuplicateVariableException(e)
        } catch (GroovyException e) {
            println e
            throw e
        }

        return parsedScript
    }

}
