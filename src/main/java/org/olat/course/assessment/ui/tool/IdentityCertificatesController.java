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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a copy, perhaps only maintains the orginal: AssessedIdentityCertificatesController
 * with different arguments for the constructor.
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCertificatesController extends BasicController implements GenericEventListener {
	
	private Link generateLink;
	private final VelocityContainer mainVC;
	private DialogBoxController confirmDeleteCtrl;
	private DialogBoxController confirmCertificateCtrl;
	
	private final Identity assessedIdentity;
	private final RepositoryEntry courseEntry;
	
	private final boolean canDelete;
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public IdentityCertificatesController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, Identity assessedIdentity) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));

		this.courseEntry = courseEntry;
		this.assessedIdentity = assessedIdentity;
		
		CourseConfig courseConfig = CourseFactory.loadCourse(courseEntry).getCourseConfig();
		canDelete = courseConfig.isManualCertificationEnabled();
		mainVC = createVelocityContainer("certificate_overview");
		formatter = Formatter.getInstance(getLocale());

		if(courseConfig.isManualCertificationEnabled()) {
			generateLink = LinkFactory.createLink("generate.certificate", "generate", getTranslator(), mainVC, this, Link.BUTTON);
			generateLink.setElementCssClass("o_sel_certificate_generate");
		}
		loadList();
		putInitialPanel(mainVC);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(ce.getOwnerKey().equals(assessedIdentity.getKey())
					&& courseEntry.getOlatResource().getKey().equals(ce.getResourceKey())) {
				loadList();
			}
		}
	}

	private void loadList() {
		List<Certificate> certificates = certificatesManager.getCertificates(assessedIdentity, courseEntry.getOlatResource());
		List<Links> certificatesLink = new ArrayList<>(certificates.size());
		int count = 0;
		for(Certificate certificate:certificates) {
			String displayName = formatter.formatDateAndTime(certificate.getCreationDate());
			String url = DownloadCertificateCellRenderer.getUrl(certificate);
			Links links = new Links(url, displayName, certificate.getStatus().name());
			certificatesLink.add(links);
			
			if(canDelete) {
				Link deleteLink = LinkFactory.createLink("delete." + count++, "delete",
						getTranslator(), mainVC, this, Link.NONTRANSLATED);
				deleteLink.setCustomDisplayText(" ");
				deleteLink.setElementCssClass("o_sel_certificate_delete");
				deleteLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
				deleteLink.setUserObject(certificate);
				links.setDelete(deleteLink);
			}
		}
		mainVC.contextPut("certificates", certificatesLink);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(generateLink == source) {
			doConfirmGenerateCertificate(ureq) ;
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("delete".equals(cmd)) {
				Certificate certificate = (Certificate)link.getUserObject();
				doConfirmDelete(ureq, certificate);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCertificateCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doGenerateCertificate(ureq);
			}
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				Certificate certificate = (Certificate)confirmDeleteCtrl.getUserObject();
				doDelete(certificate);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doConfirmDelete(UserRequest ureq, Certificate certificate) {
		String title = translate("confirm.delete.certificate.title");
		String text = translate("confirm.delete.certificate.text");
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(certificate);
	}
	
	private void doDelete(Certificate certificate) {
		certificatesManager.deleteCertificate(certificate);
		loadList();
		String displayName = formatter.formatDateAndTime(certificate.getCreationDate());
		showInfo("confirm.certificate.deleted", displayName);
	}

	private void doConfirmGenerateCertificate(UserRequest ureq) {
		if(certificatesManager.isCertificationAllowed(assessedIdentity, courseEntry)) {
			//don't need to confirm
			doGenerateCertificate(ureq);
		} else {
			String title = translate("confirm.certificate.title");
			String fullName = userManager.getUserDisplayName(assessedIdentity);
			String text = translate("confirm.certificate.description", new String[] { fullName });
			confirmCertificateCtrl = activateYesNoDialog(ureq, title, text, confirmCertificateCtrl);
		}
	}
	
	private void doGenerateCertificate(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		ScoreAccounting scoreAccounting = assessedUserCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll();
		ScoreEvaluation scoreEval = scoreAccounting.evalCourseNode((AssessableCourseNode)rootNode);

		CertificateTemplate template = null;
		Long templateKey = course.getCourseConfig().getCertificateTemplate();
		if(templateKey != null) {
			template = certificatesManager.getTemplateById(templateKey);
		}

		Float score = scoreEval == null ? null : scoreEval.getScore();
		Boolean passed = scoreEval == null ? null : scoreEval.getPassed();
		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, score, passed);
		certificatesManager.generateCertificate(certificateInfos, courseEntry, template, true);
		loadList();
		showInfo("msg.certificate.pending");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	public static class Links {
		private String url;
		private String name;
		private String status;
		private Link delete;
		
		public Links(String url, String name, String status) {
			this.url = url;
			this.name = name;
			this.status = status;
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getName() {
			return name;
		}
		
		public String getStatus() {
			return status;
		}

		public String getDeleteName() {
			return delete == null ? null : delete.getComponentName();
		}

		public void setDelete(Link delete) {
			this.delete = delete;
		}
	}
}