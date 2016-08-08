/**
 *
 */
package com.github.cjnygard.filecache.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * @author carl
 *
 */
@Service
public class LocalStorageManager implements StorageManager {

	private static final Logger log = LoggerFactory.getLogger(LocalStorageManager.class);

	@Value("${filecache.rootdir:/var/cache/filecache}")
	private String ROOTDIR;

	public LocalStorageManager(String rootdir) {
		this.ROOTDIR = rootdir;
	}

	public LocalStorageManager() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.github.cjnygard.filecache.model.StorageManager#storeFile(java.lang.
	 * String, java.lang.String, java.io.InputStream)
	 */
	@Override
	public long storeFile(String basePath, String hash, InputStream stream) throws IOException {
		Path folder = getFolder(basePath);
		if (Files.notExists(folder)) {
			Files.createDirectory(folder);
		}
		Path p = getPath(basePath, hash);
		log.info(String.format("Storing [%s:%s] in path [%s]", basePath, hash, p.toString()));
		long sz = Files.copy(stream, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		log.info(String.format("Copied stream to path [%s]", p.toString()));
		return sz;
	}

	private Path getFolder(String basepath) {
		return Paths.get(ROOTDIR, basepath);
	}

	private Path getPath(String basePath, String hash) {
		return Paths.get(ROOTDIR, basePath, hash);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.github.cjnygard.filecache.model.StorageManager#allocatePath(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public String allocatePath(String basePath, String hash) {
		return getPath(basePath, hash).toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.github.cjnygard.filecache.model.StorageManager#getFileSize(java.lang.
	 * String)
	 */
	@Override
	public Long getFileSize(String hash) {
		return new Long(100);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.cjnygard.filecache.model.StorageManager#clearCache()
	 */
	@Override
	public void clearCache() throws IOException {
		FileSystemUtils.deleteRecursively(new File(ROOTDIR));
		if (Files.notExists(Paths.get(ROOTDIR))) {
			Files.createDirectory(Paths.get(ROOTDIR));
		}
	}

}
