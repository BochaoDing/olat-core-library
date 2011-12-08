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
* <p>
*/ 

package org.olat.course.nodes.projectbroker.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupFactory;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.testutils.codepoints.server.Codepoint;


/**
 * 
 * @author guretzki
 */

public class ProjectGroupManagerImpl extends BasicManager implements ProjectGroupManager {
	
	//////////////////////
	// ACCOUNT MANAGEMENT
	//////////////////////
	public BusinessGroup getAccountManagerGroupFor(CoursePropertyManager cpm, CourseNode courseNode, ICourse course, String groupName, String groupDescription, Identity identity) {
		Long groupKey = null;
		BusinessGroup accountManagerGroup = null;
  	Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		// Check if account-manager-group-key-property already exist
		if (accountManagerGroupProperty != null) {
		  groupKey = accountManagerGroupProperty.getLongValue();
		  logDebug("accountManagerGroupProperty=" + accountManagerGroupProperty + "  groupKey=" + groupKey);
		} 
    logDebug("groupKey=" + groupKey);
    if (groupKey != null) {
			accountManagerGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(groupKey, false);
			logDebug("load businessgroup=" + accountManagerGroup);
			if (accountManagerGroup != null) {
				return accountManagerGroup;
			} else {
				if (accountManagerGroupProperty != null) {
					cpm.deleteProperty(accountManagerGroupProperty);
				}
				groupKey = null;
				logWarn("ProjectBroker: Account-manager does no longer exist, create a new one", null);
			}
    } else {
			logDebug("No group for project-broker exist => create a new one");
			BGContext context = createGroupContext(course);
			
			accountManagerGroup = BusinessGroupManagerImpl.getInstance().createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName, groupDescription, null, null, false, false, context);
			int i = 2;
			while (accountManagerGroup == null) {
				// group with this name exist already, try another name
				accountManagerGroup = BusinessGroupManagerImpl.getInstance().createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName + " _" + i, groupDescription, null, null, false, false, context);
				i++;
			}
			logDebug("createAndPersistBusinessGroup businessgroup=" + accountManagerGroup);			
			
			saveAccountManagerGroupKey(accountManagerGroup.getKey(), cpm, courseNode);
			logDebug("created account-manager default businessgroup=" + accountManagerGroup);
		} 
		return accountManagerGroup;
	}

	public void saveAccountManagerGroupKey(Long accountManagerGroupKey, CoursePropertyManager cpm, CourseNode courseNode) {
		Property accountManagerGroupKeyProperty = cpm.createCourseNodePropertyInstance(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY, null, accountManagerGroupKey, null, null);
		cpm.saveProperty(accountManagerGroupKeyProperty);	
		logDebug("saveAccountManagerGroupKey accountManagerGroupKey=" + accountManagerGroupKey);
	}

	public boolean isAccountManager(Identity identity, CoursePropertyManager cpm, CourseNode courseNode) {
  	Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
  	if (accountManagerGroupProperty != null) {
  	 	Long groupKey = accountManagerGroupProperty.getLongValue();
  		BusinessGroup accountManagerGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(groupKey, false);
  		if (accountManagerGroup != null) {
  			return isAccountManager(identity,  accountManagerGroup);
  		}
  	}
  	return false;
 	}

	public void deleteAccountManagerGroup( CoursePropertyManager cpm, CourseNode courseNode) {
		logDebug("deleteAccountManagerGroup start...");
  	Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
  	if (accountManagerGroupProperty != null) {
  		Long groupKey = accountManagerGroupProperty.getLongValue();
  		if (groupKey != null) {
				BusinessGroup accountManagerGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(groupKey, false);
				if (accountManagerGroup != null) {
					BusinessGroupManagerImpl.getInstance().deleteBusinessGroup(accountManagerGroup);
					logAudit("ProjectBroker: Deleted accountManagerGroup=" + accountManagerGroup);
				} else {
					logDebug("deleteAccountManagerGroup: accountManagerGroup=" + accountManagerGroup + " has already been deleted");
				}
			}
  		cpm.deleteProperty(accountManagerGroupProperty);
			logDebug("deleteAccountManagerGroup: deleted accountManagerGroupProperty=" + accountManagerGroupProperty );
 	} else {
			logDebug("deleteAccountManagerGroup: found no accountManagerGroup-key");
		}
	}

	public void updateAccountManagerGroupName(String groupName, String groupDescription, BusinessGroup accountManagerGroup) {
		// group could have been deleted, see FXOLAT-295
		if (accountManagerGroup != null){
			BusinessGroup reloadedBusinessGroup = (BusinessGroup)DBFactory.getInstance().loadObject(BusinessGroupImpl.class, accountManagerGroup.getKey());
			reloadedBusinessGroup.setName(groupName);
			reloadedBusinessGroup.setDescription(groupDescription);
			BusinessGroupManagerImpl.getInstance().updateBusinessGroup(reloadedBusinessGroup);
		}
	}


	////////////////////////////
	// PROJECT GROUP MANAGEMENT
	////////////////////////////
	public BusinessGroup createProjectGroupFor(Long projectBrokerId, Identity identity, String groupName, String groupDescription, Long courseId) {
		List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);

		BGContext context = createGroupContext(CourseFactory.loadCourse(courseId));
		logDebug("createProjectGroupFor groupName=" + groupName);
		BusinessGroup projectGroup = BusinessGroupManagerImpl.getInstance().createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName, groupDescription, null, null, false, false, context);
		// projectGroup could be null when a group with name already exists
		int counter = 2;
		while (projectGroup == null) {
			// name alreday exist try another one
			String newGroupName = groupName + " _" + counter ;
			projectGroup = BusinessGroupManagerImpl.getInstance().createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, newGroupName, groupDescription, null, null, false, false, context);
			counter++;
		}
		logDebug("Created a new projectGroup=" + projectGroup);
		return projectGroup;
	}
	
	public void deleteProjectGroupFor(Project project) {
		BusinessGroupManagerImpl.getInstance().deleteBusinessGroup(project.getProjectGroup());
	}
	
	/**
	 * Change group-name and description. Check if new group-name does not already exist in the course-group-context.
	 * If the goup-name already exist, it will be automatically try another one with suffix e.g. ' _2'
	 * @see org.olat.course.nodes.projectbroker.service.ProjectGroupManager#changeProjectGroupName(org.olat.group.BusinessGroup, java.lang.String, java.lang.String)
	 */
	public void changeProjectGroupName(BusinessGroup projectGroup, String initialGroupName, String groupDescription ) {
		BusinessGroup reloadedBusinessGroup = (BusinessGroup)DBFactory.getInstance().loadObject(BusinessGroupImpl.class, projectGroup.getKey());
		logDebug("initialGroupName=" + initialGroupName);
		String groupName = initialGroupName;
		Set names = new HashSet();
		names.add(groupName);
		int counter = 2;
		while (BusinessGroupFactory.checkIfOneOrMoreNameExistsInContext(names, reloadedBusinessGroup.getGroupContext())) {
		// a group with name already exist => look for an other one, append a number
			groupName = initialGroupName + " _" + counter++ ;
			logDebug("try groupName=" + groupName);
			names = new HashSet();
			names.add(groupName);
			
		}
		logDebug("groupName=" + groupName);
		reloadedBusinessGroup.setName(groupName);
		reloadedBusinessGroup.setDescription(groupDescription);
		BusinessGroupManagerImpl.getInstance().updateBusinessGroup(reloadedBusinessGroup);
	}

	public List<Identity> addCandidates(final List<Identity> addIdentities, final Project project) {
		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
		List<Identity> addedIdentities = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<List<Identity>>(){
			public List<Identity> execute() {
				List<Identity> addedIdentities = new ArrayList<Identity>();
				for (Identity identity : addIdentities) {
					if (!BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, project.getCandidateGroup()) ) {
						BaseSecurityManager.getInstance().addIdentityToSecurityGroup(identity, project.getCandidateGroup());
						addedIdentities.add(identity);
						logAudit("ProjectBroker: Add user as candidate, identity=" + identity);
					}
					// fireEvents ?
				}
				return addedIdentities;
			}
		});// end of doInSync
		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
		return addedIdentities;
	}

	public void removeCandidates(final List<Identity> addIdentities, final Project project) {
		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
		Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			public Boolean execute() {
				Project reloadedProject = (Project) DBFactory.getInstance().loadObject(project, true);
				for (Identity identity : addIdentities) {
					BaseSecurityManager.getInstance().removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
					logAudit("ProjectBroker: Remove user as candidate, identity=" + identity);
					// fireEvents ?
				}
				return Boolean.TRUE;
			}
		});// end of doInSync
		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
	}

	public BusinessGroupAddResponse acceptCandidates(final List<Identity> identities, final Project project, final Identity actionIdentity, final boolean autoSignOut, final boolean isAcceptSelectionManually) {
		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
		final Project reloadedProject = (Project) DBFactory.getInstance().loadObject(project, true);
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			public Boolean execute() {
				for (final Identity identity : identities) {
					if (!BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, reloadedProject.getProjectGroup().getPartipiciantGroup())) {
						BaseSecurityManager.getInstance().removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
						final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
						BusinessGroupManagerImpl.getInstance().addParticipantAndFireEvent(actionIdentity, identity, reloadedProject.getProjectGroup(), flags, false);
						logAudit("ProjectBroker: Accept candidate, identity=" + identity + " project=" + reloadedProject);
						response.getAddedIdentities().add(identity);
					} else {
						response.getIdentitiesAlreadyInGroup().add(identity);
					}				
				}
				return Boolean.TRUE;
			}
		});// end of doInSync
		if (autoSignOut && result.booleanValue()) {
			ProjectBrokerManagerFactory.getProjectBrokerManager().signOutFormAllCandidateList(response.getAddedIdentities(), reloadedProject.getProjectBroker().getKey());
		}
		if (isAcceptSelectionManually && (reloadedProject.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) 
				&& reloadedProject.getSelectedPlaces() >= reloadedProject.getMaxMembers()) {
			ProjectBrokerManagerFactory.getProjectBrokerManager().setProjectState(reloadedProject, Project.STATE_ASSIGNED);
			logInfo("ProjectBroker: Accept candidate, change project-state=" + Project.STATE_ASSIGNED);
		}

		Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
		return response;
	}

	@Override
	public void sendGroupChangeEvent(Project project, Long courseResourceableId, Identity identity) {
		ICourse course = CourseFactory.loadCourse(courseResourceableId);
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		MultiUserEvent modifiedEvent = new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, project.getProjectGroup(), identity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, ores);
	}

	public boolean isProjectManager(Identity identity, Project project) {
		return BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, project.getProjectLeaderGroup());
	}

	public boolean isProjectManagerOrAdministrator(UserRequest ureq, CourseEnvironment courseEnv, Project project) {	
		return    ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(ureq.getIdentity(), project)
				   || courseEnv.getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity())
	         || ureq.getUserSession().getRoles().isOLATAdmin();
	}
	
	public boolean isProjectParticipant(Identity identity, Project project) {
		return BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup());
	}

	public boolean isProjectCandidate(Identity identity, Project project) {
		return BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, project.getCandidateGroup());
	}

   public void setProjectGroupMaxMembers(BusinessGroup projectGroup, int maxMembers ) {
       BusinessGroup reloadedBusinessGroup = (BusinessGroup)DBFactory.getInstance().loadObject(BusinessGroupImpl.class, projectGroup.getKey());
       logDebug("ProjectGroup.name=" + reloadedBusinessGroup.getName() + " setMaxParticipants=" + maxMembers);
       reloadedBusinessGroup.setMaxParticipants(maxMembers);
      BusinessGroupManagerImpl.getInstance().updateBusinessGroup(reloadedBusinessGroup);
   }

	///////////////////
	// PRIVATE METHODS
	///////////////////
	private BGContext createGroupContext(ICourse course) {
		List<BGContext> groupContexts = course.getCourseEnvironment().getCourseGroupManager().getLearningGroupContexts();
		logDebug("createGroupContext groupContexts.size=" + groupContexts.size());
		for (Iterator<BGContext> iterator = groupContexts.iterator(); iterator.hasNext();) {
			BGContext iterContext = iterator.next();
			logDebug("createGroupContext loop iterContext=" + iterContext);
			if (iterContext.isDefaultContext()) {
				logDebug("createGroupContext default groupContexts=" + iterContext);
				return iterContext;
			}
		}
  	// found no default context
		String defaultContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + course.getCourseTitle();
		if (groupContexts.size() == 0) {
			logDebug("no group context exists, create a new default defaultContextName=" + defaultContextName);
		} else {
			logDebug("Found no default group context, create a new default defaultContextName=" + defaultContextName);
		}
		// no context exists => create a new default context
		OLATResource courseResource = OLATResourceManager.getInstance().findOrPersistResourceable(course);
		BGContext context = BGContextManagerImpl.getInstance().createAndAddBGContextToResource(defaultContextName, courseResource, BusinessGroup.TYPE_LEARNINGROUP, null, true);
		return context;
	}

	private boolean isAccountManager(Identity identity, BusinessGroup businessGroup) {
		if ( (businessGroup == null) || (businessGroup.getPartipiciantGroup() == null) ) {
			return false;
		}
		return    BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, businessGroup.getPartipiciantGroup())
				   || BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, businessGroup.getOwnerGroup());
	}

	@Override
	public void acceptAllCandidates(Long projectBrokerId, Identity actionIdentity, boolean autoSignOut, boolean isAcceptSelectionManually) {
		// loop over all project
		List<Project> projectList = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				logAudit("ProjectBroker: Accept ALL candidates, project=" + project);
				acceptCandidates(candidates, project, actionIdentity, autoSignOut, isAcceptSelectionManually);
			}
		}	
		
	}

	@Override
	public boolean hasProjectBrokerAnyCandidates(Long projectBrokerId) {
		List<Project> projectList = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCandidateListEmpty(SecurityGroup candidateGroup) {
		List<Identity> candidates = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(candidateGroup);
		return candidates.isEmpty();
	}

}
