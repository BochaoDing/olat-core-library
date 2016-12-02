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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.CatalogEntryImpl;
import org.olat.repository.model.RepositoryEntryMyCourseImpl;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.resource.OLATResource;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Queries for the view "RepositoryEntryMyCourseView" dedicated to the "My course" feature.
 * The identity is a mandatory parameter.
 * 
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryMyCourseQueries {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryMyCourseQueries.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	public int countViews(SearchMyRepositoryEntryViewParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}
		
		TypedQuery<Number> query = creatMyViewQuery(params, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return Collections.emptyList();
		}

		TypedQuery<Object[]> query = creatMyViewQuery(params, Object[].class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		// we don't need statistics when rating and comments are disabled unless
		// were searching for videos, there we want to see the launch counter
		// from the statistics
		boolean needStats = repositoryModule.isRatingEnabled() || repositoryModule.isCommentEnabled() ||
				(params.getResourceTypes() != null && params.getResourceTypes().contains(VideoFileResource.TYPE_NAME));
		
		List<Long> effKeys = new ArrayList<>();
		List<Object[]> objects = query.getResultList();
		List<RepositoryEntryMyView> views = new ArrayList<>(objects.size());
		Map<OLATResource,RepositoryEntryMyCourseImpl> viewsMap = new HashMap<>();
		for(Object[] object:objects) {
			RepositoryEntry re = (RepositoryEntry)object[0];
			Number numOfMarks = (Number)object[1];
			boolean hasMarks = numOfMarks == null ? false : numOfMarks.longValue() > 0;
			Number numOffers = (Number)object[2];
			long offers = numOffers == null ? 0l : numOffers.longValue();
			Integer myRating = (Integer)object[3];
			
			RepositoryEntryStatistics stats;
			if (needStats) {
				stats = re.getStatistics();
			} else {
				stats = null;
			}
			RepositoryEntryMyCourseImpl view = new RepositoryEntryMyCourseImpl(re, stats, hasMarks, offers, myRating);
			views.add(view);
			viewsMap.put(re.getOlatResource(), view);

			Long effKey = (Long)object[4];
			if(effKey != null) {
				effKeys.add(effKey);
			}
		}
		
		if(effKeys.size() > 0) {
			List<UserEfficiencyStatementLight> efficiencyStatements =
					efficiencyStatementManager.findEfficiencyStatementsLight(effKeys);
			for(UserEfficiencyStatementLight efficiencyStatement:efficiencyStatements) {
				if(viewsMap.containsKey(efficiencyStatement.getResource())) {
					viewsMap.get(efficiencyStatement.getResource()).setEfficiencyStatement(efficiencyStatement);
				}
			}
		}
		return views;
	}

	protected <T> TypedQuery<T> creatMyViewQuery(SearchMyRepositoryEntryViewParams params,
			Class<T> type) {

		Roles roles = params.getRoles();
		Identity identity = params.getIdentity();
		List<String> resourceTypes = params.getResourceTypes();

		boolean needIdentityKey = false;
		boolean count = Number.class.equals(type);
		boolean oracle = "oracle".equals(dbInstance.getDbVendor());
		StringBuilder sb = new StringBuilder();
		
		if(count) {
			sb.append("select count(v.key) ")
			  .append(" from repositoryentry as v")
			  .append(" inner join v.olatResource as res")
			  .append(" left join v.lifecycle as lifecycle ");
		} else {
			sb.append("select v, ");
			if(params.getMarked() != null && params.getMarked().booleanValue()) {
				sb.append(" 1 as marks,");
			} else {
				needIdentityKey = true;
				sb.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
				  .append("   where mark.creator.key=:identityKey and mark.resId=v.key and mark.resName='RepositoryEntry'")
				  .append(" ) as marks,");
			}
			sb.append(" (select count(offer.key) from acoffer as offer ")
			  .append("   where offer.resource=res and offer.valid=true")
			  //TODO validity
			  .append(" ) as offers, ");
			if(repositoryModule.isRatingEnabled()) {
				needIdentityKey = true;
				sb.append(" (select rating.rating from userrating as rating")
				  .append("   where rating.resId=v.key and rating.creator.key=:identityKey and rating.resName='RepositoryEntry'")
				  .append(" ) as myrating");
			} else {
				sb.append(" 0 as myrating");
			}
			needIdentityKey = true;
			sb.append(" ,(select eff.key from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff")
			  .append("    where eff.resource=res and eff.identity.key=:identityKey")
			  .append(" ) as effKey");
			needIdentityKey |= appendOrderByInSelect(params, sb);
			sb.append(" from repositoryentry as v")
			  .append(" inner join ").append(oracle ? "" : "fetch").append(" v.olatResource as res");
			if(repositoryModule.isRatingEnabled() || repositoryModule.isCommentEnabled()) {
				sb.append(" inner join fetch v.statistics as stats");
			}
			sb.append(" left join fetch v.lifecycle as lifecycle ");
		}
		//user course informations
		//efficiency statements

		sb.append(" where ");
		needIdentityKey |= appendMyViewAccessSubSelect(sb, roles, params.getFilters(), params.isMembershipMandatory());
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			sb.append(" and v.key in (:repoEntryKeys) ");
		}
		
		if(params.getClosed() != null) {
			if(params.getClosed().booleanValue()) {
				sb.append(" and v.statusCode>0");
			} else {
				sb.append(" and v.statusCode=0");
			}
		}
		
		if(params.getFilters() != null) {
			for(Filter filter:params.getFilters()) {
				needIdentityKey |= appendFiltersInWhereClause(filter, sb);
			}
		}
		
		if(params.getParentEntry() != null) {
			sb.append(" and exists (select cei.parent.key from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
			  .append("   where cei.parent.key=:parentCeiKey and cei.repositoryEntry.key=v.key")
			  .append(" )");
		}
		if (params.isResourceTypesDefined()) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		if(params.getMarked() != null && params.getMarked().booleanValue()) {
			sb.append(" and exists (select mark2.key from ").append(MarkImpl.class.getName()).append(" as mark2 ")
			  .append("   where mark2.creator.key=:identityKey and mark2.resId=v.key and mark2.resName='RepositoryEntry'")
			  .append(" )");
		}
		
		String author = params.getAuthor();
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(author);

			sb.append(" and v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.group.key=membership.group.key and membership.identity.key=identity.key and identity.user.key=user.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(sb, "user.firstName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.lastName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" ))");
		}

		String text = params.getText();
		if (StringHelper.containsNonWhitespace(text)) {
			//displayName = '%' + displayName.replace('*', '%') + '%';
			//query.append(" and v.displayname like :displayname");
			text = PersistenceHelper.makeFuzzyQueryString(text);
			sb.append(" and (");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.description", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.objectives", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.authors", "displaytext", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		Long id = null;
		String refs = null;
		if(StringHelper.containsNonWhitespace(params.getIdAndRefs())) {
			refs = params.getIdAndRefs();
			sb.append(" and (v.externalId=:ref or v.externalRef=:ref or v.softkey=:ref");
			if(StringHelper.isLong(refs)) {
				try {
					id = Long.parseLong(refs);
					sb.append(" or v.key=:vKey or res.resId=:vKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		//alt id, refs and title
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		if(StringHelper.containsNonWhitespace(params.getIdRefsAndTitle())) {
			quickRefs = params.getIdRefsAndTitle();
			quickText = PersistenceHelper.makeFuzzyQueryString(quickRefs);
			
			sb.append(" and (v.externalId=:quickRef or v.externalRef=:quickRef or v.softkey=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "quickText", dbInstance.getDbVendor());
			if(StringHelper.isLong(quickRefs)) {
				try {
					quickId = Long.parseLong(quickRefs);
					sb.append(" or v.key=:quickVKey or res.resId=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		if(!count) {
			appendOrderBy(params.getOrderBy(), params.isOrderByAsc(), sb);
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if(params.getParentEntry() != null) {
			dbQuery.setParameter("parentCeiKey", params.getParentEntry().getKey());
		}
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			dbQuery.setParameter("repoEntryKeys", params.getRepoEntryKeys());
		}
		if (params.isResourceTypesDefined()) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(params.isLifecycleFilterDefined()) {
			dbQuery.setParameter("now", new Date());
		}
		if(id != null) {
			dbQuery.setParameter("vKey", id);
		}
		if(refs != null) {
			dbQuery.setParameter("ref", refs);
		}
		if(quickId != null) {
			dbQuery.setParameter("quickVKey", quickId);
		}
		if(quickRefs != null) {
			dbQuery.setParameter("quickRef", quickRefs);
		}
		if(quickText != null) {
			dbQuery.setParameter("quickText", quickText);
			
		}
		if(StringHelper.containsNonWhitespace(text)) {
			dbQuery.setParameter("displaytext", text);
		}
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			dbQuery.setParameter("author", author);
		}
		if(needIdentityKey) {
			dbQuery.setParameter("identityKey", identity.getKey());
		}
		return dbQuery;
	}
	
	private boolean appendMyViewAccessSubSelect(StringBuilder sb, Roles roles, List<Filter> filters, boolean membershipMandatory) {
		boolean needIdentityKey = false;
		
		sb.append("(v.access >= ");
		if (roles.isAuthor()) {
			sb.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
		} else if (roles.isGuestOnly()) {
			sb.append(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			sb.append(RepositoryEntry.ACC_USERS);
		}

		StringBuilder inRoles = new StringBuilder();
		if(filters != null && filters.size() > 0) {
			for(Filter filter: filters) {
				if(Filter.asAuthor.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.owner.name()).append("'");
				} else if(Filter.asCoach.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.coach.name()).append("'");
				} else if (Filter.asParticipant.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.participant.name()).append("'");
				}
			}
		}

		//+ membership
		if(roles.isGuestOnly()) {
			sb.append(")");
		} else {
			if(inRoles.length() == 0 && !membershipMandatory) {
				needIdentityKey = true;
				//sub select are very quick
				sb.append(" or (")
				  .append("  v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
				  .append("  and v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
				  .append("    where rel.group.key=membership.group.key and membership.identity.key=:identityKey")
				  .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
				  .append("  )")
				  .append(" )")
				  .append(")");
			} else {
				if(inRoles.length() == 0) {
					inRoles.append("'").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("'");
				}
				needIdentityKey = true;
				//make sure that in all case the role is mandatory
				sb.append(" or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
				  .append(" and v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
				  .append("    where rel.group.key=membership.group.key and membership.identity.key=:identityKey")
				  .append("      and membership.role in (").append(inRoles).append(")")
				  .append(" )");
			}
		}
		return needIdentityKey;
	}
	
	private boolean appendFiltersInWhereClause(Filter filter, StringBuilder sb) {
		boolean needIdentityKey = false;
		switch(filter) {
			case showAll: break;
			case currentCourses:
				sb.append(" and lifecycle.validFrom<=:now and lifecycle.validTo>=:now");
				break;
			case upcomingCourses:
				sb.append(" and lifecycle.validFrom>=:now");
				break;
			case oldCourses:
				sb.append(" and lifecycle.validTo<=:now");
				break;
			case passed:
				needIdentityKey = true;
				sb.append(" and exists (select eff2.key from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff2")
				  .append("    where eff2.resource=res and eff2.identity.key=:identityKey and eff2.passed=true")
				  .append(" )");
				break;
			case notPassed:
				needIdentityKey = true;
				sb.append(" and exists (select eff3.key from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff3")
				  .append("    where eff3.resource=res and eff3.identity.key=:identityKey and eff3.passed=false")
				  .append(" )");
				break;
			case withoutPassedInfos:
				needIdentityKey = true;
				sb.append(" and exists (select eff4.key from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff4")
				  .append("    where eff4.resource=res and eff4.identity.key=:identityKey and eff4.passed is null")
				  .append(" )");
				break;
			default: {}
		}
		return needIdentityKey;
	}
	
	/**
	 * Append additional informations and values to the select part of the query
	 * needed by the order by.
	 * 
	 * @param params
	 * @param sb
	 * @return
	 */
	private boolean appendOrderByInSelect(SearchMyRepositoryEntryViewParams params, StringBuilder sb) {
		boolean needIdentityKey = false;
		OrderBy orderBy = params.getOrderBy();
		if(orderBy != null) {
			switch(orderBy) {
				case automatic://need lastVisited
				case lastVisited:
					needIdentityKey = true;
					sb.append(" ,(select infos2.recentLaunch from usercourseinfos as infos2")
					  .append("    where infos2.resource=res and infos2.identity.key=:identityKey")
					  .append(" ) as recentLaunch");
					break;
				case passed:
					needIdentityKey = true;
					sb.append(" ,(select eff3.passed from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff3")
					  .append("    where eff3.resource=res and eff3.identity.key=:identityKey")
					  .append(" ) as passed");
					break;
				case score:
					needIdentityKey = true;
					sb.append(" ,(select eff4.score from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as eff4")
					  .append("    where eff4.resource=res and eff4.identity.key=:identityKey")
					  .append(" ) as score");
					break;
				default: //do nothing
			}
		}
		return needIdentityKey;
	}
	
	private void appendOrderBy(OrderBy orderBy, boolean asc, StringBuilder sb) {
		if(orderBy != null) {
			switch(orderBy) {
				case automatic://! the sorting is reverse
					if(asc) {
						sb.append(" order by recentLaunch desc nulls last, lifecycle.validFrom desc nulls last, marks desc nulls last, lower(v.displayname) asc ");
					} else {
						sb.append(" order by recentLaunch asc nulls last, lifecycle.validFrom asc nulls last, marks asc nulls last, lower(v.displayname) desc ");
					}
					break;
				case favorit:
					if(asc) {
						sb.append(" order by marks asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by marks desc, lower(v.displayname) desc");
					}
					break;
				case lastVisited:
					sb.append(" order by recentLaunch ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case passed:
					sb.append(" order by passed ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case score:
					sb.append(" order by score ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case title:
					//life cycle always sorted from the newer to the older.
					if(asc) {
						sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last");
					} else {
						sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last");
					}
					break;
				case lifecycle:
					sb.append(" order by lifecycle.validFrom ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case author:
					sb.append(" order by lower(v.authors)");
					appendAsc(sb, asc).append(" nulls last");
					break;
				case location:
					sb.append(" order by lower(v.location)");
					appendAsc(sb, asc).append(" nulls last");
					break;	
				case creationDate:
					sb.append(" order by v.creationDate ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case lastModified:
					sb.append(" order by v.lastModified ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case rating:
					sb.append(" order by v.statistics.rating ");
					if(asc) {
						sb.append(" asc nulls first");
					} else {
						sb.append(" desc nulls last");
					}
					sb.append(", lower(v.displayname) asc");
					break;
				case launchCounter:
					sb.append(" order by v.statistics.launchCounter ");
					if(asc) {
						sb.append(" asc nulls first");
					} else {
						sb.append(" desc nulls last");
					}
					sb.append(", lower(v.displayname) asc");
					break;
				case key:
					sb.append(" order by v.key");
					appendAsc(sb, asc);
					break;
				case displayname:
					sb.append(" order by lower(v.displayname)");
					appendAsc(sb, asc);	
					break;
				case externalRef:
					sb.append(" order by lower(v.externalRef)");
					appendAsc(sb, asc);	
					break;
				case externalId:
					sb.append(" order by lower(v.externalId)");
					appendAsc(sb, asc);	
					break;
				case lifecycleLabel:
					sb.append(" order by lifecycle.label");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleSoftkey:
					sb.append(" order by lifecycle.softKey");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleStart:
					sb.append(" order by lifecycle.validFrom ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleEnd:
					sb.append(" order by lifecycle.validTo ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				default:
					if(asc) {
						sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last");
					} else {
						sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last");
					}
					break;
			}
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
}