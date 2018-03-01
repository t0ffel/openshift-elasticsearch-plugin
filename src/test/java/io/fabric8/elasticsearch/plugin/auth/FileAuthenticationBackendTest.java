/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.elasticsearch.plugin.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestRequest;
import org.junit.Before;
import org.junit.Test;

import com.floragunn.searchguard.action.configupdate.TransportConfigUpdateAction;
import com.floragunn.searchguard.support.ConfigConstants;
import com.floragunn.searchguard.user.AuthCredentials;
import com.floragunn.searchguard.user.User;

import io.fabric8.elasticsearch.plugin.OpenShiftElasticSearchConfigurationException;
import io.fabric8.elasticsearch.plugin.Samples;

public class FileAuthenticationBackendTest {

    private static final String AUTHORIZATION = "Authorization";
    private FileAuthenticationBackend auth;
    private User user;
    private Settings settings;
    private File tmp;
    private TransportConfigUpdateAction tcua = mock(TransportConfigUpdateAction.class);
    
    @Before
    public void setup() throws IOException {
        tmp = File.createTempFile("passwd", Integer.toString(new Random().nextInt()), new File(System.getProperty("java.io.tmpdir")));
        FileUtils.writeStringToFile(tmp, Samples.PASSWORDS.getContent());
        givenFilePath(tmp.getAbsolutePath());
        givenAuthenticationBackend();
    }
    
    private void givenFilePath(String path) {
        settings = Settings.builder().put(FileAuthenticationBackend.FILE, path).build();
    }
    
    private void givenAuthenticationBackend() {
        auth = new FileAuthenticationBackend(settings, tcua);
    }
    
    private User whenAuthenticating(String username, String passwd) {
        AuthCredentials credentials = new AuthCredentials(username, passwd.getBytes());
        return auth.authenticate(credentials );
    }

    private ThreadContext newThreadContext(String sslPrincipal) {
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        threadContext.putTransient(ConfigConstants.SG_SSL_PRINCIPAL, sslPrincipal);
        return threadContext;
    }
    
    @Test(expected = OpenShiftElasticSearchConfigurationException.class)
    public void testInitializationWithEmptyFilePath() {
        givenFilePath("");
        givenAuthenticationBackend();
    }
    
    public void testReRequestAuthenticationAlwaysReturnsFalse() {
        assertFalse(auth.reRequestAuthentication(null, null));
    }
    
    @Test
    public void testExtractCredentialsWithNoAuthHeader() {
        RestRequest request = mock(RestRequest.class);
        request.getHeaders();
        when(request.header(AUTHORIZATION)).thenReturn(null);
        assertNull(auth.extractCredentials(request,newThreadContext("cn=abc,l=ert,st=zui,c=qwe")));
    }

    @Test
    public void testExtractCredentialsWithAuthHeaderNotBasic() {
        RestRequest request = mock(RestRequest.class);
        when(request.header(eq(AUTHORIZATION))).thenReturn("foo bar");
        assertNull(auth.extractCredentials(request, newThreadContext("cn=abc,l=ert,st=zui,c=qwe")));
    }
    
    @Test
    public void testExtractCredentialsWithAuthHeaderNoPassword() {
        RestRequest request = mock(RestRequest.class);
        when(request.header(eq(AUTHORIZATION))).thenReturn("basic Zm9vOgo="); //basic foo:
        assertNull("Exp a password to be required to auth", auth.extractCredentials(request, newThreadContext("cn=abc,l=ert")));
    }
    
    @Test
    public void testExtractCredentialsWithAuthHeaderAndPassword() {
        RestRequest request = mock(RestRequest.class);
        when(request.header(eq(AUTHORIZATION))).thenReturn("Basic Zm9vOmJhcgo="); //Basic foo:bar
        AuthCredentials exp = new AuthCredentials("foo","bar".getBytes());
        AuthCredentials act = auth.extractCredentials(request, newThreadContext("cn=abc,l=ert"));
        assertEquals("Exp. the AuthCredentials to match", exp, act);
    }
    
    @Test(expected = OpenShiftElasticSearchConfigurationException.class)
    public void testInitializationWithNonExistentFilePath() {
        givenFilePath("/foo/bar");
        givenAuthenticationBackend();
    }

    @Test(expected = OpenShiftElasticSearchConfigurationException.class)
    public void testInitializationWithUnParsableContent() throws Exception {
        FileUtils.writeStringToFile(tmp, "random content");
        //givenFilePath
        givenAuthenticationBackend();
    }

    @Test(expected = ElasticsearchSecurityException.class)
    public void testAuthenticateWhenUsernameNotFound() {
        //givenFilePath
        //givenAuthenticationBackend
        whenAuthenticating("somerandomuser", "somepasswd");
    }

    @Test(expected = ElasticsearchSecurityException.class)
    public void testAuthenticateWhenUsernameIsFoundAndPasswordMatchFails() {
        //givenFilePath
        //givenAuthenticationBackend
        whenAuthenticating("foo", "somepasswd");
    }

    @Test
    public void testAuthenticateWhenUsernameIsFoundAndPasswordMatchSucceeds() {
        //givenFilePath
        //givenAuthenticationBackend
        user = whenAuthenticating("foo", "bar");
        assertEquals("foo", user.getName());
    }

    @Test
    public void testExistsWhenUserExists() {
        //givenFilePath
        //givenAuthenticationBackend
        user = new User("foo");
        assertTrue("Exp. true when user does exist in the file", auth.exists(user));
    }

    @Test
    public void testExistsWhenUserDoesNotExists() {
        user = new User("someuser");
        assertFalse("Exp. false when user does not exist in the file", auth.exists(user));
    }

}
