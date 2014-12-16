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
package org.olat.course.assessment.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.assessment.AssessmentMode;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeListModel extends DefaultFlexiTableDataModel<AssessmentMode> {
	
	public AssessmentModeListModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public AssessmentModeListModel createCopyWithEmptyList() {
		return new AssessmentModeListModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentMode mode = getObject(row);
		switch(Cols.values()[col]) {
			case name: return mode.getName();
			case begin: return mode.getBegin();
			case end: return mode.getEnd();
			case leadTime: return mode.getLeadTime();
			case target: return mode.getTargetAudience();
		}
		return null;
	}
	
	public enum Cols {
		
		name("table.header.name"),
		begin("table.header.begin"),
		end("table.header.end"),
		leadTime("table.header.leadTime"),
		target("table.header.target");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
