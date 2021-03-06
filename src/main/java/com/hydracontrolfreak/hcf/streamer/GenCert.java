package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.*;
import com.hydracontrolfreak.hcf.json.*;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@WebServlet(urlPatterns={"/gencert"})
public class GenCert extends HttpServlet {
	private static final long serialVersionUID = 1820179491456040451L;
	private static final Logger logger = Logger.getLogger(GenCert.class);
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	private void byte2hex(byte b, StringBuffer buf) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
	}

	private String toHexString(byte[] block) {
		StringBuffer buf = new StringBuffer();
		int len = block.length;
		for (int i = 0; i < len; i++) {
			byte2hex(block[i], buf);
			if (i < len - 1) {
				buf.append(":");
			}
		}
		return buf.toString();
	}

	private String getCertFingerPrint(String mdAlg, Certificate cert) throws Exception {
		byte[] encCertInfo = cert.getEncoded();
		MessageDigest md = MessageDigest.getInstance(mdAlg);
		byte[] digest = md.digest(encCertInfo);
		return toHexString(digest);
	}

	private CertificateJSON installCert(CertificateExchangeJSON certificateExchangeJSON) throws Exception {
		KeyStore ks = hcfConfig.getCertificateConfig().getKeyStore();
		Security.addProvider(new BouncyCastleProvider());

		if (ks == null) {
			CertTools certTools = new CertTools(freak);
			ks = certTools.initialiseKeyStore(hcfConfig);
		}

		// Create private key
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
		keyPairGenerator.initialize(4096, new SecureRandom());
		KeyPair pair = keyPairGenerator.generateKeyPair();
		PrivateKey privKey = pair.getPrivate();

		// Calculate expiry date
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date(System.currentTimeMillis()));
		cal.add(GregorianCalendar.DAY_OF_YEAR, Integer.parseInt(certificateExchangeJSON.getValidity()));

		// Create subject for self-signed certificate
		String subject = "CN=" + certificateExchangeJSON.getCommonName() + ", C=" + certificateExchangeJSON.getCountry()
				+ ", O=" + certificateExchangeJSON.getOrganisation() + ", OU="
				+ certificateExchangeJSON.getOrganisationalUnit() + ", ST=" + certificateExchangeJSON.getState()
				+ ", L=" + certificateExchangeJSON.getLocality();

		// Initialize the certificate generator
		X509v1CertificateBuilder certGen = new JcaX509v1CertificateBuilder(
				new org.bouncycastle.asn1.x500.X500Name(subject), BigInteger.valueOf(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), cal.getTime(),
				new org.bouncycastle.asn1.x500.X500Name(subject), pair.getPublic());

		// Create a certificate holder
		X509CertificateHolder certHolder = certGen
				.build(new JcaContentSignerBuilder("SHA512withRSA").setProvider("BC").build(privKey));

		// Convert to certificate
		X509Certificate newCert = new JcaX509CertificateConverter().getCertificate(certHolder);

		// First item in the array is the self-signed certificate
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = newCert;

		try {
			ks.deleteEntry("hcf");
			if (logger.isDebugEnabled())
				logger.debug("Deleted existing truststore entry for hcf");
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Truststore entry for hcf not found");
		}

		char SEP = File.separatorChar;
		File keystoreFile = new File(freak.getHcfBase() + SEP + "cacerts" + SEP + "keystore.jks");

		String password = hcfConfig.getSettingsConfig().getKeystorePassword();
		ks.setKeyEntry("hcf", privKey, password.toCharArray(), chain);
		FileOutputStream outFile = new FileOutputStream(keystoreFile);
		ks.store(outFile, password.toCharArray());
		outFile.close();

		Certificate cert = ks.getCertificate("hcf");
		CertificateJSON certificateJSON = new CertificateJSON();

		if (cert != null && cert instanceof X509Certificate) {
			X509Certificate x509Certificate = (X509Certificate) cert;
			if (logger.isDebugEnabled())
				logger.debug("It's X509: " + x509Certificate.getNotAfter().toString());
			if (logger.isDebugEnabled())
				logger.debug("getIssuerDN().toString(): " + x509Certificate.getIssuerDN().toString());

			String ownerDnString = x509Certificate.getSubjectDN().toString();
			Dn owner = Dn.parseDn(ownerDnString);
			String issuerDnString = x509Certificate.getSubjectDN().toString();
			Dn issuer = Dn.parseDn(issuerDnString);

			certificateJSON.setIssuer(issuer);
			certificateJSON.setOwner(owner);

			certificateJSON.setValidFrom(x509Certificate.getNotBefore().toString());
			certificateJSON.setValidTo(x509Certificate.getNotAfter().toString());

			certificateJSON.setKeyAlg(x509Certificate.getPublicKey().getAlgorithm());
			if (x509Certificate.getPublicKey() instanceof RSAPublicKey) {
				RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();
				certificateJSON.setKeySize(rsaPublicKey.getModulus().bitLength());
			}

			certificateJSON.setSerial(x509Certificate.getSerialNumber().toString(16).toUpperCase()
					.replaceAll("((?!^.)..)(?=(?:..)*$)", ":$1").replaceAll("^(.(?:...)*)$", "0$1"));
			try {
				certificateJSON.setFingerprint("MD5: " + getCertFingerPrint("MD5", x509Certificate) + "\nSHA: "
						+ getCertFingerPrint("SHA", x509Certificate));
			} catch (Exception e1) {
				logger.error("Exception printing cert fingerprint");
				e1.printStackTrace();
			}

		}

		hcfConfig.getCertificateConfig().setKeyStore(ks);

		return certificateJSON;
	}

	@RequestMapping("/gencert")
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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
		CertificateExchangeJSON certificateExchangeJSON = fromGson.fromJson(body, CertificateExchangeJSON.class);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (logger.isDebugEnabled())
			logger.debug("actionsJSON has become: " + gson.toJson(certificateExchangeJSON));

		CertificatesJSON certificatesJSON = new CertificatesJSON();
		certificatesJSON.setResult(true);

		CertificateJSON certificateJSON = null;
		try {
			certificateJSON = installCert(certificateExchangeJSON);
			if (logger.isDebugEnabled())
				logger.debug("Successfully installed cert");
		} catch (Exception e) {
			logger.error("Can't install certificate: " + e.getMessage());
			e.printStackTrace();
			certificatesJSON.setResult(false);
			if (logger.isDebugEnabled())
				logger.debug("About to add the bad message");
			certificatesJSON.getMessages().add("Failed to install certificate");
		}

		CertificateChainJSON certificateChainJSON = new CertificateChainJSON();
		certificateChainJSON.getCertificateChain().add(certificateJSON);

		PrintWriter out = response.getWriter();

		out.print(gson.toJson(certificatesJSON));
	}
}
