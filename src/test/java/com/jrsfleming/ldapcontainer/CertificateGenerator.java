package com.jrsfleming.ldapcontainer;

import org.testcontainers.shaded.org.bouncycastle.asn1.x500.X500Name;
import org.testcontainers.shaded.org.bouncycastle.cert.X509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testcontainers.shaded.org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.testcontainers.shaded.org.bouncycastle.operator.ContentSigner;
import org.testcontainers.shaded.org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }



    public static void generateCertificate(Path certFile, Path keyFile, Path caFile) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X509Certificate certificate = generateSelfSignedCertificate(keyPair);
        X509Certificate caCertificate = generateSelfSignedCertificate(keyPair);

        saveCertificate(certFile, certificate);
        savePrivateKey(keyFile, keyPair.getPrivate());
        saveCertificate(caFile, caCertificate);
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        X500Name owner = new X500Name("CN=Test, OU=Test, O=Test, L=Test, ST=Test, C=Test");
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        Date from = new Date();
        Date to = new Date(from.getTime() + 365 * 86400000L);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                owner, serialNumber, from, to, owner, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC").build(keyPair.getPrivate());

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certBuilder.build(signer));
    }

    private static void saveCertificate(Path fileName, X509Certificate certificate) throws IOException, CertificateException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(fileName.toFile()))) {
            pemWriter.writeObject(certificate);
        }
    }

    private static void savePrivateKey(Path fileName, PrivateKey privateKey) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(fileName.toFile()))) {
            pemWriter.writeObject(privateKey);
        }
    }
}