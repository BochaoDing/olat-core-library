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
package org.olat.selenium.page.course;

import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 15 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOptionsPage {

	private WebDriver browser;
	
	public CourseOptionsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CourseOptionsPage calendar(Boolean enable) {
		By calendarBy = By.cssSelector(".o_sel_course_options_calendar input[type='checkbox']");
		WebElement calendarEl = browser.findElement(calendarBy);
		OOGraphene.check(calendarEl, enable);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CourseOptionsPage save() {
		By saveBy = By.cssSelector("a.o_sel_course_options_save");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CoursePageFragment clickToolbarBack() {
		browser.findElement(NavigationPage.toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return CoursePageFragment.getCourse(browser);
	}

}
