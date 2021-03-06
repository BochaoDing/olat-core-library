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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.CatalogEntry;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 * TODO sev26
 * Its one parameter container class and not many i.e. params. In order not to
 * confuse the reader, rename it to "Param".
 */
public class SearchMyRepositoryEntryViewParams {
	private Identity identity;
	private Roles roles;
	
	private Boolean marked;
	private Boolean closed;
	private boolean membershipMandatory = false;
	
	private OrderBy orderBy;
	private boolean asc;
	private List<Filter> filters;
	private CatalogEntry parentEntry;
	private List<String> resourceTypes;
	private List<Long> repoEntryKeys;

	private String idAndRefs;
	private String idRefsAndTitle;
	private String author;
	private String text;
	
	public SearchMyRepositoryEntryViewParams(Identity identity, Roles roles, String... resourceTypes) {
		this.identity = identity;
		this.roles = roles;
		addResourceTypes(resourceTypes);
	}
	
	public CatalogEntry getParentEntry() {
		return parentEntry;
	}

	public void setParentEntry(CatalogEntry parentEntry) {
		this.parentEntry = parentEntry;
	}

	public boolean isMembershipMandatory() {
		return membershipMandatory;
	}

	public void setMembershipMandatory(boolean membershipMandatory) {
		this.membershipMandatory = membershipMandatory;
	}

	public List<Long> getRepoEntryKeys() {
		return repoEntryKeys;
	}

	public void setRepoEntryKeys(List<Long> repoEntryKeys) {
		this.repoEntryKeys = repoEntryKeys;
	}

	public String getIdAndRefs() {
		return idAndRefs;
	}

	public void setIdAndRefs(String idAndRefs) {
		this.idAndRefs = idAndRefs;
	}

	public String getIdRefsAndTitle() {
		return idRefsAndTitle;
	}

	public void setIdRefsAndTitle(String idRefsAndTitle) {
		this.idRefsAndTitle = idRefsAndTitle;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isOrderByAsc() {
		return asc;
	}

	public void setOrderByAsc(boolean asc) {
		this.asc = asc;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	public boolean isPassedFiltered() {
		return filters != null && (filters.contains(Filter.notPassed)
				|| filters.contains(Filter.passed)
				|| filters.contains(Filter.withoutPassedInfos));
	}
	
	public boolean isLifecycleFilterDefined() {
		return filters != null && (filters.contains(Filter.upcomingCourses)
				|| filters.contains(Filter.currentCourses)
				|| filters.contains(Filter.oldCourses));
	}
	
	public boolean isResourceTypesDefined() {
		return resourceTypes != null && resourceTypes.size() > 0;
	}

	public List<String> getResourceTypes() {
		return resourceTypes;
	}
	
	public void setResourceTypes(List<String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}
	
	public void addResourceTypes(String... types) {
		if(this.resourceTypes == null) {
			this.resourceTypes = new ArrayList<String>();
		}
		if(types != null) {
			for(String resourceType:types) {
				this.resourceTypes.add(resourceType);
			}
		}
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}
	
	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
	}
	
	public enum OrderBy {
		automatic,
		favorit,
		lastVisited,
		passed,
		score,
		title,
		lifecycle,
		author,
		location,
		creationDate,
		lastModified,
		rating,
		launchCounter,
		key,
		displayname,
		externalRef,
		externalId,
		lifecycleLabel,
		lifecycleSoftkey,
		lifecycleStart,
		lifecycleEnd,
	}
	
	public enum Filter {
		showAll,
		currentCourses,
		oldCourses,
		upcomingCourses,
		asParticipant,
		asCoach,
		asAuthor,
		notBooked,
		passed,
		notPassed,
		withoutPassedInfos
	}
}
