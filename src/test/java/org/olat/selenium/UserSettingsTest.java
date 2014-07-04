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
package org.olat.selenium;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.user.PortalPage;
import org.olat.selenium.page.user.UserPasswordPage;
import org.olat.selenium.page.user.UserPreferencesPageFragment;
import org.olat.selenium.page.user.UserPreferencesPageFragment.ResumeOption;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class UserSettingsTest {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	@Page
	private UserToolsPage userTools;
	@Page
	private NavigationPage navBar;
	
	/**
	 * Set the resume preferences to automatically resume the session,
	 * open a course, log out, log in and check if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resume_resumeCourseAutomatically(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		//deploy a course
		CourseVO course = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		//login
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		userTools
			.assertOnUserTools()
			.openUserToolsMenu()
			.openMySettings()
			.assertOnUserSettings()
			.openPreferences()
			.assertOnUserPreferences()
			.setResume(ResumeOption.auto);
		
		//open a course via REST url
		CoursePageFragment coursePage = CoursePageFragment.getCourse(browser, deploymentUrl, course);
		coursePage
			.assertOnCoursePage()
			.clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(course.getTitle()));
	}
	
	/**
	 * Set the resume preferences to resume the session on request,
	 * open a course, log out, log in, resume the session and check
	 * if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resume_resumeCourseOnDemand(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		//deploy a course
		CourseVO course = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		//login
		loginPage.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setResume(ResumeOption.ondemand);
		
		//open a course via REST url and click it
		CoursePageFragment.getCourse(browser, deploymentUrl, course).clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		//resume
		loginPage.resumeWithAssert();

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(course.getTitle()));
	}
	
	/**
	 * Disable the resume function and check that the resume
	 * popup don't stay in our way after login.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void resume_disabled(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		//set the preferences to resume automatically
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setResume(ResumeOption.none);
	
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		//check that we don't see the resume button
		List<WebElement> resumeButtons = browser.findElements(LoginPage.resumeButton);
		Assert.assertTrue(resumeButtons.isEmpty());
		//double check that we are really logged in
		loginPage.assertLoggedIn(user);
	}
	
	/**
	 * Switch the language to german and after logout login to english
	 * and check every time.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void language_switch(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		//set the languages preferences to german
		userTools
			.assertOnUserTools()
			.openUserToolsMenu()
			.openMySettings()
			.assertOnUserSettings()
			.openPreferences()
			.assertOnUserPreferences()
			.setLanguage("de");
		
		userTools.logout();
		
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		WebElement usernameDE = browser.findElement(LoginPage.usernameFooterBy);
		boolean de = usernameDE.getText().contains("Eingeloggt als");
		Assert.assertTrue(de);
		List<WebElement> deMarker = browser.findElements(By.className("o_lang_de"));
		Assert.assertFalse(deMarker.isEmpty());
		
		
		//set the languages preferences to english
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setLanguage("en");
		
		userTools.logout();
				
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		WebElement usernameEN = browser.findElement(LoginPage.usernameFooterBy);
		boolean en = usernameEN.getText().contains("Logged in as");
		Assert.assertTrue(en);
		List<WebElement> enMarker = browser.findElements(By.className("o_lang_en"));
		Assert.assertFalse(enMarker.isEmpty());
	}
	
	/**
	 * Change the password, log out and try to log in again
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void password_change(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		userTools
			.assertOnUserTools()
			.openUserToolsMenu()
			.openPassword();
		
		String newPassword = UUID.randomUUID().toString();
		UserPasswordPage password = UserPasswordPage.getUserPasswordPage(browser);
		password.setNewPassword(user.getPassword(), newPassword);
		
		userTools.logout();
		
		loginPage
			.loginAs(user.getLogin(), newPassword)
			.resume()
			.assertLoggedIn(user);
	}
	
	/**
	 * Reset the preferences and check that a log out happens
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void resetPreferences(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		UserPreferencesPageFragment prefs = userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences();
		//reset the preferences
		prefs.resetPreferences();
		//check the user is log out
		loginPage.assertOnLoginPage();
	}
	
	/**
	 * Go in portal, edit it, deactivate the quick start portlet,
	 * finish editing, check that the quick start portlet disappears,
	 * re-edit, reactivate the quick start portlet and check it is
	 * again in the non-edit view. 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void portletDeactivateActivate(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword());
		
		PortalPage portal = navBar.openPortal()
			.assertPortlet(PortalPage.quickStartBy)
			.edit()
			.disable(PortalPage.quickStartBy)
			.finishEditing()
			.assertNotPortlet(PortalPage.quickStartBy);
		
		//re-enable quickstart
		portal.edit()
			.enable(PortalPage.quickStartBy)
			.finishEditing()
			.assertPortlet(PortalPage.quickStartBy);
		
		List<WebElement> portalInactive = browser.findElements(PortalPage.inactiveBy);
		Assert.assertTrue(portalInactive.isEmpty());
	}
	
	/**
	 * Go to the portal, edit it, move the notes to the
	 * top, quit editing, check the notes are the first
	 * portlet in the view mode.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void movePortletToTheTop(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		loginPage
			.loginAs(user.getLogin(), user.getPassword());
		
		PortalPage portal = navBar.openPortal()
			.assertPortlet(PortalPage.notesBy)
			.edit()
			.moveLeft(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveRight(PortalPage.quickStartBy)
			.moveDown(PortalPage.quickStartBy);
		//finish editing
		portal.finishEditing();
		
		//no inactive panel -> we are in view mode
		List<WebElement> portalInactive = browser.findElements(PortalPage.inactiveBy);
		Assert.assertTrue(portalInactive.isEmpty());

		//notes must be first
		List<WebElement> portlets = browser.findElements(By.className("o_portlet"));
		Assert.assertFalse(portlets.isEmpty());	
		WebElement notesPortlet = portlets.get(0);
		String cssClass = notesPortlet.getAttribute("class");
		Assert.assertNotNull(cssClass);
		Assert.assertTrue(cssClass.contains("o_portlet_notes"));
	}
	
	/**
	 * Browse some tabs, two times back and check where I am.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void browserBack(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		Assume.assumeTrue(browser instanceof FirefoxDriver);
		
		loginPage.loginAs("administrator", "openolat");
		
		navBar
			.openPortal()
			.assertPortlet(PortalPage.quickStartBy);
		navBar.openAuthoringEnvironment();
		navBar.openGroups(browser);
		navBar.openMyCourses();
		navBar.openUserManagement();
		navBar.openAdministration();
		
		//we are in administration
		browser.navigate().back();
		//we are in user management
		browser.navigate().back();
		//we are in "My courses", check
		OOGraphene.waitElement(NavigationPage.myCoursesAssertBy);
	}
}
