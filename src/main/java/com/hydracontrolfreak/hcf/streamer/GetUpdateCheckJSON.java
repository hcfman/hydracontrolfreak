package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.UpdateCheckJSON;

public class GetUpdateCheckJSON {
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	public GetUpdateCheckJSON(FreakApi freak, HcfDeviceConfig hcfConfig) {
		this.freak = freak;
		this.hcfConfig = hcfConfig;
	}

	public UpdateCheckJSON getUpdateCheckResult() throws Exception {
		String version = hcfConfig.getSettingsConfig().getVersion();
		ScriptRunner scriptRunner = new ScriptRunner();

		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", System.getenv("HOME")
						+ "/update/bin/updateavailable.sh",
				"updateavailable.sh", hcfConfig.getSettingsConfig()
						.isForceUpdate() ? "1.0" : hcfConfig
						.getSettingsConfig().getVersion());

		if (scriptRunnerResult.getResult() != 0)
			throw new Exception("Can't retrieve network settings");

		Gson fromGson = new Gson();
		UpdateCheckJSON updateCheckJSON = fromGson.fromJson(
				scriptRunnerResult.getOutput(), UpdateCheckJSON.class);

		return updateCheckJSON;
	}
}
