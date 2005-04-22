/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transition takes a flow from one state to another when executed. A
 * transition is associated with exactly one source
 * <code>TransitionableState</code> managed by exactly one <code>Flow</code>
 * definition.
 * 
 * @see org.springframework.web.flow.TransitionableState
 * @see org.springframework.web.flow.Flow
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition {

	protected final Log logger = LogFactory.getLog(Transition.class);

	/**
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The criteria that determine whether or not this transition should
	 * execute. These criteria are said to <i>guard</i> execution of the
	 * transition.
	 */
	private TransitionCriteria criteria;

	/**
	 * The target state that this transition should transition to when executed.
	 */
	private State targetState;

	/**
	 * The state id for the target state; needed to lazily resolve the target
	 * state once on first execution (after configuration).
	 */
	private String targetStateId;

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition
	 *        should be executed given contextual information
	 * @param targetStateId the id of the state to transition to when this
	 *        transition is executed
	 */
	public Transition(TransitionCriteria criteria, String targetStateId) {
		Assert.notNull(criteria, "The transition criteria property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.criteria = criteria;
		this.targetStateId = targetStateId;
	}

	/**
	 * Returns the owning source (<i>from</i>) state of this transition.
	 * @return the source state
	 * @throws IllegalStateException if the source state has not been set
	 */
	public TransitionableState getSourceState() throws IllegalStateException {
		Assert.state(sourceState != null,
				"The source state is not yet been set -- this transition must be added to exactly one owning state definition!");
		return sourceState;
	}

	/**
	 * Set the owning source (<i>from</i>) state of this transition.
	 */
	protected void setSourceState(TransitionableState owningState) {
		this.sourceState = owningState;
	}

	/**
	 * Returns the id of the target (<i>to</i>) state of this transition.
	 * @return the target state id
	 */
	public String getTargetStateId() {
		return targetStateId;
	}

	/**
	 * Returns the target (<i>to</i>) state of this transition.
	 * @return the target state
	 * @throws NoSuchFlowStateException when the target state cannot be found
	 */
	public State getTargetState() throws NoSuchFlowStateException {
		synchronized (this) {
			if (this.targetState != null) {
				return this.targetState;
			}
		}
		State targetState = getSourceState().getFlow().getRequiredState(getTargetStateId());
		synchronized (this) {
			this.targetState = targetState;
		}
		return this.targetState;
	}

	/**
	 * Returns the strategy used to determine if this transition should execute
	 * given an execution context.
	 * @return the constraint
	 */
	public TransitionCriteria getCriteria() {
		return this.criteria;
	}

	/**
	 * Checks if this transition should be executed given the state of the
	 * provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean shouldExecute(RequestContext context) {
		return this.criteria.test(context);
	}

	/**
	 * Execute this state transition.
	 * @param context the flow execution request context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the transition execution
	 * @throws CannotExecuteStateTransitionException when this transition cannot
	 *         be executed
	 */
	protected ViewDescriptor execute(StateContext context) throws CannotExecuteStateTransitionException {
		State state = null;
		try {
			state = getTargetState();
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, e);
		}
		// enter the target state (note: any exceptions are propagated)
		ViewDescriptor viewDescriptor = state.enter(context);
		if (logger.isDebugEnabled()) {
			if (context.isFlowExecutionActive()) {
				logger.debug("Transition '" + this + "' executed; as a result, the new state is '"
						+ context.getCurrentState().getId() + "' in flow '" + context.getActiveFlow().getId() + "'");
			}
			else {
				logger.debug("Transition '" + this + "' executed; as a result, the flow '"
						+ context.getRootFlow().getId() + "' execution has ended");
			}
		}
		return viewDescriptor;
	}

	public String toString() {
		return new ToStringCreator(this).append("on", criteria).append("to", targetStateId).toString();
	}
}