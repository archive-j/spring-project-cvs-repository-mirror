/*
 * Created on 20-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

/**
 * Provides end state functionality for a web flow.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class EndState extends AbstractState {
    
    public static final String DEFAULT_BACK_STATE_ID = "back";

    public static final String DEFAULT_FINISH_STATE_ID = "finish";

    public static final String DEFAULT_CANCEL_STATE_ID = "cancel";

    private String viewName;

    public EndState(String id) {
        this(id, null);
    }

    public EndState(String id, String viewName) {
        super(id);
        this.viewName = viewName;
    }

    public boolean isEndState() {
        return true;
    }

    protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        ViewDescriptor descriptor;
        if (viewName != null) {
            descriptor = new ViewDescriptor(viewName, sessionExecutionStack.getAttributes());
        }
        else {
            descriptor = ViewDescriptor.NULL_OBJECT;
        }
        FlowSession endingFlowSession = sessionExecutionStack.pop();
        Assert.isTrue(endingFlowSession.getCurrentStateId().equals(getId()),
                "The ending flow session current state should equal this end state - this should not happen");
        if (logger.isDebugEnabled()) {
            logger.debug("Flow session '" + endingFlowSession.getFlowId() + "' ended, details=" + endingFlowSession);
        }
        if (flow.isLifecycleListenerSet()) {
            flow.getLifecycleListener().flowEnded(flow, endingFlowSession, sessionExecutionStack, request);
        }
        if (!sessionExecutionStack.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resuming parent flow '" + sessionExecutionStack.getQualifiedActiveFlowId()
                        + "' in state '" + sessionExecutionStack.getCurrentStateId() + "'");
            }
            Flow resumingParentFlow = flow.getFlowDao().getFlow(sessionExecutionStack.getActiveFlowId());
            SubFlowState resumingState = (SubFlowState)resumingParentFlow.getState(sessionExecutionStack
                    .getCurrentStateId());
            if (resumingState.getAttributesMapper(flow.getFlowDao()) != null) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Messaging the configured attributes mapper to map subflow attributes back up to the resuming parent flow - "
                                    + "the resuming parent flow will now have access to attributes passed up by the completed subflow");
                }
                resumingState.getAttributesMapper(flow.getFlowDao()).mapToResumingParentFlow(
                        endingFlowSession, sessionExecutionStack.getActiveFlowSession());
            }
            else {
                if (logger.isInfoEnabled()) {
                    logger.info("No attributes mapper is configured for the resuming state '" + getId()
                            + "' - note: as a result, no attributes in the ending subflow '"
                            + endingFlowSession.getFlowId() + "' scope will be passed to the resuming parent flow '"
                            + resumingParentFlow.getId() + "'");
                }
            }
            // treat the returned end state as a transitional event in the
            // resuming state, this is so cool!
            String eventId = endingFlowSession.getCurrentStateId();
            descriptor = resumingParentFlow.execute(eventId, resumingState.getId(), sessionExecutionStack, request,
                    response);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Root flow '" + endingFlowSession.getFlowId() + "' ended");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning view descriptor '" + descriptor + "'");
        }
        return descriptor;
    }
}
