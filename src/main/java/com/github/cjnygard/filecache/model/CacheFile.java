/**
 *
 */
package com.github.cjnygard.filecache.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author carl
 *
 */
@Entity
@XmlRootElement
public class CacheFile {

	@JsonIgnore
	@ManyToOne
	private Account account;

	@Id
	@GeneratedValue
	@XmlAttribute
	private Long id;

	private Long lastAccess = new Long(0);
	private Long fileSize = new Long(0);
	private String filename = "unknown";
	private String storagePath = "./unknown"; // full path

	static public CacheFile build(Account account, String hash, String user) {
		return new CacheFile(account, hash + user, "/var/cache/filecache/" + hash + user, new Long(100));
	}

	public CacheFile() { // jpa only
		this.fileSize = new Long(0);
	}

	public CacheFile(Account account, String filename, String path, Long fileSize) {
		this.filename = filename;
		this.account = account;
		this.storagePath = path;
		this.fileSize = fileSize;
	}

	/**
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the storagePath
	 */
	public String getStoragePath() {
		return storagePath;
	}

	/**
	 * @param storagePath
	 *            the storagePath to set
	 */
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	/**
	 * @return the lastAccess
	 */
	public Long getLastAccess() {
		return lastAccess;
	}

	/**
	 * @param lastAccess
	 *            the lastAccess to set
	 */
	public void setLastAccess(Long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public Account getAccount() {
		return account;
	}

	public Long getId() {
		return id;
	}

}