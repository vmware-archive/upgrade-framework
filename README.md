Overview
=================

The `upgrade-framework` is a product-agnostic framework for defining and sequencing upgrades.

The framework consists of four projects:
<dl>
<dt><code>upgrade-framework-core</code></dt>
<dd>Contains constructs to describe the process of performing a set of actions, represented as <code>Task</code> objects, to transition one or more persistence mechanisms, described by <code>PersistenceContext</code> objects encapsulated within an overall <code>UpgradeContext</code>, from one <code>Version</code> to another later <code>Version</code>.</dd>
<dt><code>upgrade-framework-dsl</code></dt>
<dd>Defines a domain specific language for defining upgrade framework constructs.</dd>
<dt><code>upgrade-framework-sql</code></dt>
<dd>Provides SQL-related extensions to the core framework.</dd>
<dt><code>upgrade-framework-sql-dsl</code></dt>
<dd>Provides SQL-related extensions to the DSL.</dd>
</dl>

Getting Started
===============

Building the `upgrade-framework`
--------------------------------
The `upgrade-framework` can be built using Maven. To do so from the command line, you may use `mvn install`. If you don't wish to build the source or javadoc `jar` files, you can disable the `createFullAssembly` profile by adding `-P'!createFullAssembly'` to the command.


Consuming the `upgrade-framework`
---------------------------------
Building the `upgrade-framework` using Maven will produce a set of deliverables in the `target/upgrade-framework-assembly/` directory. The build deliverables contain the `jar`, `-sources.jar`, and `-javadoc.jar` as well as a POM file for each project, structured as a Maven repository to facilitate consumption.
```
publish/com/vmware/vcloud/
└── com
    └── vmware
        └── vcloud
            ├── upgrade-framework-core
            │   └── 1.0.0
            │       ├── upgrade-framework-core.pom
            │       ├── upgrade-framework-core-1.0.0.jar
            │       ├── upgrade-framework-core-1.0.0-javadoc.jar
            │       ├── upgrade-framework-core-1.0.0-sources.jar
            │       └── upgrade-framework-core-1.0.0-tests.jar
            ├── upgrade-framework-distribution
            │   └── 1.0.0
            │       └── upgrade-framework-distribution.pom
            ├── upgrade-framework-dsl
            │   └── 1.0.0
            │       ├── upgrade-framework-dsl.pom
            │       ├── upgrade-framework-dsl-1.0.0.jar
            │       ├── upgrade-framework-dsl-1.0.0-javadoc.jar
            │       ├── upgrade-framework-dsl-1.0.0-sources.jar
            │       └── upgrade-framework-dsl-1.0.0-tests.jar
            ├── upgrade-framework-sql
            │   └── 1.0.0
            │       ├── upgrade-framework-sql.pom
            │       ├── upgrade-framework-sql-1.0.0.jar
            │       ├── upgrade-framework-sql-1.0.0-javadoc.jar
            │       ├── upgrade-framework-sql-1.0.0-sources.jar
            │       └── upgrade-framework-sql-1.0.0-tests.jar
            └── upgrade-framework-sql-dsl
                └── 1.0.0
                    ├── upgrade-framework-sql-dsl.pom
                    ├── upgrade-framework-sql-dsl-1.0.0.jar
                    ├── upgrade-framework-sql-dsl-1.0.0-javadoc.jar
                    ├── upgrade-framework-sql-dsl-1.0.0-sources.jar
                    └── upgrade-framework-sql-dsl-1.0.0-tests.jar
```

Integrating with the `upgrade-framework`
----------------------------------------
To use the framework, you must implement an `UpgradeContext`, which encapsulates the functionality specific to your application: a method for retrieving a logger suitable for use with your application, methods for accessing and mutating the `Version` of the thing being upgraded, and methods for retrieving contexts to interact with the persistence mechanisms being used.

Next, you must implement the appropriate `PersistenceContext`s, such as a `DatabasePersistenceContext`. (Aside: There should be some framework logic, such as an abstract implementation of `DatabasePersistenceContext` or a factory for producing `DatabasePersistenceContext` instances given a JDBC URL, but it's tricky to implement these in the framework without making assumptions about what JDBC drivers a consumer wants to use.)

This `UpgradeContext` is then used to retrieve an `UpgradeDefinition`, a list of `Task`s which should be run to complete an upgrade, from an `UpgradeDefinitionFactory`.

The most direct way to do this is to construct a `Graph` describing the upgrade and pass it to the `GraphUpgradeDefinitionFactory`. If support for concurrent development of sequential versions or loosely coupled components is desired, a `CompositeUpgradeDefinitionFactory` can be used as well.

Alternatively, the constructs in `upgrade-framework-dsl` can be used to express the graph in Groovy. When using the DSL to express the graph, the static factory method `Loader.createUpgradeDefinitionFactory` is used to create an `UpgradeDefinitionFactory` instance. To call that method, you must provide a reference to a script file which ends with a call to `ScriptSyntax.version` (colloquially referred to as a "master manifest" as it usually contains only inclusion of other files and that call), a resource mapper (the closure responsible for resolving includes according to your system's conventions), a DSL keyword `Processor` such as `BasicSqlProcessor`, and a `TaskResolver` such as `BasicTaskResolver` or `SqlTaskResolver`.

Thus, a consumer who wanted to use the DSL to upgrade a relational database might: implement an `UpgradeContext`, implement a `DatabasePersistenceContext`, create a master manifest file which included a single release-specific manifest file which initially just defined an empty graph, and then call into the framework from the appropriate place in their system. That code would probably use `Loader.createUpgradeDefinitionFactory` to create an `UpgradeDefinitionFactory`, obtain a distributed lock on the database (so multiple upgrades don't occur at once), retrieve an `UpgradeDefinition` for the system's `UpgradeContext`, and then iterate over the `Task`s returned by that `UpgradeDefinition`'s accessor method.
