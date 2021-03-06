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
package org.olat.upgrade;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.StartupException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Class to execute upgrade code during system startup. The idea is to
 * have a place in the code where you can manage your migration code that is
 * necessary because of new code or because of buggy old code that left some dead
 * stuff in the database or the fileSystem. 
 * 
 * 
 * <P>
 * Initial Date:  11.09.2008 <br>
 * @author guido
 */

public abstract class UpgradeManager extends BasicManager implements Initializable, GenericEventListener {

	private final Provider<DB> dbInstance;
	
	protected String INSTALLED_UPGRADES_XML = "installed_upgrades.xml";
	public static final String SYSTEM_DIR = "system";

	private DataSource dataSource;
	protected List<OLATUpgrade> upgrades;
	protected Map<String, UpgradeHistoryData> upgradesHistories;
	protected UpgradesDefinitions upgradesDefinitions;
	protected boolean needsUpgrade = true;

	@Autowired
	protected UpgradeManager(Provider<DB> dbInstance) {
		this.dbInstance = dbInstance;
	}


	public DataSource getDataSource() {
  	return dataSource;
  }
  
	/**
   * [used by spring]
   * @param dataSource
   */
  public void setDataSource (DataSource dataSource) {
  	this.dataSource = dataSource;
  }
  
	/**
	 * [used by spring]
	 * @param upgradesDefinitions
	 */
	public void setUpgradesDefinitions(UpgradesDefinitions upgradesDefinitions) {
		this.upgradesDefinitions = upgradesDefinitions;
	}

	/**
	 * Initialize the upgrade manager: get all upgrades from the configuration file and load
	 * the upgrade history from the olatdata directory
	 */
	public void init() {
		//register for framework starup event
		FrameworkStartupEventChannel.registerForStartupEvent(this);
		// load upgrades using spring framework 
		upgrades = upgradesDefinitions.getUpgrades();
		// load history of previous upgrades using xstream
		initUpgradesHistories();
		if (needsUpgrade) {
			boolean sevenOrNewer = false;
			for(OLATUpgrade upgrade:upgradesDefinitions.getUpgrades()) {
				if(upgrade.getVersion().startsWith("OLAT_7") || upgrade.getVersion().startsWith("OLAT_8")) {
					sevenOrNewer = true;
				}
			}
			if(!sevenOrNewer) {
				throw new AssertException("Upgrade first your installtion to OLAT 7.0 and after go with OpenOLAT");
			}
			
			doPreSystemInitUpgrades();
			
			//post system init task are triggered by an event
			dbInstance.get().commitAndCloseSession();
		}
	}

	/**
	 * Execute the pre system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public abstract void doPreSystemInitUpgrades();

	/**
	 * Execute the post system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public abstract void doPostSystemInitUpgrades();

	/**
	 * @param version identifier of UpgradeHistoryData
	 * @return UpgradeHistoryData of the given version or null if no such history object exists
	 */
	public UpgradeHistoryData getUpgradesHistory(String version) {
		return upgradesHistories.get(version);
	}
	
	/**
	 * Persists the UpgradeHistoryData on the file system
	 * @param upgradeHistoryData UpgradeHistoryData of the given version
	 * @param version identifier of UpgradeHistoryData
	 */
	public void setUpgradesHistory(UpgradeHistoryData upgradeHistoryData, String version) {
		this.upgradesHistories.put(version, upgradeHistoryData);
		File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
		upgradesDir.mkdirs(); // create if not exists
		File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
		XStreamHelper.writeObject(upgradesHistoriesFile, this.upgradesHistories);
	}

	/**
	 * Load all persisted UpgradeHistoryData objects from the fileSystem
	 */
	@SuppressWarnings("unchecked")
	protected void initUpgradesHistories() {
		File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
		File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
		if (upgradesHistoriesFile.exists()) {
			this.upgradesHistories = (Map<String, UpgradeHistoryData>) XStreamHelper.createXStreamInstance().fromXML(upgradesHistoriesFile);
		} else {
			if (this.upgradesHistories == null) {
				this.upgradesHistories = new HashMap<String, UpgradeHistoryData>();
			}
			needsUpgrade = false; //looks like a new install, no upgrade necessary
			logInfo("This looks like a new install or droped data, will not do any upgrades.");
			createUpgradeData();
		}
	}
	
	/**
	 * create fake upgrade data as this is a new installation
	 */
	protected void createUpgradeData() {
		for (OLATUpgrade upgrade: upgrades) {
			UpgradeHistoryData uhd = new UpgradeHistoryData();
			uhd.setInstallationComplete(true);
			uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
			setUpgradesHistory(uhd, upgrade.getVersion());
		}
	}

	/**
	 * On any RuntimeExceptions during init. Abort loading of application.
	 * Modules should throw RuntimeExceptions if they can't live with a the
	 * given state of configuration.
	 * @param e
	 */
	protected void abort(Throwable e) {
		if (e instanceof StartupException) {
			StartupException se = (StartupException) e;
			logWarn("Message: " + se.getLogMsg(), se);
			Throwable cause = se.getCause();
			logWarn("Cause: " + (cause != null ? cause.getMessage() : "n/a"), se);
		}
		throw new RuntimeException("*** CRITICAL ERROR IN UPGRADE MANAGER. Loading aborted.", e);
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent) {
			doPostSystemInitUpgrades();
		}
	}

}