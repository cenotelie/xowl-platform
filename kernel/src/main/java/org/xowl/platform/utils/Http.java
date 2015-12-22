/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.utils;

import org.xowl.utils.logging.Logger;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Http Utilities
 *
 * @author Laurent Wouters
 */
public class Http {
    /**
     * The SSL context for HTTPS connections
     */
    private static final SSLContext sslContext;
    /**
     * The host name verifier for HTTPS connections
     */
    private static final HostnameVerifier hostnameVerifier;

    static {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException exception) {
            exception.printStackTrace();
        }
        sslContext = sc;
        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
    }

    /**
     * Sends an HTTP request
     *
     * @param logger      The logger to use
     * @param method      The HTTP method to use
     * @param uri         The target URI
     * @param contentType The MIME for the payload, if any
     * @param content     The payload, if any
     * @param authToken   The auth token, if any
     * @param accept      The requested content type for the response, if any
     * @return The response, or null if an error occured
     */
    public static HttpResponse request(Logger logger, String method, String uri, String contentType, String content, String authToken, String accept) {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException exception) {
            logger.error(exception);
            return null;
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException exception) {
            logger.error(exception);
            return null;
        }
        if (connection instanceof HttpsURLConnection) {
            // for SSL connections we should do this
            ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException exception) {
            logger.error(exception);
            return null;
        }
        if (contentType != null)
            connection.setRequestProperty("Content-Type", contentType);
        if (accept != null)
            connection.setRequestProperty("Accept", accept);
        if (authToken != null)
            connection.setRequestProperty("Authorization", "Basic " + authToken);
        connection.setUseCaches(false);
        if (content != null) {
            connection.setDoOutput(true);
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(content.getBytes());
            } catch (IOException exception) {
                logger.error(exception);
                return null;
            }
        }

        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException exception) {
            logger.error(exception);
            connection.disconnect();
            return null;
        }

        String resultType = connection.getContentType();
        byte[] result;
        //Get Response
        try (InputStream is = connection.getInputStream()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = is.read(buffer);
            while (read > 0) {
                output.write(buffer, 0, read);
                read = is.read(buffer);
            }
            result = output.toByteArray();
        } catch (IOException exception) {
            logger.error(exception);
            return null;
        }
        connection.disconnect();
        return new HttpResponse(responseCode, resultType, result);
    }
}
