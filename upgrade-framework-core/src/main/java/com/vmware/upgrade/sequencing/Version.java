/* ****************************************************************************
 * Copyright (c) 2012-2015 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.sequencing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.upgrade.UpgradeDefinition;

import org.apache.commons.lang.StringUtils;

/**
 * This class describes a (potentially complex) version.
 * <p>
 * {@link Version}s are used to uniquely identify the state of the target of the upgrade framework;
 * {@link UpgradeDefinition}s are constructed from the directed graph of version transition
 * information and represent a path through that graph from the current {@link Version} to the
 * highest reachable {@link Version}.
 * <p>
 * In the simplest case, a {@link Version} can act like a {@link org.osgi.framework.Version}. This
 * case is appropriate when a single number can uniquely and globally describe the overall state.
 * <p>
 * To facilitate introduction of new edges within the overall upgrade graph, {@link Version} can
 * also describe both dependent and independent sub-graphs. Dependent sub-graphs allow edges to be
 * easily added at a point within the graph and provide the guarantee that transitions defined in
 * a graph will always be made prior to transitions in a subsequent graph. Such sub-graphs can be
 * used to evolve an upgrade process for multiple releases in parallel. Independent sub-graphs
 * behave similarly, but without any guarantees about order of traversal, and serve a different
 * purpose; they can be used to evolve the upgrade process for multiple unrelated components
 * without introducing a common point where changes to those components must be serialized.
 * <p>
 * Note: While an upgrade is in progress, the upgrade code is expected to set
 * a transition version for which {@link #isTransition()} is true.  Internally
 * this is a {@code Version} whose qualifier contains the string "transition".
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public class Version implements Comparable<Version> {
    /**
     * The database version representing the initial state of a manifest before
     * any upgrade definitions from it have been executed.
     */
    private static final String INITIAL_VALUE = "0.0.0";
    public static final Version INITIAL = lookup(INITIAL_VALUE);

    private final Component<?> version;


    /**
     * The fundamental version building block.
     * <p>
     * A schema version consists of one or more component versions which can linked and/or nested
     * within each other allowing for as much customization/complexity as necessary.
     */
    protected interface Component<T> extends Comparable<Component<?>> {
        /**
         * Return the underlying component version implementation.
         */
        T get();

        /**
         * If the underlying implementation is indexed, return the {@link Component} at the
         * specified location.
         *
         * @param index
         * @return {@link Component} at {@code index}
         */
        Component<?> get(int index);

        /**
         * If the underlying implementation is associative, return the {@link Component} at the
         * specified location.
         *
         * @param key
         * @return {@link Component} mapped to {@code key}
         */
        Component<?> get(String key);

        /**
         * If the underlying implementation is indexed, return a new {@link Component} with the
         * {@link Component} at the specified index replaced.
         *
         * @param index
         * @param element
         * @return a new {@link Component}
         */
        Component<?> replace(int index, Version element);

        /**
         * If the underlying implementation is associative, return a new {@link Component} with the
         * {@link Component} at the specified key replaced.
         *
         * @param key
         * @param element
         * @return a new {@link Component}
         */
        Component<?> replace(String key, Version element);

        /**
         * Return true if the {@link Component} represents a transition-state version.
         */
        boolean isTransition();

        /**
         * Compare this {@link Component} to another.
         * <p>
         * Each component version in this and the other {@link Component}
         * are compared according to {@link Component#compareTo(Object)}. In the case
         * that each component at a valid index for both {@link Component}s
         * are equal, then their number of components are compared.
         * <p>
         * This {@link Component} is considered to be <b>less than</b>
         * another if, when iterating through component versions,
         * {@link Component#compareTo(Object)} returns an integer less than zero, or
         * if all comparable component versions are equal, this
         * {@link Component}'s number of component versions is smaller.
         * <p>
         * This {@link Component} is considered to be <b>equal to</b>
         * another if all component versions are equal in value and quantity.
         * <p>
         * This {@link Component} is considered to be <b>greater than</b>
         * another if, when iterating through component versions,
         * {@link Component#compareTo(Object)} returns an integer greater than zero,
         * or if all comparable component versions are equal, this
         * {@link Component}'s number of component versions is larger.
         *
         * @return A negative integer, zero, or a positive integer if this
         *         {@link Component} is less than, equal to, or greater than
         *         the specified {@link Component}.
         */
        @Override
        int compareTo(Component<?> other);
    }

    /**
     * Factory used to create a new {@link Component} based on a JSON string.
     *
     * @see #parse(String)
     */
    protected static class ComponentFactory {
        /**
         * Create a new {@link Component} based on a JSON string representation of a schema version.
         *
         * @param version a JSON version
         * @return a new {@link Component}
         * @throws JsonParseException if the version can not be parsed
         * @throws IOException
         */
        public static Component<?> parse(String version) throws JsonParseException, IOException {
            if (version.isEmpty()) {
                return VersionComponent.parse(version);
            }

            if (!version.contains(",") && !(version.startsWith("\"") || version.startsWith("[") || version.startsWith("{"))) {
                version = "\"" + version + "\"";;
            } else if (version.lastIndexOf(",") == version.indexOf(",") && version.indexOf(",") != -1 && !(version.startsWith("[") || version.startsWith("{"))) {
                String[] v = version.split(",");
                version = "[\"" + v[0] + "\"," + "\"" + v[1] + "\" ]";
            } else {
                // Accept version as is
            }

            final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
            final JsonParser jp = jsonFactory.createParser(version);
            jp.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

            final JsonToken token = jp.nextToken();

            final JsonNode node = jp.readValueAsTree();

            switch(token) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    return VersionComponent.parse(version);
                case VALUE_STRING:
                    return VersionComponent.parse(node.asText());
                case START_OBJECT:
                    return MapComponent.parse(jsonFactory, node.toString());
                case START_ARRAY:
                    return ListComponent.parse(jsonFactory, node.toString());
                default:
                    throw new IllegalArgumentException("Unsupported version JSON");
            }
        }
    }

    protected static final class VersionComponent implements Component<org.osgi.framework.Version> {
        private final org.osgi.framework.Version component;

        private VersionComponent(org.osgi.framework.Version component) {
            this.component = component;
        }

        @Override
        public org.osgi.framework.Version get() {
            return component;
        }

        @Override
        public Component<?> get(int index) {
            if (index > 0) {
                return VersionComponent.parse(INITIAL_VALUE);
            } else if (index == 0) {
                return this;
            }

            throw new UnsupportedOperationException("can not call get(index) where index < 0 (actual = " + index + ")");
        }

        @Override
        public Component<?> get(String key) {
            return this;
        }

        @Override
        public Component<?> replace(int index, Version element) {
            /*
             * If replace has been called with a index greater than 0, then a list is expected but
             * doesn't yet exist, so create it and append the new element.
             */
            if (index > 0) {
                return new ListComponent(this, element.version);
            }
            return element.version;
        }

        @Override
        public Component<?> replace(String key, Version element) {
            /*
             * If replace has been called with a key and the current version is INITIAL, then
             * it means a new map component has been added to the schema version, so create one.
             */
            if (component.toString().equals(INITIAL_VALUE)) {
                try {
                    return ComponentFactory.parse("{ \"" + key + "\": " + element.toString() + " }");
                } catch (IOException e) {
                    throw new RuntimeException("Error adding new manifest version (map): unable to parse");
                }
            }

            return element.version;
        }

        public static VersionComponent parse(String component) throws IllegalArgumentException {
            return new VersionComponent(org.osgi.framework.Version.parseVersion(component));
        }

        public static VersionComponent parse(org.osgi.framework.Version component) throws IllegalArgumentException {
            return new VersionComponent(component);
        }

        @Override
        public boolean isTransition() {
            if (component.getQualifier().toLowerCase().contains("transition")) {
                return true;
            }

            return false;
        }

        @Override
        public int compareTo(Component<?> other) {
            if (!(other instanceof VersionComponent)) {
                throw new IllegalArgumentException("Can not compare a VersionComponent with a " + other.getClass().getSimpleName());
            }

            org.osgi.framework.Version otherVersion = ((VersionComponent) other).get();

            return component.compareTo(otherVersion);
        }

        @Override
        public boolean equals(Object obj) {
            return component.equals(((VersionComponent) obj).component);
        }

        @Override
        public int hashCode() {
            return component.hashCode();
        }

        @Override
        public String toString() {
            return "\"" + component.toString() + "\"";
        }
    }

    protected static final class ListComponent implements Component<List<Component<?>>> {
        private final List<Component<?>> component;

        private ListComponent(List<Component<?>> component) {
            this.component = component;
        }

        private ListComponent(Component<?>... component) {
            this.component = new ArrayList<>();
            this.component.addAll(Arrays.asList(component));

        }

        @Override
        public List<Component<?>> get() {
            return component;
        }

        @Override
        public Component<?> get(int index) {
            if (index >= component.size()) {
                return VersionComponent.parse(INITIAL_VALUE);
            }

            return component.get(index);
        }

        @Override
        public Component<?> get(String key) {
            throw new UnsupportedOperationException("can not call get(key) on ListComponent");
        }

        @Override
        public Component<?> replace(int index, Version element) {
            if (index == component.size()) {
                component.add(element.version);
            } else if (index < component.size()) {
                component.set(index, element.version);
            } else {
                final String msg = "Cannot specify an index of %s for '%s'";
                throw new IllegalArgumentException(String.format(msg, index, component));
            }

            return new ListComponent(component);
        }

        @Override
        public Component<?> replace(String key, Version element){
            throw new UnsupportedOperationException("can not call replace(key, element) on ListComponent");
        }

        public static ListComponent parse(JsonFactory jsonFactory, String component) throws JsonParseException, IOException {
            final JsonParser jp = jsonFactory.createParser(component);
            final List<Component<?>> versionList = new ArrayList<Component<?>>();

            jp.nextToken();

            while (jp.nextValue() != JsonToken.END_ARRAY) {
                final JsonNode node = jp.readValueAsTree();

                versionList.add(ComponentFactory.parse(node.toString()));
            }

            return new ListComponent(versionList);
        }

        @Override
        public boolean isTransition() {
            for (Component<?> c : component) {
                if (c.isTransition()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int compareTo(Component<?> other) {
            if (!(other instanceof ListComponent)) {
                throw new IllegalArgumentException("Can not compare a ListComponent with a " + other.getClass().getSimpleName());
            }
            if (component == other) {
                return 0;
            }

            final ListComponent otherList = (ListComponent) other;

            int compareResult = 0;
            for (int i = 0; i < Math.max(component.size(), otherList.get().size()); i++) {
                final int componentCompareResult = compare(get(i), otherList.get(i));
                if (compareResult == 0) {
                    compareResult = componentCompareResult;
                } else if (compareResult > 0 && componentCompareResult < 0 ||
                        compareResult < 0 && componentCompareResult > 0 ) {
                    final String msg = "Bad version value, cannot compare %s to %s";
                    throw new IllegalArgumentException(String.format(msg, component, otherList));

                }
            }
            return compareResult;
        }

        @Override
        public boolean equals(Object obj) {
            return component.equals(((ListComponent) obj).component);
        }

        @Override
        public int hashCode() {
            return component.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            String delim = "";

            for (Component<?> c : component) {
                sb.append(delim).append(c);
                delim = ", ";
            }

            return "[ " + sb.toString() + " ]";
        }
    }

    protected static final class MapComponent implements Component<Map<String, Component<?>>> {
        private final Map<String, Component<?>> component;

        private MapComponent(Map<String, Component<?>> component) {
            this.component = component;
        }

        @Override
        public Map<String, Component<?>> get() {
            return component;
        }

        @Override
        public Component<?> get(int index) {
            throw new UnsupportedOperationException("can not call get(index)");
        }

        @Override
        public Component<?> get(String key) {
            if (!component.containsKey(key)) {
                // Initial case - when a new project manifest has been added to an existing version map
                return VersionComponent.parse(INITIAL_VALUE);
            }

            return component.get(key);
        }

        @Override
        public Component<?> replace(int index, Version element) {
            throw new UnsupportedOperationException("can not call replace(index, element) on MapComponent");
        }

        @Override
        public Component<?> replace(String key, Version element){
            component.put(key, element.version);

            return new MapComponent(component);
        }

        public static MapComponent parse(JsonFactory jsonFactory, String component) throws JsonParseException, IOException {
            final JsonParser jp = jsonFactory.createParser(component);
            final Map<String, Component<?>> versionMap = new LinkedHashMap<String, Component<?>>();

            jp.nextToken();

            while (jp.nextValue() != JsonToken.END_OBJECT) {
                final JsonNode node = jp.readValueAsTree();

                versionMap.put(jp.getCurrentName(), ComponentFactory.parse(node.toString()));
            }

            return new MapComponent(versionMap);
        }

        @Override
        public boolean isTransition() {
            for (Map.Entry<String, Component<?>> entry : component.entrySet()) {
                if (entry.getValue().isTransition()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int compareTo(Component<?> other) {
            if (!(other instanceof MapComponent)) {
                throw new IllegalArgumentException("Can not compare a MapComponent with a " + other.getClass().getSimpleName());
            }

            if (component == other) {
                return 0;
            }

            final Map<String, Component<?>> otherMap = ((MapComponent) other).get();

            for (String key : component.keySet()) {
                if (!otherMap.containsKey(key)) {
                    throw new IllegalArgumentException("Comparing MapComponent does not contain key: " + key);
                }

                final int compareResult = component.get(key).compareTo(otherMap.get(key));

                if (compareResult != 0) {
                    if ((compareResult > 0 && component.size() < otherMap.size()) ||
                            (compareResult < 0 && component.size() > otherMap.size())) {
                        final String msg = "Bad version value, cannot compare %s to %s";
                        throw new IllegalArgumentException(String.format(msg, component, otherMap));
                    } else {
                        return compareResult;
                    }
                }
            }

            if (component.size() != otherMap.size()) {
                return component.size() - otherMap.size();
            }

            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return component.equals(((MapComponent) obj).component);
        }

        @Override
        public int hashCode() {
            return component.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            String delim = "";

            for (Map.Entry<String, Component<?>> entry : component.entrySet()) {
                sb.append(delim).append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
                delim = ", ";
            }

            return "{ " + sb.toString() + " }";
        }
    }

    protected Version(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        try {
            this.version = ComponentFactory.parse(value);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Bad version value", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Bad version value", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("value is not a json array", e);
        }
    }

    protected Version(org.osgi.framework.Version version) {
        this.version = VersionComponent.parse(version);
    }

    protected Version(Component<?> version) {
        this.version = version;
    }

    protected Component<?> getRawVersion() {
        return this.version;
    }

    /**
     * Returns the {@link Version} corresponding to the given string value.
     *
     * @param value a string representation of the {@link Version}
     * @return a {@link Version} representing value, if the value is valid. Otherwise, {@code null}
     */
    public static Version lookup(String value) {
        try {
            return new Version(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Indicates whether the {@link Version} represents a version where an upgrade is in progress.
     *
     * @return {@code true} if any of the {@link Version}'s components are in a transition state.
     */
    public final boolean isTransition() {
        return version.isTransition();
    }

    /**
     * Get the next {@link Version}.
     *
     * @return the next {@link Version} if this {@link Version} represents a single version
     * @throws UnsupportedOperationException if this does not represent a single version
     */
    public Version getNext() {
        if (!(version instanceof VersionComponent)) {
            throw new UnsupportedOperationException("Version does not represent a single version");
        }

        final org.osgi.framework.Version currentVersion = (org.osgi.framework.Version) version.get();

        return new Version(new org.osgi.framework.Version(currentVersion.getMajor(), currentVersion.getMinor(), currentVersion.getMicro() + 1, currentVersion.getQualifier()));
    }

    /**
     * Return the transition form of the current {@link Version} based on the {@code destination}.
     * <p>
     * Examples:
     * <ul>
     * <li>if the current schema version is {@code 1.0.0} and
     * {@code destination} equals {@code 2.0.0}, this will return
     * {@code 1.0.0.transition}</li>
     * <li>if the current schema version is {@code 1.0.0} and
     * {@code destination} equals {@code 1.0.0,2.0.0}, this will return
     * {@code 1.0.0,0.0.0.transition}</li>
     * <li>if the current schema version is {@code 1.0.0,2.0.0} and
     * {@code destination} equals {@code 1.0.0,2.1.0}, this will return
     * {@code 1.0.0,2.0.0.transition}</li>
     * </ul>
     * <p>
     * The {@code destination} argument is needed for two reasons:
     * <ol>
     * <li>the current and destination versions might not have the same number
     * of component versions, and
     * <li>if they do not have the same number of component versions, then
     * whichever of current's components is not equal to destination's is the
     * one that is transitioning</li>
     * </ol>
     *
     * @param destination the {@link Version} that is being transitioned to
     * @return a new {@link Version} containing the new transition component version
     * @throws UnsupportedOperationException if this or the {@code destination} do not represent a
     *          single version
     */
    public Version getTransition(Version destination) {
        if (!(version instanceof VersionComponent)) {
            throw new UnsupportedOperationException("Source version does not represent a single version");
        }

        if (!(destination.version instanceof VersionComponent)) {
            throw new UnsupportedOperationException("Destination version does not represent a single version");
        }

        final org.osgi.framework.Version currentVersion = (org.osgi.framework.Version) version.get();

        final String oldQualifier = currentVersion.getQualifier();
        final String newQualifier;

        if (StringUtils.isEmpty(oldQualifier)) {
            newQualifier = "transition";
        } else {
            newQualifier = oldQualifier + "-transition";
        }

        return new Version(
                    new org.osgi.framework.Version(
                        currentVersion.getMajor(),
                        currentVersion.getMinor(),
                        currentVersion.getMicro(),
                        newQualifier
                    )
                );
    }

    /**
     * Returns the element at the specified position in the list.
     * <p>
     * If the {@code index} is {@code >=} the size of the list, {@link Version#INITIAL} is returned.
     *
     * @param index index of the element to return
     * @return the {@linkplain Version} at the specified {@code index}
     */
    public Version get(int index) {
        return new Version(version.get(index));
    }

    /**
     * Returns the element with the specified {@code key}.
     * <p>
     * If the {@code key} is not in the map, {@link Version#INITIAL} is returned.
     *
     * @param key key of the element to return
     * @return the {@linkplain Version} with the specified {@code key}
     */
    public Version get(String key) {
        return new Version(version.get(key));
    }


    /**
     * Given a {@link Version}, replace the component version in this {@link Version}'s internal
     * list at the specified index.
     * <p>
     * If index equals the size of this {@link Version}, then {@code element} is added to this
     * {@link Version}'s internal list.
     *
     * @param index index of the element to replace
     * @param element version to be stored at the specified position
     * @return a new {@link Version} containing the newly placed version.
     * @throws IndexOutOfBoundsException if index is less than 0 or greater
     *         than the size of the {@link Version}s' internal
     *         list.
     */
    public Version replace(int index, Version element) {
        return new Version(version.replace(index, element));
    }

    /**
     * Given an {@link Version}, replace the component version in this {@link Version}'s internal
     * map at the specified index.
     * <p>
     * If key is not present in this {@link Version}, then {@code element} is added to this
     * {@link Version}'s internal map.
     *
     * @param key key of the map to replace
     * @param element version to be stored with the specified key
     * @return a new {@link Version} containing the newly placed version.
     */
    public Version replace(String key, Version element) {
        return new Version(version.replace(key, element));
    }

    @Override
    public String toString() {
        return version.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }

        Version that = (Version) o;
        return compare(version, that.version) == 0;
    }

    @Override
    public int hashCode() {
        if (version != null) {
            int hc = version.get().hashCode();

            return hc;
        }

        return 0;
    }

    /**
     * Compare this {@link Version} to another.
     * <p>
     * Each component version in this and the other {@link Version}
     * are compared according to {@link Version#compareTo(Object)}. In the case
     * that each component at a valid index for both {@link Version}s
     * are equal, then their number of components are compared.
     * <p>
     * This {@link Version} is considered to be <b>less than</b>
     * another if, when iterating through component versions,
     * {@link Version#compareTo(Object)} returns an integer less than zero, or
     * if all comparable component versions are equal, this
     * {@link Version}'s number of component versions is smaller.
     * <p>
     * This {@link Version} is considered to be <b>equal to</b>
     * another if all component versions are equal in value and quantity.
     * <p>
     * This {@link Version} is considered to be <b>greater than</b>
     * another if, when iterating through component versions,
     * {@link Version#compareTo(Object)} returns an integer greater than zero,
     * or if all comparable component versions are equal, this
     * {@link Version}'s number of component versions is larger.
     *
     * @return A negative integer, zero, or a positive integer if this
     *         {@link Version} is less than, equal to, or greater than
     *         the specified {@link Version}.
     */
    @Override
    public int compareTo(Version other) {
        Objects.requireNonNull(other);
        return compare(this.version, other.version);
    }

    private static int compare(final Component<?> a, final Component<?> b) {
        final String msg = "Cannot compare Component to null";
        Objects.requireNonNull(a, msg);
        Objects.requireNonNull(b, msg);

        if (a instanceof ListComponent && b instanceof VersionComponent) {
            return compareListComponentToVersionComponent((ListComponent) a, (VersionComponent) b);
        } else if (b instanceof ListComponent && a instanceof VersionComponent) {
            return -compareListComponentToVersionComponent((ListComponent) b, (VersionComponent) a);
        }

        return a.compareTo(b);
    }

    /**
     * Compares a {@link ListComponent} to a {@link VersionComponent} by converting the
     * {@link VersionComponent} into a {@link ListComponent} with a single element.
     *
     * @param a {@link ListComponent}
     * @param b {@link VersionComponent}
     * @return A negative integer, zero, or a positive integer if {@code a} is less than, equal to,
     * or greater than {@code b}.
     */
    private static int compareListComponentToVersionComponent(final ListComponent a, final VersionComponent b) {
        return a.compareTo(new ListComponent(b));
    }
}
