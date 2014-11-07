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
	private static final String HASH_ALGORTHM = "SHA-1";
	private MessageDigest digest;
	private String hostname;
	private int hostPort;
	
	private static final boolean DEBUG = false;
	
	public SSLPinGenerator(String host, int port) throws Exception {
		digest = MessageDigest.getInstance(HASH_ALGORTHM);
		hostname=host;
		hostPort=port;
	}

	/**
	 * 
	 * @param args hostname (i.e android.com) and optionally port in form hostname:port or hostname port
	 */
	public static void main(String[] args) {
		if ((args.length == 1) || (args.length == 2)) {
			String[] hostAndPort = args[0].split(":");
			String host = hostAndPort[0];
			
			// if port blank assume 443
			int port = (hostAndPort.length == 1) ? 443 : Integer
					.parseInt(hostAndPort[1]);
			
			if ("help".equalsIgnoreCase(host)){
				printHelp();
				return;
			}
			
			try {
				SSLPinGenerator calc = new SSLPinGenerator(host, port);
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
	}

	private static void printHelp(){
		System.out.println("##SSL pins generator##");
	    System.out.println("The generated pins are base-64 SHA-1 hashes. Note: only run this on a trusted network.");
		System.out.println("\nUsage: java -jar generatePins.jar <host>[:port]");
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
	 */
	public class PublicKeyExtractingTrustManager implements X509TrustManager {

		public X509Certificate[] getAcceptedIssuers() {
			//do nothing this is just to extract pins
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			//do nothing this is just to extract pins
		}

		/**
		 * receives the list of SSL certifications for a given connection 
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			for (X509Certificate cert : chain) {
				//we use the public key as it is consistent trough certificate renewals
				byte[] pubKey = cert.getPublicKey().getEncoded();
				
				
				if (DEBUG){
					//printing the cert details can help you identify which pin belongs to which certificate in the chain
					Principal subject = cert.getSubjectDN();
					if (subject!=null) {
						System.out.println("subject :  " + subject.getName());
					}
				}
				final byte[] hash = digest.digest(pubKey);
				System.out.println("sha1/"+new BASE64Encoder().encode(hash));
			}
		}
	}

}
