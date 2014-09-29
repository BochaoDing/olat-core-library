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
package org.olat.ims.qti.qpool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.jgroups.util.UUID;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QLicenseDAO;
import org.olat.modules.qpool.manager.TaxonomyLevelDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.QuestionItemImpl;

/**
 * 
 * This is an helper class to convert metadata, retrieve specific type
 * of metadatas...
 * 
 * Initial date: 10.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class QTIMetadataConverter {
	
	private Element qtimetadata;

	private QLicenseDAO licenseDao;
	private QItemTypeDAO itemTypeDao;
	private TaxonomyLevelDAO taxonomyLevelDao;
	private QEducationalContextDAO educationalContextDao;
	
	QTIMetadataConverter(Element qtimetadata) {
		this.qtimetadata = qtimetadata;
	}
	
	QTIMetadataConverter(Element qtimetadata, QItemTypeDAO itemTypeDao, QLicenseDAO licenseDao,
			TaxonomyLevelDAO taxonomyLevelDao, QEducationalContextDAO educationalContextDao) {
		this.qtimetadata = qtimetadata;
		this.licenseDao = licenseDao;
		this.itemTypeDao = itemTypeDao;
		this.taxonomyLevelDao = taxonomyLevelDao;
		this.educationalContextDao = educationalContextDao;
	}
	
	QTIMetadataConverter(QItemTypeDAO itemTypeDao, QLicenseDAO licenseDao,
			TaxonomyLevelDAO taxonomyLevelDao, QEducationalContextDAO educationalContextDao) {
		this.licenseDao = licenseDao;
		this.itemTypeDao = itemTypeDao;
		this.taxonomyLevelDao = taxonomyLevelDao;
		this.educationalContextDao = educationalContextDao;
	}
	
	public QItemType toType(String itemType) {
		QItemType type = itemTypeDao.loadByType(itemType);
		if(type == null) {
			type = itemTypeDao.create(itemType, true);
		}
		return type;
	}
	
	public QLicense toLicense(String license) {
		QLicense qLicense = null;
		if(StringHelper.containsNonWhitespace(license)) {
			qLicense = licenseDao.searchLicense(license);
			if(qLicense == null) {
				String key = "perso-" + UUID.randomUUID().toString();
				qLicense = licenseDao.create(key, license, false);
			}
		}
		return qLicense;
	}
	
	public TaxonomyLevel toTaxonomy(String str) {
		String[] path = str.split("/");
		List<String> cleanedPath = new ArrayList<>(path.length);
		for(String segment:path) {
			if(StringHelper.containsNonWhitespace(segment)) {
				cleanedPath.add(segment);
			}
		}
		
		TaxonomyLevel lowerLevel = null;
		if(path != null && path.length > 0) {
			for(String field :cleanedPath) {
				TaxonomyLevel level = taxonomyLevelDao.loadLevelBy(lowerLevel, field);
				if(level == null) {
					level = taxonomyLevelDao.createAndPersist(lowerLevel, field);
				}
				lowerLevel = level;
			}
		}
		return lowerLevel;
	}
	
	public QEducationalContext toEducationalContext(String txt) {
		QEducationalContext context = educationalContextDao.loadByLevel(txt);
		if(context == null) {
			context = educationalContextDao.create(txt, true);
		}
		return context;
	}
	
	protected void toQuestion(QuestionItemImpl fullItem) {
		String addInfos = getMetadataEntry("additional_informations");
		if(StringHelper.containsNonWhitespace(addInfos)) {
			fullItem.setAdditionalInformations(addInfos);
		}
		String assessmentType = getMetadataEntry("oo_assessment_type");
		if(StringHelper.containsNonWhitespace(assessmentType)) {
			fullItem.setAssessmentType(assessmentType);
		}
		String coverage = getMetadataEntry("coverage");
		if(StringHelper.containsNonWhitespace(coverage)) {
			fullItem.setCoverage(coverage);
		}
		String description = getMetadataEntry("description");
		if(description != null) {
			fullItem.setDescription(description);
		}
		String differentiation = getMetadataEntry("oo_differentiation");
		if(StringHelper.containsNonWhitespace(differentiation)) {
			fullItem.setDifferentiation(toBigDecimal(differentiation));
		}
		String difficulty = getMetadataEntry("qmd_levelofdifficulty");
		if(StringHelper.containsNonWhitespace(difficulty)) {
			fullItem.setDifficulty(toBigDecimal(difficulty));
		}
		String vendor = getMetadataEntry("qmd_toolvendor");
		if(vendor != null) {
			fullItem.setEditor(vendor);
		}
		String editorVersion = getMetadataEntry("oo_toolvendor_version");
		if(StringHelper.containsNonWhitespace(editorVersion)) {
			fullItem.setEditorVersion(editorVersion);
		}
		String learningTime = getMetadataEntry("oo_education_learning_time");
		if(StringHelper.containsNonWhitespace(learningTime)) {
			fullItem.setEducationalLearningTime(learningTime);
		}
		String format = getMetadataEntry("format");
		if(StringHelper.containsNonWhitespace(format)) {
			fullItem.setFormat(format);
		}
		String identifier = getMetadataEntry("oo_identifier");
		if(StringHelper.containsNonWhitespace(identifier)) {
			fullItem.setMasterIdentifier(identifier);
		}
		String itemType = getMetadataEntry("type");
		if(StringHelper.containsNonWhitespace(itemType)) {
			fullItem.setType(toType(itemType));
		}
		String version = getMetadataEntry("version");
		if(StringHelper.containsNonWhitespace(version)) {
			fullItem.setItemVersion(version);
		}
		String keywords = getMetadataEntry("keywords");
		if(StringHelper.containsNonWhitespace(keywords)) {
			fullItem.setKeywords(keywords);
		}
		String language = getMetadataEntry("language");
		if(StringHelper.containsNonWhitespace(language)) {
			fullItem.setLanguage(language);
		}
		String numOfAnswers = getMetadataEntry("oo_num_of_answer_alternatives");
		if(StringHelper.containsNonWhitespace(numOfAnswers)) {
			fullItem.setNumOfAnswerAlternatives(toInt(numOfAnswers));
		}
		String status = getMetadataEntry("status");
		if(StringHelper.containsNonWhitespace(status) && validStatus(status)) {
			fullItem.setStatus(status);
		}
		String stdDevDifficulty = getMetadataEntry("oo_std_dev_difficulty");
		if(StringHelper.containsNonWhitespace(stdDevDifficulty)) {
			fullItem.setStdevDifficulty(toBigDecimal(stdDevDifficulty));
		}
		String title = getMetadataEntry("title");
		if(StringHelper.containsNonWhitespace(title)) {
			fullItem.setTitle(title);
		}
		String license = getMetadataEntry("license");
		if(StringHelper.containsNonWhitespace(license)) {
			fullItem.setLicense(toLicense(license));
		}
		String taxonomy = getMetadataEntry("oo_taxonomy");
		if(StringHelper.containsNonWhitespace(taxonomy)) {
			fullItem.setTaxonomyLevel(toTaxonomy(taxonomy));
		}
		String educationalContext = getMetadataEntry("oo_educational_context");
		if(StringHelper.containsNonWhitespace(educationalContext)) {
			fullItem.setEducationalContext(toEducationalContext(educationalContext));
		}
	}
	
	protected void toXml(QuestionItemFull fullItem) {
		addMetadataField("additional_informations", fullItem.getAdditionalInformations(), qtimetadata);
		addMetadataField("oo_assessment_type", fullItem.getAssessmentType(), qtimetadata);
		addMetadataField("coverage", fullItem.getCoverage(), qtimetadata);
		addMetadataField("description", fullItem.getDescription(), qtimetadata);
		addMetadataField("oo_differentiation", fullItem.getDifferentiation(), qtimetadata);
		addMetadataField("qmd_levelofdifficulty", fullItem.getDifficulty(), qtimetadata);
		addMetadataField("qmd_toolvendor", fullItem.getEditor(), qtimetadata);
		addMetadataField("oo_toolvendor_version", fullItem.getEditorVersion(), qtimetadata);
		addMetadataField("oo_educational_context", fullItem.getEducationalContext(), qtimetadata);
		addMetadataField("oo_education_learning_time", fullItem.getEducationalLearningTime(), qtimetadata);
		addMetadataField("format", fullItem.getFormat(), qtimetadata);
		addMetadataField("oo_identifier", fullItem.getIdentifier(), qtimetadata);
		addMetadataField("type", fullItem.getItemType(), qtimetadata);
		addMetadataField("version", fullItem.getItemVersion(), qtimetadata);
		addMetadataField("keywords", fullItem.getKeywords(), qtimetadata);
		addMetadataField("language", fullItem.getLanguage(), qtimetadata);
		addMetadataField("license", fullItem.getLicense(), qtimetadata);
		addMetadataField("oo_master", fullItem.getMasterIdentifier(), qtimetadata);
		addMetadataField("oo_num_of_answer_alternatives", fullItem.getNumOfAnswerAlternatives(), qtimetadata);
		addMetadataField("status", fullItem.getQuestionStatus(), qtimetadata);
		addMetadataField("oo_std_dev_difficulty", fullItem.getStdevDifficulty(), qtimetadata);
		addMetadataField("oo_taxonomy", fullItem.getTaxonomicPath(), qtimetadata);
		//fullItem.getTaxonomicLevel();
		addMetadataField("title", fullItem.getTitle(), qtimetadata);
		addMetadataField("oo_usage", fullItem.getUsage(), qtimetadata);
	}
	
	private void addMetadataField(String label, int entry, Element metadata) {
		if(entry >=  0) {
			addMetadataField(label, Integer.toString(entry), metadata);
		}
	}
	
	private void addMetadataField(String label, QLicense entry, Element metadata) {
		if(entry != null) {
			addMetadataField(label, entry.getLicenseText(), metadata);
		}
	}
	
	private void addMetadataField(String label, QEducationalContext entry, Element metadata) {
		if(entry != null) {
			addMetadataField(label, entry.getLevel(), metadata);
		}
	}
	
	private void addMetadataField(String label, QuestionStatus entry, Element metadata) {
		if(entry != null) {
			addMetadataField(label, entry.name(), metadata);
		}
	}
	
	private void addMetadataField(String label, BigDecimal entry, Element metadata) {
		if(entry != null) {
			addMetadataField(label, entry.toPlainString(), metadata);
		}
	}
	
	private void addMetadataField(String label, String entry, Element metadata) {
		if(entry != null) {
			Element qtimetadatafield = metadata.addElement("qtimetadatafield");
			qtimetadatafield.addElement("fieldlabel").setText(label);
			qtimetadatafield.addElement("fieldentry").setText(entry);
		}
	}
	
	private String getMetadataEntry(String label) {
		String entry = null;
		
		@SuppressWarnings("unchecked")
		List<Element> qtimetadatafields = qtimetadata.elements("qtimetadatafield");
		for(Element qtimetadatafield:qtimetadatafields) {
			Element fieldlabel = qtimetadatafield.element("fieldlabel");
			if(fieldlabel != null && label.equals(fieldlabel.getText())) {
				Element fieldentry = qtimetadatafield.element("fieldentry");
				if(fieldentry != null) {
					entry = fieldentry.getText();
				}
			}
		}
		
		return entry;
	}
	
	private BigDecimal toBigDecimal(String str) {
		try {
			return new BigDecimal(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	private int toInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	private boolean validStatus(String str) {
		try {
			QuestionStatus.valueOf(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}