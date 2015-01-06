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

package com.vmware.upgrade.factory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vmware.upgrade.Task;
import com.vmware.upgrade.UpgradeContext;
import com.vmware.upgrade.UpgradeDefinition;
import com.vmware.upgrade.sequencing.Version;
import com.vmware.upgrade.task.SerialAggregateTask;

import org.apache.commons.lang.StringUtils;

/**
 * A factory which creates an {@link UpgradeDefinition} encapsulating the
 * {@link UpgradeDefinition}s produced by the delegate {@link UpgradeDefinitionFactory} instances.
 *
 * @author Ryan Lewis <ryanlewis@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class CompositeUpgradeDefinitionFactory implements UpgradeDefinitionFactory {
    /**
     * A two-tuple which associates an identifying {@link String} with an
     * {@link UpgradeDefinitionFactory}.
     */
    private static class UpgradeDefinitionFactoryRef {
        private final String name;

        private final UpgradeDefinitionFactory factory;

        public UpgradeDefinitionFactoryRef(String name, UpgradeDefinitionFactory factory) {
            this.name = name;
            this.factory = factory;
        }

        public String getName() {
            return name;
        }

        public UpgradeDefinitionFactory getFactory() {
            return factory;
        }
    }

    /**
     * Provides a filtered way to access and mutate a {@link Version}.
     */
    private interface VersionFilter {
        /**
         * Retrieve the {@link Version} in a filtered way.
         *
         * @param source The raw {@link Version}.
         * @return The filtered {@link Version}.
         */
        Version getVersion(final Version source);

        /**
         * Mutate the {@link Version} in a filtered way.
         *
         * @param source The raw {@link Version}.
         * @param element The change to make.
         * @return The {@link Version} with the mutation applied.
         */
        Version setVersion(final Version source, final Version element);

        static class KeyBasedVersionFilter implements VersionFilter {
            private final String key;

            KeyBasedVersionFilter(final String key) {
                if (key == null) {
                    throw new NullPointerException("key must be non-null");
                }

                this.key = key;
            }

            @Override
            public Version getVersion(final Version source) {
                return source.get(key);
            }

            @Override
            public Version setVersion(final Version source, final Version element) {
                return source.replace(key, element);
            }
        }

        static class IndexBasedVersionFilter implements VersionFilter {
            private final Integer index;

            IndexBasedVersionFilter(final Integer index) {
                if (index == null) {
                    throw new NullPointerException("index must be non-null");
                }

                this.index = index;
            }

            @Override
            public Version getVersion(final Version source) {
                if (source == null) {
                    return null;
                }
                return source.get(index);
            }

            @Override
            public Version setVersion(final Version source, final Version element) {
                return source.replace(index, element);
            }
        }
    }

    /**
     * A database context proxy that allows a database schema version to be filtered.
     * <p>
     * With this in place an {@link UpgradeDefinitionFactory} only manipulates the portion of the
     * overall database schema version that it represents.
     */
    private class ProxyUpgradeContext implements InvocationHandler {
        private final UpgradeContext target;
        private final VersionFilter versionFilter;

        public ProxyUpgradeContext(final UpgradeContext target, final VersionFilter versionFilter) {
            this.target = target;
            this.versionFilter = versionFilter;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getName().equals("getVersion")) {
                final Version current = (Version) method.invoke(this.target);

                return versionFilter.getVersion(current);
            } else if (method.getName().equals("setVersion")) {
                final Version current = this.target.getVersion();

                final Version newVersion = versionFilter.setVersion(
                        current,
                        (Version) args[0]
                );

                return method.invoke(this.target, newVersion);
            }

            try {
                return method.invoke(this.target, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

    private final List<UpgradeDefinitionFactoryRef> definitionFactoryRefs;

    private final boolean ordered;

    public CompositeUpgradeDefinitionFactory(final List<UpgradeDefinitionFactory> factories) throws IOException {
        final List<UpgradeDefinitionFactoryRef> factoryRefList = new ArrayList<UpgradeDefinitionFactoryRef>();

        for (UpgradeDefinitionFactory factory : factories) {
            factoryRefList.add(new UpgradeDefinitionFactoryRef(factory.toString(), factory));
        }

        this.definitionFactoryRefs = factoryRefList;
        this.ordered = true;
    }

    public CompositeUpgradeDefinitionFactory(final Map<String, UpgradeDefinitionFactory> factories) throws IOException {
        final List<UpgradeDefinitionFactoryRef> factoryRefList = new ArrayList<UpgradeDefinitionFactoryRef>();

        for (Map.Entry<String, UpgradeDefinitionFactory> entry : factories.entrySet()) {
            factoryRefList.add(new UpgradeDefinitionFactoryRef(entry.getKey(), entry.getValue()));
        }

        this.definitionFactoryRefs = factoryRefList;
        this.ordered = false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} iff all delegate {@link UpgradeDefinitionFactory}s return {@code true}
     */
    @Override
    public boolean isUpgradeSupported(final UpgradeContext context) throws IOException {
        int indexCount = 0;

        for (final UpgradeDefinitionFactoryRef factoryRef : definitionFactoryRefs) {
            final VersionFilter versionFilter;
            if (ordered) {
                versionFilter = new VersionFilter.IndexBasedVersionFilter(indexCount);
                indexCount++;
            } else {
                versionFilter = new VersionFilter.KeyBasedVersionFilter(factoryRef.getName());
            }

            final UpgradeContext filteredUpgradeContext = (UpgradeContext) Proxy.newProxyInstance(
                    UpgradeContext.class.getClassLoader(),
                    new Class[] { UpgradeContext.class },
                    new ProxyUpgradeContext(context, versionFilter)
            );

            if (!factoryRef.getFactory().isUpgradeSupported(filteredUpgradeContext)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return an {@link UpgradeDefinition} containing the union of those returned by the delegates
     */
    @Override
    public UpgradeDefinition create(final UpgradeContext context) throws IOException {
        final List<Task> upgradeTasks = new ArrayList<Task>();

        // When using an index based version filter, the indexed needs to be tracked
        int indexCount = 0;

        /*
         *  Create a task representing all of the workload for each of the definitionFactories. Each
         *  holds a SerialAggregateTask that contains all of the workload of a single
         *  UpgradeDefinitionFactory.
         */
        for (final UpgradeDefinitionFactoryRef factoryRef : definitionFactoryRefs) {
            final VersionFilter versionFilter;

            if (ordered) {
                versionFilter = new VersionFilter.IndexBasedVersionFilter(indexCount);
                indexCount++;
            } else {
                versionFilter = new VersionFilter.KeyBasedVersionFilter(factoryRef.getName());
            }

            final Object proxy = Proxy.newProxyInstance(
                    UpgradeContext.class.getClassLoader(),
                    new Class[] { UpgradeContext.class },
                    new ProxyUpgradeContext(context, versionFilter)
            );

            final UpgradeContext filteredContext = (UpgradeContext) proxy;

            final UpgradeDefinition delegateUpgradeDefinition = factoryRef.getFactory().create(filteredContext);
            final List<Task> delegateUpgradeTasks = delegateUpgradeDefinition.getUpgradeTasks();
            upgradeTasks.add(new SerialAggregateTask(filteredContext, factoryRef.getName(), delegateUpgradeTasks));
        }

        return new UpgradeDefinition() {
            @Override
            public List<Task> getUpgradeTasks() {
                return Collections.unmodifiableList(upgradeTasks);
            }
        };
    }

    @Override
    public String toString() {
        final List<String> names = new ArrayList<>();
        for (UpgradeDefinitionFactoryRef ref : definitionFactoryRefs) {
            names.add(ref.name);
        }
        return names.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @return an aggregate representation of the {@link Version}s returned by the
     *          {@linkplain UpgradeDefinitionFactory#getTargetVersion() targets}
     */
    @Override
    public Version getTargetVersion() {
        final List<String> parts = new ArrayList<String>(definitionFactoryRefs.size());

        for (UpgradeDefinitionFactoryRef factoryRef : definitionFactoryRefs) {
            final Version targetVersion = factoryRef.getFactory().getTargetVersion();

            if (targetVersion != null) {
                final String part;
                if (ordered) {
                    part = factoryRef.getFactory().getTargetVersion().toString();
                } else {
                    part = "\"" + factoryRef.getName() + "\": " + factoryRef.getFactory().getTargetVersion().toString();
                }
                parts.add(part);
            }
        }

        final String partsString = StringUtils.join(parts, ", ");
        final String fullString;
        if (ordered) {
            fullString = "[ " + partsString + " ]";
        } else {
            fullString = "{ " + partsString + " }";
        }

        return Version.lookup(fullString);
    }
}
