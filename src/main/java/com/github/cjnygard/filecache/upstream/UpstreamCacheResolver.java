package com.github.cjnygard.filecache.upstream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.cjnygard.filecache.model.CacheFile;
import com.github.cjnygard.filecache.repository.AccountRepository;
import com.github.cjnygard.filecache.repository.CacheFileRepository;
import com.github.cjnygard.filecache.storage.StorageManager;

@Service
public class UpstreamCacheResolver implements CacheResolver {
	private static final Logger log = LoggerFactory.getLogger(UpstreamCacheResolver.class);

	private final CacheFileRepository cacheFileRepository;

	private final AccountRepository accountRepository;

	private final StorageManager storageManager;

	@Value("${cache.parentName:http://localhost}")
	private String parentName;

	@Value("${cache.parentPort:8082}")
	private Long parentPort;

	private String getParentUrl() {
		return String.format("http://%s:%d/", parentName, parentPort);
	}

	@Autowired
	UpstreamCacheResolver(CacheFileRepository cacheFileRepository, AccountRepository accountRepository,
			StorageManager storageManager) {
		this.cacheFileRepository = cacheFileRepository;
		this.accountRepository = accountRepository;
		this.storageManager = storageManager;
	}

	@Override
	public CacheFile downloadFileFromUpstream(String userId, String hash, CacheFile cache) throws IOException {
		// TODO Auto-generated method stub
		return accountRepository.findByUsername(userId).map(account -> {
			try {
				String filename = cache.getFilename().isEmpty() ? hash : cache.getFilename();
				String path = storageManager.allocatePath(account.getLocalRootPath(), filename);
				String url = String.format("%s/%s/download/file/{%s}", getParentUrl(), userId, hash);
				log.info(String.format("Downloading from upstream[%s]", url));
				BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream());
				long sz = storageManager.storeFile(account.getLocalRootPath(), filename, bis);
				log.info(String.format("Stored file [%s]", path));
				CacheFile cacheFile = cacheFileRepository.save(new CacheFile(account, hash, path, sz));
				bis.close();
				return cacheFile;
			} catch (IOException | RuntimeException e) {
				log.error(String.format("Error: hash[%s] not saved [%s]", hash, e.getMessage()));
				return null;
			}
		}).get();
	}

	@Override
	public CacheFile fetchCacheFromUpstream(String userId, String hash) throws IOException {
		// :TODO: figure out how to retrieve and save file
		RestTemplate restTemplate = new RestTemplate();
		String url = String.format("%s/%s/cache/file/{%s}", getParentUrl(), userId, hash);
		CacheFile cacheFile = downloadFileFromUpstream(userId, hash, restTemplate.getForObject(url, CacheFile.class));
		log.info(String.format("Retrieved [%s] from path [%s]", cacheFile.toString()));
		return cacheFile;
	}

}
