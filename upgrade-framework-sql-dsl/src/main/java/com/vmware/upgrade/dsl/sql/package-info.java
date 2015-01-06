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
 * SQL-related extensions to the domain specific language for defining upgrade framework constructs.
 * <p>
 * The conceptual model for the language is described by {@link com.vmware.upgrade.dsl.sql.model} and
 * the syntax classes for constructing this model are in {@link com.vmware.upgrade.dsl.sql.syntax}.
 * <h3>A simple example</h3>
 * This example demonstrates defining an upgrade which defines a single SQL statement to be
 * executed for all database types.
 * <h4><tt>src/main/resources/upgrade/upgrade.groovy</tt></h4>
 * <pre><code>
 * foo = upgrade {
 *     name "Upgrade to do 'foo'"
 *
 *     sql """INSERT INTO config (name) VALUES ("Hello World")"""
 * }
 *
 * manifest {
 *     from "" to "1.0.0" call foo
 * }
 * </code></pre>
 * <h4>Usage</h4>
 * <pre><code>
 * static Graph load(String path) {
 *     def resourceMapper = { String name ->
 *         URL u = getClass().getResource("/upgrade/${name}")
 *         if (u == null) {
 *             throw new MissingFileException(name)
 *         }
 *         new GroovyCodeSource(u)
 *     }
 *     ManifestModel manifest = Loader.loadManifest(
 *         path,
 *         resourceMapper,
 *         new SqlTaskResolver(),
 *         new BasicSqlProcessor())
 *     manifest.name = path
 *
 *     return manifest
 * }
 *
 * Graph graph = load("upgrade.groovy")
 * </code></pre>
 * <h3>Database-dialect specific SQL example</h3>
 * This example demonstrates defining an upgrade which uses different SQL statements for each of
 * three different database types.
 * <p>
 * The first SQL statement explicitly specifies the SQL to use for each database type while the
 * second demonstrates defining a default value used for all database types for which no SQL is
 * explicitly defined.
 * <h4><tt>src/main/resources/upgrade/upgrade.groovy</tt></h4>
 * <pre><code>
 * foo = upgrade {
 *     name "Upgrade to do 'foo'"
 *
 *     sql ms_sql: """INSERT INTO config (name) VALUES ("Hello Microsoft")""",
 *         oracle: """INSERT INTO config (name) VALUES ("Hello Oracle")""",
 *         postges: """INSERT INTO config (name) VALUES ("Hello Postgres")"""
 *
 *     sql postges: """INSERT INTO config (name) VALUES ("Hello Open Source")""",
 *         default: """INSERT INTO config (name) VALUES ("Hello Proprietary")"""
 * }
 *
 * manifest {
 *     from "" to "1.0.0" call foo
 * }
 * </code></pre>
 * <h3>Leveraging {@link com.vmware.upgrade.dsl.sql.util.AgnosticSqlProcessor}</h3>
 * This example demonstrates defining an upgrade which using keywords processed by
 * {@link com.vmware.upgrade.dsl.sql.util.AgnosticSqlProcessor}.
 * <h4><tt>src/main/resources/upgrade/upgrade.groovy</tt></h4>
 * <pre><code>
 * foo = upgrade {
 *     name "Upgrade to do 'foo'"
 *
 *     create 'table1' columns {
 *         add 'id' storing id
 *         add 'moref' storing moref
 *         add 'name' storing name
 *         add 'status' string bool
 *         add 'org_id' storing id
 *     } constraints {
 *         primary 'id' and 'moref'
 *         unique 'name' and 'org_id'
 *     }
 *
 *     reference 'id' of 'organizations' from 'org_id' of 'table1'
 *
 *     comment 'This table needs a comment' on 'table1'
 *     comment '1 = running, 0 = stopped' on 'status' of 'table1'
 * }
 *
 * manifest {
 *     from "" to "1.0.0" call foo
 * }
 * </code></pre>
 * <h4>Usage</h4>
 * <pre><code>
 * static Graph load(String path) {
 *     def resourceMapper = { String name ->
 *         URL u = getClass().getResource("/upgrade/${name}")
 *         if (u == null) {
 *             throw new MissingFileException(name)
 *         }
 *         new GroovyCodeSource(u)
 *     }
 *     ManifestModel manifest = Loader.loadManifest(
 *         path,
 *         resourceMapper,
 *         new SqlTaskResolver(),
 *         new AgnosticSqlProcessor())
 *     manifest.name = path
 *
 *     return manifest
 * }
 *
 * Graph graph = load("upgrade.groovy")
 * </code></pre>
 *
 * @since 1.0
 */
package com.vmware.upgrade.dsl.sql;

