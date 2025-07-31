package org.unirest;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SslTest {

    public static void main(String[] args) throws Exception {

        // Load the PKCS12 keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("C:\\Users\\fg892105\\CA31.p12")) {
            keyStore.load(fis, "PRAGUE12".toCharArray());
        }

        // Init KeyManager with client certificate
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
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

        // Configure Unirest with SSL context
        Unirest.config()
                .sslContext(sc)
                .verifySsl(true);

        // Make an Unirest GET request
        HttpResponse<String> response = Unirest.get("https://usilCA31.lvn.broadcom.net:1443/zosmf/restjobs/jobs")
                .queryString("owner", "*")
                .queryString("jobid", "JOB17099")
                .header("X-CSRF-ZOSMF-HEADER", "true")
                .header("Content-Type", "application/json")
                .asString();

        // Print response details
        System.out.println("Response Code: " + response.getStatus());
        System.out.println("Status Message: " + response.getStatusText());
        System.out.println("Response Body: " + response.getBody());

        // Clean up Unirest
        Unirest.shutDown();
    }

}
