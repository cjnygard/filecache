/**
 *
 */
package com.github.cjnygard.filecache.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author carl
 *
 */
@Entity
public class Account {

	@OneToMany(mappedBy = "account")
	private Set<CacheFile> cacheFiles = new HashSet<>();

	@Id
	@GeneratedValue
	private Long id;

	String localRootPath = "common";
	String parentUrl = "rootCache";
	Long maxFileSize = new Long(5 * 1024 * 1024 * 1024); // 5G
	Long maxTTL = new Long(24 * 60 * 60); // 1 day

	@JsonIgnore
	public String password;
	public String username;

	public Account(String name, String password) {
		this.username = name;
		this.password = password;
	}

	public Account(String name, String password, Account proto) {
		this.username = name;
		this.password = password;
		this.localRootPath = proto.localRootPath;
		this.parentUrl = proto.parentUrl;
		this.maxFileSize = proto.maxFileSize;
		this.maxTTL = proto.maxTTL;
	}

	Account() { // jpa only
	}

	/**
	 * @return the localRootPath
	 */
	public String getLocalRootPath() {
		return localRootPath;
	}

	/**
	 * @param localRootPath
	 *            the localRootPath to set
	 */
	public void setLocalRootPath(String localRootPath) {
		this.localRootPath = localRootPath;
	}

	/**
	 * @return the parentUrl
	 */
	public String getParentUrl() {
		return parentUrl;
	}

	/**
	 * @param parentUrl
	 *            the parentUrl to set
	 */
	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	/**
	 * @return the maxFileSize
	 */
	public Long getMaxFileSize() {
		return maxFileSize;
	}

	/**
	 * @param maxFileSize
	 *            the maxFileSize to set
	 */
	public void setMaxFileSize(Long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	/**
	 * @return the maxTTL
	 */
	public Long getMaxTTL() {
		return maxTTL;
	}

	/**
	 * @param maxTTL
	 *            the maxTTL to set
	 */
	public void setMaxTTL(Long maxTTL) {
		this.maxTTL = maxTTL;
	}

	public Set<CacheFile> getCacheFiles() {
		return cacheFiles;
	}

	public Long getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

}