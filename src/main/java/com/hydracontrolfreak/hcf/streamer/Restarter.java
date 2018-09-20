package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Restarter {
	private static final Logger opLogger = Logger.getLogger("operations");
	private FreakApi freak;
	
	public Restarter(FreakApi freak) {
		this.freak = freak;
	}

	public static void restart(FreakApi freak) throws IOException {
		FileAppender fileAppender = (FileAppender)opLogger.getAppender("operationsAppender");
/*
		fileAppender.setImmediateFlush(true);
*/
		opLogger.info("Restarting HCF system");
/*
		fileAppender.setImmediateFlush(false);
*/

		Runtime.getRuntime().exec(freak.getHcfBase() + "/bin/restart.sh");
	}

	public static void reboot(FreakApi freak) throws IOException {
		FileAppender fileAppender = (FileAppender)opLogger.getAppender("operationsAppender");
/*
		fileAppender.setImmediateFlush(true);
*/
		opLogger.info("Rebooting HCF system");
/*
		fileAppender.setImmediateFlush(false);
*/

		ScriptRunner scriptRunner = new ScriptRunner();
		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/reboot.sh", "reboot.sh");
	}

	public static void shutdown(FreakApi freak) throws IOException {
		FileAppender fileAppender = (FileAppender)opLogger.getAppender("operationsAppender");
/*
		fileAppender.setImmediateFlush(true);
*/
		opLogger.info("Shutting down HCF system");
/*
		fileAppender.setImmediateFlush(false);
*/

		ScriptRunner scriptRunner = new ScriptRunner();
		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/shutdown.sh", "shutdown.sh");
	}

}
