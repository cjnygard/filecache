/**
 *
 */
package com.github.cjnygard.filecache.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author carl
 *
 */
public interface StorageManager {
	long storeFile(String basePath, String hash, InputStream stream) throws IOException;

	String allocatePath(String basePath, String hash);

	Long getFileSize(String hash);

	void clearCache() throws IOException;
}
