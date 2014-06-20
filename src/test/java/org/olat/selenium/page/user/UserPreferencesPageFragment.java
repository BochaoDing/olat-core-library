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
package org.olat.selenium.page.user;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jcodec.common.Assert;
import org.olat.selenium.page.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The user system preferences.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserPreferencesPageFragment {
	
	public static final By noneRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='none']");
	public static final By autoRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='auto']");
	public static final By ondemandRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='ondemand']");
	
	public static final By saveSystemSettingsButton = By.xpath("//div[contains(@class,'o_sel_home_settings_gui_buttons')]//button[@type='button']");
	
	@Drone
	private WebDriver browser;
	
	@FindBy(className = "o_sel_home_settings_resume")
	private WebElement resumeFieldset;
	
	/**
	 * @return True if the user's preference panel is displayed.
	 */
	public boolean isDisplayed() {
		return resumeFieldset.isDisplayed();
	}
	
	/**
	 * Check that the user preferences page is displayed.
	 * 
	 * @return The user preferences page fragment
	 */
	public UserPreferencesPageFragment assertOnUserPreferences() {
		Assert.assertTrue(resumeFieldset.isDisplayed());
		return this;
	}
	
	/**
	 * Set and save the resume preferences.
	 * 
	 * @param resume
	 * @return
	 */
	public UserPreferencesPageFragment setResume(ResumeOption resume) {
		Assert.assertTrue(resumeFieldset.isDisplayed());
		WebElement radio = null;
		switch(resume) {
			case none: radio = browser.findElement(noneRadio); break;
			case auto: radio = browser.findElement(autoRadio); break;
			case ondemand: radio = browser.findElement(ondemandRadio); break;
		}
		
		radio.click();
		
		WebElement saveButton = browser.findElement(saveSystemSettingsButton);
		saveButton.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public enum ResumeOption {
		none,
		auto,
		ondemand
	}
}
