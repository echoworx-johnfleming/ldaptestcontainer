package com.jrsfleming.ldapcontainer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.nio.file.Path;
import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class LdapContainerTest {

    private static final Logger log = LoggerFactory.getLogger(LdapContainerTest.class);
    @TempDir
    private Path tempFolder;


    @Test
    public void testGetAdminContext() throws NamingException {
        try (LdapContainer ldapContainer = new LdapContainer()) {
            ldapContainer.start();

            DirContext adminContext = ldapContainer.getAdminContext();
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = adminContext.search("dc=example,dc=org", "(objectClass=*)", searchControls);
            assertTrue(results.hasMore());

            adminContext.close();
        }
    }

    @Test
    public void testLdapConnection() throws Exception {
        LdapContainer ldapContainer = new LdapContainer();
        ldapContainer.start();
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapContainer.getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=example,dc=org");
        env.put(Context.SECURITY_CREDENTIALS, "adminpassword");

        DirContext ctx = new InitialDirContext(env);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search("dc=example,dc=org", "(objectClass=*)", searchControls);

        assertTrue(results.hasMore());

        ctx.close();
        ldapContainer.stop();
    }

    @Test
    public void testLdapAdminCredentials() throws NamingException {
        LdapContainer ldapContainer = new LdapContainer()
                .withAdminUser("cn=customAdmin,dc=example,dc=org")
                .withAdminPassword("customPassword");
        ldapContainer.start();

        assertEquals("cn=customAdmin,dc=example,dc=org", ldapContainer.getAdminUser());
        assertEquals("customPassword", ldapContainer.getAdminPassword());

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapContainer.getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapContainer.getAdminUserDn());
        env.put(Context.SECURITY_CREDENTIALS, ldapContainer.getAdminPassword());

        DirContext ctx = new InitialDirContext(env);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search("dc=example,dc=org", "(objectClass=*)", searchControls);

        assertTrue(results.hasMore());

        ctx.close();
        ldapContainer.stop();
    }

    @Test
    void ldapContainerCanUseTLS() throws Exception {
        Path cert = tempFolder.resolve("cert.crt");
        Path key = tempFolder.resolve("key.key");
        Path ca = tempFolder.resolve("ca.crt");
        CertificateGenerator.generateCertificate(cert, key, ca);
        LdapContainer ldapContainer = new LdapContainer().withTLS(cert, key, ca);
        ldapContainer.addEnv("BITNAMI_DEBUG", "true");
        ldapContainer.start();

        String ldapsUrl = ldapContainer.getLdapsUrl();

        assertThat(ldapsUrl, startsWith("ldaps://"));
        // Set up environment for creating the DirContext
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapsUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=example,dc=org");
        env.put(Context.SECURITY_CREDENTIALS, "adminpassword");
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", "com.jrsfleming.ldapcontainer.TrustAllSSLSocketFactory");

        // Create the DirContext using the custom SSL context
        DirContext ctx = new InitialDirContext(env);

        // Perform a search to verify the connection
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search("dc=example,dc=org", "(objectClass=*)", searchControls);

        assertTrue(results.hasMore());

        ctx.close();
        ldapContainer.stop();
    }
}
