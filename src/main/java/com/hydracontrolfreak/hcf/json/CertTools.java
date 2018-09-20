package com.hydracontrolfreak.hcf.json;

import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class CertTools {
	static final Logger logger = Logger.getLogger(CertTools.class);
	private FreakApi freak;
	
	public CertTools(FreakApi freak) {
		this.freak = freak;
	}

	public KeyStore initialiseKeyStore(HcfDeviceConfig hcfConfig) throws Exception {
		if (logger.isDebugEnabled()) logger.debug("initialiseKeyStore");
		char SEP = File.separatorChar;
		File keystoreFile = new File(freak.getHcfBase() + SEP + "cacerts" + SEP + "keystore.jks");
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		String password = hcfConfig.getSettingsConfig().getKeystorePassword();
		if (logger.isDebugEnabled()) logger.debug("keystorePassword: " + password);
		if (keystoreFile.canRead()) {
			if (logger.isDebugEnabled()) logger.debug("Keystore (" + keystoreFile + ") exists, reading");
			FileInputStream fileInputStream = new FileInputStream(keystoreFile);
			ks.load(fileInputStream, password.toCharArray());
			fileInputStream.close();
			if (logger.isDebugEnabled()) logger.debug("Returning and ks is: " + ks);
		} else {
			if (logger.isDebugEnabled()) logger.debug("Keystore doesn't exist, creating");
			ks.load(null, null);
		}
		
		return ks;
	}
	
	public KeyStore initialiseTrustStore(HcfDeviceConfig hcfConfig) throws Exception {
		if (logger.isDebugEnabled()) logger.debug("initialiseTrustStore");
		char SEP = File.separatorChar;
		File keystoreFile = new File(freak.getHcfBase() + SEP + "certs" + SEP + "truststore.jks");
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		String password = hcfConfig.getSettingsConfig().getTruststorePassword();
		if (keystoreFile.canRead()) {
			if (logger.isDebugEnabled()) logger.debug("Truststore (" + keystoreFile + ") exists, reading");
			FileInputStream fileInputStream = new FileInputStream(keystoreFile);
			ks.load(fileInputStream, password.toCharArray());
			fileInputStream.close();
			if (logger.isDebugEnabled()) logger.debug("Returning and ks is: " + ks);
		} else {
			if (logger.isDebugEnabled()) logger.debug("Keystore doesn't exist, creating");
			ks.load(null, null);
		}
		
		return ks;
	}
	
}
