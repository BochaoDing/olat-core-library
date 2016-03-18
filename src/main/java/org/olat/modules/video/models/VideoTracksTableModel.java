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

package org.olat.modules.video.models;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.video.ui.TrackTableRow;

/**
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoTracksTableModel extends DefaultFlexiTableDataModel<TrackTableRow>{

	protected FormUIFactory uifactory = FormUIFactory.getInstance();
	private Translator translator;
	public VideoTracksTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public VideoTracksTableModel createCopyWithEmptyList() {
		return new VideoTracksTableModel(getTableColumnModel(), translator);
	}


	@Override
	public Object getValueAt(int row, int col) {
		TrackTableRow track = getObject(row);
		switch(TrackTableCols.values()[col]) {
			case file: return track.getTrack();
			case language: return new Locale(track.getLanguage()).getDisplayLanguage(this.translator.getLocale());
			case delete: return track.getDeleteLink();
			default: return "";
		}
	}



	public enum TrackTableCols {
		file("track.table.header.file"),
		language("track.table.header.language"),
		delete("track.table.header.delete");

		private final String i18nKey;

		private TrackTableCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String i18nKey() {
			return i18nKey;
		}
	}

}