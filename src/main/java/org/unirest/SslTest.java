package org.unirest;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SslTest {

    public static void main(String[] args) {

        try {
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
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());

            // Configure Unirest with SSL context
            Unirest.config()
                    .sslContext(sslContext);

            // Make an Unirest GET request
            HttpResponse<String> response = Unirest.get("https://xxxxx.xxxxx.xxxxx.net:1443/zosmf/restjobs/jobs")
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

