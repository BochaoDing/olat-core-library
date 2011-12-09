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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.ll;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Configuration for link lists.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

	/**
	 * [spring only]
	 * @param enabled
	 */
	private LLCourseNodeConfiguration() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAlias() {
		return "ll";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconCSSClass() {
		return "o_ll_icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseNode getInstance() {
		return new LLCourseNode();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLinkCSSClass() {
		return "o_ll_icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_ll");
	}

	/**
	 * {@inheritDoc}
	 */
	public ExtensionResource getExtensionCSS() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getExtensionResources() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getAlias();
	}

}
