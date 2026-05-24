package com.jrsfleming.ldapcontainer;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class LdapContainer extends GenericContainer<LdapContainer> {

    private static final int LDAP_PORT = 3890;
    private static final int LDAPS_PORT = 6360;
    private final String ldapRoot = "dc=example,dc=org";
    private String adminUser = "admin";
    private String adminPassword = "adminpassword";

    public LdapContainer() {
        super(DockerImageName.parse("osixia/openldap:2.6.10-alpha"));
        addExposedPort(LDAP_PORT);
        updateEnvironment();
        setWaitStrategy(new LdapConnectionWaitStrategy());
    }

    private void updateEnvironment() {
        addEnv("OPENLDAP_BOOTSTRAP_DATA_ROOT_PASSWORD_HASHED", hashPassword(adminPassword));
        addEnv("OPENLDAP_BOOTSTRAP_SUFFIX", ldapRoot);
        addEnv("OPENLDAP_BOOTSTRAP_DATA_ROOT_DN", getAdminUserDn());
        // Ensure SHA-2 module is loaded for SSHA512 support
        addEnv("OPENLDAP_BOOTSTRAP_MODULES", "back_mdb.so argon2.so ppolicy.so unique.so refint.so memberof.so syncprov.so pw-sha2.so");
    }

    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            digest.update(salt);
            byte[] hash = digest.digest();

            byte[] hashWithSalt = new byte[hash.length + salt.length];
            System.arraycopy(hash, 0, hashWithSalt, 0, hash.length);
            System.arraycopy(salt, 0, hashWithSalt, hash.length, salt.length);

            return "{SSHA512}" + Base64.getEncoder().encodeToString(hashWithSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }

    private class LdapConnectionWaitStrategy extends AbstractWaitStrategy {
        @Override
        protected void waitUntilReady() {
            Unreliables.retryUntilSuccess((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
                try {
                    getAdminContext().close();
                    return Void.TYPE;
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public String getLdapUrl() {
        return String.format("ldap://%s:%d", getHost(), getMappedPort(LDAP_PORT));
    }

    public LdapContainer withAdminUser(String adminUser) {
        this.adminUser = adminUser;
        addEnv("OPENLDAP_BOOTSTRAP_DATA_ROOT_DN", getAdminUserDn());
        return this;
    }

    public LdapContainer withAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
        addEnv("OPENLDAP_BOOTSTRAP_DATA_ROOT_PASSWORD_HASHED", hashPassword(adminPassword));
        return this;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminUserDn () {
        return "cn=" + adminUser + "," + ldapRoot;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public InitialDirContext getAdminContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, getAdminUserDn());
        env.put(Context.SECURITY_CREDENTIALS, getAdminPassword());
        return new InitialDirContext(env);
    }


    public String getLdapsUrl() {
        return String.format("ldaps://%s:%d", getHost(), getMappedPort(LDAPS_PORT));
    }

    public LdapContainer withTLS(Path cert, Path key, Path ca) {
        addEnv("OPENLDAP_BOOTSTRAP_TLS", "true");
        withFileSystemBind(cert.toAbsolutePath().toString(), "/container/services/openldap/assets/certs/openldap.crt");
        addEnv("OPENLDAP_BOOTSTRAP_TLS_CERT", "/container/services/openldap/assets/certs/openldap.crt");
        withFileSystemBind(key.toAbsolutePath().toString(), "/container/services/openldap/assets/certs/openldap.key");
        addEnv("OPENLDAP_BOOTSTRAP_TLS_CERT_KEY", "/container/services/openldap/assets/certs/openldap.key");
        withFileSystemBind(ca.toAbsolutePath().toString(), "/container/services/openldap/assets/certs/openldapCA.crt");
        addEnv("OPENLDAP_BOOTSTRAP_TLS_CA_CERT", "/container/services/openldap/assets/certs/openldapCA.crt");
        addExposedPort(LDAPS_PORT);
        addEnv("OPENLDAP_BOOTSTRAP_TLS_VERIFY_CLIENT", "never");
        return this;
    }
}
