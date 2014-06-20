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
package org.olat.selenium.page.repository;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jcodec.common.Assert;
import org.olat.selenium.page.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 
 * Page to control the different settings of a repository entry.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEditDescriptionPage {
	
	public static final By generaltabBy = By.className("o_sel_edit_repositoryentry");
	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");

	
	@Drone
	private WebDriver browser;
	
	@FindBy(className = "o_sel_edit_repositoryentry")
	private WebElement generalTab;
	
	
	public RepositoryEditDescriptionPage assertOnGeneralTab() {
		Assert.assertTrue(generalTab.isDisplayed());
		return this;
	}
	
	public RepositoryDetailsPage clickToolbarBack() {
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy();
		
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(RepositoryDetailsPage.class, main);
	}
}
