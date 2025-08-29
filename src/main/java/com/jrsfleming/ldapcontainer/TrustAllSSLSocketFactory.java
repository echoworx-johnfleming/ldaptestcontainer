package com.jrsfleming.ldapcontainer;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustAllSSLSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory sslSocketFactory;

    public TrustAllSSLSocketFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {

                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        this.sslSocketFactory = sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getDefault() {
        try {
            return new TrustAllSSLSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return sslSocketFactory.createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return sslSocketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return sslSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslSocketFactory.createSocket(address, port, localAddress, localPort);
    }
}