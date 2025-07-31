package org.javax;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class SslTest {

    public static void main(String[] args) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        // Load the PKCS12 keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("C:\\cert.p12")) {
            keyStore.load(fis, "PRAGUE12".toCharArray());
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Init KeyManager with client certificate
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "PRAGUE12".toCharArray());

        // Trust all server certs (like --insecure)
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        // Init SSLContext with client cert + trust-all policy
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());

        // Set default SSL socket factory
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Disable hostname verification (like --insecure)
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // Open connection
        URL url = new URL("https://xxxxx.xxxxx.xxxxx.net:1443/zosmf/restjobs/jobs?owner=*&jobid=JOB17099");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-CSRF-ZOSMF-HEADER", "true");
        con.setRequestProperty("Content-Type", "application/json");

        // Read response
        int responseCode = con.getResponseCode();
        String statusMessage = con.getResponseMessage();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Status Message: " + statusMessage);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);

            }
        }
        con.disconnect();
    }
}
