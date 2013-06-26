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
package org.olat.modules.qpool.ui.metadata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;

/**
 * 
 * Initial date: 02.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetaUIFactory {
	
	public static KeyValues getFormats() {
		String[] formatKeys = new String[]{ QTIConstants.QTI_12_FORMAT };
		
		return new KeyValues(formatKeys, formatKeys);
	}

	public static KeyValues getAssessmentTypes(Translator translator) {
		String[] assessmentTypeKeys = new String[]{ "summative", "formative", "both"};
		String[] assessmentTypeValues = new String[]{
				translator.translate("question.assessmentType.summative"),
				translator.translate("question.assessmentType.formative"),
				translator.translate("question.assessmentType.both"),	
		};
		return new KeyValues(assessmentTypeKeys, assessmentTypeValues);
	}
	
	public static KeyValues getStatus(Translator translator) {
		String[] statusTypeKeys = QuestionStatus.valueString();
		String[] statusTypeValues = new String[statusTypeKeys.length];
		for(int i=statusTypeKeys.length; i-->0; ) {
			statusTypeValues[i] = translator.translate("lifecycle.status." + statusTypeKeys[i]);
		}
		return new KeyValues(statusTypeKeys, statusTypeValues);
	}
	
	public static KeyValues getContextKeyValues(Translator translator, QPoolService qpoolService) {
		List<QEducationalContext> levels = qpoolService.getAllEducationlContexts();
		String[] contextKeys = new String[ levels.size() ];
		String[] contextValues = new String[ levels.size() ];
		int count = 0;
		for(QEducationalContext level:levels) {
			contextKeys[count] = level.getLevel();
			String translation = translator.translate("item.level." + level.getLevel().toLowerCase());
			if(translation.length() > 128) {
				translation = level.getLevel();
			}
			contextValues[count++] = translation;
		}
		return new KeyValues(contextKeys, contextValues);
	}
	
	public static KeyValues getQLicenseKeyValues(QPoolService qpoolService) {
		List<QLicense> allLicenses = qpoolService.getAllLicenses();
		List<QLicense> licenses = new ArrayList<QLicense>(allLicenses);
		for(Iterator<QLicense> it=licenses.iterator(); it.hasNext(); ) {
			String key = it.next().getLicenseKey();
			if(key != null && key.startsWith("perso-")) {
				it.remove();
			}
		}

		String[] keys = new String[licenses.size() + 2];
		String[] values = new String[licenses.size() + 2];
		keys[0] = "none";
		values[0] = "None";
		int count = 1;
		for(QLicense license:licenses) {
			keys[count] = license.getLicenseKey();
			values[count++] = license.getLicenseKey();
		}
		keys[keys.length  - 1] = "free";
		values[values.length - 1] = "Freetext";
		
		return new KeyValues(keys, values);
	}
	
	public static KeyValues getQItemTypeKeyValues(Translator translator, QPoolService qpoolService) {
		List<QItemType> types = qpoolService.getAllItemTypes();
		String[] typeKeys = new String[types.size()];
		String[] typeValues = new String[types.size()];
		int count = 0;
		for(QItemType type:types) {
			typeKeys[count] = type.getType();
			String translation = translator.translate("item.type." + type.getType().toLowerCase());
			if(translation.length() > 128) {
				typeValues[count++] = typeKeys[count];
			} else {
				typeValues[count++] = translation;
			}
		}
		return new KeyValues(typeKeys, typeValues);
	}
	
	public static boolean validateElementLogic(TextElement el, int maxLength, boolean mandatory, boolean enabled) {
		boolean allOk = true;
		el.clearError();
		if(enabled) {
			String value = el.getValue();
			if(mandatory && !StringHelper.containsNonWhitespace(value)) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			} else if (value != null && value.length() > maxLength) {
				String[] lengths = new String[]{ Integer.toString(maxLength), Integer.toString(value.length())};
				el.setErrorKey("error.input.toolong", lengths);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max, boolean enabled) {
		boolean allOk = true;
		el.clearError();
		if(enabled) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					double value = Integer.parseInt(val);
					if(min > value) {
						el.setErrorKey("error.wrongFloat", null);
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.wrongFloat", null);
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrongFloat", null);
					allOk = false;
				}
			}
		}
		return allOk;	
	}
	
	protected static boolean validateBigDecimal(TextElement el, double min, double max, boolean enabled) {
		boolean allOk = true;
		el.clearError();
		if(enabled) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					double value = Double.parseDouble(val);
					if(min > value) {
						el.setErrorKey("error.wrongFloat", null);
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.wrongFloat", null);
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrongFloat", null);
					allOk = false;
				}
			}
		}
		return allOk;	
	}
	
	protected static boolean validateSelection(SingleSelection el,  boolean enabled) {
		boolean allOk = true;
		
		el.clearError();
		if(enabled && !el.isOneSelected()) {
			el.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		return allOk;
	}
	
	protected static boolean validateRights(SingleSelection copyrightEl, TextElement descriptionEl,
			KeyValues licenseKeys, boolean enabled) {
		boolean allOk = true;
		copyrightEl.clearError();
		descriptionEl.clearError();
		if(enabled) {
			if(!copyrightEl.isOneSelected()) {
				copyrightEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			} else if(copyrightEl.getSelectedKey().equals(licenseKeys.getLastKey())) {
				String licence = descriptionEl.getValue();
				if(!StringHelper.containsNonWhitespace(licence)) {
					descriptionEl.setErrorKey("form.mandatory.hover", null);
					allOk &= false;
				}
			}
		}
		return allOk;
	}
	
	protected static BigDecimal toBigDecimal(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return new BigDecimal(val);
		}
		return null;
	}
	
	protected static String bigDToString(BigDecimal val) {
		if(val == null) {
			return "";
		}
		return val.toPlainString();
	}
	
	protected static int toInt(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return Integer.parseInt(val);
		}
		return 1;
	}
	
	public static class KeyValues {	
		private final String[] keys;
		private final String[] values;
		
		public KeyValues(String[] keys, String[] values) {
			this.keys = keys;
			this.values = values;
		}
		
		public String[] getKeys() {
			return keys;
		}
		
		public String[] getValues() {
			return values;
		}
		
		public String getLastKey() {
			return keys[keys.length - 1];
		}
		
		public String getFirstKey() {
			return keys[0];
		}
	}
}
