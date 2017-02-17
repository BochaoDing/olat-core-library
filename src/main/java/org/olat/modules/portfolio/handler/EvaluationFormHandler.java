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
package org.olat.modules.portfolio.handler;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.ui.EvaluationFormController;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.EvaluationFormPart;
import org.olat.modules.portfolio.ui.MultiEvaluationFormController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.portfolio.ui.editor.PageRunControllerElement;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageRunElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormHandler implements PageElementHandler {

	@Override
	public String getType() {
		return "evaluationform";
	}

	@Override
	public String getIconCssClass() {
		return null;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
		Controller ctrl = null;
		if(element instanceof EvaluationFormPart) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			
			// find assignment
			EvaluationFormPart eva = (EvaluationFormPart)element;
			PageBody body = eva.getBody();
			Assignment assignment = portfolioService.getAssignment(body);
			if(assignment == null) {
				ctrl = getController(ureq, wControl, body, eva);
			} else {
				ctrl = getControllerForAssignment(ureq, wControl, body, assignment);
			}
		}
		
		if(ctrl == null) {
			Translator translator = Util.createPackageTranslator(PortfolioHomeController.class, ureq.getLocale());
			String title = translator.translate("warning.evaluation.not.visible.title");
			String text = translator.translate("warning.evaluation.not.visible.text");
			ctrl = MessageUIFactory.createWarnMessage(ureq, wControl, title, text);
		}
		return new PageRunControllerElement(ctrl);
	}
	

	private Controller getController(UserRequest ureq, WindowControl wControl, PageBody body, EvaluationFormPart eva) {
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		
		Controller ctrl = null;
		Page page = portfolioService.getPageByBody(body);
		List<AccessRights> accessRights = portfolioService.getAccessRights(page);
		if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
			ctrl = new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, eva.getContent(), false);
		} else if(hasRole(PortfolioRoles.coach, ureq.getIdentity(), accessRights)) {
			Identity owner = getOwner(accessRights);
			ctrl =  new EvaluationFormController(ureq, wControl, owner, body, eva.getContent(), true);
		} else if(hasRole(PortfolioRoles.reviewer, ureq.getIdentity(), accessRights)
				|| hasRole(PortfolioRoles.invitee, ureq.getIdentity(), accessRights)) {
			Identity owner = getOwner(accessRights);
			ctrl = new EvaluationFormController(ureq, wControl, owner, body, eva.getContent(), true);
		}
		
		return ctrl;
	}
	
	private Controller getControllerForAssignment(UserRequest ureq, WindowControl wControl, PageBody body, Assignment assignment) {
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);

		//find the evaluation form
		RepositoryEntry re = assignment.getFormEntry();

		Page page = assignment.getPage();
		PageStatus pageStatus = page.getPageStatus();
		
		Controller ctrl = null;
		List<AccessRights> accessRights = portfolioService.getAccessRights(page);
		boolean anonym = assignment.isAnonymousExternalEvaluation();
		if(pageStatus == null || pageStatus == PageStatus.draft) {
			if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
				ctrl = new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, false, false);
			}
		} else if (assignment.isOnlyAutoEvaluation()) {
			// only the auto evaluation is shown
			if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
				ctrl =  new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, false);
			} else if(hasRole(PortfolioRoles.coach, ureq.getIdentity(), accessRights)) {
				Identity owner = getOwner(accessRights);
				ctrl =  new EvaluationFormController(ureq, wControl, owner, body, re, true, false);
			} else if(hasRole(PortfolioRoles.reviewer, ureq.getIdentity(), accessRights)
					|| hasRole(PortfolioRoles.invitee, ureq.getIdentity(), accessRights)) {
				if(assignment.isReviewerSeeAutoEvaluation()) {
					Identity owner = getOwner(accessRights);
					ctrl = new EvaluationFormController(ureq, wControl, owner, body, re, true, false);
				}
			}
		} else {
			if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
				Identity owner = getOwner(accessRights);
				List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
				if(coachesAndReviewers.size() > 0) {
					ctrl = new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, body, re, false, readOnly, anonym);
				} else {
					ctrl = new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, false);
				}
			} else if(hasRole(PortfolioRoles.coach, ureq.getIdentity(), accessRights)) {
				Identity owner = getOwner(accessRights);
				List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
				boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
				ctrl = new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, body, re, false, readOnly, anonym);
			} else if(hasRole(PortfolioRoles.reviewer, ureq.getIdentity(), accessRights)
					|| hasRole(PortfolioRoles.invitee, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
				if(assignment.isReviewerSeeAutoEvaluation()) {
					Identity owner = getOwner(accessRights);
					List<Identity> reviewers = Collections.singletonList(ureq.getIdentity());
					ctrl = new MultiEvaluationFormController(ureq, wControl, owner, reviewers, body, re, true, readOnly, anonym);
				} else {
					ctrl = new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, !readOnly);
				}
			}
		}
		return ctrl;
	}
		
	
	private Identity getOwner(List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(PortfolioRoles.owner == accessRight.getRole()) {
				return accessRight.getIdentity();
			}
		}
		return null;
	}
	
	private List<Identity> getCoachesAndReviewers(List<AccessRights> accessRights) {
		List<Identity> identities = new ArrayList<>(accessRights.size());
		for(AccessRights accessRight:accessRights) {
			if(PortfolioRoles.coach == accessRight.getRole() || PortfolioRoles.reviewer == accessRight.getRole() || PortfolioRoles.invitee == accessRight.getRole()) {
				identities.add(accessRight.getIdentity());
			}
		}
		return identities;
	}
	
	private boolean hasRole(PortfolioRoles role, Identity identity, List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(role == accessRight.getRole() && accessRight.getIdentity() != null && accessRight.getIdentity().equals(identity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof EvaluationFormPart) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			// find assignment
			EvaluationFormPart eva = (EvaluationFormPart)element;
			PageBody body = eva.getBody();
			Assignment assignment = portfolioService.getAssignment(body);
			
			//find the evaluation form
			RepositoryEntry re = assignment.getFormEntry();
			File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
			File formFile = new File(repositoryDir, FORM_XML_FILE);
			return new EvaluationFormController(ureq, wControl, formFile);
		}
		return null;
	}
}
