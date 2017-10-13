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

package io.fabric8.elasticsearch;

import org.junit.Test;

public class OperationsIntegrationTest extends ElasticsearchIntegrationTest {
    
    /*
     * Test the case where ES is exposed publicly and an admin user
     * logs in, accesses ES which triggers role creation.  A subsequent
     * request using the same username without a matching bearer token
     * should result in access denied
     */
    @Test
    public void testNonOpsUserUsingOpsUsername() throws Exception {
        startES();

        final String username = "admin";
        final String token = "ADMINTOKEN";

        //ops user
        givenUserIsClusterAdmin("admin", "ADMINTOKEN");
        givenTokenIsAuthorizedForUser(username);
        givenUserIsAdminForProjects("logging", "openshift");
        whenSearchingIndex(".operations.*");
        assertThatResponseIsSuccessful();

        //using admin name without providing token
        givenUserIsNotClusterAdmin("admin", null);
        givenTokenIsUnAuthorizedForUser();
        whenSearchingIndex(".operations.*");
        assertThatResponseIsUnauthorized();

        //using some name without matching token
        givenUserIsNotClusterAdmin("foo", token);
        givenTokenIsAuthorizedForUser(username);//oc returns user of token if given token
        whenSearchingIndex(".operations.*");
        assertThatResponseIsUnauthorized();
    }

}
