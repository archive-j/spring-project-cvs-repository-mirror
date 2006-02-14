/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.scripting.support;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.JdkVersion;
import org.springframework.scripting.Messenger;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.groovy.GroovyScriptFactory;

import java.lang.reflect.Field;

/**
 * Unit and integration tests for the ScriptFactoryPostProcessor class.
 *
 * @author Rick Evans
 */
public final class ScriptFactoryPostProcessorTests extends TestCase {

	private static final String MESSAGE_TEXT = "Bingo";
	private static final String MESSENGER_BEAN_NAME = "messenger";
	private static final String PROCESSOR_BEAN_NAME = "processor";
	private static final String CHANGED_SCRIPT = "package org.springframework.scripting.groovy\n" +
			"import org.springframework.scripting.Messenger\n" +
			"class GroovyMessenger implements Messenger {\n" +
			"  private String message = \"Bingo\"\n" +
			"  public String getMessage() {\n" +
			// quote the returned message (this is the change)...
			"    return \"'\"  + this.message + \"'\"\n" +
			"  }\n" +
			"  public void setMessage(String message) {\n" +
			"    this.message = message\n" +
			"  }\n" +
			"}";
	private static final String EXPECTED_CHANGED_MESSAGE_TEXT = "'" + MESSAGE_TEXT + "'";
	private static final int DEFAULT_SECONDS_TO_PAUSE = 1;


	public void testDoesNothingWhenPostProcessingNonScriptFactoryTypeBeforeInstantiation() throws Exception {
		assertNull(new ScriptFactoryPostProcessor().postProcessBeforeInstantiation(getClass(), "a.bean"));
	}

	public void testThrowsExceptionIfGivenNonAbstractBeanFactoryImplementation() throws Exception {
		MockControl mock = MockControl.createControl(BeanFactory.class);
		mock.replay();
		try {
			new ScriptFactoryPostProcessor().setBeanFactory((BeanFactory) mock.getMock());
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalStateException expected) {
		}
		mock.verify();
	}

	public void testChangeScriptWithRefreshableBeanFunctionality() throws Exception {
		// Groovy requires JDK 1.4
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		BeanDefinition processorBeanDefinition = createRefreshingScriptFactoryPostProcessor(true);
		BeanDefinition scriptedBeanDefinition = createScriptedGroovyBean();

		GenericApplicationContext ctx = new GenericApplicationContext();
		ctx.registerBeanDefinition(PROCESSOR_BEAN_NAME, processorBeanDefinition);
		ctx.registerBeanDefinition(MESSENGER_BEAN_NAME, scriptedBeanDefinition);
		ctx.refresh();

		Messenger messenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		assertEquals(MESSAGE_TEXT, messenger.getMessage());
		// cool; now let's change the script and check the refresh behaviour...
		pauseToLetRefreshDelayKickIn(DEFAULT_SECONDS_TO_PAUSE);
		StaticScriptSource source = getScriptSource(ctx);
		source.setScript(CHANGED_SCRIPT);
		Messenger refreshedMessenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		// the updated script surrounds the message in quotes before returning...
		assertEquals(EXPECTED_CHANGED_MESSAGE_TEXT, refreshedMessenger.getMessage());
	}

	public void testChangeScriptWithNoRefreshableBeanFunctionality() throws Exception {
		// Groovy requires JDK 1.4
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		BeanDefinition processorBeanDefinition = createRefreshingScriptFactoryPostProcessor(false);
		BeanDefinition scriptedBeanDefinition = createScriptedGroovyBean();

		GenericApplicationContext ctx = new GenericApplicationContext();
		ctx.registerBeanDefinition(PROCESSOR_BEAN_NAME, processorBeanDefinition);
		ctx.registerBeanDefinition(MESSENGER_BEAN_NAME, scriptedBeanDefinition);
		ctx.refresh();

		Messenger messenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		assertEquals(MESSAGE_TEXT, messenger.getMessage());
		// cool; now let's change the script and check the refresh behaviour...
		pauseToLetRefreshDelayKickIn(DEFAULT_SECONDS_TO_PAUSE);
		StaticScriptSource source = getScriptSource(ctx);
		source.setScript(CHANGED_SCRIPT);
		Messenger refreshedMessenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		assertEquals("Script seems to have been refreshed (must not be as no refreshCheckDelay set on ScriptFactoryPostProcessor)",
				MESSAGE_TEXT, refreshedMessenger.getMessage());
	}

	public void testRefreshedScriptReferencePropagatesToCollaborators() throws Exception {
		// Groovy requires JDK 1.4
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		BeanDefinition processorBeanDefinition = createRefreshingScriptFactoryPostProcessor(true);
		BeanDefinition scriptedBeanDefinition = createScriptedGroovyBean();
		BeanDefinitionBuilder collaboratorBuilder = BeanDefinitionBuilder.rootBeanDefinition(MessengerService.class);
		collaboratorBuilder.addPropertyReference(MESSENGER_BEAN_NAME, MESSENGER_BEAN_NAME);

		GenericApplicationContext ctx = new GenericApplicationContext();
		ctx.registerBeanDefinition(PROCESSOR_BEAN_NAME, processorBeanDefinition);
		ctx.registerBeanDefinition(MESSENGER_BEAN_NAME, scriptedBeanDefinition);
		final String collaboratorBeanName = "collaborator";
		ctx.registerBeanDefinition(collaboratorBeanName, collaboratorBuilder.getBeanDefinition());
		ctx.refresh();

		Messenger messenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		assertEquals(MESSAGE_TEXT, messenger.getMessage());
		// cool; now let's change the script and check the refresh behaviour...
		pauseToLetRefreshDelayKickIn(DEFAULT_SECONDS_TO_PAUSE);
		StaticScriptSource source = getScriptSource(ctx);
		source.setScript(CHANGED_SCRIPT);
		Messenger refreshedMessenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		// the updated script surrounds the message in quotes before returning...
		assertEquals(EXPECTED_CHANGED_MESSAGE_TEXT, refreshedMessenger.getMessage());
		// ok, is this change reflected in the reference that the collaborator has?
		MessengerService collaborator = (MessengerService) ctx.getBean(collaboratorBeanName);
		assertEquals(EXPECTED_CHANGED_MESSAGE_TEXT, collaborator.getMessage());
	}

	public void testForRefreshedScriptHavingErrorPickedUpOnFirstCall() throws Exception {
		// Groovy requires JDK 1.4
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		BeanDefinition processorBeanDefinition = createRefreshingScriptFactoryPostProcessor(true);
		BeanDefinition scriptedBeanDefinition = createScriptedGroovyBean();
		BeanDefinitionBuilder collaboratorBuilder = BeanDefinitionBuilder.rootBeanDefinition(MessengerService.class);
		collaboratorBuilder.addPropertyReference(MESSENGER_BEAN_NAME, MESSENGER_BEAN_NAME);

		GenericApplicationContext ctx = new GenericApplicationContext();
		ctx.registerBeanDefinition(PROCESSOR_BEAN_NAME, processorBeanDefinition);
		ctx.registerBeanDefinition(MESSENGER_BEAN_NAME, scriptedBeanDefinition);
		final String collaboratorBeanName = "collaborator";
		ctx.registerBeanDefinition(collaboratorBeanName, collaboratorBuilder.getBeanDefinition());
		ctx.refresh();

		Messenger messenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		assertEquals(MESSAGE_TEXT, messenger.getMessage());
		// cool; now let's change the script and check the refresh behaviour...
		pauseToLetRefreshDelayKickIn(DEFAULT_SECONDS_TO_PAUSE);
		StaticScriptSource source = getScriptSource(ctx);
		// needs The Sundays compiler; must NOT throw any exception here...
		source.setScript("I keep hoping you are the same as me, and I'll send you letters and come to your house for tea");
		Messenger refreshedMessenger = (Messenger) ctx.getBean(MESSENGER_BEAN_NAME);
		try {
			refreshedMessenger.getMessage();
			fail("Must have thrown an Exception (invalid script)");
		}
		catch (FatalBeanException expected) {
			assertTrue(expected.contains(ScriptCompilationException.class));
		}
	}

	// Rick TODO: this is very brittle (depends on hidden implementation details of the ScriptFactoryPostProcessor class).
	private static StaticScriptSource getScriptSource(GenericApplicationContext ctx) throws Exception {
		ScriptFactoryPostProcessor processor = (ScriptFactoryPostProcessor) ctx.getBean(PROCESSOR_BEAN_NAME);
		final Field factoryField = processor.getClass().getDeclaredField("scriptBeanFactory");
		factoryField.setAccessible(true);
		DefaultListableBeanFactory scriptFactory = (DefaultListableBeanFactory) factoryField.get(processor);
		BeanDefinition bd = scriptFactory.getBeanDefinition("scriptedObject.messenger");
		return (StaticScriptSource) bd.getConstructorArgumentValues().getIndexedArgumentValue(0, StaticScriptSource.class).getValue();
	}

	private static BeanDefinition createRefreshingScriptFactoryPostProcessor(boolean isRefreshable) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ScriptFactoryPostProcessor.class);
		if (isRefreshable) {
			builder.addPropertyValue("refreshCheckDelay", new Long(1));
		}
		return builder.getBeanDefinition();
	}

	private static BeanDefinition createScriptedGroovyBean() {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GroovyScriptFactory.class);
		builder.addConstructorArg("inline:package org.springframework.scripting;\n" +
				"class GroovyMessenger implements Messenger {\n" +
				"  private String message = \"Bingo\"\n" +
				"  public String getMessage() {\n" +
				"    return this.message\n" +
				"  }\n" +
				"  public void setMessage(String message) {\n" +
				"    this.message = message\n" +
				"  }\n" +
				"}");
		builder.addPropertyValue("message", MESSAGE_TEXT);
		return builder.getBeanDefinition();
	}

	private static void pauseToLetRefreshDelayKickIn(int secondsToPause) {
		try {
			Thread.sleep(secondsToPause * 1000);
		}
		catch (InterruptedException ignored) {
		}
	}


	public static final class MessengerService {

		private Messenger messenger;

		public void setMessenger(Messenger messenger) {
			this.messenger = messenger;
		}

		public String getMessage() {
			return this.messenger.getMessage();
		}
	}

}
