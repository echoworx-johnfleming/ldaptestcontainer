package com.jrsfleming.ldapcontainer;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class LdapContainer extends GenericContainer<LdapContainer> {

    private static final int LDAP_PORT = 1389;
    private static final int LDAPS_PORT = 1636;
    private final String ldapRoot = "dc=example,dc=org";
    private String adminUser = "admin";
    private String adminPassword = "adminpassword";

    public LdapContainer() {
        super(DockerImageName.parse("bitnami/openldap:2.6.6"));
        addExposedPort(LDAP_PORT);
        addEnv("LDAP_ADMIN_USERNAME", adminUser);
        addEnv("LDAP_ADMIN_PASSWORD", adminPassword);
        addEnv("LDAP_ROOT", ldapRoot);

        setWaitStrategy(new LdapConnectionWaitStrategy());
    }

    private class LdapConnectionWaitStrategy extends AbstractWaitStrategy {
        @Override
        protected void waitUntilReady() {
            Unreliables.retryUntilSuccess((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
                Hashtable<String, String> env = new Hashtable<>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, LdapContainer.this.getLdapUrl());
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, "cn=" + adminUser + "," + ldapRoot);
                env.put(Context.SECURITY_CREDENTIALS, adminPassword);

                try {
                    new InitialDirContext(env);
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
        addEnv("LDAP_ADMIN_USERNAME", adminUser);
        return this;
    }

    public LdapContainer withAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
        addEnv("LDAP_ADMIN_PASSWORD", adminPassword);
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


    public String getLdapsUrl() {
        return String.format("ldaps://%s:%d", getHost(), getMappedPort(LDAPS_PORT));
    }

    public LdapContainer withTLS(Path cert, Path key, Path ca) {
        addEnv("LDAP_ENABLE_TLS", "yes");
        withFileSystemBind(cert.toAbsolutePath().toString(), "/opt/bitnami/openldap/certs/openldap.crt");
        addEnv("LDAP_TLS_CERT_FILE", "/opt/bitnami/openldap/certs/openldap.crt");
        withFileSystemBind(key.toAbsolutePath().toString(), "/opt/bitnami/openldap/certs/openldap.key");
        addEnv("LDAP_TLS_KEY_FILE", "/opt/bitnami/openldap/certs/openldap.key");
        withFileSystemBind(ca.toAbsolutePath().toString(), "/opt/bitnami/openldap/certs/openldapCA.crt");
        addEnv("LDAP_TLS_CA_FILE", "/opt/bitnami/openldap/certs/openldapCA.crt");
        addExposedPort(LDAPS_PORT);
        addEnv("LDAP_TLS_VERIFY_CLIENT", "never");
        return this;
    }
}