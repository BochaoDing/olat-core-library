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
package org.olat.modules.coach.manager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.NativeQueryBuilder;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class CoachingDAO {
	
	private static final OLog log = Tracing.createLoggerFor(CoachingDAO.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;

	public boolean isCoach(IdentityRef coach) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("select v.key from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join v.olatResource as res on res.resName='CourseModule'")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("')")
		  .append(" where membership.identity.key=:identityKey")
		  .append(" and (")
		  .append("  (membership.role = 'coach' and (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true)))")
		  .append("  or")
		  .append("  (membership.role = 'owner' and v.access>=1)")
		  .append(" )");
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", coach.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return firstKey.size() > 0;
	}

	public EfficiencyStatementEntry getEfficencyStatementEntry(UserEfficiencyStatement statement,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(statement.getCourseRepoKey(), false);
		Identity identity = statement.getIdentity();
		return new EfficiencyStatementEntry(identity, re, statement, userPropertyHandlers, locale);
	}

	public List<EfficiencyStatementEntry> getEfficencyStatementEntries(List<Identity> students, List<RepositoryEntry> courses,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		
		if(students.isEmpty() || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Long> studentsKey = new ArrayList<Long>();
		for(Identity student:students) {
			studentsKey.add(student.getKey());
		}
		
		List<UserEfficiencyStatement> statements = getEfficiencyStatementByStudentKeys(studentsKey, courses);
		List<EfficiencyStatementEntry> entries = new ArrayList<>(students.size() * courses.size());
		for(RepositoryEntry course:courses) {
			for(Identity student:students) {
				UserEfficiencyStatement statement = getUserEfficiencyStatementFor(student.getKey(), course, statements);
				entries.add(new EfficiencyStatementEntry(student, course, statement, userPropertyHandlers, locale));
			}
		}
		return entries;
	}
	
	public List<UserEfficiencyStatement> getEfficencyStatementEntries(Identity student) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from ").append(UserEfficiencyStatementLight.class.getName()).append(" as statement ")
		  .append(" where statement.identity.key=:studentKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("studentKey", student.getKey())
				.getResultList();
	}
	
	private UserEfficiencyStatement getUserEfficiencyStatementFor(Long studentKey, RepositoryEntry course, List<UserEfficiencyStatement> statements) {
		for(UserEfficiencyStatement statement:statements) {
			if(studentKey.equals(statement.getIdentity().getKey()) && course.getKey().equals(statement.getCourseRepoKey())) {
				return statement;
			}
		}
		return null;
	}
	
	private List<UserEfficiencyStatement> getEfficiencyStatementByStudentKeys(List<Long> studentKeys, List<RepositoryEntry> courses) {
		if(studentKeys == null || studentKeys.isEmpty() || courses == null || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from ").append(UserEfficiencyStatementLight.class.getName()).append(" as statement ")
		  .append(" where statement.identity.key in (:studentsKey) and statement.resource.key in (:courseResourcesKey)");
		
		List<Long> coursesKey = new ArrayList<Long>();
		for(RepositoryEntry course:courses) {
			coursesKey.add(course.getOlatResource().getKey());
		}

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("courseResourcesKey",coursesKey)
				.setParameter("studentsKey", studentKeys).getResultList();
	}
	
	protected List<GroupStatEntry> getGroupsStatisticsNative(Identity coach) {
		Map<Long,GroupStatEntry> map = new HashMap<>();
		boolean hasGroups = getGroups(coach, map);
		if(hasGroups) {
			boolean hasCoachedGroups = getGroupsStatisticsInfosForCoach(coach, map);
			boolean hasOwnedGroups = getGroupsStatisticsInfosForOwner(coach, map);
			for(GroupStatEntry entry:map.values()) {
				entry.getRepoIds().clear();
				entry.setCountStudents(entry.getCountDistinctStudents() * entry.getCountCourses());
			}
			if(hasOwnedGroups) {
				getGroupsStatisticsStatementForOwner(coach, map);
			}
			if(hasCoachedGroups) {
				getGroupsStatisticsStatementForCoach(coach, map);
			}
			
			for(Iterator<Map.Entry<Long, GroupStatEntry>> it=map.entrySet().iterator(); it.hasNext() ; ) {
				Map.Entry<Long, GroupStatEntry> entry = it.next();
				GroupStatEntry groupEntry = entry.getValue();
				if(groupEntry.getCountStudents() == 0) {
					it.remove();
				} else {
					groupEntry.setRepoIds(null);
					int attempted = groupEntry.getCountPassed() + groupEntry.getCountFailed();
					groupEntry.setCountNotAttempted(groupEntry.getCountStudents() - attempted);
					if(attempted > 0) {
						float averageScore = (float)groupEntry.getSumScore() / attempted;
						groupEntry.setAverageScore(averageScore);
					}
				}
			}
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getGroups(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append(" infos.group_id as grp_id, ")
		  .append(" infos.fk_group_id as bgrp_id, ")
		  .append(" infos.groupname as grp_name, ")
		  .append(" (select count(sg_participant.fk_identity_id) from o_bs_group_member sg_participant ")
		  .append("   where infos.fk_group_id = sg_participant.fk_group_id and sg_participant.g_role='participant' ")
		  .append(" ) as num_of_participant ")
		  .append(" from o_gp_business infos where infos.fk_group_id in ( select ")
		  .append("   distinct togroup.fk_group_id ")
		  .append("  from o_re_to_group togroup ")
		  .append("  inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append("  inner join o_repositoryentry sg_re on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule') ")
		  .append("  where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   sg_re.accesscode>=").append(RepositoryEntry.ACC_USERS)
		  .append("   or ")
		  .append("   (sg_re.accesscode=").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(" ) or infos.fk_group_id in ( select ")
		  .append("	  distinct togroup.fk_group_id ")
		  .append("  from o_re_to_group togroup ")
		  .append("  inner join o_repositoryentry sg_re on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule') ")
		  .append("  inner join o_re_to_group owngroup on (owngroup.r_defgroup=").appendTrue().append(" and owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role = 'owner') ")
		  .append("  where togroup.r_defgroup=").appendFalse().append(" and sg_owner.fk_identity_id=:coachKey and sg_re.accesscode>=").append(RepositoryEntry.ACC_OWNERS)
		  .append(" ) ");

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long groupKey = ((Number)rawStat[0]).longValue();
			Long baseGroupKey = ((Number)rawStat[1]).longValue();
			String title = (String)rawStat[2];
			GroupStatEntry entry = new GroupStatEntry(groupKey, title);
			entry.setCountDistinctStudents(((Number)rawStat[3]).intValue());
			map.put(baseGroupKey, entry);
		}
		return rawList.size() > 0;
	}
	
	private boolean getGroupsStatisticsInfosForCoach(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append("  togroup.fk_group_id as basegr_id, ")
		  .append("  togroup.fk_entry_id as re_id, ")
		  .append("  count(distinct pg_initial_launch.id) as pg_id ")
		  .append(" from o_repositoryentry sg_re  ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" left join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id) ")
		  .append(" where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_USERS).append(" and sg_coach.g_role = 'coach') ")//BAR
		  .append("   or ")
		  .append("   (sg_re.accesscode = ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(" group by togroup.fk_group_id, togroup.fk_entry_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null) {
				Long repoKey = ((Number)rawStats[1]).longValue();
				if(!entry.getRepoIds().contains(repoKey)) {
					int initalLaunch = ((Number)rawStats[2]).intValue();
					entry.setInitialLaunch(initalLaunch + entry.getInitialLaunch());
					entry.setCountCourses(entry.getCountCourses() + 1);
					entry.getRepoIds().add(repoKey);
				}
			}
		}
		return rawList.size() > 0;
	}
	
	private boolean getGroupsStatisticsInfosForOwner(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append("  togroup.fk_group_id as basegr_id, ")
		  .append("  togroup.fk_entry_id as re_id, ")
		  .append("  count(distinct pg_initial_launch.id) as pg_id ")
		  .append(" from o_repositoryentry sg_re  ")
		  .append(" inner join o_re_to_group owngroup on (owngroup.r_defgroup=").appendTrue().append(" and owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role = 'owner') ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" left join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id) ")
		  .append(" where sg_owner.fk_identity_id=:coachKey and sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS)
		  .append(" group by togroup.fk_group_id, togroup.fk_entry_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null) {
				Long repoKey = ((Number)rawStats[1]).longValue();
				if(!entry.getRepoIds().contains(repoKey)) {
					int initalLaunch = ((Number)rawStats[2]).intValue();
					entry.setInitialLaunch(initalLaunch + entry.getInitialLaunch());
					entry.setCountCourses(entry.getCountCourses() + 1);
					entry.getRepoIds().add(repoKey);
				}
			}
		}
		return rawList.size() > 0;
	}
	
	private boolean getGroupsStatisticsStatementForCoach(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append(" fin_statement.bgp_id,")
		  .append(" fin_statement.re_id,")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed,")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed,")
		  .append(" sum(fin_statement.score) as avg_score ")
		  .append("from ( select ")
		  .append("  distinct sg_statement.id as id,")
		  .append("  togroup.fk_group_id as bgp_id,")
		  .append("  togroup.fk_entry_id as re_id,")
		  .append("  sg_statement.passed as passed,")
		  .append("  sg_statement.score as score ")
		  .append(" from o_repositoryentry sg_re ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
		  .append(" where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_USERS).append(" and sg_coach.g_role = 'coach') ")//BAR
		  .append("   or ")
		  .append("   (sg_re.accesscode = ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(") ").appendAs().append(" fin_statement ")
		  .append("group by fin_statement.bgp_id, fin_statement.re_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null && !entry.getRepoIds().contains(repoKey)) {
				int passed = ((Number)rawStats[2]).intValue();
				int failed = ((Number)rawStats[3]).intValue();
				entry.setCountFailed(failed + entry.getCountFailed());
				entry.setCountPassed(passed + entry.getCountPassed());
				if(rawStats[4] != null) {
					entry.setSumScore(entry.getSumScore() + ((Number)rawStats[4]).floatValue());
				}
				entry.getRepoIds().add(repoKey);
			}
		}
		return rawList.size() > 0;
	}
	
	private boolean getGroupsStatisticsStatementForOwner(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append(" fin_statement.bgp_id,")
		  .append(" fin_statement.re_id,")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed,")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed,")
		  .append(" sum(fin_statement.score) as avg_score ")
		  .append("from ( select ")
		  .append("  distinct sg_statement.id as id,")
		  .append("  togroup.fk_group_id as bgp_id,")
		  .append("  togroup.fk_entry_id as re_id,")
		  .append("  sg_statement.passed as passed,")
		  .append("  sg_statement.score as score ")
		  .append(" from o_repositoryentry sg_re ")
		  .append(" inner join o_re_to_group owngroup on (owngroup.r_defgroup=").appendTrue().append(" and owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role = 'owner') ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
		  .append(" where sg_owner.fk_identity_id=:coachKey and sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS)
		  .append(") ").appendAs().append(" fin_statement ")
		  .append("group by fin_statement.bgp_id, fin_statement.re_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null && !entry.getRepoIds().contains(repoKey)) {
				int passed = ((Number)rawStats[2]).intValue();
				int failed = ((Number)rawStats[3]).intValue();
				entry.setCountFailed(failed + entry.getCountFailed());
				entry.setCountPassed(passed + entry.getCountPassed());
				if(rawStats[4] != null) {
					entry.setSumScore(entry.getSumScore() + ((Number)rawStats[4]).floatValue());
				}
				entry.getRepoIds().add(repoKey);
			}
		}
		return rawList.size() > 0;
	}
	
	protected List<CourseStatEntry> getCoursesStatisticsNative(Identity coach) {
		Map<Long,CourseStatEntry> map = new HashMap<>();		
		boolean hasCourses = getCourses(coach, map);
		if(hasCourses) {
			getCoursesStatisticsUserInfosForCoach(coach, map);
			getCoursesStatisticsUserInfosForOwner(coach, map);
			getCoursesStatisticsStatements(coach, map);
			for(Iterator<Map.Entry<Long,CourseStatEntry>> it=map.entrySet().iterator(); it.hasNext(); ) {
				CourseStatEntry entry = it.next().getValue();
				if(entry.getCountStudents() == 0) {
					it.remove();
				} else {
					int notAttempted = entry.getCountStudents() - entry.getCountPassed() - entry.getCountFailed();
					entry.setCountNotAttempted(notAttempted);
				}
			}
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getCourses(IdentityRef coach, Map<Long,CourseStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select v.key, v.displayname")
		  .append(" from repositoryentry v")
		  .append(" inner join v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as coach on coach.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.owner.name()).append("')")
		  .append(" where coach.identity.key=:coachKey and res.resName='CourseModule'")
		  .append(" and ((v.access=1 and v.membersOnly=true) ")
		  .append(" or (v.access >= ").append(RepositoryEntry.ACC_USERS).append(" and coach.role='").append(GroupRoles.coach.name()).append("')")
		  .append(" or (v.access >= ").append(RepositoryEntry.ACC_OWNERS).append(" and coach.role='").append(GroupRoles.owner.name()).append("'))");

		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		for(Object[] rawStat:rawList) {
			CourseStatEntry entry = new CourseStatEntry();
			entry.setRepoKey(((Number)rawStat[0]).longValue());
			entry.setRepoDisplayName((String)rawStat[1]);
			map.put(entry.getRepoKey(), entry);
		}
		return rawList.size() > 0;
	}
	
	private boolean getCoursesStatisticsUserInfosForCoach(Identity coach, Map<Long,CourseStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append("  sg_re.repositoryentry_id as re_id,")
		  .append("  count(distinct sg_participant.fk_identity_id) as student_id,")
		  .append("  count(distinct pg_initial_launch.id) as pg_id")
		  .append(" from o_repositoryentry sg_re ")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach')")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')")
		  .append(" left join o_as_user_course_infos pg_initial_launch")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)")
		  .append(" where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_USERS).append(" and sg_coach.g_role = 'coach') ")//BAR
		  .append("   or ")
		  .append("   (sg_re.accesscode = ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(" group by sg_re.repositoryentry_id");

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long repoKey = ((Number)rawStats[0]).longValue();
			CourseStatEntry entry = map.get(repoKey);
			if(entry != null) {
				entry.setCountStudents(((Number)rawStats[1]).intValue());
				entry.setInitialLaunch(((Number)rawStats[2]).intValue());
			}
		}
		return rawList.size() > 0;
	}
	
	private boolean getCoursesStatisticsUserInfosForOwner(Identity coach, Map<Long,CourseStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		if(dbInstance.isMySQL()) {
			sb.append("select")
			  .append("  sg_re.repositoryentry_id as re_id,")
			  .append("  count(distinct sg_participant.fk_identity_id) as student_id,")
			  .append("  count(distinct pg_initial_launch.id) as pg_id")
			  .append(" from o_repositoryentry sg_re ")
			  .append(" inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id and owngroup.r_defgroup=").appendTrue().append(")")
			  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=owngroup.fk_group_id and sg_coach.g_role = 'owner')")
			  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)")
			  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')")
			  .append(" left join o_as_user_course_infos pg_initial_launch")
			  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)")
			  .append(" where sg_coach.fk_identity_id=:coachKey and sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS)
			  .append(" group by sg_re.repositoryentry_id");
		} else {
			sb.append("select")
			  .append("  sg_re.repositoryentry_id as re_id,")
			  .append("  count(distinct sg_participant.fk_identity_id) as student_id,")
			  .append("  count(distinct pg_initial_launch.id) as pg_id")
			  .append(" from o_repositoryentry sg_re ")
			  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)")
			  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')")
			  .append(" left join o_as_user_course_infos pg_initial_launch")
			  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)")
			  .append(" where sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.fk_olatresource in (")
			  .append("  select sg_res.resource_id from o_olatresource sg_res where sg_res.resname = 'CourseModule'")
			  .append(" ) and exists (")
			  .append("  select owngroup.id from o_re_to_group owngroup inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id)")
			  .append("  where owngroup.fk_entry_id = sg_re.repositoryentry_id and owngroup.r_defgroup=").appendTrue().append(" and sg_owner.g_role='owner' and sg_owner.fk_identity_id=:coachKey")
			  .append(" )")
			  .append(" group by sg_re.repositoryentry_id");
		}

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long repoKey = ((Number)rawStats[0]).longValue();
			CourseStatEntry entry = map.get(repoKey);
			if(entry != null) {
				entry.setCountStudents(((Number)rawStats[1]).intValue());
				entry.setInitialLaunch(((Number)rawStats[2]).intValue());
			}
		}
		return rawList.size() > 0;
	}
	
	private boolean getCoursesStatisticsStatements(Identity coach, Map<Long,CourseStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append(" fin_statement.course_repo_key, ")
		  .append(" count(fin_statement.id), ")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed, ")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed, ")
		  .append(" avg(fin_statement.score) ")
		  .append("from o_as_eff_statement fin_statement ")
		  .append("where fin_statement.id in ( select ")
		  .append("  distinct sg_statement.id ")
		  .append("	from o_repositoryentry sg_re ")
		  .append("	inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role in ('owner','coach')) ")
		  .append("	inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
		  .append("	where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_USERS).append(" and sg_coach.g_role = 'coach') ")//BAR
		  .append("   or ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_coach.g_role = 'owner') ")//B
		  .append("   or ")
		  .append("   (sg_re.accesscode = ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(") or fin_statement.id in ( select ")
		  .append("   distinct sg_statement.id ")
		  .append(" from o_repositoryentry sg_re ")
		  .append(" inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id and owngroup.r_defgroup=").appendTrue().append(") ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=owngroup.fk_group_id and sg_coach.g_role = 'owner') ")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
		  .append(" where sg_coach.fk_identity_id=:coachKey and sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS).append(") ")
		  .append("group by fin_statement.course_repo_key ");

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long repoKey = ((Number)rawStats[0]).longValue();
			CourseStatEntry entry = map.get(repoKey);
			if(entry != null) {
				int passed = ((Number)rawStats[2]).intValue();
				int failed = ((Number)rawStats[3]).intValue();
				entry.setCountFailed(failed);
				entry.setCountPassed(passed);
				if(rawStats[4] != null) {
					entry.setAverageScore(((Number)rawStats[4]).floatValue());
				}
			}
		}
		return rawList.size() > 0;
	}
	
	protected List<StudentStatEntry> getStudentsStatisticsNative(Identity coach, List<UserPropertyHandler> userPropertyHandlers) {
		Map<Long, StudentStatEntry> map = new HashMap<>();
		//long start1 = System.nanoTime();
		boolean hasCoachedStudents = getStudentsStastisticInfosForCoach(coach, map, userPropertyHandlers);
		//CodeHelper.printNanoTime(start1, "Coached infos");
		//long start2 = System.nanoTime();
		boolean hasOwnedStudents = getStudentsStastisticInfosForOwner(coach, map, userPropertyHandlers);
		//CodeHelper.printNanoTime(start2, "Owned infos");
		if(hasOwnedStudents || hasCoachedStudents) {
			for(StudentStatEntry entry:map.values()) {
				entry.setCountRepo(entry.getRepoIds().size());
				entry.setRepoIds(null);
				entry.setInitialLaunch(entry.getLaunchIds().size());
				entry.setLaunchIds(null);
			}
			//long start3 = System.nanoTime();
			getStudentsStatisticStatement(coach, hasCoachedStudents, hasOwnedStudents, map);
			//CodeHelper.printNanoTime(start3, "Statistics students");
			for(StudentStatEntry entry:map.values()) {
				int notAttempted = entry.getCountRepo() - entry.getCountPassed() - entry.getCountFailed();
				entry.setCountNotAttempted(notAttempted);
			}
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getStudentsStastisticInfosForCoach(IdentityRef coach, Map<Long, StudentStatEntry> map, List<UserPropertyHandler> userPropertyHandlers) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append("  sg_participant_id.id as part_id,")
		  .append("  sg_participant_id.name as part_name,")
		  .append("  sg_participant_user.user_id as part_user_id,");
		writeUserProperties("sg_participant_user",  sb, userPropertyHandlers);
		sb.append("  ").appendToArray("sg_re.repositoryentry_id").append(" as re_ids,")
		  .append("  ").appendToArray("pg_initial_launch.id").append(" as pg_ids")
		  .append(" from o_repositoryentry sg_re")
		  .append(" inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule') ")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach')")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')")
		  .append(" inner join o_bs_identity sg_participant_id on (sg_participant_id.id=sg_participant.fk_identity_id)")
		  .append(" inner join o_user sg_participant_user on (sg_participant_user.fk_identity=sg_participant_id.id)")
		  .append(" left join o_as_user_course_infos pg_initial_launch")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)")
		  .append(" where sg_coach.fk_identity_id=:coachKey and ( ")
		  .append("   (sg_re.accesscode >= ").append(RepositoryEntry.ACC_USERS).append(" and sg_coach.g_role = 'coach') ")//BAR
		  .append("   or ")
		  .append("   (sg_re.accesscode = ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.membersonly=").appendTrue().append(")) ")
		  .append(" group by sg_participant_id.id, sg_participant_user.user_id");
		if(dbInstance.isOracle()) {
			sb.append(", sg_participant_id.name");
			writeUserPropertiesGroupBy("sg_participant_user", sb, userPropertyHandlers);
		}

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		int numOfProperties = userPropertyHandlers.size();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			int pos = 0;
			
			Long identityKey = ((Number)rawStat[pos++]).longValue();
			String identityName = (String)rawStat[pos++];
			((Number)rawStat[pos++]).longValue();//user key
			
			String[] userProperties = new String[numOfProperties];
			for(int i=0; i<numOfProperties; i++) {
				userProperties[i] = (String)rawStat[pos++];
			}
			
			StudentStatEntry entry = new StudentStatEntry(identityKey, identityName, userProperties);
			appendArrayToSet(rawStat[pos++], entry.getRepoIds());
			appendArrayToSet(rawStat[pos++], entry.getLaunchIds());
			map.put(entry.getIdentityKey(), entry);
		}
		
		return rawList.size() > 0;
	}
	
	private void writeUserProperties(String user, NativeQueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers) {
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(" ").append(user).append(".").append(handler.getDatabaseColumnName()).append(" as ")
			  .append("p_").append(handler.getDatabaseColumnName()).append(",");
		}	
	}
	
	private void writeUserPropertiesGroupBy(String user, NativeQueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers) {
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(", ").append(user).append(".").append(handler.getDatabaseColumnName());
		}	
	}
	
	private boolean getStudentsStastisticInfosForOwner(IdentityRef coach, Map<Long, StudentStatEntry> map, List<UserPropertyHandler> userPropertyHandlers) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append("  sg_participant_id.id as part_id,")
		  .append("  sg_participant_id.name as part_name,")
		  .append("  sg_participant_user.user_id as part_user_id,");
		writeUserProperties("sg_participant_user",  sb, userPropertyHandlers);
		sb.append("  ").appendToArray("sg_re.repositoryentry_id").append(" as re_ids,")
		  .append("  ").appendToArray("pg_initial_launch.id").append(" as pg_ids")
		  .append(" from o_repositoryentry sg_re")
		  .append(" inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id)")
		  .append(" inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id ")
		  .append("  and owngroup.r_defgroup=").appendTrue().append(" and sg_owner.g_role='owner' and sg_owner.fk_identity_id=:coachKey)")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')")
		  .append(" inner join o_bs_identity sg_participant_id on (sg_participant_id.id=sg_participant.fk_identity_id)")
		  .append(" inner join o_user sg_participant_user on (sg_participant_user.fk_identity=sg_participant_id.id)")
		  .append(" left join o_as_user_course_infos pg_initial_launch")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant_id.id)")
		  .append(" where sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS).append(" and sg_re.fk_olatresource in (")
		  .append("  select sg_res.resource_id from o_olatresource sg_res where sg_res.resname = 'CourseModule'")
		  .append(" )")
		  .append(" group by sg_participant_id.id, sg_participant_user.user_id");
		if(dbInstance.isOracle()) {
			sb.append(", sg_participant_id.name");
			writeUserPropertiesGroupBy("sg_participant_user", sb, userPropertyHandlers);
		}

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		int numOfProperties = userPropertyHandlers.size();
		Map<Long,StudentStatEntry> stats = new HashMap<>();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			
			int pos = 0;
			Long identityKey = ((Number)rawStat[pos++]).longValue();
			String identityName = (String)rawStat[pos++];
			((Number)rawStat[pos++]).longValue();//user key
			
			StudentStatEntry entry;
			if(map.containsKey(identityKey)) {
				entry = map.get(identityKey);
				pos += numOfProperties;
			} else {
				String[] userProperties = new String[numOfProperties];
				for(int i=0; i<numOfProperties; i++) {
					userProperties[i] = (String)rawStat[pos++];
				}
				entry = new StudentStatEntry(identityKey, identityName, userProperties);
				
				map.put(identityKey, entry);
			}
			appendArrayToSet(rawStat[pos++], entry.getRepoIds());
			appendArrayToSet(rawStat[pos++], entry.getLaunchIds());
			stats.put(entry.getIdentityKey(), entry);
		}
		return rawList.size() > 0;
	}
	
	/**
	 * Catch null value, strings and blob
	 * 
	 * @param rawObject
	 * @param ids
	 */
	private void appendArrayToSet(Object rawObject, Set<String> ids) {
		String rawString = null;
		if(rawObject instanceof String) {
			rawString = (String)rawObject;
		} else if(rawObject instanceof byte[]) {
			try {
				byte[] rawByteArr = (byte[])rawObject;
				rawString = new String(rawByteArr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
			}
		} else if (rawObject != null) {
			log.error("Unkown format: " + rawObject.getClass().getName() + " / " + rawObject);
		}
		
		if(StringHelper.containsNonWhitespace(rawString)) {
			for(String launchId:rawString.split(",")) {
				ids.add(launchId);
			}
		}
	}
	
	private boolean getStudentsStatisticStatement(IdentityRef coach, boolean hasCoached, boolean hasOwned, Map<Long,StudentStatEntry> stats) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append(" fin_statement.fk_identity, ")
		  .append("  count(fin_statement.id), ")
		  .append("  sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed, ")
		  .append("  sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed ")
		  .append(" from o_as_eff_statement fin_statement ")
		  .append(" where ");
		if(hasCoached) {
			sb.append(" fin_statement.id in ( select ")
			  .append("   distinct sg_statement.id as st_id ")
			  .append("  from o_repositoryentry sg_re ")
			  .append("  inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
			  .append("  inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id")
			  .append("   and sg_coach.fk_identity_id=:coachKey and sg_coach.g_role = 'coach') ")
			  .append("  inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
			  .append("  inner join o_as_eff_statement sg_statement ")
			  .append("    on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
			  .append("  where  ( ")
			  .append("    (sg_re.accesscode>2) ")
			  .append("    or ")
			  .append("    (sg_re.accesscode=1 and sg_re.membersonly=").appendTrue().append(")) ")
			  .append(" )");
		}
		if(hasOwned) {
			if(hasCoached) {
				sb.append(" or ");
			}
		
			sb.append("  fin_statement.id in ( select  ")
			  .append("    distinct sg_statement.id as st_id ")
			  .append("  from o_repositoryentry sg_re ")
			  .append("  inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id ) ")
			  .append("  inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id")
			  .append("    and sg_owner.g_role='owner' and sg_owner.fk_identity_id=:coachKey and owngroup.r_defgroup=").appendTrue().append(")")
			  .append("  inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
			  .append("  inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
			  .append("  inner join o_as_eff_statement sg_statement ")
			  .append("    on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
			  .append("  where sg_re.accesscode>=").append(RepositoryEntry.ACC_OWNERS).append(") ");
		  
		}
		sb.append(" group by fin_statement.fk_identity");

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long identityKey = ((Number)rawStat[0]).longValue();
			StudentStatEntry entry = stats.get(identityKey);
			if(entry != null) {
				int passed = ((Number)rawStat[2]).intValue();
				int failed = ((Number)rawStat[3]).intValue();
				entry.setCountPassed(passed);
				entry.setCountFailed(failed);
			}
		}
		return rawList.size() > 0;
	}
	
	/**
	 * Search all participants without restrictions on coach or owner relations.
	 * 
	 * @param params
	 * @return The list of statistics
	 */
	protected List<StudentStatEntry> getUsersStatisticsNative(SearchCoachedIdentityParams params, List<UserPropertyHandler> userPropertyHandlers) {
		Map<Long,StudentStatEntry> map = new HashMap<>();
		boolean hasUsers = getUsersStatisticsInfos(params, map, userPropertyHandlers);
		if(hasUsers) {
			getUsersStatisticsStatements(params, map);
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getUsersStatisticsInfos(SearchCoachedIdentityParams params, Map<Long, StudentStatEntry> map, List<UserPropertyHandler> userPropertyHandlers) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		Map<String,Object> queryParams = new HashMap<>();
		sb.append("select ")
		  .append("  sg_participant_id.id as part_id,")
		  .append("  sg_participant_id.name as part_name,")
		  .append("  sg_participant_user.user_id as part_user_id,");
		writeUserProperties("sg_participant_user",  sb, userPropertyHandlers);
		sb.append("  count(distinct sg_re.repositoryentry_id) as re_count, ")
		  .append("  count(distinct pg_initial_launch.id) as pg_id ")
		  .append("  from o_repositoryentry sg_re ")
		  .append(" inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule')")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_bs_identity id_participant on (sg_participant.fk_identity_id = id_participant.id) ")
		  .append(" inner join o_bs_identity sg_participant_id on (sg_participant_id.id=sg_participant.fk_identity_id)")
		  .append(" inner join o_user sg_participant_user on (sg_participant_user.fk_identity=sg_participant_id.id)")
		  .append(" left join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = id_participant.id) ")
		  .append(" inner join o_user user_participant on (user_participant.fk_identity=id_participant.id)")
		  .append(" where sg_re.accesscode >= ").append(RepositoryEntry.ACC_OWNERS).append(" ");
		appendUsersStatisticsSearchParams(params, queryParams, sb)
		  .append(" group by sg_participant_id.id, sg_participant_user.user_id");
		if(dbInstance.isOracle()) {
			sb.append(", sg_participant_id.name");
			writeUserPropertiesGroupBy("sg_participant_user", sb, userPropertyHandlers);
		}

		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sb.toString());
		for(Map.Entry<String, Object> entry:queryParams.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		List<?> rawList = query.getResultList();

		int numOfProperties = userPropertyHandlers.size();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			
			int pos = 0;
			Long identityKey = ((Number)rawStat[pos++]).longValue();
			String identityName = (String)rawStat[pos++];
			((Number)rawStat[pos++]).longValue();//user key
			
			String[] userProperties = new String[numOfProperties];
			for(int i=0; i<numOfProperties; i++) {
				userProperties[i] = (String)rawStat[pos++];
			}
			StudentStatEntry entry = new StudentStatEntry(identityKey, identityName, userProperties);
			entry.setCountRepo(((Number)rawStat[pos++]).intValue());
			entry.setInitialLaunch(((Number)rawStat[pos++]).intValue());
			map.put(identityKey, entry);
		}
		return rawList.size() > 0;
	}
	
	private boolean getUsersStatisticsStatements(SearchCoachedIdentityParams params, Map<Long,StudentStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		Map<String,Object> queryParams = new HashMap<>();
		sb.append("select ")
		  .append(" fin_statement.fk_identity, ")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed, ")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed ")
		  .append("from o_as_eff_statement fin_statement ")
		  .append("where fin_statement.id in ( select ")
		  .append("  distinct sg_statement.id as st_id ")
		  .append(" from o_repositoryentry sg_re ")
 		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource) ")
		  .append(" inner join o_bs_identity id_participant on (sg_participant.fk_identity_id = id_participant.id) ");
		appendUsersStatisticsJoins(params, sb)
		  .append(" where  sg_re.accesscode>0 ");
		appendUsersStatisticsSearchParams(params, queryParams, sb)
		  .append(") ")
		  .append("group by fin_statement.fk_identity ");
		
		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sb.toString());
		for(Map.Entry<String, Object> entry:queryParams.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		List<?> rawList = query.getResultList();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long userKey = ((Number)rawStat[0]).longValue();
			StudentStatEntry entry = map.get(userKey);
			if(entry != null) {
				int passed = ((Number)rawStat[1]).intValue();
				int failed = ((Number)rawStat[2]).intValue();
				entry.setCountPassed(passed);
				entry.setCountFailed(failed);
				int notAttempted = entry.getCountRepo() - passed - failed;
				entry.setCountNotAttempted(notAttempted);
			}
		}
		return rawList.size() > 0;
	}
	

	private NativeQueryBuilder appendUsersStatisticsJoins(SearchCoachedIdentityParams params, NativeQueryBuilder sb) {
		if(params != null && params.getUserProperties() != null && params.getUserProperties().size() > 0) {
			sb.append(" inner join o_user user_participant on (user_participant.fk_identity=id_participant.id)");
		}
		return sb;
	}
	
	private NativeQueryBuilder appendUsersStatisticsSearchParams(SearchCoachedIdentityParams params, Map<String,Object> queryParams, NativeQueryBuilder sb) {
		if(params == null) return sb;
		
		if(params.getIdentityKey() != null) {
			sb.append(" and id_participant.id=:identityKey");
			queryParams.put("identityKey", params.getIdentityKey());
		}
		
		if(StringHelper.containsNonWhitespace(params.getLogin())) {
			String login = PersistenceHelper.makeFuzzyQueryString(params.getLogin());
			if (login.contains("_") && dbInstance.isOracle()) {
				//oracle needs special ESCAPE sequence to search for escaped strings
				sb.append(" and lower(id_participant.name) like :login ESCAPE '\\'");
			} else if (dbInstance.isMySQL()) {
				sb.append(" and id_participant.name like :login");
			} else {
				sb.append(" and lower(id_participant.name) like :login");
			}
			queryParams.put("login", login);
		}
		
		if(params.getStatus() != null) {
			Integer status = params.getStatus();
			if (status.equals(Identity.STATUS_VISIBLE_LIMIT)) {
				sb.append(" and id_participant.status<").append(Identity.STATUS_VISIBLE_LIMIT);
			} else {
				sb.append(" and id_participant.status=:status");
				queryParams.put("status", params.getStatus());
			}
		}
		
		if(params.getUserProperties() != null && params.getUserProperties().size() > 0) {
			Map<String,String> searchParams = new HashMap<>(params.getUserProperties());
	
			int count = 0;
			for(Map.Entry<String, String> entry:searchParams.entrySet()) {
				String propName = entry.getKey();
				String propValue = entry.getValue();
				String qName = "p_" + ++count;
				
				UserPropertyHandler handler = userManager.getUserPropertiesConfig().getPropertyHandler(propName);
				if(dbInstance.isMySQL()) {
					sb.append(" and user_participant.").append(handler.getDatabaseColumnName()).append(" like :").append(qName);
				} else {
					sb.append(" and lower(user_participant.").append(handler.getDatabaseColumnName()).append(") like :").append(qName);
					if(dbInstance.isOracle()) {
						sb.append(" escape '\\'");
					}
				}
				queryParams.put(qName, PersistenceHelper.makeFuzzyQueryString(propValue));
			}
		}
		return sb;
	}
	
	public List<Identity> getStudents(Identity coach) {
		StringBuilder sc = new StringBuilder();
		sc.append("select participantIdent from repositoryentry as re")
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" inner join participant.identity as participantIdent")
		  .append(" inner join fetch participantIdent.user as participantUser")
          .append(" where owner.identity.key=:coachKey and re.key=:repoKey");

		List<Identity> identityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Identity.class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		//owner see all participants
		if(identityKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select participantIdent from repoentrytogroup as relGroup ")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as coach on coach.role = 'coach'")
			  .append(" inner join baseGroup.members as participant on participant.role='participant'")
			  .append(" inner join participant.identity as participantIdent")
			  .append(" inner join fetch participantIdent.user as participantUser")
	          .append(" where coach.identity.key=:coachKey and relGroup.entry.key=:repoKey");
	
			identityKeys = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("coachKey", coach.getKey())
					.getResultList();
		}
		return new ArrayList<>(new HashSet<>(identityKeys));
	}
	
	public List<Identity> getStudents(Identity coach, RepositoryEntry entry) {
		StringBuilder sc = new StringBuilder();
		sc.append("select participantIdent from repositoryentry as re")
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" inner join participant.identity as participantIdent")
		  .append(" inner join fetch participantIdent.user as participantUser")
          .append(" where owner.identity.key=:coachKey and re.key=:repoKey");

		List<Identity> identityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Identity.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("repoKey", entry.getKey())
				.getResultList();
		
		//owner see all participants
		if(identityKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select participantIdent from repoentrytogroup as relGroup ")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as coach on coach.role = 'coach'")
			  .append(" inner join baseGroup.members as participant on participant.role='participant'")
			  .append(" inner join participant.identity as participantIdent")
			  .append(" inner join fetch participantIdent.user as participantUser")
	          .append(" where coach.identity.key=:coachKey and relGroup.entry.key=:repoKey");
	
			identityKeys = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("coachKey", coach.getKey())
					.setParameter("repoKey", entry.getKey())
					.getResultList();
		}
		return new ArrayList<>(new HashSet<>(identityKeys));
	}

	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student) {
		StringBuilder sb = new StringBuilder();
		sb.append("select re from ").append(RepositoryEntry.class.getName()).append(" as re ")
		  .append(" inner join re.olatResource res on res.resName='CourseModule'")
		  .append(" inner join re.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as coach on coach.role='coach'")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where coach.identity.key=:coachKey and participant.identity.key=:studentKey")
		  .append(" and (re.access >= ").append(RepositoryEntry.ACC_USERS)
		  .append("  or (re.access = ").append(RepositoryEntry.ACC_OWNERS).append(" and re.membersOnly=true))");

		List<RepositoryEntry> coachedEntries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("studentKey", student.getKey())
				.getResultList();
		
		StringBuilder sc = new StringBuilder();
		sc.append("select re from ").append(RepositoryEntry.class.getName()).append(" as re ")
		  .append(" inner join re.olatResource res on res.resName='CourseModule'")
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true ")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where owner.identity.key=:coachKey and participant.identity.key=:studentKey")
		  .append(" and re.access >= ").append(RepositoryEntry.ACC_OWNERS);

		List<RepositoryEntry> ownedEntries = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), RepositoryEntry.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("studentKey", student.getKey())
				.getResultList();
		
		Set<RepositoryEntry> uniqueRes = new HashSet<>(coachedEntries);
		uniqueRes.addAll(ownedEntries);
		return new ArrayList<>(uniqueRes);
	}
	
	public List<RepositoryEntry> getUserCourses(IdentityRef student) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(v) from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where res.resName='CourseModule' and v.access >= ").append(RepositoryEntry.ACC_OWNERS).append(" and participant.identity.key=:studentKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("studentKey", student.getKey())
				.getResultList();
	}
}