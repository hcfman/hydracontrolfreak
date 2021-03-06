package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.CertTools;
import com.hydracontrolfreak.hcf.json.CsrJSON;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.X509Certificate;

public class ExportCert {
	private static final Logger logger = Logger.getLogger(ExportCert.class);
	private FreakApi freak;

	public ExportCert(FreakApi freak) {
		this.freak = freak;
	}

	public CsrJSON export(HcfDeviceConfig hcfConfig, String alias, boolean isKeyStore) {
		if (logger.isDebugEnabled())
			logger.debug("Alias: " + alias);
		CsrJSON exportJSON = new CsrJSON();

		if (logger.isDebugEnabled())
			logger.debug("Get keystore");
		KeyStore keyStore;
		Security.addProvider(new BouncyCastleProvider());
		if (isKeyStore) {
			if (logger.isDebugEnabled())
				logger.debug("Fetching from the keyStore");
			keyStore = hcfConfig.getCertificateConfig().getKeyStore();

			if (keyStore == null) {
				CertTools certTools = new CertTools(freak);
				try {
					keyStore = certTools.initialiseKeyStore(hcfConfig);
					hcfConfig.getCertificateConfig().setKeyStore(keyStore);
				} catch (Exception e) {
					logger.error("Exception initialising keystore: " + e.getMessage());
					e.printStackTrace();

					exportJSON.getMessages().add("Can't read the keystore");
					return exportJSON;
				}
			}

		} else {
			if (logger.isDebugEnabled())
				logger.debug("Fetching from the truststore");
			keyStore = hcfConfig.getCertificateConfig().getTrustStore();

			if (keyStore == null) {
				CertTools certTools = new CertTools(freak);
				try {
					keyStore = certTools.initialiseTrustStore(hcfConfig);
					hcfConfig.getCertificateConfig().setTrustStore(keyStore);
				} catch (Exception e) {
					logger.error("Exception initialising truststore: " + e.getMessage());
					e.printStackTrace();

					exportJSON.getMessages().add("Can't read the truststore");
					return exportJSON;
				}
			}

		}

		X509Certificate cert = null;
		if (logger.isDebugEnabled())
			logger.debug("alias: " + alias);
		try {
			cert = (X509Certificate) keyStore.getCertificate(alias);
			if (logger.isDebugEnabled())
				logger.debug("cert: " + cert);
			StringWriter pemStrWriter = new StringWriter();
			JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(pemStrWriter);
			jcaPEMWriter.writeObject(cert);
			jcaPEMWriter.close();

			exportJSON.setCsr(pemStrWriter.toString());
		} catch (KeyStoreException | IOException e) {
			e.printStackTrace();
			logger.error("Certificate encoding exception: " + e.getMessage());
			exportJSON.getMessages().add("Problem with the certificate, try re-generating it");
			return exportJSON;
		}

		exportJSON.setResult(true);

		return exportJSON;
	}
}
