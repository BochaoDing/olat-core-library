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
package org.olat.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.StartupException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.inject.Provider;

/**
 * 
 * Description:<br>
 * Upgrade the database
 * 
 * <P>
 * Initial Date:  8 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DatabaseUpgradeManager extends UpgradeManagerImpl {

	private String dbVendor;
	private boolean autoUpgradeDatabase = true;
	
	protected UpgradesDefinitions olatUpgradesDefinitions;

	@Autowired
	public DatabaseUpgradeManager(Provider<DB> dbInstance) {
        super(dbInstance);
        INSTALLED_UPGRADES_XML = "installed_database_upgrades.xml";
	}
	
	
	public String getDbVendor() {
		return dbVendor;
	}
	
	/**
	 * [user by Spring]
	 * @param dbVendor
	 */
	public void setDbVendor(String dbVendor) {
		this.dbVendor = dbVendor;
	}
  
	/**
	 * [used by spring]
	 * @param autoUpgradeDatabase
	 */
	public void setAutoUpgradeDatabase(boolean autoUpgradeDatabase) {
		this.autoUpgradeDatabase = autoUpgradeDatabase;
	}
	
	/**
	 * [used by Spring]
	 * @param olatUpgradesDefinitions
	 */
	public void setOlatUpgradesDefinitions(UpgradesDefinitions olatUpgradesDefinitions) {
		this.olatUpgradesDefinitions = olatUpgradesDefinitions;
	}
	
	@Override
	public void init() {
		// load upgrades using spring framework 
		upgrades = upgradesDefinitions.getUpgrades();
		// load history of previous upgrades using xstream
		initUpgradesHistories();
		if (autoUpgradeDatabase) {
			runAlterDbStatements();
		} else {
			logInfo("Auto upgrade of the database is disabled. Make sure you do it manually by applying the " +
					"alter*.sql scripts and adding an entry to system/installed_upgrades.xml file.");
		}
	}

	public void runAlterDbStatements() {
		Dialect dialect;
		//only run upgrades on mysql or postgresql
		if (getDbVendor().contains("mysql")) dialect = Dialect.mysql;
		else if (getDbVendor().contains("postgresql")) dialect = Dialect.postgresql;
		else return;
			
		Statement statement = null;
		try {
			
			logAudit("+--------------------------------------------------------------+");
			logAudit("+... Pure database upgrade: starting alter DB statements ...+");
			logAudit("+ If it fails, do it manually by applying the content of the alter_X_to_Y.sql files.+");
			logAudit("+ For each file you upgraded to add an entry like this to the [pathToOlat]/olatdata/system/installed_database_upgrades.xml: +");
			logAudit("+ <entry><string>Database update</string><boolean>true</boolean></entry>+");
			logAudit("+--------------------------------------------------------------+");
			
			statement  = getDataSource().getConnection().createStatement();
			
			Iterator<OLATUpgrade> iter = upgrades.iterator();
			OLATUpgrade upgrade = null;
			while (iter.hasNext()) {
				upgrade = iter.next();
				String alterDbStatementsFilename = upgrade.getAlterDbStatements();
				if (alterDbStatementsFilename != null) {
					UpgradeHistoryData uhd = getUpgradesHistory(upgrade.getVersion());
					if (uhd == null) {
						// has never been called, initialize
						uhd = new UpgradeHistoryData();
					} 
						
					if (!uhd.getBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE)) {
						loadAndExecuteSqlStatements(statement, alterDbStatementsFilename, dialect);
						uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
						setUpgradesHistory(uhd, upgrade.getVersion());
						logAudit("Successfully executed alter DB statements for Version::" + upgrade.getVersion());
					}
				}
			}
			
		}	catch (SQLException e) {
			logError("Could not upgrade your database! Please do it manually and add ", e);
			throw new StartupException("Could not execute alter db statements. Please do it manually.", e);
			
		} catch (Throwable e) {
			logWarn("Error executing alter DB statements::", e);
			abort(e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2){
				logWarn("Could not close sql statement", e2);
				throw new StartupException("Could not close sql statements.", e2);
			}
		}
	}
	
	/**
	 * load file with alter statements and add to batch
	 * @param alterDbStatements
	 */
	private void loadAndExecuteSqlStatements(Statement statement, String alterDbStatements, Dialect dialect) {
		try {
			Resource setupDatabaseFile = new ClassPathResource("/database/"+dialect+"/"+alterDbStatements);
			if (!setupDatabaseFile.exists()) {
				throw new StartupException("The database upgrade file was not found on the classpath: "+"/database/"+dialect+"/"+alterDbStatements);
			}
			InputStream in = setupDatabaseFile.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			StringBuilder sb = new StringBuilder();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				if (strLine.length() > 1 && (!strLine.startsWith("--") && !strLine.startsWith("#"))) {
					sb.append(strLine.trim()).append(' ');
				}
			}
			
			StringTokenizer tokenizer = new StringTokenizer(sb.toString(), ";");
			String sql = null;
				while (tokenizer.hasMoreTokens()) {
					try {
						String token = tokenizer.nextToken();
						if(!StringHelper.containsNonWhitespace(token)) {
							continue;
						}
						
						sql = token + ";".toLowerCase();
						
						if (sql.startsWith("update") || sql.startsWith("delete") || sql.startsWith("alter") || sql.startsWith("insert")) {
							statement.executeUpdate(sql);
						} else {
							statement.execute(sql);
						}
						logInfo("Successfully upgraded database with the following sql: "+sql);
					} catch (SQLException e) {
						if(isErrorFatal(dialect, sql, e)) {
							throw new StartupException("Fatal error trying to update database.", e);
						}
					} catch (Exception e) {
						//handle non sql errors
						logError("Could not upgrade your database!",e);
						throw new StartupException("Could not add alter db statements to batch.", e);
					}
				}
			in.close();
		} catch (FileNotFoundException e1) {
			logError("could not find deleteDatabase.sql file!", e1);
			abort(e1);
		} catch (IOException e) {
			logError("could not read deleteDatabase.sql file!", e);
			abort(e);
		}
	}
	
	private boolean isErrorFatal(Dialect dialect, String sql, SQLException e) {
		if(dialect == Dialect.mysql) {
			return isMysqlErrorFatal(sql, e);
		} else if(dialect == Dialect.postgresql) {
			return isPostgresqlErrorFatal(sql, e);
		}
		return false;	
	}
	
	private boolean isMysqlErrorFatal(String sql, SQLException e) {
		logError("Error while trying to upgrade the database with:("+sql+"). We will continue with upgrading but check the errors manually! Error says:", e);
		return false;
	}
	
	private boolean isPostgresqlErrorFatal(String sql, SQLException e) {
		logError("Error while trying to upgrade the database with:("+sql+"). We will continue with upgrading but check the errors manually! Error says:", e);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected void initUpgradesHistories() {
		File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
		upgradesDir.mkdirs();

		File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
		File stdUpgradesHistoriesFile = new File(upgradesDir, "installed_upgrades.xml");
		
		boolean newInstance = !upgradesHistoriesFile.exists() && !stdUpgradesHistoriesFile.exists();
		if(newInstance) {
			upgradesHistories = new HashMap<String, UpgradeHistoryData>();
			// Fill the history
			for(OLATUpgrade upgrade:upgrades) {
				UpgradeHistoryData uhd = new UpgradeHistoryData();
				uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
				uhd.setInstallationComplete(true);
				setUpgradesHistory(uhd, upgrade.getVersion());
			}
			logInfo("This looks like a new install, will not do any database upgrades.");
		} else {
			if (upgradesHistoriesFile.exists()) {
				upgradesHistories = (Map<String, UpgradeHistoryData>) XStreamHelper.readObject(upgradesHistoriesFile);
			}
			if (upgradesHistories == null) {
				upgradesHistories = new HashMap<String, UpgradeHistoryData>();
			}
			
			if (stdUpgradesHistoriesFile.exists()) {
				Set<String> versions = new HashSet<String>();
				for(OLATUpgrade upgrade:upgradesDefinitions.getUpgrades()) {
					versions.add(upgrade.getVersion());
				}

				Map<String, UpgradeHistoryData> stdUpgradesHistories = (Map<String, UpgradeHistoryData>) XStreamHelper.readObject(stdUpgradesHistoriesFile);
				for(Map.Entry<String, UpgradeHistoryData> entry: stdUpgradesHistories.entrySet()) {
					String version = entry.getKey();
					UpgradeHistoryData data = entry.getValue();
					boolean updated = data.getBooleanDataValue("Database update");
					if(versions.contains(version) && updated && !upgradesHistories.containsKey(version)) {
						upgradesHistories.put(version, data);
					}
				}
			}
		}
	}

	@Override
	public void doPreSystemInitUpgrades() {
		//
	}

	@Override
	public void doPostSystemInitUpgrades() {
		//
	}
	
	private enum Dialect {
		mysql,
		postgresql
	}
}
