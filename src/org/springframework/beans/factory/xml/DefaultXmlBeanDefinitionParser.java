/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedLinkedMap;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.JdkVersion;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the XmlBeanDefinitionParser interface.
 * Parses bean definitions according to the "spring-beans" DTD.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 18.12.2003
 * @version $Id$
 */
public class DefaultXmlBeanDefinitionParser implements XmlBeanDefinitionParser {

	public static final String BEAN_NAME_DELIMITERS = ",; ";

	/**
	 * Value of a T/F attribute that represents true.
	 * Anything else represents false. Case seNsItive.
	 */
	public static final String TRUE_VALUE = "true";
	public static final String DEFAULT_VALUE = "default";
	public static final String DESCRIPTION_ELEMENT = "description";

	public static final String AUTOWIRE_BY_NAME_VALUE = "byName";
	public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";
	public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";
	public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

	public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";
	public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";
	public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

	public static final String BEAN_ELEMENT = "bean";
	public static final String ID_ATTRIBUTE = "id";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String PARENT_ATTRIBUTE = "parent";

	public static final String CLASS_ATTRIBUTE = "class";
	public static final String SINGLETON_ATTRIBUTE = "singleton";
	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";
	public static final String AUTOWIRE_ATTRIBUTE = "autowire";
	public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";
	public static final String INIT_METHOD_ATTRIBUTE = "init-method";
	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	public static final String INDEX_ATTRIBUTE = "index";
	public static final String TYPE_ATTRIBUTE = "type";
	public static final String PROPERTY_ELEMENT = "property";
	public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

	public static final String REPLACED_METHOD_ELEMENT = "replaced-method";
	public static final String REPLACER_ATTRIBUTE = "replacer";
	public static final String ARG_TYPE_ELEMENT = "arg-type";
	public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

	public static final String REF_ELEMENT = "ref";
	public static final String IDREF_ELEMENT = "idref";
	public static final String BEAN_REF_ATTRIBUTE = "bean";
	public static final String LOCAL_REF_ATTRIBUTE = "local";
	public static final String PARENT_REF_ATTRIBUTE = "parent";

	public static final String LIST_ELEMENT = "list";
	public static final String SET_ELEMENT = "set";
	public static final String MAP_ELEMENT = "map";
	public static final String ENTRY_ELEMENT = "entry";
	public static final String KEY_ATTRIBUTE = "key";
	public static final String PROPS_ELEMENT = "props";
	public static final String PROP_ELEMENT = "prop";
	public static final String VALUE_ELEMENT = "value";
	public static final String NULL_ELEMENT = "null";



	protected final Log logger = LogFactory.getLog(getClass());

	private BeanDefinitionRegistry beanFactory;

	private ClassLoader beanClassLoader;

	private Resource resource;

	private String defaultLazyInit;

	private String defaultDependencyCheck;

	private String defaultAutowire;


	public void registerBeanDefinitions(BeanDefinitionRegistry beanFactory, ClassLoader beanClassLoader,
																			Document doc, Resource resource) {
		this.beanFactory = beanFactory;
		this.beanClassLoader = beanClassLoader;
		this.resource = resource;

		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();

		this.defaultLazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
		logger.debug("Default lazy init '" + this.defaultLazyInit + "'");
		this.defaultDependencyCheck = root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE);
		logger.debug("Default dependency check '" + this.defaultDependencyCheck + "'");
		this.defaultAutowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
		logger.debug("Default autowire '" + this.defaultAutowire + "'");

		NodeList nl = root.getChildNodes();
		int beanDefinitionCounter = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && BEAN_ELEMENT.equals(node.getNodeName())) {
				beanDefinitionCounter++;
				registerBeanDefinition((Element) node);
			}
		}
		logger.debug("Found " + beanDefinitionCounter + " <" + BEAN_ELEMENT + "> elements defining beans");
	}

	protected BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	protected ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

	protected String getDefaultLazyInit() {
		return defaultLazyInit;
	}

	protected String getDefaultDependencyCheck() {
		return defaultDependencyCheck;
	}

	protected String getDefaultAutowire() {
		return defaultAutowire;
	}

	protected Resource getResource() {
		return resource;
	}


	/**
	 * Parse a "bean" element and register it with the bean factory.
	 */
	protected void registerBeanDefinition(Element ele) {
		BeanDefinitionHolder bdHolder = parseBeanDefinition(ele);
		logger.debug("Registering bean definition with id '" + bdHolder.getBeanName() + "'");
		this.beanFactory.registerBeanDefinition(bdHolder.getBeanName(), bdHolder.getBeanDefinition());
		if (bdHolder.getAliases() != null) {
			for (int i = 0; i < bdHolder.getAliases().length; i++) {
				this.beanFactory.registerAlias(bdHolder.getBeanName(), bdHolder.getAliases()[i]);
			}
		}
	}

	/**
	 * Parse a standard bean definition into a BeanDefinitionHolder,
	 * including bean name and aliases.
	 * <p>Bean elements specify their canonical name as "id" attribute
	 * and their aliases as a delimited "name" attribute.
	 * <p>If no "id" specified, uses the first name in the "name" attribute
	 * as canonical name, registering all others as aliases.
	 */
	protected BeanDefinitionHolder parseBeanDefinition(Element ele) {
		String id = ele.getAttribute(ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
		List aliases = new ArrayList();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS, true, true);
			aliases.addAll(Arrays.asList(nameArr));
		}

		if (!StringUtils.hasLength(id) && !aliases.isEmpty()) {
			id = (String) aliases.remove(0);
			logger.debug("No XML 'id' specified - using '" + id + "' as ID and " + aliases + " as aliases");
		}

		BeanDefinition beanDefinition = parseBeanDefinition(ele, id);

		if (!StringUtils.hasLength(id)) {
			if (beanDefinition instanceof RootBeanDefinition) {
				id = ((RootBeanDefinition) beanDefinition).getBeanClassName();
				logger.debug("Neither XML 'id' nor 'name' specified - using bean class name [" + id + "] as ID");
			}
			else if (beanDefinition instanceof ChildBeanDefinition) {
				throw new BeanDefinitionStoreException(this.resource, "",
																							 "Child bean definition has neither 'id' nor 'name'");
			}
		}

		String[] aliasesArray = (String[]) aliases.toArray(new String[aliases.size()]);
		return new BeanDefinitionHolder(beanDefinition, id, aliasesArray);
	}

	/**
	 * Parse the BeanDefinition itself, without regard to name or aliases.
	 */
	protected BeanDefinition parseBeanDefinition(Element ele, String beanName) {
		String className = null;
		try {
			if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
				className = ele.getAttribute(CLASS_ATTRIBUTE);
			}
			String parent = null;
			if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
				parent = ele.getAttribute(PARENT_ATTRIBUTE);
			}

			MutablePropertyValues pvs = getPropertyValueSubElements(beanName, ele);
			ConstructorArgumentValues cargs = getConstructorArgSubElements(beanName, ele);

			Class beanClass = null;
			if (className != null && this.beanClassLoader != null) {
				beanClass = Class.forName(className, true, this.beanClassLoader);
			}

			AbstractBeanDefinition bd = null;
			if (parent == null) {
				if (beanClass != null) {
					bd = new RootBeanDefinition(beanClass, cargs, pvs);
				}
				else {
					bd = new RootBeanDefinition(className, cargs, pvs);
				}
			}
			else {
				if (beanClass != null) {
					bd = new ChildBeanDefinition(parent, beanClass, cargs, pvs);
				}
				else {
					bd = new ChildBeanDefinition(parent, className, cargs, pvs);
				}
			}

			if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
				String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
				bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, BEAN_NAME_DELIMITERS, true, true));
			}

			if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
				bd.setStaticFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
			}

			String dependencyCheck = ele.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(dependencyCheck)) {
				dependencyCheck = this.defaultDependencyCheck;
			}
			bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

			String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(autowire)) {
				autowire = this.defaultAutowire;
			}
			bd.setAutowireMode(getAutowireMode(autowire));

			String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
			if (!initMethodName.equals("")) {
				bd.setInitMethodName(initMethodName);
			}
			String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
			if (!destroyMethodName.equals("")) {
				bd.setDestroyMethodName(destroyMethodName);
			}

			getLookupOverrideSubElements(bd.getMethodOverrides(), beanName, ele);
			getReplacedMethodSubElements(bd.getMethodOverrides(), beanName, ele);

			bd.setResourceDescription(this.resource.getDescription());

			if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
				bd.setSingleton(TRUE_VALUE.equals(ele.getAttribute(SINGLETON_ATTRIBUTE)));
			}

			String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(lazyInit) && bd.isSingleton()) {
				// just apply default to singletons, as lazy-init has no meaning for prototypes
				lazyInit = this.defaultLazyInit;
			}
			bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(this.resource, beanName,
						"Bean class [" + className + "] not found", ex);
		}
		catch (NoClassDefFoundError err) {
			throw new BeanDefinitionStoreException(this.resource, beanName,
						"Class that bean class [" + className + "] depends on not found", err);
		}
	}

	/**
	 * Parse constructor argument subelements of the given bean element.
	 */
	protected ConstructorArgumentValues getConstructorArgSubElements(String beanName, Element beanEle)
			throws ClassNotFoundException {
		NodeList nl = beanEle.getChildNodes();
		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && CONSTRUCTOR_ARG_ELEMENT.equals(node.getNodeName())) {
				parseConstructorArgElement(beanName, cargs, (Element) node);
			}
		}
		return cargs;
	}

	/**
	 * Parse property value subelements of the given bean element.
	 */
	protected MutablePropertyValues getPropertyValueSubElements(String beanName, Element beanEle) {
		NodeList nl = beanEle.getChildNodes();
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && PROPERTY_ELEMENT.equals(node.getNodeName())) {
				parsePropertyElement(beanName, pvs, (Element) node);
			}
		}
		return pvs;
	}

	/**
	 * Parse lookup-override sub elements
	 */
	protected void getLookupOverrideSubElements(MethodOverrides overrides, String beanName, Element beanEle) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && LOOKUP_METHOD_ELEMENT.equals(node.getNodeName())) {
				Element ele = (Element) node;
				String methodName = ele.getAttribute(NAME_ATTRIBUTE);
				String beanRef = ele.getAttribute(BEAN_ELEMENT);
				overrides.addOverride(new LookupOverride(methodName, beanRef));
			}
		}
	}

	protected void getReplacedMethodSubElements(MethodOverrides overrides, String beanName, Element beanEle) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && REPLACED_METHOD_ELEMENT.equals(node.getNodeName())) {
				Element replacedMethodEle = (Element) node;
				String name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE);
				String callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE);
				ReplaceOverride replaceOverride = new ReplaceOverride(name, callback);

				// Look for arg-type match elements
				NodeList argTypeNodes = replacedMethodEle.getElementsByTagName(ARG_TYPE_ELEMENT);
				for (int j = 0; j < argTypeNodes.getLength(); j++) {
					Element argTypeEle = (Element) argTypeNodes.item(j);
					replaceOverride.addTypeIdentifier(argTypeEle.getAttribute(ARG_TYPE_MATCH_ATTRIBUTE));
				}
				overrides.addOverride(replaceOverride);
			}
		}
	}

	/**
	 * Parse a constructor-arg element.
	 */
	protected void parseConstructorArgElement(String beanName, ConstructorArgumentValues cargs, Element ele)
			throws DOMException, ClassNotFoundException {
		Object val = getPropertyValue(ele, beanName);
		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		if (StringUtils.hasLength(indexAttr)) {
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					throw new BeanDefinitionStoreException(this.resource, beanName, "'index' cannot be lower than 0");
				}
				if (StringUtils.hasLength(typeAttr)) {
					cargs.addIndexedArgumentValue(index, val, typeAttr);
				}
				else {
					cargs.addIndexedArgumentValue(index, val);
				}
			}
			catch (NumberFormatException ex) {
				throw new BeanDefinitionStoreException(this.resource, beanName,
						"Attribute 'index' of tag 'constructor-arg' must be an integer");
			}
		}
		else {
			if (StringUtils.hasLength(typeAttr)) {
				cargs.addGenericArgumentValue(val, typeAttr);
			}
			else {
				cargs.addGenericArgumentValue(val);
			}
		}
	}

	/**
	 * Parse a property element.
	 */
	protected void parsePropertyElement(String beanName, MutablePropertyValues pvs, Element ele)
			throws DOMException {
		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			throw new BeanDefinitionStoreException(this.resource, beanName,
																						 "Tag 'property' must have a 'name' attribute");
		}
		Object val = getPropertyValue(ele, beanName);
		pvs.addPropertyValue(new PropertyValue(propertyName, val));
	}

	/**
	 * Get the value of a property element. May be a list.
	 * @param ele property element
	 */
	protected Object getPropertyValue(Element ele, String beanName) {
		// should only have one element child: value, ref, collection
		NodeList nl = ele.getChildNodes();
		Element valueRefOrCollectionElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element candidateEle = (Element) nl.item(i);
				if (DESCRIPTION_ELEMENT.equals(candidateEle.getTagName())) {
					// keep going: we don't use this value for now
				}
				else {
					// child element is what we're looking for
					valueRefOrCollectionElement = candidateEle;
				}
			}
		}
		if (valueRefOrCollectionElement == null) {
			throw new BeanDefinitionStoreException(this.resource, beanName,
																						 "<property> element must have a subelement like 'value' or 'ref'");
		}
		return parsePropertySubelement(valueRefOrCollectionElement, beanName);
	}

	/**
	 * Parse a value, ref or collection subelement of a property element
	 * @param ele subelement of property element; we don't know which yet
	 */
	protected Object parsePropertySubelement(Element ele, String beanName) {
		if (ele.getTagName().equals(BEAN_ELEMENT)) {
			return parseBeanDefinition(ele);
		}
		else if (ele.getTagName().equals(REF_ELEMENT)) {
			// a generic reference to any name of any bean
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// a reference to the id of another bean in the same XML file
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					// a reference to the id of another bean in the same XML file
					beanRef = ele.getAttribute(PARENT_REF_ATTRIBUTE);
					if (!StringUtils.hasLength(beanRef)) {
						throw new BeanDefinitionStoreException(this.resource, beanName,
																									 "'bean', 'local' or 'parent' is required for a reference");
					}
					return new RuntimeBeanReference(beanRef, true);
				}
			}
			return new RuntimeBeanReference(beanRef);
		}
		else if (ele.getTagName().equals(IDREF_ELEMENT)) {
			// a generic reference to any name of any bean
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// a reference to the id of another bean in the same XML file
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					throw new BeanDefinitionStoreException(this.resource, beanName,
																								 "Either 'bean' or 'local' is required for an idref");
				}
			}
			return beanRef;
		}
		else if (ele.getTagName().equals(LIST_ELEMENT)) {
			return getList(ele, beanName);
		}
		else if (ele.getTagName().equals(SET_ELEMENT)) {
			return getSet(ele, beanName);
		}
		else if (ele.getTagName().equals(MAP_ELEMENT)) {
			return getMap(ele, beanName);
		}
		else if (ele.getTagName().equals(PROPS_ELEMENT)) {
			return getProps(ele, beanName);
		}
		else if (ele.getTagName().equals(VALUE_ELEMENT)) {
			// it's a literal value
			return getTextValue(ele, beanName);
		}
		else if (ele.getTagName().equals(NULL_ELEMENT)) {
			// it's a distinguished null value
			return null;
		}
		throw new BeanDefinitionStoreException(this.resource, beanName,
																					 "Unknown subelement of <property>: <" + ele.getTagName() + ">");
	}

	protected List getList(Element collectionEle, String beanName) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedList list = new ManagedList(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				list.add(parsePropertySubelement(ele, beanName));
			}
		}
		return list;
	}

	protected Set getSet(Element collectionEle, String beanName) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet set = new ManagedSet(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				set.add(parsePropertySubelement(ele, beanName));
			}
		}
		return set;
	}

	protected Map getMap(Element mapEle, String beanName) {
		List list = getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		Map map = null;
		// A LinkedHashMap will preserve insertion order, but is not available pre-1.4.
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			map = ManagedLinkedMapCreator.createManagedLinkedMap(list.size());
			// ManagedLinkedMap = a tag subclass of java.util.LinkedHashMap
		}
		else {
			map = new ManagedMap(list.size());  // a tag subclass of java.util.HashMap
		}
		for (int i = 0; i < list.size(); i++) {
			Element entryEle = (Element) list.get(i);
			String key = entryEle.getAttribute(KEY_ATTRIBUTE);
			// TODO hack: make more robust
			NodeList subEles = entryEle.getElementsByTagName("*");
			map.put(key, parsePropertySubelement((Element) subEles.item(0), beanName));
		}
		return map;
	}

	/**
	 * Don't use the horrible DOM API to get child elements:
	 * Get an element's children with a given element name
	 */
	protected List getChildElementsByTagName(Element mapEle, String elementName) {
		NodeList nl = mapEle.getChildNodes();
		List nodes = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element && elementName.equals(n.getNodeName())) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	protected Properties getProps(Element propsEle, String beanName) {
		Properties props = new Properties();
		NodeList nl = propsEle.getElementsByTagName(PROP_ELEMENT);
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			String key = propEle.getAttribute(KEY_ATTRIBUTE);
			// trim the text value to avoid unwanted whitespace
			// caused by typical XML formatting
			String value = getTextValue(propEle, beanName).trim();
			props.setProperty(key, value);
		}
		return props;
	}

	/**
	 * Make the horrible DOM API slightly more bearable:
	 * get the text value we know this element contains.
	 */
	protected String getTextValue(Element ele, String beanName) {
		NodeList nl = ele.getChildNodes();
		if (nl.getLength() == 0) {
			// treat empty value as empty String
			return "";
		}
		Text text = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Object item = nl.item(i);
			if (item instanceof Text) {
				if (text != null) {
					throw new BeanDefinitionStoreException(this.resource, beanName,
																								 "Expected to find single node of type text " +
																								 "as child of <value> element");
				}
				text = ((Text) item);
			}
			else if (item instanceof Comment) {
				// ignore
			}
			else {
				throw new BeanDefinitionStoreException(this.resource, beanName,
																							 "<value> element is just allowed to have text and comment nodes, " +
																							 "not: " + item.getClass().getName());
			}
		}
		if (text != null) {
			return text.getData();
		}
		else {
			throw new BeanDefinitionStoreException(this.resource, beanName,
																						 "Expected to find a node of type text as child of <value> element");
		}
	}

	protected int getDependencyCheck(String att) {
		int dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_NONE;
		if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_ALL;
		}
		else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
		}
		else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
		}
		// else leave default value
		return dependencyCheckCode;
	}

	protected int getAutowireMode(String att) {
		int autowire = RootBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_BY_NAME;
		}
		else if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_BY_TYPE;
		}
		else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_CONSTRUCTOR;
		}
		else if (AUTOWIRE_AUTODETECT_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_AUTODETECT;
		}
		// else leave default value
		return autowire;
	}


	/**
	 * Actual creation of a ManagedLinkedMap.
	 * In separate inner class to avoid runtime dependency on JDK 1.4.
	 */
	private static abstract class ManagedLinkedMapCreator {

		private static Map createManagedLinkedMap(int capacity) {
			return new ManagedLinkedMap(capacity);
		}
	}

}
