/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.statistic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL specific class which creates a temporary table with the
 * result of 'o_loggingtable where actionverb=launch and actionobject=node'.
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class MySQLTempStatTableCreator implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(MySQLTempStatTableCreator.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private JdbcTemplate jdbcTemplate_;
	
	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate_ = jdbcTemplate;
		DataSource dataSource = jdbcTemplate==null ? null : jdbcTemplate.getDataSource();
		Connection connection = null;
		try{
			if (dataSource!=null) {
				connection = dataSource.getConnection();
			}
		} catch(SQLException e) {
			log_.warn("setJdbcTemplate: SQLException while trying to get connection for logging", e);
		}
		log_.info("setJdbcTemplate: jdbcTemplate="+jdbcTemplate+", dataSource="+dataSource+", connection="+connection);
	}
	
	@Override
	public void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager) {
		// create temp table
		final long startTime = System.currentTimeMillis();
		try{
			log_.info("updateStatistic: dropping o_stat_temptable if still existing");
			jdbcTemplate_.execute("DROP TABLE IF EXISTS o_stat_temptable;");
			
			log_.info("updateStatistic: creating o_stat_temptable");
			final String createSql =
				"CREATE TABLE o_stat_temptable (" +
				" log_id bigint(20) not null," +
				" creationdate datetime not null," +
				" businesspath varchar(2048) not null," +
				" signature varchar(32) not null," +
				" resid bigint(20) not null," +
				" day datetime not null," +
				" week varchar(7) not null," +
				" dayofweek int(11) not null," +
				" hourofday int(11) not null," +
				" userproperty2 varchar(255)," +							// homeOrg
				" userproperty4 varchar(255)," +							// orgType
				" userproperty10 varchar(255)," +						// studyBranch3
				" userproperty3 varchar(255)" +							// studyLevel
				");";
			log_.info("updateStatistic: " + createSql); // TODO Remove log record after debugging
			jdbcTemplate_.execute(createSql);
			jdbcTemplate_.execute("ALTER TABLE o_stat_temptable ADD INDEX(signature, day);");
			jdbcTemplate_.execute("ALTER TABLE o_stat_temptable ADD INDEX(signature, week);");
			jdbcTemplate_.execute("ALTER TABLE o_stat_temptable ADD INDEX(signature, dayofweek);");
			jdbcTemplate_.execute("ALTER TABLE o_stat_temptable ADD INDEX(signature, hourofday);");

			log_.info("updateStatistic: inserting logging actions from " + from + " until " + until);
			
			// same month optimization
			String oLoggingTable = "o_loggingtable";
			Calendar lastUpdatedCalendar = Calendar.getInstance();
			lastUpdatedCalendar.setTime(from);
			Calendar nowUpdatedCalendar = Calendar.getInstance();
			nowUpdatedCalendar.setTime(until);
			
			if (lastUpdatedCalendar.get(Calendar.MONTH)==nowUpdatedCalendar.get(Calendar.MONTH)) {
				// that means we are in the same month, so use the current month's o_loggingtable
				// e.g. o_loggingtable_201002
				String monthStr = String.valueOf(lastUpdatedCalendar.get(Calendar.MONTH)+1);
				if (monthStr.length()==1) {
					monthStr = "0"+monthStr;
				}
				String sameMonthTable = "o_loggingtable_"+String.valueOf(lastUpdatedCalendar.get(Calendar.YEAR))+monthStr;
				List<Map<String,Object>> tables = jdbcTemplate_.queryForList("show tables like '"+sameMonthTable+"'");
				if (tables!=null && tables.size()==1) {
					log_.info("updateStatistic: using "+sameMonthTable+" instead of "+oLoggingTable);
					oLoggingTable = sameMonthTable;
				} else {
					log_.info("updateStatistic: using "+oLoggingTable+" ("+sameMonthTable+" didn't exist)");
				}
			} else {
				log_.info("updateStatistic: using "+oLoggingTable+" since from and to months are not the same");
			}

			// Check if we can skip some records in o_loggingtable scan
			final long lastLogId = jdbcTemplate_.queryForLong("SELECT MAX(last_log_id) FROM o_stat_lastupdated;");
			String whereClause;
			if (!fullRecalculation && lastLogId > 0) {
				whereClause = " WHERE log_id > " + String.valueOf(lastLogId) + " ";
			} else {
				whereClause = " WHERE creationdate > FROM_UNIXTIME('" + (from.getTime() / 1000) + "') ";
				whereClause = whereClause + " AND creationdate <= FROM_UNIXTIME('" + (until.getTime() / 1000) + "') ";
			}

			final String insertSql =
				"INSERT INTO o_stat_temptable (" +
				" log_id, creationdate, businesspath, " +
				" signature, " +
				" resid, " +
				" day, week, dayofweek, hourofday, " +
				" userproperty2, userproperty4, userproperty10, userproperty3" +
				") SELECT " +
				" log_id, creationdate, businesspath, " +
				" MD5(businesspath), " +
				" SUBSTR(businesspath, LOCATE(':',businesspath) + 1, LOCATE(']', businesspath) - LOCATE(':', businesspath) - 1), " +
				" DATE(creationdate), CONCAT(YEAR(creationdate), '-', LPAD(WEEK(creationdate,3), 2, '0')), DAYOFWEEK(creationdate), HOUR(creationdate), " +
				" userproperty2, userproperty4, userproperty10, userproperty3 " +
				" FROM " + oLoggingTable +
				whereClause +
				" AND actionverb = 'launch' AND actionobject = 'node';";

			log_.info("updateStatistic: insert query used for gathering stats: " + insertSql);
			jdbcTemplate_.execute(insertSql);
			long numLoggingActions = jdbcTemplate_.queryForLong("select count(*) from o_stat_temptable;");
			log_.info("updateStatistic: insert done. number of logging actions: " + numLoggingActions);
			final long lastCopiedLogId = jdbcTemplate_.queryForLong("SELECT MAX(log_id) from o_stat_temptable;");
			jdbcTemplate_.execute("INSERT INTO o_stat_lastupdated (lastupdated, last_log_id) VALUES (NOW(), " + lastCopiedLogId + ");");
			log_.info("updateStatistic: saved the last processed log_id: " + lastCopiedLogId);
		} catch(RuntimeException e) {
			log_.warn("updateStatistic: ran into a RuntimeException: " + e, e);
		} catch(Error er) {
			log_.warn("updateStatistic: ran into an Error: " + er, er);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("updateStatistic: END. duration=" + diff + " milliseconds");
		}
	}

}
