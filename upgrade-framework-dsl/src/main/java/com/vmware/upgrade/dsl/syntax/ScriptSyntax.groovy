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

package com.vmware.upgrade.dsl.syntax

import com.vmware.upgrade.dsl.Loader
import com.vmware.upgrade.dsl.Processor
import com.vmware.upgrade.dsl.TaskResolver
import com.vmware.upgrade.dsl.model.ManifestModel
import com.vmware.upgrade.dsl.model.NamespaceModel
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel
import com.vmware.upgrade.dsl.util.FinalVariableBinding
import com.vmware.upgrade.factory.CompositeUpgradeDefinitionFactory
import com.vmware.upgrade.factory.GraphUpgradeDefinitionFactory
import com.vmware.upgrade.factory.UpgradeDefinitionFactory

import org.apache.commons.lang.StringUtils

/**
 * This class handles top-level loading of a script and handling of script-level
 * {@linkplain #keywords keywords}.
 *
 * @author Emil Sit <sit@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class ScriptSyntax {
    Closure mapper
    Binding binding
    TaskResolver taskResolver
    Processor processor

    static final def keywords = ['manifest', 'namespace', 'upgrade', 'include', 'from', 'version']

    ScriptSyntax(final Closure mapper, final Binding binding, final TaskResolver taskResolver, final Processor processor) {
        this.mapper = mapper.clone() as Closure
        this.binding = binding
        this.taskResolver = taskResolver
        this.processor = processor
    }

    ManifestModel manifest(Closure cl) {
        def manifest = new ManifestSyntax()
        manifest.with cl
        manifest.manifest
    }

    NamespaceModel namespace(Closure cl) {
        def namespace = new NamespaceSyntax()
        namespace.with cl
        namespace.namespace
    }

    UpgradeDefinitionModel upgrade(Closure cl) {
        def upgrade = new UpgradeDefinitionSyntax(getPosition(), taskResolver, processor)
        upgrade.with cl
        def error = upgrade.popError()
        if (error) {
            throw new IllegalArgumentException(error)
        }
        upgrade.upgrade
    }

    void include(path) {
        def file = new File(path)
        def basedMapper = resolve(file, mapper)

        Loader.loadManifest(file.getName(), basedMapper, taskResolver, processor, binding)
    }

    /**
     * Usage: from 'path/to/resource.groovy' include 'variable'
     *
     * @return a variable from the specified resource's binding into the
     *         caller's binding
     */
    def from(path) {
        Binding nested_context = new FinalVariableBinding()

        for (Map.Entry<?,?> variable : binding.getVariables()) {
            nested_context.setVariable(variable.key, variable.value)
        }

        def file = new File(path)
        def basedMapper = resolve(file, mapper)

        Loader.loadManifest(file.getName(), basedMapper, taskResolver, processor, nested_context)

        return ["include" : { variable -> nested_context.getVariable(variable) }]
    }

    /**
     * Return a mapper which can be used to resolve other files in the same
     * directory as the {@code file}.
     *
     * A mapper simply takes a file name and returns the full path to that
     * file.
     *
     * @param file
     *          The parent directory path of this file will be used as the base
     *          for the mapper.
     * @param mapper
     *          The existing mapper closure. It should accept two args: the
     *          script name and an {@code Object[]}.
     *
     * @return The modified mapper.
     */
    def resolve(File file, mapper) {
        def base = file.getParent()

        def basedMapper = mapper

        if (!StringUtils.isBlank(base)) {
            /*
             *  Mapper's second argument is an Object[], so using ncurry will
             *  result in base being appened to the array. If rcurry was used
             *  instead, it would be prepended.
             */
            basedMapper = mapper.ncurry(1, base)
        }

        return basedMapper
    }

    /**
     * Creates an {@link UpgradeDefinitionFactory} based on specified manifests.
     * Manifests may be specified directly as a {@link ManifestModel}, or as a
     * collection ({@link List} or {@link Map}).
     */
    UpgradeDefinitionFactory version(manifests) {
        if (manifests instanceof ManifestModel) {
            return new GraphUpgradeDefinitionFactory(manifests)
        } else if (manifests instanceof List) {
            return new CompositeUpgradeDefinitionFactory(manifests.collect { version(it) })
        } else if (manifests instanceof Map) {
            return new CompositeUpgradeDefinitionFactory(manifests.collectEntries { name, manifest -> [name, version(manifest)] })
        }

        throw new IllegalArgumentException("Illegal manifest type")
    }

    def dispatchKeyword(String methodName, Object args) {
        if (methodName in keywords) {
            invokeMethod(methodName, args)
        } else {
            throw new UnknownKeywordException(methodName, args)
        }
    }

    /**
     * Search backwards through the stack to get the location of the call site
     * of the innermost DSL keyword.
     *
     * Searches for the "doCall" or "run" method names. "doCall" will match for
     * upgrade step keywords and run will match for manifest step keywords.
     *
     * @return a String of the format "file name:line number"
     */
    def getPosition() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace()
        for (StackTraceElement stackFrame : stackTraceElements) {
            if (!stackFrame.getClassName().contains("ManifestLoader")
                && (stackFrame.getMethodName().equals("doCall")
                    || stackFrame.getMethodName().equals("run"))) {
                return stackFrame.getFileName() + ":" + stackFrame.getLineNumber()
            }
        }
    }
}
