package com.hydracontrolfreak.hcf.system;

import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

public class Common {
	public static final Logger logger = Logger.getLogger(Common.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private static final String HCF_BASE = System.getenv("HCF_HOME");
	private static volatile HcfDeviceConfig hcfConfig;

	static {
		if (logger.isDebugEnabled())
			logger.debug("In static initialization of Common");
		if (logger.isDebugEnabled())
			logger.debug("HCF_BASE = " + HCF_BASE);
	}

	private Common() {
		throw new RuntimeException("This class may not be instantiated");
	}

/*	public static HcfDeviceConfig getHcfConfig() {
		if (hcfConfig == null) {
			if (logger.isDebugEnabled())
				logger.debug("No variable, initializing");
			reload();
		}
		if (logger.isDebugEnabled())
			logger.debug("In getHcfConfig");
		return hcfConfig;
	}

	public static void reload() {
		if (logger.isDebugEnabled())
			logger.debug("Reloading context");
		if (HCF_BASE == null)
			throw new RuntimeException(
					"Structure corrupt, HCF_BASE not defined");
		try {
			hcfConfig = new HcfDeviceConfig(HCF_BASE + "/conf/hcf.xml");
		} catch (Exception e) {
			logger.error("Cannot initialize from config file");
			e.printStackTrace();
		}
	}

	public static boolean save() {
		if (logger.isDebugEnabled())
			logger.debug("Saving config");

		if (hcfConfig == null) {
			logger.error("There is no config to save");
			return false;
		}

		if (HCF_BASE == null)
			throw new RuntimeException(
					"Structure corrupt, HCF_BASE not defined");

		File oldConfig = new File(HCF_BASE + "/conf/oldhcf.xml");
		oldConfig.delete();
		try {
			for (Action action : hcfConfig.getActionConfig().getActionList()) {
				if (action.getProfiles() != null) {
					if (logger.isDebugEnabled())
						logger.debug(action.getName());
					for (String s : action.getProfiles())
						if (logger.isDebugEnabled())
							logger.debug("profile: " + s);
				}
			}
			hcfConfig.outputXML(HCF_BASE + "/conf/newhcf.xml");
			File currentConfig = new File(HCF_BASE + "/conf/hcf.xml");
			File newConfig = new File(HCF_BASE + "/conf/newhcf.xml");
			if (currentConfig.renameTo(oldConfig))
				newConfig.renameTo(currentConfig);
			else
				logger.error("Could not move current config to old and thus could not save config");
		} catch (IOException e) {
			logger.error("Can't save config: " + e.getMessage(), e);
			opLogger.error("Can't save config: " + e.getMessage());
		} catch (TransformerException e) {
			logger.error("Can't save config: " + e.getMessage(), e);
			opLogger.error("Can't save config: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error("Can't save config: " + e.getMessage(), e);
			opLogger.error("Can't save config: " + e.getMessage());
		}

		ScriptRunner scriptRunner = new ScriptRunner();
		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				Common.HCF_BASE + "/bin/suwrapper", Common.HCF_BASE
						+ "/bin/sync.sh", "sync.sh");

		if (scriptRunnerResult.getResult() != 0) {
			logger.error("Error syncing the filesystem");
		}

		return true;
	}
*/
}
