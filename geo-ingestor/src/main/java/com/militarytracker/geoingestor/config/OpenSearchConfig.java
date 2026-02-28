package com.militarytracker.geoingestor.config;

import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchConfig.class);

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    @Value("${opensearch.scheme}")
    private String scheme;

    @Value("${opensearch.ssl.truststore-path:}")
    private String truststorePath;

    @Value("${opensearch.ssl.truststore-password:changeit}")
    private String truststorePassword;

    @Bean
    public OpenSearchClient openSearchClient() throws Exception {
        OpenSearchTransport transport = buildTransport();
        return new OpenSearchClient(transport);
    }

    private OpenSearchTransport buildTransport() throws Exception {
        var httpHost = new org.apache.hc.core5.http.HttpHost(scheme, host, port);

        var builder = ApacheHttpClient5TransportBuilder.builder(httpHost)
                .setMapper(new JacksonJsonpMapper());

        if ("https".equalsIgnoreCase(scheme)) {
            SSLContext sslContext;
            if (truststorePath != null && !truststorePath.isEmpty()) {
                log.info("OpenSearch HTTPS enabled with truststore: {}", truststorePath);
                sslContext = createTruststoreSslContext();
            } else {
                log.info("OpenSearch HTTPS enabled with trust-all (development mode)");
                sslContext = createTrustAllSslContext();
            }

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                var tlsStrategy = org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
                        .create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier((hostname, session) -> true)
                        .build();
                var connManager = org.apache.hc.client5.http.impl.nio
                        .PoolingAsyncClientConnectionManagerBuilder
                        .create()
                        .setTlsStrategy(tlsStrategy)
                        .build();
                return httpClientBuilder.setConnectionManager(connManager);
            });
        }

        return builder.build();
    }

    private SSLContext createTruststoreSslContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            trustStore.load(fis, truststorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private SSLContext createTrustAllSslContext()
            throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }
}
