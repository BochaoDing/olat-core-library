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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void createNewOwnedPorfolio() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-1");
		String title = "My portfolio";
		String summary = "My live";
		
		Binder binder = portfolioService.createNewBinder(title, summary, null, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(binder);
		Assert.assertNotNull(binder.getKey());
		Assert.assertNotNull(binder.getCreationDate());
		Assert.assertNotNull(binder.getLastModified());
		Assert.assertEquals(title, binder.getTitle());
		Assert.assertEquals(summary, binder.getSummary());
		
		List<Binder> ownedBinders = portfolioService.getOwnedBinders(id);
		Assert.assertNotNull(ownedBinders);
		Assert.assertEquals(1, ownedBinders.size());
		Binder ownedBinder = ownedBinders.get(0);
		Assert.assertNotNull(ownedBinder);
		Assert.assertEquals(binder, ownedBinder);
	}
	
	@Test
	public void binderAccessRights() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-2");
		String title = "My private binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commitAndCloseSession();
		
		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		Assert.assertNotNull(rights);
		Assert.assertEquals(1, rights.size());
		AccessRights ownerRight = rights.get(0);
		Assert.assertEquals(binder.getKey(), ownerRight.getBinderKey());
		Assert.assertEquals(owner, ownerRight.getIdentity());
		Assert.assertEquals(PortfolioRoles.owner, ownerRight.getRole());
	}

	@Test
	public void binderAndSectionAndPageAccessRights() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-3");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-4");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-5");
		String title = "My published binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Coached section", null, null, binder);
		dbInstance.commit();
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		portfolioService.appendNewPage(owner, "Reviewed page", "", null, null, section);
		portfolioService.addAccessRights(section, coach, PortfolioRoles.coach);
		
		dbInstance.commit();
		List<Page> pages = portfolioService.getPages(section, null);
		Page page = pages.get(0);
		portfolioService.addAccessRights(page, reviewer, PortfolioRoles.reviewer);

		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		Assert.assertNotNull(rights);
		Assert.assertEquals(4, rights.size());
		
		boolean foundOwner = false;
		boolean foundCoach = false;
		boolean foundReviewer = false;
		
		for(AccessRights right:rights) {
			if(PortfolioRoles.owner.equals(right.getRole()) && owner.equals(right.getIdentity())) {
				foundOwner = true;
			} else if(PortfolioRoles.coach.equals(right.getRole()) && coach.equals(right.getIdentity())) {
				foundCoach = true;
			} else if(PortfolioRoles.reviewer.equals(right.getRole()) && reviewer.equals(right.getIdentity())) {
				foundReviewer = true;
			}
		}
		
		Assert.assertTrue(foundOwner);
		Assert.assertTrue(foundCoach);
		Assert.assertTrue(foundReviewer);
	}
	
	@Test
	public void binderAndSectionAndPageAccessRights_byIdentity() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-5");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-6");
		String title = "My published binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Coached section", null, null, binder);
		dbInstance.commit();
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		portfolioService.appendNewPage(owner, "Reviewed page", "", null, null, section);
		portfolioService.addAccessRights(section, identity, PortfolioRoles.coach);
		
		dbInstance.commit();
		List<Page> pages = portfolioService.getPages(section, null);
		Page page = pages.get(0);
		portfolioService.addAccessRights(page, identity, PortfolioRoles.reviewer);

		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder, identity);
		Assert.assertNotNull(rights);
		Assert.assertEquals(2, rights.size());
	}
	
	
	@Test
	public void searchOwnedBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner-");
		Binder binder = portfolioService.createNewBinder("Binder 2", "Binder with one section.", null, owner);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("First section", "My first section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("Second section", "My second section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		List<Section> sections = portfolioService.getSections(binder);
		for(int i=0; i<2; i++) {
			Section section = sections.get(1);
			portfolioService.appendNewPage(owner, "Page-1-" + i, "", null, null, section);
			portfolioService.appendNewPage(owner, "Page-2-" + i, "", null, null, section);
		}

		List<BinderStatistics> rows = portfolioService.searchOwnedBinders(owner);
		Assert.assertNotNull(rows);
		Assert.assertEquals(1, rows.size());
		
		BinderStatistics myBinder = rows.get(0);
		Assert.assertEquals(2, myBinder.getNumOfSections());
		Assert.assertEquals(4, myBinder.getNumOfPages());
	}
	
	@Test
	public void assignTemplate() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-7");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-8");
		
		//create a template
		String title = "Template binder";
		String summary = "Binder used as a template";
		Binder template = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		for(int i=0; i<4; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, template);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
		List<Section> templateSections = portfolioService.getSections(template);
		Assert.assertNotNull(templateSections);
		Assert.assertEquals(4, templateSections.size());
		
		
		//user copy the template
		Binder binder = portfolioService.assignBinder(id, template, null, null, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(binder);
		Assert.assertNotNull(binder.getKey());
		Assert.assertNotNull(binder.getCopyDate());
		Assert.assertNotNull(template.getTitle(), binder.getTitle());
		
		List<Section> reloadedSections = portfolioService.getSections(binder);
		Assert.assertNotNull(reloadedSections);
		Assert.assertEquals(4, reloadedSections.size());
		Assert.assertEquals(templateSections.get(0).getTitle(), reloadedSections.get(0).getTitle());
		Assert.assertEquals("Section 1", reloadedSections.get(1).getTitle());
		Assert.assertEquals(templateSections.get(2).getTitle(), reloadedSections.get(2).getTitle());
		Assert.assertEquals("Section 3", reloadedSections.get(3).getTitle());
		
		Assert.assertEquals(templateSections.get(0), ((SectionImpl)reloadedSections.get(0)).getTemplateReference());
		Assert.assertEquals(templateSections.get(1), ((SectionImpl)reloadedSections.get(1)).getTemplateReference());
		Assert.assertEquals(templateSections.get(2), ((SectionImpl)reloadedSections.get(2)).getTemplateReference());
		Assert.assertEquals(templateSections.get(3), ((SectionImpl)reloadedSections.get(3)).getTemplateReference());
	}

	@Test
	public void isTemplateInUse() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-9");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		//assign a template
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		Binder template = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		dbInstance.commit();
		Assert.assertNotNull(template);

		boolean inUse = portfolioService.isTemplateInUse(templateBinder, templateEntry, null);
		Assert.assertTrue(inUse);
	}

	@Test
	public void syncBinder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		//add 2 sections
		for(int i=0; i<2; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, templateBinder);
			dbInstance.commit();
		}
		
		List<Section> templateSections = portfolioService.getSections(templateBinder);
		Assert.assertNotNull(templateSections);
		Assert.assertEquals(3, templateSections.size());
		
		//user get a the binder from the template
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "ac-234", new Date());
		dbInstance.commit();
		Assert.assertNotNull(binder);
		boolean inUse = portfolioService.isTemplateInUse(templateBinder, templateEntry, "ac-234");
		Assert.assertTrue(inUse);
		
		//update the template with 2 more sections
		for(int i=2; i<4; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, templateBinder);
			dbInstance.commit();
		}
		
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		Assert.assertNotNull(synchedBinder);
		dbInstance.commit();
		Assert.assertTrue(synchedBinder.isChanged());
		Assert.assertEquals(binder, synchedBinder.getBinder());
		List<Section> synchedSections = portfolioService.getSections(synchedBinder.getBinder());
		Assert.assertEquals(5, synchedSections.size());
	}
	
	@Test
	public void syncBinder_move() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef0 = portfolioService.getSections(templateBinder).get(0);
		//add 2 sections
		SectionRef sectionRef1 = portfolioService.appendNewSection("1 section ", "Section 1", null, null, templateBinder);
		SectionRef sectionRef2 = portfolioService.appendNewSection("2 section ", "Section 2", null, null, templateBinder);
		dbInstance.commit();
		
		//make 4 assigments
		Section templateSection0 = portfolioService.getSection(sectionRef0);
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Section templateSection2 = portfolioService.getSection(sectionRef2);
		Assignment assignment1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, templateSection1);
		Assignment assignment1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, templateSection1);
		Assignment assignment2_1 = portfolioService.addAssignment("2.1 Assignment", "", "", AssignmentType.essay, templateSection2);
		Assignment assignment2_2 = portfolioService.addAssignment("2.2 Assignment", "", "", AssignmentType.essay, templateSection2);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getAssignments(templateBinder);
		Assert.assertEquals(4, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "72", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assigments
		List<Assignment> assignments = portfolioService.getAssignments(binder);
		Assert.assertEquals(4, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment, id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(4, pages.size());
		
		//the author move an assigment
		portfolioService.moveAssignment(templateSection1, assignment1_1, templateSection2);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_1, templateSection1);
		dbInstance.commitAndCloseSession();
		
		//check the move
		List<Assignment> templateAssignmentsSection1 = portfolioService.getAssignments(templateSection1);
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment1_2));
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment2_1));
		List<Assignment> templateAssignmentsSection2 = portfolioService.getAssignments(templateSection2);
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment2_2));
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment1_1));
		
		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		List<Section> sections = portfolioService.getSections(freshBinder);
		Assert.assertEquals(3, sections.size());
		Section section0 = sections.get(0);
		Section section1 = sections.get(1);
		Section section2 = sections.get(2);
		Assert.assertEquals(templateSection0, section0.getTemplateReference());
		Assert.assertEquals(templateSection1, section1.getTemplateReference());
		Assert.assertEquals(templateSection2, section2.getTemplateReference());
		
		// load pages from section 1
		List<Page> pagesSection1 = portfolioService.getPages(section1, null);
		Assert.assertEquals(2, pagesSection1.size());
		
		Page page1_2 = pagesSection1.get(0);
		Page page2_1 = pagesSection1.get(1);
		Assert.assertTrue(page1_2.getTitle().equals("2.1 Assignment") || page1_2.getTitle().equals("1.2 Assignment"));
		Assert.assertTrue(page2_1.getTitle().equals("2.1 Assignment") || page2_1.getTitle().equals("1.2 Assignment"));
		
		// and pages from section 2
		List<Page> pagesSection2 = portfolioService.getPages(section2, null);
		Assert.assertEquals(2, pagesSection2.size());
		Page page2_2 = pagesSection2.get(0);
		Page page1_1 = pagesSection2.get(1);
		Assert.assertTrue(page2_2.getTitle().equals("1.1 Assignment") || page2_2.getTitle().equals("2.2 Assignment"));
		Assert.assertTrue(page1_1.getTitle().equals("1.1 Assignment") || page1_1.getTitle().equals("2.2 Assignment"));
	}
	
	@Test
	public void syncBinder_moveInNewSection() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef0 = portfolioService.getSections(templateBinder).get(0);
		//add 2 sections
		SectionRef sectionRef1 = portfolioService.appendNewSection("1 section ", "Section 1", null, null, templateBinder);
		SectionRef sectionRef2 = portfolioService.appendNewSection("2 section ", "Section 2", null, null, templateBinder);
		dbInstance.commit();
		
		//make 4 assignments
		Section templateSection0 = portfolioService.getSection(sectionRef0);
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Section templateSection2 = portfolioService.getSection(sectionRef2);
		Assignment assignment1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, templateSection1);
		Assignment assignment1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, templateSection1);
		Assignment assignment2_1 = portfolioService.addAssignment("2.1 Assignment", "", "", AssignmentType.essay, templateSection2);
		Assignment assignment2_2 = portfolioService.addAssignment("2.2 Assignment", "", "", AssignmentType.essay, templateSection2);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getAssignments(templateBinder);
		Assert.assertEquals(4, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "72", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assignments
		List<Assignment> assignments = portfolioService.getAssignments(binder);
		Assert.assertEquals(4, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment, id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		// check that the student has it's 4 pages
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(4, pages.size());
		
		//author create a new section and move an assignment
		SectionRef sectionRef3 = portfolioService.appendNewSection("3 section ", "Section 3", null, null, templateBinder);
		dbInstance.commit();

		Section templateSection3 = portfolioService.getSection(sectionRef3);
		Assignment assignment3_1 = portfolioService.addAssignment("3.1 Assignment", "", "", AssignmentType.essay, templateSection3);
		dbInstance.commit();
		
		//the author move an assigment
		portfolioService.moveAssignment(templateSection1, assignment1_1, templateSection2);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_1, templateSection3);
		dbInstance.commitAndCloseSession();
		
		//check the move
		List<Assignment> templateAssignmentsSection1 = portfolioService.getAssignments(templateSection1);
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment1_2));
		List<Assignment> templateAssignmentsSection2 = portfolioService.getAssignments(templateSection2);
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment2_2));
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment1_1));
		List<Assignment> templateAssignmentsSection3 = portfolioService.getAssignments(templateSection3);
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment2_1));
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment3_1));
		
		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		List<Section> sections = portfolioService.getSections(freshBinder);
		Assert.assertEquals(4, sections.size());
		Section section0 = sections.get(0);
		Section section1 = sections.get(1);
		Section section2 = sections.get(2);
		Section section3 = sections.get(3);
		Assert.assertEquals(templateSection0, section0.getTemplateReference());
		Assert.assertEquals(templateSection1, section1.getTemplateReference());
		Assert.assertEquals(templateSection2, section2.getTemplateReference());
		Assert.assertEquals(templateSection3, section3.getTemplateReference());
		
		// load pages from section 1
		List<Page> pagesSection1 = portfolioService.getPages(section1, null);
		Assert.assertEquals(1, pagesSection1.size());
		Page page1_2 = pagesSection1.get(0);
		Assert.assertTrue(page1_2.getTitle().equals("1.2 Assignment"));
		
		// and pages from section 2
		List<Page> pagesSection2 = portfolioService.getPages(section2, null);
		Assert.assertEquals(2, pagesSection2.size());
		Page page2_2 = pagesSection2.get(0);
		Page page1_1 = pagesSection2.get(1);
		Assert.assertTrue(page2_2.getTitle().equals("1.1 Assignment") || page2_2.getTitle().equals("2.2 Assignment"));
		Assert.assertTrue(page1_1.getTitle().equals("1.1 Assignment") || page1_1.getTitle().equals("2.2 Assignment"));
		
		// and pages from section 3
		List<Page> pagesSection3 = portfolioService.getPages(section3, null);
		Assert.assertEquals(1, pagesSection3.size());
		Page page2_1 = pagesSection3.get(0);
		Assert.assertTrue(page2_1.getTitle().equals("2.1 Assignment"));		
		
		List<Assignment> assignmentsSection3 = section3.getAssignments();
		Assert.assertEquals(2, assignmentsSection3.size());
		
		Assignment templateSynchedSection3a = assignmentsSection3.get(0).getTemplateReference();
		Assignment templateSynchedSection3b = assignmentsSection3.get(1).getTemplateReference();
		Assert.assertTrue(assignment3_1.equals(templateSynchedSection3a) || assignment3_1.equals(templateSynchedSection3b));
		Assert.assertTrue(assignment2_1.equals(templateSynchedSection3a) || assignment2_1.equals(templateSynchedSection3b));
	}
	
	private RepositoryEntry createTemplate(Identity initialAuthor, String displayname, String description) {
		OLATResource resource = portfolioService.createBinderTemplateResource();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		portfolioService.createAndPersistBinderTemplate(initialAuthor, re, Locale.ENGLISH);
		return re;
	}
}
