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

package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.author.CreateEntryController;
import org.olat.repository.ui.author.CreateRepositoryEntryController;


/**
 * Initial Date:  Apr 5, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
/**
 * TODO sev26
 * Number of methods should be reduced.
 */
public interface RepositoryHandler {

	/**
	 * @return Return the typeNames of OLATResourceable this Handler can handle.
	 */
	public String getSupportedType();
	
	/**
	 * This resource support creation within OpenOLAT.
	 * @return
	 */
	public boolean isCreate();
	
	public String getCreateLabelI18nKey();
	
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Object createObject, Locale locale);
	
	/**
	 * Typically for course wizard
	 * @return
	 */
	public boolean isPostCreateWizardAvailable();
	
	/**
	 * 
	 * @param file
	 * @param filename
	 * @return
	 */
	public ResourceEvaluation acceptImport(File file, String filename);
	
	/**
	 * 
	 * @param initialAuthor
	 * @param initialAuthorAlt 
	 * @param displayname
	 * @param description
	 * @param withReferences if true import references
	 * @param locale
	 * @param file
	 * @param filename
	 * @return
	 */
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, boolean withReferences, Locale locale, File file, String filename);	

	/**
	 * 
	 * @param source
	 * @param target
	 * @return The target repository entry
	 */
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target);
	
	/**
	 * @return true if this handler supports donwloading Resourceables of its type.
	 */
	public boolean supportsDownload();

	/**
	 * @return true if this handler supports an editor for Resourceables of its type.
	 */
	public EditionSupport supportsEdit(OLATResourceable resource);
	
	/**
	 * If the resource handler can deliver an assessment details controller,
	 * it returns true.
	 * @return
	 */
	public boolean supportsAssessmentDetails();
	
	/**
	 * Return the container where image and files can be saved for the description field.
	 * the folder MUST be under the root folder has its name "media".
	 * @param repoEntry
	 * @return
	 */
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry);

	/**
	 * Called if a user launches a Resourceable that this handler can handle.
	 * @param reSecurity The permissions wrapper
	 * @param ureq
	 * @param wControl
	 * @param res
	 * @param initialViewIdentifier if null the default view will be started, otherwise a controllerfactory type dependant view will be activated (subscription subtype)
	 * @return Controller able to launch resourceable.
	 */
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl);
	
	/**
	 * Called if a user wants to edit a Resourceable that this handler can provide an editor for. 
	 * (it is given here that this method 
	 * can only be called when the current user is either olat admin or in the owning group of this resource
	 * @param ureq
	 * @param wControl
	 * @param toolbar
	 * @param res
	 * @return Controler able to edit resourceable.
	 */
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar);
	
	/**
	 * Called if a user wants to create a Resourceable via wizard.
	 * @param res
	 * @param ureq
	 * @param wControl
	 * @return Controller that guides trough the creation workflow via wizard.
	 */
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl);
	
	/**
	 * Called if a user wants to open the create repository entry dialog for a Resourceable
	 * @param ureq
	 * @param wControl
	 * @return Controller able to create resourceable.
	 */
	default CreateEntryController createCreateRepositoryEntryController(UserRequest ureq, WindowControl wControl) {
		return new CreateRepositoryEntryController(ureq, wControl, this);
	}
	
	/**
	 * Return the details controller for the assessed identity.
	 * 
	 * @param re
	 * @param ureq
	 * @param wControl
	 * @param toolbar
	 * @param assessedIdentity
	 * @return
	 */
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity);
	
	/**
	 * Called if a user downloads a Resourceable that this handler can handle.
	 * @param res
	 * @return MediaResource delivering resourceable.
	 */
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible);
	
	/**
	 * Called if the repository entry referencing the given Resourceable will be deleted
	 * from the repository. Do any necessary cleanup work specific to this handler's type.
	 * The handler is responsible for deleting the resourceable aswell.
	 * @param res
	 * @param ureq
	 * @param wControl
	 * @return true if delete successfull, false if not.
	 */
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res);

	/**
	 * Called if the repository entry referencing the given Resourceable will be deleted
	 * from the repository. Return status wether to proceed with the delete action. If
	 * this method returns false, the entry will not be deleted.
	 * @param res
	 * @param identity
	 * @param roles
	 * @param locale
	 * @param errors
	 * @return true if ressource is ready to delete, false if not.
	 */
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors);
	
	/**
	 * Acquires lock for the input ores and identity.
	 * @param ores
	 * @param identity
	 * @return the LockResult or null if no locking supported.
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity);
	
	/**
	 * Releases the lock.
	 * 
	 * @param lockResult the LockResult received when locking
	 */
	public void releaseLock(LockResult lockResult);
	
	/**
	 * 
	 * @param ores
	 * @return
	 */
	public boolean isLocked(OLATResourceable ores); 
	
}
