/**
 *
 */
package com.github.cjnygard.filecache;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.github.cjnygard.filecache.model.CacheFile;

/**
 * @author carl
 *
 */
public class CacheFileResource extends ResourceSupport {
	private final CacheFile cacheFile;

	public CacheFileResource(CacheFile cacheFile) {
		String username = cacheFile.getAccount().getUsername();
		this.cacheFile = cacheFile;
		this.add(new Link(cacheFile.getStoragePath(), "cacheFile-path"));
		this.add(linkTo(CacheFileRestController.class, username).withRel("filecache"));
		this.add(
				linkTo(methodOn(CacheFileRestController.class, username).readFileCacheJson(username, cacheFile.getId()))
						.withSelfRel());
	}

	public CacheFile getCacheFile() {
		return cacheFile;
	}

	@Override
	public String toString() {
		return String.format("User[%s] path[%s] file[%s]", cacheFile.getAccount().getUsername(),
				cacheFile.getStoragePath(), cacheFile.getFilename());
	}
}
