/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ClassUtils;

/**
 * Factory for collections, being aware of JDK 1.4+ extended collections
 * and Commons Collection 3.x's corresponding versions for older JDKs.
 * Mainly for internal use within the framework.
 *
 * <p>The goal of this class is to avoid runtime dependencies on JDK 1.4+
 * or Commons Collections 3.x, simply using the best collection implementation
 * that is available. Prefers JDK 1.4+ collection implementations to Commons
 * Collections 3.x versions, falling back to JDK 1.3 collections as worst case.
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see #createLinkedSetIfPossible
 * @see #createLinkedMapIfPossible
 * @see #createLinkedCaseInsensitiveMapIfPossible
 * @see #createIdentityMapIfPossible
 */
public abstract class CollectionFactory {

	private static final Log logger = LogFactory.getLog(CollectionFactory.class);

	private static final boolean commonsCollections3Available =
			ClassUtils.isPresent("org.apache.commons.collections.map.LinkedMap");


	static {
		// Check whether JDK 1.4+ collections and/or
		// Commons Collections 3.x are available.
		if (JdkVersion.isAtLeastJava14()) {
			logger.info("JDK 1.4+ collections available");
		}
		if (commonsCollections3Available) {
			logger.info("Commons Collections 3.x available");
		}
	}


	/**
	 * Create a linked set if possible: that is, if running on JDK >= 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4+
	 * LinkedHashSet to a Commons Collections 3.x ListOrderedSet.
	 * @param initialCapacity the initial capacity of the set
	 * @return the new set instance
	 * @see java.util.LinkedHashSet
	 * @see org.apache.commons.collections.set.ListOrderedSet
	 */
	public static Set createLinkedSetIfPossible(int initialCapacity) {
		if (JdkVersion.isAtLeastJava14()) {
			logger.trace("Creating [java.util.LinkedHashSet]");
			return Jdk14CollectionFactory.createLinkedHashSet(initialCapacity);
		}
		else if (commonsCollections3Available) {
			logger.trace("Creating [org.apache.commons.collections.set.ListOrderedSet]");
			return CommonsCollectionFactory.createListOrderedSet(initialCapacity);
		}
		else {
			logger.trace("Falling back to [java.util.HashSet] for linked set");
			return new HashSet(initialCapacity);
		}
	}

	/**
	 * Create a linked map if possible: that is, if running on JDK >= 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4+
	 * LinkedHashMap to a Commons Collections 3.x LinkedMap.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see java.util.LinkedHashMap
	 * @see org.apache.commons.collections.map.LinkedMap
	 */
	public static Map createLinkedMapIfPossible(int initialCapacity) {
		if (JdkVersion.isAtLeastJava14()) {
			logger.trace("Creating [java.util.LinkedHashMap]");
			return Jdk14CollectionFactory.createLinkedHashMap(initialCapacity);
		}
		else if (commonsCollections3Available) {
			logger.trace("Creating [org.apache.commons.collections.map.LinkedMap]");
			return CommonsCollectionFactory.createLinkedMap(initialCapacity);
		}
		else {
			logger.trace("Falling back to [java.util.HashMap] for linked map");
			return new HashMap(initialCapacity);
		}
	}

	/**
	 * Create a linked case-insensitive map if possible: if Commons Collections
	 * 3.x is available, a CaseInsensitiveMap with ListOrderedMap decorator will
	 * be created. Else, a JDK 1.4+ LinkedHashMap or plain HashMap will be used.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see org.apache.commons.collections.map.CaseInsensitiveMap
	 * @see org.apache.commons.collections.map.ListOrderedMap
	 */
	public static Map createLinkedCaseInsensitiveMapIfPossible(int initialCapacity) {
		if (commonsCollections3Available) {
			logger.trace("Creating [org.apache.commons.collections.map.ListOrderedMap/CaseInsensitiveMap]");
			return CommonsCollectionFactory.createListOrderedCaseInsensitiveMap(initialCapacity);
		}
		else if (JdkVersion.isAtLeastJava14()) {
			logger.trace("Falling back to [java.util.LinkedHashMap] for linked case-insensitive map");
			return Jdk14CollectionFactory.createLinkedHashMap(initialCapacity);
		}
		else {
			logger.trace("Falling back to [java.util.HashMap] for linked case-insensitive map");
			return new HashMap(initialCapacity);
		}
	}

	/**
	 * Create an identity map if possible: that is, if running on JDK >= 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4+
	 * IdentityHashMap to a Commons Collections 3.x IdentityMap.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see java.util.IdentityHashMap
	 * @see org.apache.commons.collections.map.IdentityMap
	 */
	public static Map createIdentityMapIfPossible(int initialCapacity) {
		if (JdkVersion.isAtLeastJava14()) {
			logger.trace("Creating [java.util.IdentityHashMap]");
			return Jdk14CollectionFactory.createIdentityHashMap(initialCapacity);
		}
		else if (commonsCollections3Available) {
			logger.trace("Creating [org.apache.commons.collections.map.IdentityMap]");
			return CommonsCollectionFactory.createIdentityMap(initialCapacity);
		}
		else {
			logger.trace("Falling back to [java.util.HashMap] for identity map");
			return new HashMap(initialCapacity);
		}
	}


	/**
	 * Create the most approximate collection for the given collection.
	 * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
	 * or Set, respectively.
	 * @param collection the original collection object
	 * @param initialCapacity the initial capacity
	 * @return the new collection instance
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see #createLinkedSetIfPossible
	 */
	public static Collection createApproximateCollection(Object collection, int initialCapacity) {
		if (collection instanceof List) {
			return new ArrayList(initialCapacity);
		}
		else if (collection instanceof SortedSet) {
			return new TreeSet(((SortedSet) collection).comparator());
		}
		else {
			return createLinkedSetIfPossible(initialCapacity);
		}
	}

	/**
	 * Create the most approximate map for the given map.
	 * <p>Creates a TreeMap or linked Map for a SortedMap or Map, respectively.
	 * @param map the original map object
	 * @param initialCapacity the initial capacity
	 * @return the new collection instance
	 * @see java.util.TreeMap
	 * @see #createLinkedMapIfPossible
	 */
	public static Map createApproximateMap(Object map, int initialCapacity) {
		if (map instanceof SortedMap) {
			return new TreeMap(((SortedMap) map).comparator());
		}
		else {
			return createLinkedMapIfPossible(initialCapacity);
		}
	}


	/**
	 * Actual creation of JDK 1.4+ Collections.
	 * In separate inner class to avoid runtime dependency on JDK 1.4+.
	 */
	private static abstract class Jdk14CollectionFactory {

		private static Set createLinkedHashSet(int initialCapacity) {
			return new LinkedHashSet(initialCapacity);
		}

		private static Map createLinkedHashMap(int initialCapacity) {
			return new LinkedHashMap(initialCapacity);
		}

		private static Map createIdentityHashMap(int initialCapacity) {
			return new IdentityHashMap(initialCapacity);
		}
	}


	/**
	 * Actual creation of Commons Collections.
	 * In separate inner class to avoid runtime dependency on Commons Collections 3.x.
	 */
	private static abstract class CommonsCollectionFactory {

		private static Set createListOrderedSet(int initialCapacity) {
			return ListOrderedSet.decorate(new HashSet(initialCapacity));
		}

		private static Map createLinkedMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return new LinkedMap(initialCapacity == 0 ? 1 : initialCapacity);
		}

		private static Map createListOrderedCaseInsensitiveMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return ListOrderedMap.decorate(new CaseInsensitiveMap(initialCapacity == 0 ? 1 : initialCapacity));
		}

		private static Map createIdentityMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return new IdentityMap(initialCapacity == 0 ? 1 : initialCapacity);
		}
	}

}
