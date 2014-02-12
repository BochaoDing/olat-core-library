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
package org.olat.course.nodes.cl;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.AssessmentDataView;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CheckboxManager {
	
	public DBCheckbox createDBCheckbox(String checkboxId, OLATResourceable ores, String resSubPath);
	
	public List<DBCheckbox> loadCheckbox(OLATResourceable ores, String resSubPath);
	
	public DBCheckbox loadCheckbox(OLATResourceable ores, String resSubPath, String checkboxId);
	
	public void removeCheckbox(DBCheckbox checkbox);
	
	public void syncCheckbox(CheckboxList checkboxList, OLATResourceable ores, String resSubPath);
	
	public void deleteCheckbox(OLATResourceable ores, String resSubPath);
	
	public void check(DBCheckbox checkbox, Identity owner, Float score, Boolean checked);
	
	public List<DBCheck> loadCheck(Identity identity, OLATResourceable ores, String resSubPath);
	
	public int countChecked(Identity identity, OLATResourceable ores, String resSubPath);
	
	public float calculateScore(Identity identity, OLATResourceable ores, String resSubPath);
	
	public List<AssessmentData> getAssessmentDatas(OLATResourceable ores, String resSubPath, List<SecurityGroup> groups);
	
	public List<AssessmentDataView> getAssessmentDataViews(OLATResourceable ores, String resSubPath, List<Checkbox> checkbox,
			List<SecurityGroup> secGroups, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	public VFSContainer getFileContainer(CourseEnvironment courseEnv, CheckListCourseNode cNode, Checkbox checkbox);
	
	public File getFileDirectory(CourseEnvironment courseEnv, CheckListCourseNode cNode, Checkbox checkbox);
}
