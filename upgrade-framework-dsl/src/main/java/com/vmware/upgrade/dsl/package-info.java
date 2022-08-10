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

/**
 * The core domain specific language for defining upgrade framework constructs.
 * <p>
 * The purpose of this language is to allow product developers to easily define and describe the
 * upgrade process in a way that facilitates incremental development of the upgrade in lock-step
 * with implementation of the functionality requiring the upgrade.
 * <p>
 * The conceptual model for the language is described by {@link com.vmware.upgrade.dsl.model} and
 * the syntax classes for constructing this model are in {@link com.vmware.upgrade.dsl.syntax}.
 * <p>
 * This package contains the objects a consumer of the language will need to directly interface
 * with. com.vmware.upgrade.dsl.Loader is the primary entry point for consuming a script.
 * Implementations of {@link com.vmware.upgrade.dsl.Processor} and
 * {@link com.vmware.upgrade.dsl.TaskResolver} are used to extend the language and control the
 * {@link com.vmware.upgrade.Task}s which are produced.
 * <h3>A single-file example</h3>
 * This example demonstrates defining an upgrade and sequencing it in a manifest in a single file.
 * <h4><code>src/main/resources/upgrade/upgrade.groovy</code></h4>
 * <pre><code>
 * foo = upgrade {
 *     name "Upgrade to do 'foo'"
 *
 *     java "com.example.upgrade.Foo"
 * }
 *
 * manifest {
 *     from "" to "1.0.0" call foo
 * }
 * </code></pre>
 * <h4>Usage</h4>
 * <pre><code>
 * static Graph load(String path) {
 *     def resourceMapper = { String name -&gt;
 *         URL u = getClass().getResource("/upgrade/${name}")
 *         if (u == null) {
 *             throw new MissingFileException(name)
 *         }
 *         new GroovyCodeSource(u)
 *     }
 *     ManifestModel manifest = Loader.loadManifest(
 *         path,
 *         resourceMapper,
 *         new BasicTaskResolver(),
 *         new NoopProcessor())
 *     manifest.name = path
 *
 *     return manifest
 * }
 *
 * Graph graph = load("upgrade.groovy")
 * </code></pre>
 * <h3>A multi-file example</h3>
 * This example demonstrates defining an upgrade within a namespace in a file included from a
 * separate file which defines a manifest which sequences the upgrade.
 * <h4><code>src/main/resources/upgrade/baz.groovy</code></h4>
 * <pre><code>
 * bar = namespace {
 *     foo = upgrade {
 *         name "Upgrade to do 'foo'"
 *
 *         java "com.example.upgrade.Foo"
 *     }
 * }
 * </code></pre>
 * <h4><code>src/main/resources/upgrade/manifest.groovy</code></h4>
 * <pre><code>
 * import "baz.groovy"
 *
 * manifest {
 *     from "" to "1.0.0" call bar.foo
 * }
 * </code></pre>
 * <h4>Usage</h4>
 * <pre><code>
 * Graph graph = load("manifest.groovy") // as defined above
 * </code></pre>
 * <h3>A manifest file for a complex graph</h3>
 * This example demonstrates the ability defining a set of upgrades to form a non-trivial graph.
 * <h4><code>src/main/resources/upgrade/manifest.groovy</code></h4>
 * <pre><code>
 * import "upgrades.groovy"
 *
 * manifest {
 *     from "" to "1.0.0" call upgrades.initial
 *     from "1.0.0" /* to "1.0.1" *&#47; call upgrades.incrementalChange
 *     from "1.0.1" /* to "1.0.2" *&#47; call upgrades.anotherIncrementalChange
 *  // from "1.0.2" /* to "1.0.3" *&#47; call upgrades.aBrokenUpgrade
 *     from "1.0.2" to "1.0.4" call upgrades.aFixedUpgrade
 *     from "1.0.3" /* to "1.0.4" *&#47; call upgrades.aFixForTheBrokenUpgrade
 *  // from "1.0.4" /* to "1.0.5" *&#47; call upgrades.firstHalfOfAnOptimizableUpgrade
 *     from "1.0.4" to "1.0.5.optimization" call upgrades.combinedOptimizabledUpgrade
 *     from "1.0.5" /* to "1.0.6" *&#47; call upgrades.unrelatedUpgrade
 *     from "1.0.5.optimization" to "1.0.7" call upgrades.unrelatedUpgrade
 *     from "1.0.6" /* to "1.0.7" *&#47; call upgrades.secondHalfOfAnOptimizableUpgrade
 * }
 * </code></pre>
 * <h4>Usage</h4>
 * <pre><code>
 * Graph graph = load("manifest.groovy") // as defined above
 * </code></pre>
 * <h3>An upgrade definition with complex orchestration</h3>
 * This example demonstrates the ability to define an upgrade that explicitly controls the way in
 * which a set of {@link com.vmware.upgrade.Task}s are executed.
 * <h4><code>src/main/resources/upgrade/upgrade.groovy</code></h4>
 * <pre><code>
 * foo = upgrade {
 *     name "Run a complex set of tasks"
 *
 *     java "com.example.upgrade.First"
 *     parallel {
 *         java "com.example.upgrade.Foo"
 *         java "com.example.upgrade.Bar"
 *         serial {
 *             java "com.example.upgrade.Baz.Beginning"
 *             parallel {
 *                 java "com.example.upgrade.Baz.Planets"
 *                 java "com.example.upgrade.Baz.Moons"
 *                 java "com.example.upgrade.Baz.Stars"
 *             }
 *             java "com.example.upgrade.Baz.Ending"
 *         }
 *     }
 *     java "com.example.upgrade.Last"
 * }
 * </code></pre>
 *
 * @since 1.0
 */
package com.vmware.upgrade.dsl;
