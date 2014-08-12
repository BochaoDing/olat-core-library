/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.repository.handlers;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQPreviewSecurityCallback;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.OLATResource;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.run.OnyxRunController;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class QTITestHandler extends QTIHandler {
	
	@Override
	public boolean isCreate() {
		return true;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.test";
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
		TestFileResource ores = new TestFileResource();
		return super.createResource(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS, ores, initialAuthor, displayname, description, locale);
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		ResourceEvaluation eval = TestFileResource.evaluate(file, filename);
		if(!eval.isValid() && CoreSpringFactory.getImpl(OnyxModule.class).isEnabled()) {
			eval = OnyxModule.isOnyxTest(file, filename);
		}
		return eval;
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Locale locale, File file, String filename) {
		return super.importResource(initialAuthor, displayname, description, new TestFileResource(), file, filename);
	}

	@Override
	public String getSupportedType() {
		return TestFileResource.TYPE_NAME;
	}

	@Override
	public boolean supportsLaunch() {
		return true;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public boolean supportsEdit(OLATResourceable resource) { 
		if(resource != null && OnyxModule.isOnyxTest(resource)) {
			return false;
		}
		return true;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}
	
	/**
	 * @param res
	 * @param ureq
	 * @param wControl
	 * @return Controller
	 */
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		Controller runController;
		OLATResource res = re.getOlatResource();
		if (OnyxModule.isOnyxTest(res)) {
			// <OLATCE-1054>
			runController = new OnyxRunController(ureq, wControl, re, false);
		} else {
			Resolver resolver = new ImsRepositoryResolver(re);
			IQSecurityCallback secCallback = new IQPreviewSecurityCallback();
			runController = CoreSpringFactory.getImpl(IQManager.class)
				.createIQDisplayController(res, resolver, AssessmentInstance.QMD_ENTRY_TYPE_SELF, secCallback, ureq, wControl);
		}
		
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, runController);
		layoutCtr.addDisposableChildController(runController); // dispose content on layout dispose
		return layoutCtr;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		OLATResource res = re.getOlatResource();
		if(OnyxModule.isOnyxTest(res)) {
			return null;
		}

		TestFileResource fr = new TestFileResource();
		fr.overrideResourceableId(res.getResourceableId());
		
		//check if we can edit in restricted mode -> only typos 
		ReferenceManager refM = ReferenceManager.getInstance();
		List<ReferenceImpl> referencees = refM.getReferencesTo(res);
		//String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		//boolean restrictedEdit = referencesSummary != null;
		QTIEditorMainController editor =  new QTIEditorMainController(referencees,ureq, wControl, fr);
		if (editor.isLockedSuccessfully()) {
			return editor;
		} else {
			return null;
		}
	}

	@Override
	protected String getDeletedFilePrefix() {
		return "del_qtitest_"; 
	}

	@Override
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("not implemented");
	}
}