/**
 *
 */
package com.github.cjnygard.filecache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.cjnygard.filecache.storage.StorageManager;

/**
 * @author carl
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringFilecacheApplication.class)
public class StorageManagerTest {

	@Value("${filecache.rootdir:/var/cache/filecache}")
	private String rootDir;

	private String path = "somepath";

	private String filename = "somefile";

	@Autowired
	private StorageManager storageManager;

	void setup() throws IOException {
		Path folder = Paths.get(rootDir, path);
		if (Files.notExists(folder)) {
			Files.createDirectory(folder);
		}
		Path file = Paths.get(rootDir, path, filename);
		if (Files.notExists(file)) {
			Files.createFile(file);
		}
	}

	@Test
	public void testInitialization() throws IOException {
		setup();
		storageManager.clearCache();
		assert (Files.notExists(Paths.get(rootDir, path, filename)));
	}

	@Test
	public void testAllocatePath() {
		String p = storageManager.allocatePath(path, filename);
		assert (p.equals(Paths.get(rootDir, path, filename).toString()));
	}

	@Test
	public void testStoreFile() throws IOException {
		String buffer = "FileContents";
		long sz = storageManager.storeFile(path, filename,
				new ByteArrayInputStream(buffer.getBytes(StandardCharsets.UTF_8)));
		assert (sz == buffer.length());
		assert (Files.exists(Paths.get(rootDir, path, filename)));
	}

	@Test
	public void testLoad() {

	}
}
