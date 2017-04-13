package com.scottyab.ssl.util;


import java.security.MessageDigest;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import sun.misc.BASE64Encoder;

/**
 * <p>This class currently generates SSL pins based on a certificate's Subject Public Key Info as
 * described on <a href="http://goo.gl/AIx3e5">Adam Langley's Weblog</a> (a.k.a Public Key pinning). Pins
 * are base-64 SHA-1 hashes, consistent with the format Chromium uses for <a
 * href="http://goo.gl/XDh6je">static certificates</a>. See Chromium's <a
 * href="http://goo.gl/4CCnGs">pinsets</a> for hostnames that are pinned in that
 * browser.
 * 
 * Designed to be compatible with okhttp 2.1+
 * 
 * @author Scott Alexander-Bown github.com/scottyab
 *
 */
public class SSLPinGenerator {

	private static final String DEFAULT_HASH_ALGORTHM = "SHA-1";
	private String hashAlgorthm = DEFAULT_HASH_ALGORTHM;
	private MessageDigest digest;
	private String hostname;
	private int hostPort;
	
	private boolean debugPrinting = false;
	
	public SSLPinGenerator(String host, int port, String hashAlg,  boolean argDebug) throws Exception {
		this.hashAlgorthm=hashAlg;
		this.hostname=host;
		this.hostPort=port;
		this.debugPrinting = argDebug;

		digest = MessageDigest.getInstance(hashAlg);

	}

	/**
	 * 
	 * @param args hostname (i.e android.com) and optionally port in form hostname:port, hash alg
	 */
	public static void main(String[] args) {
		try{
			if (args.length >= 1) {

				if ("help".equalsIgnoreCase(args[0])){
					printHelp();
					return;
				}

				String[] argHostAndPort = args[0].split(":");
				String argHost = argHostAndPort[0];

				// if port blank assume 443
				int argPort = (argHostAndPort.length == 1) ? 443 : Integer
						.parseInt(argHostAndPort[1]);

				String argAlg;
				if(args.length>=2) {
					argAlg = args[1];
				}else{
					argAlg = DEFAULT_HASH_ALGORTHM;
				}

				boolean argDebug = false;
				if(args.length>=3 && ("debug".equalsIgnoreCase(args[2]))) {
					argDebug = true;
				}

				try {
					SSLPinGenerator calc = new SSLPinGenerator(argHost, argPort, argAlg, argDebug);
					calc.fetchAndPrintPinHashs();
				} catch (Exception e) {
					printHelp();
					System.out.println("\nWhoops something went wrong: " +e.getMessage());
					e.printStackTrace();

				}
			} else {
				printHelp();
				return;
			}
		}catch (Exception e){
			System.out.println("CLI Error: " + e.getMessage());
			printHelp();
		}
	}

	private static void printHelp(){
		System.out.println("##SSL pin set generator##");
	    System.out.println("The generated pinset are base-64 encoded hashes (default SHA-1, but supports SHA-256). Note: only run this on a trusted network.");
		System.out.println("\nUsage: \"java -jar generatePins.jar <host>[:port] hashAlgorthm\" i.e scottyab.com:443 sha-256 ");
	}
	
	private void fetchAndPrintPinHashs() throws Exception {
		System.out.println("**Run this on a trusted network**\nGenerating SSL pins for: " + hostname);
		SSLContext context = SSLContext.getInstance("TLS");
		PublicKeyExtractingTrustManager tm = new PublicKeyExtractingTrustManager();
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();
		SSLSocket socket = (SSLSocket) factory.createSocket(hostname, hostPort);
		socket.setSoTimeout(10000);
		socket.startHandshake();
		socket.close();
	}

	/**
	 * Calculates and prints hash of each certificate in the chain
	 *
	 * PLEASE DO NOT COPY THIS TrustManager IMPLEMENTATION FOR USE IN REAL WORLD. This is just to print the pins.
	 *
	 */
	public class PublicKeyExtractingTrustManager implements X509TrustManager {

		private final BASE64Encoder base64Encoder;

		public PublicKeyExtractingTrustManager() {
			base64Encoder = new BASE64Encoder();
		}

		public X509Certificate[] getAcceptedIssuers() {
			//do nothing this is just to extract/print pins
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			//do nothing this is just to extract/print pins
		}

		/**
		 * receives the list of SSL certifications for a given connection 
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			for (X509Certificate cert : chain) {
				//we use the public key as it is consistent trough certificate renewals
				byte[] pubKey = cert.getPublicKey().getEncoded();
				
				if (debugPrinting){
					//printing the cert details can help you identify which pin belongs to which certificate in the chain
					Principal subject = cert.getSubjectDN();
					if (subject!=null) {
						System.out.println("Subject :  " + subject.getName());
					}
				}
				final byte[] hash = digest.digest(pubKey);
				String hashAlgorthmWithoutHyphen = removeHyphen(hashAlgorthm);
				System.out.println(String.format("%s/%s", hashAlgorthmWithoutHyphen,base64Encoder.encode(hash)));
			}
		}

		private String removeHyphen(String hashAlgorthm) {
			return hashAlgorthm.replace("-", "");
		}
	}

}
