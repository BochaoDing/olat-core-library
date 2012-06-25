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

package org.olat.modules.iq;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Util;
import org.olat.core.util.controller.OLATResourceableListeningWrapperController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.HttpItemInput;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.ItemInput;
import org.olat.ims.qti.container.ItemsInput;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.process.Resolver;
import org.olat.ims.qti.render.LocalizedXSLTransformer;
import org.olat.ims.qti.render.ResultsBuilder;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataDeletable;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Initial Date: Mar 4, 2004
 * @author Mike Stock
 */
public class IQManager extends BasicManager implements UserDataDeletable {

	private static IQManager INSTANCE;
	
	/**
	 *  [spring]
	 */
	private IQManager(UserDeletionManager userDeletionManager) {
		userDeletionManager.registerDeletableUserData(this);
		INSTANCE = this;
	}

	/**
	 * @return Singleton.
	 */
	public static IQManager getInstance() { 
		return INSTANCE; 
	}
	
	
	//--- methods for controller creation
	/**
	 * IMS QTI Display Controller from within course -> moduleConfiguration
	 * 
	 * concurrent access check needed -> Editor may save (commit changes) while displaying reads old/new data mix (files and xml structure)
	 *  
	 */
	public Controller createIQDisplayController(ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback, UserRequest ureq,
			WindowControl wControl, long callingResId, String callingResDetail) {
		
		//two cases:
		// -- VERY RARE CASE -- 1) qti is open in an editor session right now on the screen (or session on the way to timeout)
		// -- 99% of cases   -- 2) qti is ready to be run as test/survey
		String repositorySoftkey = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true);
		if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(re.getOlatResource(), null)){
			Translator translator = Util.createPackageTranslator(this.getClass(), ureq.getLocale());
      //so this resource is locked, let's find out who locked it
			LockResult lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(re.getOlatResource(), ureq.getIdentity(), null);
			return MessageUIFactory.createInfoMessage(ureq, wControl, translator.translate("status.currently.locked.title"), 
					translator.translate("status.currently.locked", new String[] {lockResult.getOwner().getName()}));
		}else{
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(re, OlatResourceableType.iq));
			return new IQDisplayController(moduleConfiguration, secCallback, ureq, wControl, callingResId, callingResDetail);
		}
	}

	/**
	 * IMS QTI Display Controller used by QTI Editor for preview.
	 * 
	 * no concurrency protection needed here -> it is Editor <-> Preview of edited file
	 * 
	 * @param resolver
	 * @param type
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 */
	public IQDisplayController createIQDisplayController(Resolver resolver, String type, IQSecurityCallback secCallback, 
			UserRequest ureq, WindowControl wControl) {
		return new IQDisplayController(resolver, type, secCallback, ureq, wControl);
	}
	
	/**
	 * IMS QTI Display Controller used for IMS course node run view, or for the direct launching from learning resources.
	 * 
	 * concurrent access check needed -> Editor may save (commit changes) while displaying reads old/new data mix (files and xml structure)
	 * 
	 * 
	 * @param res
	 * @param resolver
	 * @param type
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public MainLayoutController createIQDisplayController(OLATResourceable res, Resolver resolver, String type, IQSecurityCallback secCallback, 
			UserRequest ureq, WindowControl wControl) {
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(res, OlatResourceableType.iq));
		
		//two cases:
		// -- VERY RARE CASE -- 1) qti is open in an editor session right now on the screen (or session on the way to timeout)
		// -- 99% of cases   -- 2) qti is ready to be run as test/survey
		if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(res, null)){
			GenericMainController glc = createLockedMessageController(ureq, wControl);
			glc.init(ureq);
			return glc;
		}else{
			Controller controller = new IQDisplayController(resolver, type, secCallback, ureq, wControl);
			//fxdiff BAKS-7 Resume function
			OLATResourceableListeningWrapperController dwc = new OLATResourceableListeningWrapperController(ureq, wControl, res, controller, null, ureq.getIdentity());
			return dwc;
		}
	}

	private GenericMainController createLockedMessageController(UserRequest ureq, WindowControl wControl) {
		//
		//wrap simple message into mainLayout
		GenericMainController glc = new GenericMainController(ureq, wControl) {
		
			private MessageController contentCtr;
			private Panel empty;
			private LayoutMain3ColsController columnLayoutCtr;

			@Override
			public void init(UserRequest ureq) {
				empty = new Panel("empty");			Translator translator = Util.createPackageTranslator(this.getClass(), ureq.getLocale()); 
				contentCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), translator.translate("status.currently.locked.title"), translator.translate("status.currently.locked"));
				listenTo(contentCtr); // auto dispose later
				Component resComp = contentCtr.getInitialComponent();

				columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), empty, empty, resComp, /*do not save no prefs*/null);
				listenTo(columnLayoutCtr); // auto dispose later
				putInitialPanel(columnLayoutCtr.getInitialComponent());
			}
		
			@Override
			protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
				//no menutree means no menu events.
				return null;
			}
		
		};
		return glc;
	}
	
	// --- end of controller creation
	/**
	 * 
	 * @param ai
	 * @param ureq
	 * @return
	 */
	public Document getResultsReporting(AssessmentInstance ai, Identity assessedIdentity, Locale locale) {
		ResultsBuilder resB = new ResultsBuilder();
		return resB.getResDoc(ai, locale, assessedIdentity);
	}

	/**
	 * 
	 * @param identity
	 * @param type
	 * @param assessID
	 * @return
	 */
	public Document getResultsReportingFromFile(Identity identity, String type, long assessID) {
		return FilePersister.retreiveResultsReporting(identity, type, assessID);
	}

	/**
	 * 
	 * @param docResReporting
	 * @param locale
	 * @param detailed
	 * @return
	 */
	public String transformResultsReporting(Document docResReporting, Locale locale, int summaryType) {
		switch (summaryType) {
			case AssessmentInstance.SUMMARY_COMPACT: // Result summary without solutions
				                                       ResultsBuilder.stripDetails(docResReporting);
			                                         break;
			case AssessmentInstance.SUMMARY_SECTION: // Section summary without solutions
				                                       ResultsBuilder.stripItemResults(docResReporting);
				                                       System.out.println("TEST: Section summary without solutions");
				                                       break;
			case AssessmentInstance.SUMMARY_DETAILED:// Strip nothing 
                                               break;
			default: // default => Strip nothing 
				       break;
		}
		StringBuilder sb = LocalizedXSLTransformer.getInstance(locale).renderResults(docResReporting);
		return sb.toString();
	}

	/**
	 * Extract item inputs from http request
	 * 
	 * @param ureq The request to extract item responses from.
	 * @return ItemsInput
	 */
	public ItemsInput getItemsInput(UserRequest ureq) {
		ItemsInput result = new ItemsInput();
		Enumeration params = ureq.getHttpReq().getParameterNames();
		while (params.hasMoreElements()) {
			String paramKey = (String) params.nextElement();
			StringTokenizer st = new StringTokenizer(paramKey, "§", false);
			String value = ureq.getParameter(paramKey);
			if (st.countTokens() == 4) {
				String itemType = st.nextToken();
				String itemIdent = st.nextToken();
				String responseID = st.nextToken();
				HttpItemInput itemInput = (HttpItemInput) result.getItemInput(itemIdent);
				if (itemInput == null) {
					itemInput = new HttpItemInput(itemIdent);
					result.addItemInput(itemInput);
				}
				// 'dummy' type is used to make sure iteminput is constructed for
				// all items. it does not provide any response data
				if (itemType.equals("qti")) itemInput.putSingle(responseID, value);
			}
			// refactoring to new setFormDirty() javascript method sends now an additional param "olat_fosm" which has no tokens inside
			// so assertExc. is useless.
			//else {
			//	throw new AssertException ("not 4 tokens in form name: orig='"+paramKey+"'");
			//}
			//<input id="QTI_1098869464495" type="checkbox" 
			// name="qti§QTIEDIT:MCQ:1098869464490§1098869464492§1098869464495" .... 
		}
		return result;
	}

	/**
	 * Create the QTIResults on the database for a given assessments,
	 * self-assessment or survey. These database entries can be used for
	 * statistical downloads.
	 * 
	 * 
	 * @param ai
	 * @param resId
	 * @param resDetail
	 * @param ureq
	 */

	public void persistResults(AssessmentInstance ai, long resId, String resDetail, Identity assessedIdentity, String remoteAddr) {
		AssessmentContext ac = ai.getAssessmentContext();
		
		QTIResultSet qtiResultSet = new QTIResultSet();
		qtiResultSet.setLastModified(new Date(System.currentTimeMillis()));
		qtiResultSet.setOlatResource(resId);
		qtiResultSet.setOlatResourceDetail(resDetail);
		qtiResultSet.setRepositoryRef(ai.getRepositoryEntryKey());
		qtiResultSet.setIdentity(assessedIdentity);
		qtiResultSet.setQtiType(ai.getType());
		qtiResultSet.setAssessmentID(ai.getAssessID());
		
		qtiResultSet.setDuration(new Long(ai.getAssessmentContext().getDuration()));
		// TODO qtiResultSet.setLastModified();
		
		if (ai.isSurvey()){
			qtiResultSet.setScore(0);
			qtiResultSet.setIsPassed(true);
		} else {
			qtiResultSet.setScore(ac.getScore());
			qtiResultSet.setIsPassed(ac.isPassed());
		}
		
		DBFactory.getInstance().saveObject(qtiResultSet);
		
		// Loop over all sections in this assessment
		int sccnt = ac.getSectionContextCount();
		for (int i = 0; i < sccnt; i++) {
			// Loop over all items in this section
			SectionContext sc = ac.getSectionContext(i);
			int iccnt = sc.getItemContextCount();
			for (int j = 0; j < iccnt; j++) {
				ItemContext ic = sc.getItemContext(j);
				// Create new result item for this item
				QTIResult qtiResult = new QTIResult();
				qtiResult.setResultSet(qtiResultSet);
				qtiResult.setItemIdent(ic.getIdent());
				qtiResult.setDuration(new Long(ic.getTimeSpent()));
				if (ai.isSurvey()) qtiResult.setScore(0);
				else qtiResult.setScore(ic.getScore());
				qtiResult.setTstamp(new Date(ic.getLatestAnswerTime()));
				qtiResult.setLastModified(new Date(System.currentTimeMillis()));
				qtiResult.setIp(remoteAddr);
				
				// Get user answers for this item
				StringBuilder sb = new StringBuilder();
				if (ic.getItemInput() == null) {} else {
					ItemInput inp = ic.getItemInput();
					if (inp.isEmpty()) {
						sb.append("[]");
					} else {
						Map im = inp.getInputMap();
						// Create answer block
						Set keys = im.keySet();
						Iterator iter = keys.iterator();
						while (iter.hasNext()) {
							String ident = (String) iter.next();
							sb.append(ident); // response_lid ident
							sb.append("[");
							List answers = inp.getAsList(ident);
							for (int y = 0; y < answers.size(); y++) {
								sb.append("[");
								String answer = (String) answers.get(y);
								// answer is referenced to response_label ident, if
								// render_choice
								// answer is userinput, if render_fib
								answer = quoteSpecialQTIResultCharacters(answer);
								sb.append(answer);
								sb.append("]");
							}
							sb.append("]");
						}
					}
				}
				qtiResult.setAnswer(sb.toString());
				// Persist result data in database
				DBFactory.getInstance().saveObject(qtiResult);
			}
		}
	}
	/**
	 * 
	 * @param assessmentID
	 * @return
	 */
	public List findQtiResults(long assessmentID) {
		DB persister = DBFactory.getInstance();
		return persister.find("from q in class org.olat.ims.qti.QTIResult where q.assessmentID = ?",
				new Long(assessmentID), StandardBasicTypes.LONG);
	}

	/**
	 * 
	 * @param assessmentID
	 * @param versionID
	 * @return
	 */
	public List findQtiResults(long assessmentID, long versionID) {
		DB persister = DBFactory.getInstance();
		return persister.find("from q in class org.olat.ims.qti.QTIResult where "
				+ "q.assessmentID = ? and q.versionid = ?", new Object[]{new Long(assessmentID), new Long(versionID)},
				new Type[]{StandardBasicTypes.LONG, StandardBasicTypes.LONG});
	}
	
	/**
	 * 
	 * @param assessmentID
	 * @param versionID
	 * @param itemIdent
	 * @return
	 */
	public List findQtiResults(long assessmentID, long versionID, String itemIdent) {
		DB persister = DBFactory.getInstance();
		return persister.find("from q in class org.olat.ims.qti.QTIResult where "
				+ "q.assessmentID = ? and q.versionid = ? and q.itemident = ?", new Object[]{new Long(assessmentID),
				new Long(versionID), itemIdent}, new Type[]{StandardBasicTypes.LONG, StandardBasicTypes.LONG, StandardBasicTypes.STRING});
	}
	
	/**
	 * 
	 * @param identity
	 * @param assessmentID
	 * @return
	 */
//TODO: chg: No References to this method, QTIResult has no identity attribute => Query does NOT work !!! => Remove this code	
//	public List findQtiResults(Identity identity, long assessmentID) {
//		DB persister = DBFactory.getInstance();
//		return persister.find("from q in class org.olat.ims.qti.QTIResult where " + "q.assessmentID = ? and q.identity = ?",
//				new Object[]{new Long(assessmentID), identity.getKey()}, new Type[]{Hibernate.LONG, Hibernate.LONG});
//	}


	/**
	 * 
	 * @param identity
	 * @param assessmentID
	 * @param versionID
	 * @return
	 */
// TODO: chg: No References to this method, QTIResult has no identity attribute => Query does NOT work !!! => Remove this code	
//	public List findQtiResults(Identity identity, long assessmentID, long versionID) {
//		DB persister = DBFactory.getInstance();
//		return persister.find("from q in class org.olat.ims.qti.QTIResult where "
//				+ "q.assessmentID = ? and q.versionid = ? and q.identity = ?", new Object[]{new Long(assessmentID),
//				new Long(versionID), identity.getKey()}, new Type[]{Hibernate.LONG, Hibernate.LONG, Hibernate.LONG});
//	}

	/**
	 * 
	 * @param identity
	 * @param assessmentID
	 * @param versionID
	 * @param itemIdent
	 * @return
	 */
//TODO: chg: No References to this method, QTIResult has no identity attribute => Query does NOT work !!! => Remove this code	
//	public List findQtiResults(Identity identity, long assessmentID, long versionID, String itemIdent) {
//		DB persister = DBFactory.getInstance();
//		return persister.find("from q in class org.olat.ims.qti.QTIResult where "
//				+ "q.assessmentID = ? and q.versionid = ? and q.identity = ? and q.itemident = ?", new Object[]{
//				new Long(assessmentID), new Long(versionID), identity.getKey(), itemIdent}, new Type[]{Hibernate.LONG,
//				Hibernate.LONG, Hibernate.LONG, Hibernate.STRING});
//	}

	/**
	 * Qotes special characters used by the QTIResult answer formatting. Special
	 * characters are '\', '[', ']', '\t', '\n', '\r', '\f', '\a' and '\e'
	 * 
	 * @param string The string to be quoted
	 * @return The quoted string
	 */
	public String quoteSpecialQTIResultCharacters(String string) {
		string = string.replaceAll("\\\\", "\\\\\\\\");
		string = string.replaceAll("\\[", "\\\\[");
		string = string.replaceAll("\\]", "\\\\]");
		string = string.replaceAll("\\t", "\\\\t");
		string = string.replaceAll("\\n", "\\\\n");
		string = string.replaceAll("\\r", "\\\\r");
		string = string.replaceAll("\\f", "\\\\f");
		string = string.replaceAll("\\a", "\\\\a");
		string = string.replaceAll("\\e", "\\\\e");
		return string;
	}

	/**
	 * Unquotes special characters in the QTIResult answer texts.
	 * 
	 * @see org.olat.modules.iq.IQManager#quoteSpecialQTIResultCharacters(String)
	 * @param string
	 * @return The unquoted sting
	 */
	public String unQuoteSpecialQTIResultCharacters(String string) {
		string = string.replaceAll("\\\\[", "\\[");
		string = string.replaceAll("\\\\]", "\\]");
		string = string.replaceAll("\\\\t", "\\t");
		string = string.replaceAll("\\\\n", "\\n");
		string = string.replaceAll("\\\\r", "\\r");
		string = string.replaceAll("\\\\f", "\\f");
		string = string.replaceAll("\\\\a", "\\a");
		string = string.replaceAll("\\\\e", "\\e");
		string = string.replaceAll("\\\\\\\\", "\\\\");
		return string;
	}

	/**
	 * Delete all qti.ser and qti-resreporting files.
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		FilePersister.deleteUserData(identity);
		Tracing.logDebug("Delete all qti.ser data and qti-resreporting data for identity=" + identity, this.getClass());
	}
	
	/**
	 * Returns null if no QTIResultSet found.
	 * @param identity
	 * @param olatResource
	 * @param olatResourceDetail
	 * @return Returns the last recorded QTIResultSet
	 */
	public QTIResultSet getLastResultSet(Identity identity, long olatResource, String olatResourceDetail) {
		QTIResultSet returnQTIResultSet=null;
		DB persister = DBFactory.getInstance();
		List resultSetList =  persister.find("from q in class org.olat.ims.qti.QTIResultSet where "
				+ "q.identity = ? and q.olatResource = ? and q.olatResourceDetail = ? order by q.creationDate desc", new Object[]{identity.getKey(), new Long(olatResource), new String(olatResourceDetail)},
				new Type[]{StandardBasicTypes.LONG, StandardBasicTypes.LONG, StandardBasicTypes.STRING});
		Iterator resultSetIterator = resultSetList.iterator();
		while(resultSetIterator.hasNext()) {
			returnQTIResultSet = (QTIResultSet)resultSetIterator.next();
			break;
		}
		return returnQTIResultSet;
	}
	
	/**
	 * This should only be used as fallback solution if the assessmentID is not available via the AssessmentManager
	 * (migration of old tests)
	 * @param identity
	 * @param olatResource is the course id
	 * @param olatResourceDetail is the node id
	 * @return Returns the last assessmentID if at least a QTIResultSet was stored for the input variables, null otherwise.
	 */
	public Long getLastAssessmentID(Identity identity, long olatResource, String olatResourceDetail) {
		QTIResultSet resultSet = getLastResultSet(identity, olatResource, olatResourceDetail);
		if(resultSet!=null) {
			return resultSet.getAssessmentID();
		}
		return null;
	}

	/**
	 * Get identities with exists qti.ser file.
	 * @param resourceableId
	 * @param ident
	 * @return
	 */
	public List<Identity> getIdentitiesWithQtiSerEntry(Long resourceableId, String ident) {		
		List<Identity> identities = new ArrayList<Identity>();
		LocalFolderImpl item = new LocalFolderImpl(new File(FilePersister.getFullPathToCourseNodeDirectory(Long.toString(resourceableId), ident)));
		if (VFSManager.exists(item)) {
			for (VFSItem identityFolder : item.getItems()) {
				Identity identity = BaseSecurityManager.getInstance().findIdentityByName(identityFolder.getName());
				if (identity != null) identities.add(identity);
			}
		}
		
		return identities;
	}

	/**
	 * Removes course node directory including qti.ser files of different users. 
	 * @param resourceableId
	 * @param ident
	 * @return
	 */
	public VFSStatus removeQtiSerFiles(Long resourceableId, String ident) {
		if (resourceableId == null || ident == null || ident.length() == 0) return VFSConstants.NO;
		LocalFolderImpl item = new LocalFolderImpl(new File(FilePersister.getFullPathToCourseNodeDirectory(Long.toString(resourceableId), ident)));
		if(item.canDelete().equals(VFSConstants.YES)) return item.delete();
		return VFSConstants.NO;
	}
	
	
	
}