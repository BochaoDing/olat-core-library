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
package org.olat.ims.qti21.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.User;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.core.util.crypto.X509CertificatePrivateKeyPair;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.xml.XMLDigitalSignatureUtil;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestMarks;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21ContentPackage;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.audit.AssessmentSessionAuditFileLog;
import org.olat.ims.qti21.manager.audit.AssessmentSessionAuditOLog;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.DigitalSignatureValidation;
import org.olat.ims.qti21.model.InMemoryAssessmentTestMarks;
import org.olat.ims.qti21.model.InMemoryAssessmentTestSession;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.QtiConstants;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObject;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.result.AbstractResult;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiObjectReadResult;
import uk.ac.ed.ph.jqtiplus.reading.QtiObjectReader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.state.marshalling.ItemSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.state.marshalling.TestSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlFactories;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltSerializationOptions;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;
import uk.ac.ed.ph.qtiworks.mathassess.GlueValueBinder;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessConstants;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessExtensionPackage;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21ServiceImpl implements QTI21Service, UserDataDeletable, InitializingBean, DisposableBean {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ServiceImpl.class);
	
	private static XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		configXstream.alias("deliveryOptions", QTI21DeliveryOptions.class);
		configXstream.alias("assessmentResultsOptions", QTI21AssessmentResultsOptions.class);
	}
	
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private AssessmentResponseDAO testResponseDao;
	@Autowired
	private AssessmentTestMarksDAO testMarksDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private MailManager mailManager;
	

	private JqtiExtensionManager jqtiExtensionManager;
	private XsltStylesheetManager xsltStylesheetManager;
	private InfinispanXsltStylesheetCache xsltStylesheetCache;
	private CacheWrapper<File,ResolvedAssessmentTest> assessmentTestsCache;
	private CacheWrapper<File,ResolvedAssessmentItem> assessmentItemsCache;
	
	private final ConcurrentMap<String,URI> resourceToTestURI = new ConcurrentHashMap<>();
	
	@Autowired
	public QTI21ServiceImpl(InfinispanXsltStylesheetCache xsltStylesheetCache) {
		this.xsltStylesheetCache = xsltStylesheetCache;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
    	final List<JqtiExtensionPackage<?>> extensionPackages = new ArrayList<JqtiExtensionPackage<?>>();

        /* Enable MathAssess extensions if requested */
        if (qtiModule.isMathAssessExtensionEnabled()) {
            log.info("Enabling the MathAssess extensions");
            extensionPackages.add(new MathAssessExtensionPackage(xsltStylesheetCache));
        }
        jqtiExtensionManager = new JqtiExtensionManager(extensionPackages);
        xsltStylesheetManager = new XsltStylesheetManager(new ClassPathResourceLocator(), xsltStylesheetCache);
        
        jqtiExtensionManager.init();

        Cacher cacher = coordinatorManager.getInstance().getCoordinator().getCacher();
        assessmentTestsCache = cacher.getCache("QTIWorks", "assessmentTests");
        assessmentItemsCache = cacher.getCache("QTIWorks", "assessmentItems");
	}

    @Override
	public void destroy() throws Exception {
		if(jqtiExtensionManager != null) {
			jqtiExtensionManager.destroy();
		}
	}

	@Override
	public XsltStylesheetCache getXsltStylesheetCache() {
		return xsltStylesheetCache;
	}

	@Override
    public XsltStylesheetManager getXsltStylesheetManager() {
    	return xsltStylesheetManager;
    }

    @Override
    public JqtiExtensionManager jqtiExtensionManager() {
        return jqtiExtensionManager;
    }
    
    @Override
    public QtiSerializer qtiSerializer() {
        return new QtiSerializer(jqtiExtensionManager());
    }
    
    @Override
    public QtiXmlReader qtiXmlReader() {
    	return new QtiXmlReader(jqtiExtensionManager());
    }
	
	@Override
	public QTI21DeliveryOptions getDeliveryOptions(RepositoryEntry testEntry) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(testEntry.getOlatResource());
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		
		QTI21DeliveryOptions config;
		if(configXml.exists()) {
			config = (QTI21DeliveryOptions)configXstream.fromXML(configXml);
		} else {
			//set default config
			config = QTI21DeliveryOptions.defaultSettings();
			setDeliveryOptions(testEntry, config);
		}
		return config;
	}

	@Override
	public void setDeliveryOptions(RepositoryEntry testEntry, QTI21DeliveryOptions options) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(testEntry.getOlatResource());
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(options == null) {
			if(configXml.exists()) {
				configXml.delete();
			}
		} else {
			try (OutputStream out = new FileOutputStream(configXml)) {
				configXstream.toXML(options, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public boolean isAssessmentTestActivelyUsed(RepositoryEntry testEntry) {
		return testSessionDao.hasActiveTestSession(testEntry);
	}

	@Override
	public ResolvedAssessmentTest loadAndResolveAssessmentTest(File resourceDirectory, boolean replace, boolean debugInfo) {
        URI assessmentObjectSystemId = createAssessmentTestUri(resourceDirectory);
        if(assessmentObjectSystemId == null) {
        	return null;
        }
		File resourceFile = new File(assessmentObjectSystemId);
		if(replace) {
			ResolvedAssessmentTest resolvedAssessmentTest = internalLoadAndResolveAssessmentTest(resourceDirectory, assessmentObjectSystemId);
			assessmentTestsCache.replace(resourceFile, resolvedAssessmentTest);
			return resolvedAssessmentTest;
		}
		return assessmentTestsCache.computeIfAbsent(resourceFile, file -> {
	        return internalLoadAndResolveAssessmentTest(resourceDirectory, assessmentObjectSystemId);
		});
	}
	
	private ResolvedAssessmentTest internalLoadAndResolveAssessmentTest(File resourceDirectory, URI assessmentObjectSystemId) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(resourceDirectory.toPath());
		ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, inputResourceLocator);
        return assessmentObjectXmlLoader.loadAndResolveAssessmentTest(assessmentObjectSystemId);
	}
	
	@Override
	public ResolvedAssessmentItem loadAndResolveAssessmentItem(URI assessmentObjectSystemId, File resourceDirectory) {
		File resourceFile = new File(assessmentObjectSystemId);
		return assessmentItemsCache.computeIfAbsent(resourceFile, (file) -> {
	       	return loadAndResolveAssessmentItemForCopy(assessmentObjectSystemId, resourceDirectory);
		});
	}
	
	@Override
	public ResolvedAssessmentItem loadAndResolveAssessmentItemForCopy(URI assessmentObjectSystemId, File resourceDirectory) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(resourceDirectory.toPath());
		ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, inputResourceLocator);
       	return assessmentObjectXmlLoader.loadAndResolveAssessmentItem(assessmentObjectSystemId);
	}
	
	@Override
	public boolean updateAssesmentObject(File resourceFile, ResolvedAssessmentObject<?> resolvedAssessmentObject) {
		AssessmentObject assessmentObject;
		if(resolvedAssessmentObject instanceof ResolvedAssessmentItem) {
			assessmentObject = ((ResolvedAssessmentItem)resolvedAssessmentObject)
					.getItemLookup().getRootNodeHolder().getRootNode();
		} else if(resolvedAssessmentObject instanceof ResolvedAssessmentTest) {
			assessmentObject = ((ResolvedAssessmentTest)resolvedAssessmentObject)
					.getTestLookup().getRootNodeHolder().getRootNode();
		} else {
			return false;
		}
		return persistAssessmentObject(resourceFile, assessmentObject);
	}

	@Override
	public boolean persistAssessmentObject(File resourceFile, AssessmentObject assessmentObject) {
		try(FileOutputStream out = new FileOutputStream(resourceFile)) {
			qtiSerializer().serializeJqtiObject(assessmentObject, out);
			//TODO qti
			assessmentTestsCache.remove(resourceFile);
			assessmentItemsCache.remove(resourceFile);
			return true;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	@Override
	public boolean needManualCorrection(RepositoryEntry testEntry) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		return AssessmentTestHelper.needManualCorrection(resolvedAssessmentTest);
	}

	@Override
	public URI createAssessmentTestUri(final File resourceDirectory) {
		final String key = resourceDirectory.getAbsolutePath();
		try {
			return resourceToTestURI.computeIfAbsent(key, (directoryAbsolutPath) -> {
				File manifestPath = new File(resourceDirectory, "imsmanifest.xml");
				QTI21ContentPackage	cp = new QTI21ContentPackage(manifestPath.toPath());
				try {
					Path testPath = cp.getTest();
					return testPath.toUri();
				} catch (IOException e) {
					log.error("", e);
					return null;
				}
			});
		} catch (RuntimeException e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public int deleteUserDataPriority() {
		return 10;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		List<AssessmentTestSession> sessions = testSessionDao.getUserTestSessions(identity);
		for(AssessmentTestSession session:sessions) {
			testSessionDao.deleteTestSession(session);
		}
	}

	@Override
	public boolean deleteAssessmentTestSession(List<Identity> identities, RepositoryEntryRef testEntry, RepositoryEntryRef entry, String subIdent) {
		Set<AssessmentEntry> entries = new HashSet<>();
		for(Identity identity:identities) {
			List<AssessmentTestSession> sessions = testSessionDao.getTestSessions(testEntry, entry, subIdent, identity);
			for(AssessmentTestSession session:sessions) {
				if(session.getAssessmentEntry() != null) {
					entries.add(session.getAssessmentEntry());
				}
				File fileStorage = testSessionDao.getSessionStorage(session);
				testSessionDao.deleteTestSession(session);
				FileUtils.deleteDirsAndFiles(fileStorage, true, true);
			}
		}
		
		for(AssessmentEntry assessmentEntry:entries) {
			assessmentEntryDao.resetAssessmentEntry(assessmentEntry);
		}
		return true;
	}

	@Override
	public boolean deleteAuthorAssessmentTestSession(RepositoryEntryRef testEntry) {
		List<AssessmentTestSession> sessions = testSessionDao.getAuthorAssessmentTestSession(testEntry);
		for(AssessmentTestSession session:sessions) {
			File fileStorage = testSessionDao.getSessionStorage(session);
			testSessionDao.deleteTestSession(session);
			FileUtils.deleteDirsAndFiles(fileStorage, true, true);
		}
		return true;
	}
	
	@Override
	public boolean deleteAssessmentTestSession(AssessmentTestSession testSession) {
		if(testSession == null || testSession.getKey() == null) return false;
		int rows = testSessionDao.deleteTestSession(testSession);
		return rows > 0;
	}

	@Override
	public AssessmentSessionAuditLogger getAssessmentSessionAuditLogger(AssessmentTestSession session, boolean authorMode) {
		if(authorMode) {
			return new AssessmentSessionAuditOLog();
		}
		if(session.getIdentity() == null && StringHelper.containsNonWhitespace(session.getAnonymousIdentifier())) {
			return new AssessmentSessionAuditOLog();
		}
		try {
			File userStorage = testSessionDao.getSessionStorage(session);
			File auditLog = new File(userStorage, "audit.log");
			FileOutputStream outputStream = new FileOutputStream(auditLog, true);
			return new AssessmentSessionAuditFileLog(outputStream);
		} catch (IOException e) {
			log.error("Cannot open the user specific log audit, fall back to OLog", e);
			return new AssessmentSessionAuditOLog();
		}
	}

	@Override
	public AssessmentTestSession createAssessmentTestSession(Identity identity, String anonymousIdentifier,
			AssessmentEntry assessmentEntry,  RepositoryEntry entry, String subIdent, RepositoryEntry testEntry,
			boolean authorMode) {
		return testSessionDao.createAndPersistTestSession(testEntry, entry, subIdent, assessmentEntry, identity, anonymousIdentifier, authorMode);
	}

	@Override
	public AssessmentTestSession createInMemoryAssessmentTestSession(Identity identity) {
		InMemoryAssessmentTestSession candidateSession = new InMemoryAssessmentTestSession();
		candidateSession.setIdentity(identity);
		candidateSession.setStorage(testSessionDao.createSessionStorage(candidateSession));
		return candidateSession;
	}

	@Override
	public AssessmentTestSession getResumableAssessmentTestSession(Identity identity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, boolean authorMode) {
		AssessmentTestSession session = testSessionDao.getLastTestSession(testEntry, entry, subIdent, identity, anonymousIdentifier, authorMode);
		if(session == null || session.isExploded() || session.getFinishTime() != null || session.getTerminationTime() != null) {
			session = null;
		} else {
			File sessionFile = getTestSessionStateFile(session);
			if(sessionFile == null || !sessionFile.exists()) {
				session = null;
			}
		}
		return session;
	}
	
	@Override
	public AssessmentTestSession reloadAssessmentTestSession(AssessmentTestSession session) {
		return testSessionDao.loadByKey(session.getKey());
	}

	@Override
	public AssessmentTestSession updateAssessmentTestSession(AssessmentTestSession session) {
		return testSessionDao.update(session);
	}

	@Override
	public AssessmentTestSession getAssessmentTestSession(Long assessmentTestSessionKey) {
		return testSessionDao.loadFullByKey(assessmentTestSessionKey);
	}

	@Override
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent, IdentityRef identity) {
		return testSessionDao.getUserTestSessions(courseEntry, subIdent, identity);
	}
	
	@Override
	public AssessmentTestSession getLastAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent,
			RepositoryEntry testEntry, IdentityRef identity) {
		return testSessionDao.getLastUserTestSession(courseEntry, subIdent, testEntry, identity);
	}

	@Override
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry) {
		return testSessionDao.getTestSessions(courseEntry, subIdent, testEntry);
	}

	@Override
	public boolean isRunningAssessmentTestSession(RepositoryEntry entry, String subIdent, RepositoryEntry testEntry) {
		return testSessionDao.hasRunningTestSessions(entry, subIdent, testEntry);
	}

	@Override
	public List<AssessmentTestSession> getRunningAssessmentTestSession(RepositoryEntry entry, String subIdent, RepositoryEntry testEntry) {
		return testSessionDao.getRunningTestSessions(entry, subIdent, testEntry);
	}

	@Override
	public TestSessionState loadTestSessionState(AssessmentTestSession candidateSession) {
        Document document = loadStateDocument(candidateSession);
        return document == null ? null: TestSessionStateXmlMarshaller.unmarshal(document.getDocumentElement());
    }
	
    private Document loadStateDocument(AssessmentTestSession candidateSession) {
        File sessionFile = getTestSessionStateFile(candidateSession);
        if(sessionFile.exists()) {
	        try {
		        DocumentBuilder documentBuilder = XmlFactories.newDocumentBuilder();
	            return documentBuilder.parse(sessionFile);
	        } catch (final Exception e) {
	            throw new OLATRuntimeException("Could not parse serailized state XML. This is an internal error as we currently don't expose this data to clients", e);
	        }
        }
        return null;
    }

	@Override
	public AssessmentTestMarks getMarks(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry) {
		return testMarksDao.loadTestMarks(testEntry, entry, subIdent, identity);
	}
	
	@Override
	public AssessmentTestMarks createMarks(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, String marks) {
		return testMarksDao.createAndPersistTestMarks(testEntry, entry, subIdent, identity, marks);
	}

	@Override
	public AssessmentTestMarks updateMarks(AssessmentTestMarks marks) {
		if(marks instanceof InMemoryAssessmentTestMarks) {
			return marks;
		}
		return testMarksDao.merge(marks);
	}

	@Override
	public AssessmentItemSession getOrCreateAssessmentItemSession(AssessmentTestSession assessmentTestSession, ParentPartItemRefs parentParts, String assessmentItemIdentifier) {
		AssessmentItemSession itemSession = itemSessionDao.getAssessmentItemSession(assessmentTestSession, assessmentItemIdentifier);
		if(itemSession == null) {
			itemSession = itemSessionDao.createAndPersistAssessmentItemSession(assessmentTestSession, parentParts, assessmentItemIdentifier);
		}
		return itemSession;
	}
	
	@Override
	public AssessmentItemSession updateAssessmentItemSession(AssessmentItemSession itemSession) {
		return itemSessionDao.merge(itemSession);
	}

	@Override
	public List<AssessmentItemSession> getAssessmentItemSessions(AssessmentTestSession candidateSession) {
		return itemSessionDao.getAssessmentItemSessions(candidateSession);
	}

	@Override
	public List<AssessmentItemSession> getAssessmentItemSessions(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry, String itemRef) {
		return itemSessionDao.getAssessmentItemSessions(courseEntry, subIdent, testEntry, itemRef);
	}

	@Override
	public AssessmentResponse createAssessmentResponse(AssessmentTestSession assessmentTestSession, AssessmentItemSession assessmentItemSession, String responseIdentifier,
			ResponseLegality legality, ResponseDataType type) {
		return testResponseDao.createAssessmentResponse(assessmentTestSession, assessmentItemSession,
				responseIdentifier, legality, type);
	}
	
	@Override
	public Map<Identifier, AssessmentResponse> getAssessmentResponses(AssessmentItemSession assessmentItemSession) {
		List<AssessmentResponse> responses = testResponseDao.getResponses(assessmentItemSession);
		Map<Identifier, AssessmentResponse> responseMap = new HashMap<>();
		for(AssessmentResponse response:responses) {
			responseMap.put(Identifier.assumedLegal(response.getResponseIdentifier()), response);
		}
		return responseMap;
	}

	@Override
	public void recordTestAssessmentResponses(AssessmentItemSession itemSession, Collection<AssessmentResponse> responses) {
		testResponseDao.save(responses);
		if(itemSession instanceof Persistable) {
			itemSessionDao.merge(itemSession);
		}
	}

	@Override
	public AssessmentTestSession recordTestAssessmentResult(AssessmentTestSession candidateSession, TestSessionState testSessionState,
			AssessmentResult assessmentResult, AssessmentSessionAuditLogger auditLogger) {
		// First record full result XML to filesystem
		if(candidateSession.getFinishTime() == null) {
			storeAssessmentResultFile(candidateSession, assessmentResult);
		}
        // Then record test outcome variables to DB
        recordOutcomeVariables(candidateSession, assessmentResult.getTestResult(), auditLogger);
        // Set duration
        candidateSession.setDuration(testSessionState.getDurationAccumulated());

		if(candidateSession instanceof Persistable) {
			return testSessionDao.update(candidateSession);
		}
		return candidateSession;
	}
	
	@Override
	public void signAssessmentResult(AssessmentTestSession candidateSession, DigitalSignatureOptions signatureOptions, Identity assessedIdentity) {
		if(!qtiModule.isDigitalSignatureEnabled() || !signatureOptions.isDigitalSignature()) return;//nothing to do
		
		try {
			File resultFile = getAssessmentResultFile(candidateSession);
			File signatureFile = new File(resultFile.getParentFile(), "assessmentResultSignature.xml");
			File certificateFile = qtiModule.getDigitalSignatureCertificateFile();
			X509CertificatePrivateKeyPair kp =CryptoUtil.getX509CertificatePrivateKeyPairPfx(
					certificateFile, qtiModule.getDigitalSignatureCertificatePassword());
			
			StringBuilder uri = new StringBuilder();
			uri.append(Settings.getServerContextPathURI()).append("/")
			   .append("RepositoryEntry/").append(candidateSession.getRepositoryEntry().getKey());
			if(StringHelper.containsNonWhitespace(candidateSession.getSubIdent())) {
				uri.append("/CourseNode/").append(candidateSession.getSubIdent());
			}
			uri.append("/TestSession/").append(candidateSession.getKey())
			   .append("/assessmentResult.xml");
			Document signatureDoc = createSignatureDocumentWrapper(uri.toString(), assessedIdentity, signatureOptions);
			
			XMLDigitalSignatureUtil.signDetached(uri.toString(), resultFile, signatureFile, signatureDoc,
					certificateFile.getName(), kp.getX509Cert(), kp.getPrivateKey());
			
			if(signatureOptions.isDigitalSignature() && signatureOptions.getMailBundle() != null) {
				MailBundle mail = signatureOptions.getMailBundle();
				List<File> attachments = new ArrayList<>(2);
				attachments.add(signatureFile);
				mail.getContent().setAttachments(attachments);
				mailManager.sendMessageAsync(mail);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private Document createSignatureDocumentWrapper(String url, Identity assessedIdentity, DigitalSignatureOptions signatureOptions) {
		try {
			Document signatureDocument = XMLDigitalSignatureUtil.createDocument();
			Node rootNode = signatureDocument.appendChild(signatureDocument.createElement("assessmentTestSignature"));
			Node urlNode = rootNode.appendChild(signatureDocument.createElement("url"));
			urlNode.appendChild(signatureDocument.createTextNode(url));
			Node dateNode = rootNode.appendChild(signatureDocument.createElement("date"));
			dateNode.appendChild(signatureDocument.createTextNode(Formatter.formatDatetime(new Date())));

			if(signatureOptions.getEntry() != null) {
				Node courseNode = rootNode.appendChild(signatureDocument.createElement("course"));
				courseNode.appendChild(signatureDocument.createTextNode(signatureOptions.getEntry().getDisplayname()));
			}
			if(signatureOptions.getSubIdentName() != null) {
				Node courseNodeNode = rootNode.appendChild(signatureDocument.createElement("courseNode"));
				courseNodeNode.appendChild(signatureDocument.createTextNode(signatureOptions.getSubIdentName()));
			}
			if(signatureOptions.getTestEntry() != null) {
				Node testNode = rootNode.appendChild(signatureDocument.createElement("test"));
				testNode.appendChild(signatureDocument.createTextNode(signatureOptions.getTestEntry().getDisplayname()));
			}
			
			if(assessedIdentity != null && assessedIdentity.getUser() != null) {
				User user = assessedIdentity.getUser();
				Node firstNameNode = rootNode.appendChild(signatureDocument.createElement("firstName"));
				firstNameNode.appendChild(signatureDocument.createTextNode(user.getFirstName()));
				Node lastNameNode = rootNode.appendChild(signatureDocument.createElement("lastName"));
				lastNameNode.appendChild(signatureDocument.createTextNode(user.getLastName()));
			}

			return signatureDocument;
		} catch ( Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public DigitalSignatureValidation validateAssessmentResult(File xmlSignature) {
		try {
			Document signature = XMLDigitalSignatureUtil.getDocument(xmlSignature);
			String uri = XMLDigitalSignatureUtil.getReferenceURI(signature);
			//URI looks like: http://localhost:8081/olat/RepositoryEntry/688455680/CourseNode/95134692149905/TestSession/3231/assessmentResult.xml
			String keyName = XMLDigitalSignatureUtil.getKeyName(signature);
			
			int end = uri.indexOf("/assessmentResult");
			if(end <= 0) {
				return new DigitalSignatureValidation(DigitalSignatureValidation.Message.sessionNotFound, false);
			}
			int start = uri.lastIndexOf('/', end - 1);
			if(start <= 0) {
				return new DigitalSignatureValidation(DigitalSignatureValidation.Message.sessionNotFound, false);
			}
			String testSessionKey = uri.substring(start + 1, end);
			AssessmentTestSession testSession = getAssessmentTestSession(new Long(testSessionKey));
			if(testSession == null) {
				return new DigitalSignatureValidation(DigitalSignatureValidation.Message.sessionNotFound, false);
			}
			
			File assessmentResult = getAssessmentResultFile(testSession);
			File certificateFile = qtiModule.getDigitalSignatureCertificateFile();
			
			X509CertificatePrivateKeyPair kp = null;
			if(keyName != null && keyName.equals(certificateFile.getName())) {
				kp = CryptoUtil.getX509CertificatePrivateKeyPairPfx(
						certificateFile, qtiModule.getDigitalSignatureCertificatePassword());
			} else if(keyName != null) {
				File olderCertificateFile = new File(certificateFile.getParentFile(), keyName);
				if(olderCertificateFile.exists()) {
					kp = CryptoUtil.getX509CertificatePrivateKeyPairPfx(
							olderCertificateFile, qtiModule.getDigitalSignatureCertificatePassword());
				}
			}
				
			if(kp == null) {
				// validate document against signature
				if(XMLDigitalSignatureUtil.validate(uri, assessmentResult, xmlSignature)) {
					return new DigitalSignatureValidation(DigitalSignatureValidation.Message.validItself, true);
				}
			} else if(XMLDigitalSignatureUtil.validate(uri, assessmentResult, xmlSignature, kp.getX509Cert().getPublicKey())) {
				// validate document against signature but use the public key of the certificate
				return new DigitalSignatureValidation(DigitalSignatureValidation.Message.validCertificate, true);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return new DigitalSignatureValidation(DigitalSignatureValidation.Message.notValid, false);
	}

	@Override
	public File getAssessmentResultSignature(AssessmentTestSession candidateSession) {
		File resultFile = getAssessmentResultFile(candidateSession);
		File signatureFile = new File(resultFile.getParentFile(), "assessmentResultSignature.xml");
		return signatureFile.exists() ? signatureFile : null;
	}

	@Override
	public Date getAssessmentResultSignatureIssueDate(AssessmentTestSession candidateSession) {
		Date issueDate = null;
		File signatureFile = null;
		try {
			signatureFile = getAssessmentResultSignature(candidateSession);
			if(signatureFile != null) {
				Document doc = XMLDigitalSignatureUtil.getDocument(signatureFile);
				if(doc != null) {
					String date = XMLDigitalSignatureUtil.getElementText(doc, "date");
					if(StringHelper.containsNonWhitespace(date)) {
						issueDate = Formatter.parseDatetime(date);
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot read the issue date of the signature: " + signatureFile, e);
		}
		return issueDate;
	}

	@Override
	public AssessmentTestSession finishTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResult,
			Date timestamp, DigitalSignatureOptions digitalSignature, Identity assessedIdentity) {
		/* Mark session as finished */
        candidateSession.setFinishTime(timestamp);
        // Set duration
        candidateSession.setDuration(testSessionState.getDurationAccumulated());

        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
        //candidateSession.setLisOutcomeReportingStatus(null);
        //candidateSession.setLisScore(null);
		if(candidateSession instanceof Persistable) {
			candidateSession = testSessionDao.update(candidateSession);
		}
		
		storeAssessmentResultFile(candidateSession, assessmentResult);
		if(qtiModule.isDigitalSignatureEnabled() && digitalSignature.isDigitalSignature()) {
    		signAssessmentResult(candidateSession, digitalSignature, assessedIdentity);
    	}

        /* Finally schedule LTI result return (if appropriate and sane) */
        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
        return candidateSession;
	}

	/**
	 * Cancel delete the test session, related items session and their responses, the
	 * assessment result file, the test plan file.
	 * 
	 */
	@Override
	public void cancelTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState) {
		final File myStore = testSessionDao.getSessionStorage(candidateSession);
        final File sessionState = new File(myStore, "testSessionState.xml");
        final File resultFile = getAssessmentResultFile(candidateSession);

		testSessionDao.deleteTestSession(candidateSession);
		if(sessionState != null && sessionState.exists()) {
			sessionState.delete();
		}
		if(resultFile != null && resultFile.exists()) {
			resultFile.delete();
		}
	}
	
	private void recordOutcomeVariables(AssessmentTestSession candidateSession, AbstractResult resultNode, AssessmentSessionAuditLogger auditLogger) {
		//preserve the order
		Map<Identifier,String> outcomes = new LinkedHashMap<>();
		
		for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
			if (itemVariable instanceof OutcomeVariable) {
				recordOutcomeVariable(candidateSession, (OutcomeVariable)itemVariable, outcomes);
			}
		}
		
		if(auditLogger != null) {
			auditLogger.logCandidateOutcomes(candidateSession, outcomes);
		}
	}
	
	private void recordOutcomeVariable(AssessmentTestSession candidateSession, OutcomeVariable outcomeVariable, Map<Identifier,String> outcomes) {
		Identifier identifier = outcomeVariable.getIdentifier();
		Value computedValue = outcomeVariable.getComputedValue();

		if (QtiConstants.VARIABLE_DURATION_IDENTIFIER.equals(identifier)) {
			log.audit(candidateSession.getKey() + " :: " + outcomeVariable.getIdentifier() + " - " + stringifyQtiValue(computedValue));
		} else if (QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
			if (computedValue instanceof NumberValue) {
				double score = ((NumberValue) computedValue).doubleValue();
				candidateSession.setScore(new BigDecimal(score));
			}
		} else if (QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
			if (computedValue instanceof BooleanValue) {
				boolean pass = ((BooleanValue) computedValue).booleanValue();
				candidateSession.setPassed(pass);
			}
		}
		
		try {
			outcomes.put(identifier, stringifyQtiValue(computedValue));
		} catch (Exception e) {
			log.error("", e);
		}
	}
    
    private String stringifyQtiValue(final Value value) {
        if (qtiModule.isMathAssessExtensionEnabled() && GlueValueBinder.isMathsContentRecord(value)) {
            /* This is a special MathAssess "Maths Content" variable. In this case, we'll record
             * just the ASCIIMath input form or the Maxima form, if either are available.
             */
            final RecordValue mathsValue = (RecordValue) value;
            final SingleValue asciiMathInput = mathsValue.get(MathAssessConstants.FIELD_CANDIDATE_INPUT_IDENTIFIER);
            if (asciiMathInput!=null) {
                return "ASCIIMath[" + asciiMathInput.toQtiString() + "]";
            }
            final SingleValue maximaForm = mathsValue.get(MathAssessConstants.FIELD_MAXIMA_IDENTIFIER);
            if (maximaForm!=null) {
                return "Maxima[" + maximaForm.toQtiString() + "]";
            }
        }
        /* Just convert to QTI string in the usual way */
        return value.toQtiString();
    }
    
    private void storeAssessmentResultFile(final AssessmentTestSession candidateSession, final QtiNode resultNode) {
        final File resultFile = getAssessmentResultFile(candidateSession);
        try(OutputStream resultStream = FileUtils.getBos(resultFile);) {
            qtiSerializer().serializeJqtiObject(resultNode, resultStream);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Unexpected", e);
        }
    }
    
    public File getAssessmentResultFile(final AssessmentTestSession candidateSession) {
    	File myStore = testSessionDao.getSessionStorage(candidateSession);
        return new File(myStore, "assessmentResult.xml");
    }

	@Override
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
			CandidateTestEventType textEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		return recordCandidateTestEvent(candidateSession, testEntry, entry, textEventType, null, null, testSessionState, notificationRecorder);
	}

	@Override
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
			CandidateTestEventType textEventType, CandidateItemEventType itemEventType,
			TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		
		CandidateEvent event = new CandidateEvent(candidateSession, testEntry, entry);
		event.setTestEventType(textEventType);
		event.setItemEventType(itemEventType);
		if (itemKey != null) {
            event.setTestItemKey(itemKey.toString());
        }
		storeTestSessionState(event, testSessionState);
		return event;
	}
	
	private void storeTestSessionState(CandidateEvent candidateEvent, TestSessionState testSessionState) {
		Document stateDocument = TestSessionStateXmlMarshaller.marshal(testSessionState);
		File sessionFile = getTestSessionStateFile(candidateEvent);
		storeStateDocument(stateDocument, sessionFile);
	}

    private File getTestSessionStateFile(CandidateEvent candidateEvent) {
    	AssessmentTestSession candidateSession = candidateEvent.getCandidateSession();
    	return getTestSessionStateFile(candidateSession);
    }
    
    private File getTestSessionStateFile(AssessmentTestSession candidateSession) {
    	File myStore = testSessionDao.getSessionStorage(candidateSession);
        return new File(myStore, "testSessionState.xml");
    }
	
    @Override
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
			CandidateItemEventType itemEventType, ItemSessionState itemSessionState) {
		return recordCandidateItemEvent(candidateSession, testEntry, entry, itemEventType, itemSessionState, null);
	}
		
	@Override
    public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
    		CandidateItemEventType itemEventType, ItemSessionState itemSessionState, NotificationRecorder notificationRecorder) {

		CandidateEvent event = new CandidateEvent(candidateSession, testEntry, entry);
        event.setItemEventType(itemEventType);
    	return event;
    }
	
    @Override
	public AssessmentResult getAssessmentResult(AssessmentTestSession candidateSession) {
    	File assessmentResultFile = getAssessmentResultFile(candidateSession);
    	ResourceLocator fileResourceLocator = new PathResourceLocator(assessmentResultFile.getParentFile().toPath());
		ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
    	
		URI assessmentResultUri = assessmentResultFile.toURI();
    	QtiObjectReader qtiObjectReader = qtiXmlReader().createQtiObjectReader(inputResourceLocator, false, false);
		try {
			QtiObjectReadResult<AssessmentResult> result = qtiObjectReader.lookupRootNode(assessmentResultUri, AssessmentResult.class);
			return result.getRootNode();
		} catch (XmlResourceNotFoundException | QtiXmlInterpretationException | ClassCastException e) {
			log.error("", e);
			return null;
		}
	}

	public void storeItemSessionState(CandidateEvent candidateEvent, ItemSessionState itemSessionState) {
        Document stateDocument = ItemSessionStateXmlMarshaller.marshal(itemSessionState);
        File sessionFile = getItemSessionStateFile(candidateEvent);
        storeStateDocument(stateDocument, sessionFile);
    }
    
    private File getItemSessionStateFile(CandidateEvent candidateEvent) {
    	AssessmentTestSession candidateSession = candidateEvent.getCandidateSession();
    	File myStore = testSessionDao.getSessionStorage(candidateSession);
        return new File(myStore, "itemSessionState.xml");
    }
    
	private void storeStateDocument(Document stateXml, File sessionFile) {
        XsltSerializationOptions xsltSerializationOptions = new XsltSerializationOptions();
        xsltSerializationOptions.setIndenting(true);
        xsltSerializationOptions.setIncludingXMLDeclaration(false);
        
        Transformer serializer = XsltStylesheetManager.createSerializer(xsltSerializationOptions);
        try(OutputStream resultStream = new FileOutputStream(sessionFile)) {
            serializer.transform(new DOMSource(stateXml), new StreamResult(resultStream));
        } catch (TransformerException | IOException e) {
            throw new OLATRuntimeException("Unexpected Exception serializing state DOM", e);
        }
    }

	@Override
	public AssessmentTestSession finishItemSession(AssessmentTestSession candidateSession, AssessmentResult assessmentResult, Date timestamp) {
		/* Mark session as finished */
        candidateSession.setFinishTime(timestamp);

        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
        //candidateSession.setLisOutcomeReportingStatus(null);
        //candidateSession.setLisScore(null);
        if(candidateSession instanceof Persistable) {
        	candidateSession = testSessionDao.update(candidateSession);
        }
        /* Finally schedule LTI result return (if appropriate and sane) */
        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
		return candidateSession;
	}

	@Override
	public void recordItemAssessmentResult(AssessmentTestSession candidateSession, AssessmentResult assessmentResult, AssessmentSessionAuditLogger auditLogger) {
		//preserve the order
		Map<Identifier,String> outcomes = new LinkedHashMap<>();

		for (final ItemResult itemResult:assessmentResult.getItemResults()) {
			for (final ItemVariable itemVariable : itemResult.getItemVariables()) {
	            if (itemVariable instanceof OutcomeVariable) {
					if (itemVariable instanceof OutcomeVariable) {
						recordOutcomeVariable(candidateSession, (OutcomeVariable)itemVariable, outcomes);
					}
	            }
			}
		}
		
		if(auditLogger != null) {
			auditLogger.logCandidateOutcomes(candidateSession, outcomes);
		}
	}
	
	

	@Override
	public File getSubmissionDirectory(AssessmentTestSession candidateSession) {
		File myStore = testSessionDao.getSessionStorage(candidateSession);
        File submissionDir = new File(myStore, "submissions");
        if(!submissionDir.exists()) {
        	submissionDir.mkdir();
        }
		return submissionDir;
	}

	@Override
	public File importFileSubmission(AssessmentTestSession candidateSession, String filename, byte[] data) {
		File submissionDir = getSubmissionDirectory(candidateSession);
        
        FileOutputStream out = null;
        try {
        	//add the date in the file
        	String extension = FileUtils.getFileSuffix(filename);
        	if(extension != null && extension.length() > 0) {
        		filename = filename.substring(0, filename.length() - extension.length() - 1);
        		extension = "." + extension;
        	} else {
        		extension = "";
        	}
        	String date = testSessionDao.formatDate(new Date());
        	String datedFilename = FileUtils.normalizeFilename(filename) + "_" + date + extension;
        	
        	//make sure we don't overwrite an existing file
			File submittedFile = new File(submissionDir, datedFilename);
			String renamedFile = FileUtils.rename(submittedFile);
			if(!datedFilename.equals(renamedFile)) {
				submittedFile = new File(submissionDir, datedFilename);
			}
			
			out = new FileOutputStream(submittedFile);
			out.write(data);
			return submittedFile;
		} catch (IOException e) {
			log.error("", e);
			return null;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	public File importFileSubmission(AssessmentTestSession candidateSession, MultipartFileInfos multipartFile) {
		File submissionDir = getSubmissionDirectory(candidateSession);
        
        try {
        	//add the date in the file
        	String filename = multipartFile.getFileName();
        	String extension = FileUtils.getFileSuffix(filename);
        	if(extension != null && extension.length() > 0) {
        		filename = filename.substring(0, filename.length() - extension.length() - 1);
        		extension = "." + extension;
        	} else {
        		extension = "";
        	}
        	String date = testSessionDao.formatDate(new Date());
        	String datedFilename = FileUtils.normalizeFilename(filename) + "_" + date + extension;
        	//make sure we don't overwrite an existing file
			File submittedFile = new File(submissionDir, datedFilename);
			String renamedFile = FileUtils.rename(submittedFile);
			if(!datedFilename.equals(renamedFile)) {
				submittedFile = new File(submissionDir, datedFilename);
			}
			Files.move(multipartFile.getFile().toPath(), submittedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return submittedFile;
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
}
