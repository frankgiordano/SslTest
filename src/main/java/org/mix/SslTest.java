package org.mix;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SslTest {

    public static void main(String[] args) {

        final String filePath = "C:\\cert.p12";
        final String password = "PRAGUE12";

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

        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new FileInputStream(filePath), password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, password.toCharArray());

            // initialize SSLContext with client cert
            SSLContext sslContext = SSLContext.getInstance("TLS");  // TLSv1.3
            sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());

            // Configure Unirest with SSL context
            Unirest.config()
                    .sslContext(sslContext)
                    .verifySsl(true);

            // Make an Unirest GET request
            HttpResponse<String> response = Unirest.get("https://xxxx.xxxx.xxxx.net:1443/zosmf/restjobs/jobs")
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

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
