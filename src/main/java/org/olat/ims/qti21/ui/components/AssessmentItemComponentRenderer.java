/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.components;

import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemComponentRenderer extends AssessmentObjectComponentRenderer {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentItemComponentRenderer.class);

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		AssessmentItemComponent cmp = (AssessmentItemComponent)source;
		sb.append("<div class='qtiworks o_assessmentitem'>");

		ItemSessionController itemSessionController = cmp.getItemSessionController();
		
		CandidateSessionContext candidateSessionContext = cmp.getCandidateSessionContext();

        /* Create appropriate options that link back to this controller */
		final AssessmentTestSession candidateSession = candidateSessionContext.getCandidateSession();
        if (candidateSession != null && candidateSession.isExploded()) {
            renderExploded(sb, translator);
        } else if (candidateSessionContext.isTerminated()) {
            renderTerminated(sb, translator);
        } else {
            /* Look up most recent event */
            final CandidateEvent latestEvent = candidateSessionContext.getLastEvent();// assertSessionEntered(candidateSession);

            /* Load the ItemSessionState */
            final ItemSessionState itemSessionState = cmp.getItemSessionController().getItemSessionState();// candidateDataService.loadItemSessionState(latestEvent);

            /* Touch the session's duration state if appropriate */
            if (itemSessionState.isEntered() && !itemSessionState.isEnded() && !itemSessionState.isSuspended()) {
                final Date timestamp = candidateSessionContext.getCurrentRequestTimestamp();
                itemSessionController.touchDuration(timestamp);
            }

            /* Render event */
            AssessmentRenderer renderHints = new AssessmentRenderer(renderer);
            renderItemEvent(renderHints, sb, cmp, latestEvent, itemSessionState, ubu, translator);
        }
		
		sb.append("</div>");
	}
	
    private void renderItemEvent(AssessmentRenderer renderer, StringOutput sb, AssessmentItemComponent component,
    		CandidateEvent candidateEvent, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
        
    	final CandidateItemEventType itemEventType = candidateEvent.getItemEventType();

        /* Create and partially configure rendering request */
        //renderingRequest.setPrompt("" /* itemDeliverySettings.getPrompt() */);

        /* If session has terminated, render appropriate state and exit */
        if (itemSessionState.isExited()) {
        	renderTerminated(sb, translator);
            return;
        }

        /* Detect "modal" events. These will cause a particular rendering state to be
         * displayed, which candidate will then leave.
         */

        if (itemEventType==CandidateItemEventType.SOLUTION) {
        	renderer.setSolutionMode(true);
        }

        /* Now set candidate action permissions depending on state of session */
        if (itemEventType==CandidateItemEventType.SOLUTION || itemSessionState.isEnded()) {
            /* Item session is ended (closed) */
        	renderer.setEndAllowed(false);
        	renderer.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenEnded() */);
        	renderer.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenEnded() */);
        	renderer.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenEnded() */);
        	renderer.setCandidateCommentAllowed(false);
        } else if (itemSessionState.isOpen()) {
            /* Item session is open (interacting) */
        	renderer.setEndAllowed(true /* itemDeliverySettings.isAllowEnd() */);
        	renderer.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenOpen() */);
        	renderer.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenOpen() */);
        	renderer.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenOpen() */);
        	renderer.setCandidateCommentAllowed(false /* itemDeliverySettings.isAllowCandidateComment() */);
        } else {
            throw new OLATRuntimeException("Item has not been entered yet. We do not currently support rendering of this state.", null);
        }

        /* Finally pass to rendering layer */
       // candidateAuditLogger.logItemRendering(candidateEvent);
        //final List<CandidateEventNotification> notifications = candidateEvent.getNotifications();
        try {
        	renderTestItemBody(renderer, sb, component, itemSessionState, ubu, translator);
        } catch (final RuntimeException e) {
            /* Rendering is complex and may trigger an unexpected Exception (due to a bug in the XSLT).
             * In this case, the best we can do for the candidate is to 'explode' the session.
             * See bug #49.
             */
        	log.error("", e);
            renderExploded(sb, translator);
        }
    }
    
	private void renderTestItemBody(AssessmentRenderer renderer, StringOutput sb, AssessmentItemComponent component, ItemSessionState itemSessionState,
			URLBuilder ubu, Translator translator) {
		
		final AssessmentItem assessmentItem = component.getAssessmentItem();
		final ResolvedAssessmentItem resolvedAssessmentItem = component.getResolvedAssessmentItem();

		sb.append("<div class='o_assessmentitem_wrapper'>");
		//title + status
		sb.append("<h4 class='itemTitle'>");
		renderItemStatus(renderer, sb, itemSessionState, translator);
		sb.append(StringHelper.escapeHtml(assessmentItem.getTitle())).append("</h4>")
		  .append("<div id='itemBody' class='clearfix'>");
		
		//TODO prompt
		
		//render itemBody
		assessmentItem.getItemBody().getBlocks().forEach((block)
				-> renderBlock(renderer, sb, component, resolvedAssessmentItem, itemSessionState, block, ubu, translator));

		//comment
		renderComment(renderer, sb, component, itemSessionState, translator);
		
		//end body
		sb.append("</div>");
		
		// Display active modal feedback (only after responseProcessing)
		if(itemSessionState.getSessionStatus() == SessionStatus.FINAL) {
			renderTestItemModalFeedback(renderer, sb, component, resolvedAssessmentItem, itemSessionState, ubu, translator);
		}

		//controls
		sb.append("<div class='o_button_group o_assessmentitem_controls'>");
		//submit button
		if(component.isItemSessionOpen(itemSessionState, renderer.isSolutionMode())) {
			Component submit = component.getQtiItem().getSubmitButton().getComponent();
			submit.getHTMLRendererSingleton().render(renderer.getRenderer(), sb, submit, ubu, translator, new RenderResult(), null);
		}
		sb.append("</div>");
		
		sb.append("</div>"); // end wrapper
	}
    
	private void renderItemStatus(AssessmentRenderer renderer, StringOutput sb, ItemSessionState itemSessionState, Translator translator) {
		if(renderer.isSolutionMode()) {
			sb.append("<span class='o_assessmentitem_status review'>").append(translator.translate("assessment.item.status.modelSolution")).append("</span>");
		} else {
			super.renderItemStatus(sb, itemSessionState, null, translator);
		}
	}
	
	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {

		Identifier identifier = printedVar.getIdentifier();
		Value templateValue = itemSessionState.getTemplateValues().get(identifier);
		Value outcomeValue = itemSessionState.getOutcomeValues().get(identifier);
		
		sb.append("<span class='printedVariable'>");
		if(outcomeValue != null) {
			OutcomeDeclaration outcomeDeclaration = resolvedAssessmentItem.getRootNodeLookup()
					.extractIfSuccessful().getOutcomeDeclaration(identifier);
			renderPrintedVariable(renderer, sb, printedVar, outcomeDeclaration, outcomeValue);
		} else if(templateValue != null) {
			TemplateDeclaration templateDeclaration = resolvedAssessmentItem.getRootNodeLookup()
					.extractIfSuccessful().getTemplateDeclaration(identifier);
			renderPrintedVariable(renderer, sb, printedVar, templateDeclaration, templateValue);
		} else {
			sb.append("(variable ").append(identifier.toString()).append(" was not found)");
		}
		sb.append("</span>");
	}
}