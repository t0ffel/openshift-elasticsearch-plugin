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
package io.fabric8.elasticsearch.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representation of a ElasticSearch SearchGuard plugin ACL
 * 
 * @author jeff.cantrill
 *
 */
public class SearchGuardACL implements Iterable<SearchGuardACL.Acl>{
	
	@JsonProperty(value="acl")
	private List<Acl> acls;
	
	static class Acl {
		
		@JsonProperty(value="aclSource")
		private String aclSource = "";

		@JsonProperty(value="__Comment__")
		private String comment = "";
		
		@JsonProperty(value="hosts")
		private List<String> hosts = new ArrayList<>();
		
		@JsonProperty(value="users")
		private List<String> users = new ArrayList<>();
		
		@JsonProperty(value="roles")
		private List<String> roles = new ArrayList<>();
		
		@JsonProperty(value="indices")
		private List<String> indices = new ArrayList<>();
		
		@JsonProperty(value="aliases")
		private List<String> aliases = new ArrayList<>();
		
		@JsonProperty(value="filters_bypass")
		private List<String> filtersBypass = new ArrayList<>();

		@JsonProperty(value="filters_execute")
		private List<String> filtersExecute = new ArrayList<>();
		
		public String getComment() {
			return comment;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
		public List<String> getHosts() {
			return hosts;
		}
		public void setHosts(List<String> hosts) {
			this.hosts = hosts;
		}
		public List<String> getUsers() {
			return users;
		}
		public void setUsers(List<String> users) {
			this.users = users;
		}
		public List<String> getRoles() {
			return roles;
		}
		public void setRoles(List<String> roles) {
			this.roles = roles;
		}
		public List<String> getIndices() {
			return indices;
		}
		public void setIndices(List<String> indicies) {
			this.indices = indicies;
		}
		public List<String> getAliases() {
			return aliases;
		}
		public void setAliases(List<String> aliases) {
			this.aliases = aliases;
		}
		public List<String> getFiltersBypass() {
			return filtersBypass;
		}
		public void setFiltersBypass(List<String> filterBypass) {
			this.filtersBypass = filterBypass;
		}
		public List<String> getFiltersExecute() {
			return filtersExecute;
		}
		public void setFiltersExecute(List<String> filterExecute) {
			this.filtersExecute = filterExecute;
		}
		public String getAclSource() {
			return aclSource;
		}
		public void setAclSource(String aclSource) {
			this.aclSource = aclSource;
		}
		
	}

	/**
	 * An iterator that is safe to delete from 
	 * while spinning through the ACLs
	 */
	@Override
	public Iterator<io.fabric8.elasticsearch.plugin.SearchGuardACL.Acl> iterator() {
		return new ArrayList<>(acls).iterator();
	}
	
	/**
	 * Remove an ACL
	 * @param acl
	 */
	public void remove(Acl acl){
		acls.remove(acl);
	}
	
	public void syncFrom(OpenShiftPolicyCache cache){
		
	}
}