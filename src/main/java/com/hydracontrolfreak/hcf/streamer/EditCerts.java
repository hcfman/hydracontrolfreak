package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.CertTools;
import com.hydracontrolfreak.hcf.json.CertificateJSON;
import com.hydracontrolfreak.hcf.json.EditCertsJSON;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@RequestMapping("")
public class EditCerts extends HttpServlet {
	private static final long serialVersionUID = 1960213686411485822L;
	private static final Logger logger = Logger.getLogger(EditCerts.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	void initialiseKeyStore(KeyStore ks) throws Exception {
		char SEP = File.separatorChar;
		File truststoreFile = new File(freak.getHcfBase() + SEP + "certs" + SEP
				+ "truststore.jks");
		ks = KeyStore.getInstance(KeyStore.getDefaultType());
		String password = hcfConfig.getSettingsConfig().getTruststorePassword();
		if (truststoreFile.canRead()) {
			FileInputStream fileInputStream = new FileInputStream(
					truststoreFile);
			ks.load(fileInputStream, password.toCharArray());
			fileInputStream.close();
		} else {
			ks.load(null, null);
		}
	}

	// private void editCert(CertificateJSON[] certificateList) throws Exception
	// {
	// KeyStore ks = hcfConfig.getCertificateConfig().getKeyStore();
	//
	// if (ks == null) {
	// CertTools certTools = new CertTools();
	// ks = certTools.initialiseKeyStore(hcfConfig, ks);
	// }
	// if (logger.isDebugEnabled()) logger.debug("ks: " + ks);
	//
	// Set<String> keySet = new HashSet<String>();
	// for (CertificateJSON certificateJSON : certificateList)
	// keySet.add(certificateJSON.getAlias());
	//
	// for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
	// String alias = e.nextElement();
	// if (!ks.containsAlias(alias))
	// continue;
	//
	// if (!alias.equals("hcf") &&
	// !hcfConfig.getFreakConfig().getFreakMap().containsKey(alias) &&
	// !keySet.contains(alias))
	// try {
	// ks.deleteEntry(alias);
	// if (logger.isDebugEnabled())
	// logger.debug("Deleted existing truststore entry for hcf");
	// } catch (Exception e1) {
	// if (logger.isDebugEnabled())
	// logger.debug("Cannot delete truststore entry for alias: " + alias);
	// }
	// }
	//
	// char SEP = File.separatorChar;
	// File truststoreFile = new File(Common.HCF_BASE + SEP + "certs" + SEP +
	// "truststore.jks");
	//
	// String password = hcfConfig.getSettingsConfig().getTruststorePassword();
	// FileOutputStream outFile = new FileOutputStream(truststoreFile);
	// ks.store(outFile, password.toCharArray());
	// outFile.close();
	//
	// hcfConfig.getCertificateConfig().setKeyStore(ks);
	// }

	@RequestMapping("/editcerts")
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("application/json");

		hcfConfig = freak.getHcfConfig();

		response.setContentType("application/json");

		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		String body = stringBuilder.toString();

		if (logger.isDebugEnabled())
			logger.debug("Body: " + body);

		Gson fromGson = new Gson();
		EditCertsJSON editCertsJSON = fromGson.fromJson(body,
				EditCertsJSON.class);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (logger.isDebugEnabled())
			logger.debug("actionsJSON has become: "
					+ gson.toJson(editCertsJSON));

		PrintWriter out = response.getWriter();

		// Don't store if updating
		if (freak.getUpdating().get()) {
			editCertsJSON.setKeystore(null);
			editCertsJSON.setTruststore(null);
			editCertsJSON.getMessages().add(
					"Can't save certificates, an update is in progress");
			out.print(gson.toJson(editCertsJSON));

			return;
		}

		synchronized (freak) {
			try {

				freak.mountReadWrite();
				opLogger.info("Installing certificate");

				// editCert(certificateList);
				if (logger.isDebugEnabled())
					logger.debug("Successfully installed cert");
				KeyStore keyStore = hcfConfig.getCertificateConfig()
						.getKeyStore();

				if (logger.isDebugEnabled())
					logger.debug("keyStore: " + keyStore);
				if (keyStore == null) {
					CertTools certTools = new CertTools(freak);
					try {
						keyStore = certTools.initialiseKeyStore(hcfConfig);
						hcfConfig.getCertificateConfig().setKeyStore(keyStore);
					} catch (Exception e) {
						logger.error(
								"Exception initialising keystore: "
										+ e.getMessage(), e);

						editCertsJSON.setKeystore(null);
						editCertsJSON.setTruststore(null);
						editCertsJSON.getMessages().add(
								"Can't read the keystore");
						out.print(gson.toJson(editCertsJSON));
						return;
					}
				}

				KeyStore trustStore = hcfConfig.getCertificateConfig()
						.getTrustStore();

				if (logger.isDebugEnabled())
					logger.debug("trustStore: " + trustStore);
				if (trustStore == null) {
					CertTools certTools = new CertTools(freak);
					try {
						trustStore = certTools.initialiseTrustStore(hcfConfig);
						hcfConfig.getCertificateConfig().setTrustStore(
								trustStore);
					} catch (Exception e) {
						logger.error("Exception initialising keystore: "
								+ e.getMessage());
						e.printStackTrace();

						editCertsJSON.setKeystore(null);
						editCertsJSON.setTruststore(null);
						editCertsJSON.getMessages().add(
								"Can't read the truststore");
						out.print(gson.toJson(editCertsJSON));
						return;
					}
				}

				if (editCertsJSON.getKeystore() == null) {
					editCertsJSON.setKeystore(null);
					editCertsJSON.setTruststore(null);
					editCertsJSON.getMessages().add(
							"Was passed an invalid keystore list");
					out.print(gson.toJson(editCertsJSON));
					return;
				}

				if (editCertsJSON.getTruststore() == null) {
					editCertsJSON.setKeystore(null);
					editCertsJSON.setTruststore(null);
					editCertsJSON.getMessages().add(
							"was passed an invalid truststore list");
					out.print(gson.toJson(editCertsJSON));
					return;
				}

				Set<String> currentKeyStoreSet = new HashSet<String>();
				for (CertificateJSON certificateJSON : editCertsJSON
						.getKeystore())
					currentKeyStoreSet.add(certificateJSON.getAlias());

				Set<String> currentTrustStoreSet = new HashSet<String>();
				for (CertificateJSON certificateJSON : editCertsJSON
						.getTruststore())
					currentTrustStoreSet.add(certificateJSON.getAlias());

				for (Enumeration<String> e = keyStore.aliases(); e
						.hasMoreElements();) {
					String alias = e.nextElement();
					if (!keyStore.containsAlias(alias) || alias.equals("hcf"))
						continue;
					if (!currentKeyStoreSet.contains(alias)) {
						if (logger.isDebugEnabled())
							logger.debug("Removing " + alias
									+ " from the keystore");
						keyStore.deleteEntry(alias);
					}
				}

				for (Enumeration<String> e = trustStore.aliases(); e
						.hasMoreElements();) {
					String alias = e.nextElement();
					if (!trustStore.containsAlias(alias) || alias.equals("hcf"))
						continue;
					if (!currentTrustStoreSet.contains(alias)) {
						if (logger.isDebugEnabled())
							logger.debug("Removing " + alias
									+ " from the truststore");
						trustStore.deleteEntry(alias);
					}
				}

				char SEP = File.separatorChar;
				File keystoreFile = new File(freak.getHcfBase() + SEP
						+ "cacerts" + SEP + "keystore.jks");

				String keystorePassword = hcfConfig.getSettingsConfig()
						.getKeystorePassword();
				FileOutputStream keystoreStream = new FileOutputStream(
						keystoreFile);
				keyStore.store(keystoreStream, keystorePassword.toCharArray());
				keystoreStream.close();

				editCertsJSON.setResult(true);

				File truststoreFile = new File(freak.getHcfBase() + SEP
						+ "certs" + SEP + "truststore.jks");

				String truststorePassword = hcfConfig.getSettingsConfig()
						.getTruststorePassword();
				FileOutputStream truststoreStream = new FileOutputStream(
						truststoreFile);
				trustStore.store(truststoreStream,
						truststorePassword.toCharArray());
				truststoreStream.close();

				editCertsJSON.setResult(true);

			} catch (Exception e) {
				logger.error("Can't install certificate: " + e.getMessage());
				e.printStackTrace();
				if (logger.isDebugEnabled())
					logger.debug("About to add the bad message");
				editCertsJSON.getMessages()
						.add("Failed to install certificate");
				opLogger.error("Failed to install certificates: "
						+ e.getMessage());
			} finally {
				freak.mountReadonly();
			}
		}

		editCertsJSON.setKeystore(null);
		editCertsJSON.setTruststore(null);

		out.print(gson.toJson(editCertsJSON));
	}
}
