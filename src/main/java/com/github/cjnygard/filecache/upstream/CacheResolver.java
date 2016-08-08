package com.github.cjnygard.filecache.upstream;

import java.io.IOException;

import com.github.cjnygard.filecache.model.CacheFile;

public interface CacheResolver {
	CacheFile downloadFileFromUpstream(String userId, String hash, CacheFile cache) throws IOException;

	CacheFile fetchCacheFromUpstream(String userId, String hash) throws IOException;

}
