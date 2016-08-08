/**
 *
 */
package com.github.cjnygard.filecache.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carl
 *
 */
@XmlRootElement
public class CacheFileList {

	private List<CacheFile> cacheFile = new ArrayList<>();

	public CacheFileList(List<CacheFile> ls) { // jpa only
		cacheFile.addAll(ls);
	}

	public CacheFileList() {
	}

	/**
	 * @return the fileSize
	 */
	public List<CacheFile> getCacheFile() {
		return cacheFile;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setCacheFile(List<CacheFile> ls) {
		this.cacheFile = ls;
	}

	/**
	 * @return the filename
	 */
	public void addCacheFile(CacheFile f) {
		cacheFile.add(f);
	}
}
