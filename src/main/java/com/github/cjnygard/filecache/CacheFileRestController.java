/**
 *
 */
package com.github.cjnygard.filecache;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.github.cjnygard.filecache.model.CacheFile;
import com.github.cjnygard.filecache.model.CacheFileList;
import com.github.cjnygard.filecache.repository.AccountRepository;
import com.github.cjnygard.filecache.repository.CacheFileRepository;
import com.github.cjnygard.filecache.storage.StorageManager;
import com.github.cjnygard.filecache.upstream.CacheResolver;

/**
 * @author carl
 *
 */
@RestController
@RequestMapping("/{userId}/cache")
public class CacheFileRestController {
	private static final Logger log = LoggerFactory.getLogger(CacheFileRestController.class);

	private final CacheFileRepository cacheFileRepository;

	private final AccountRepository accountRepository;

	private final StorageManager storageManager;

	private final CacheResolver cacheResolver;

	private final ResourceLoader resourceLoader;

	private Optional<CacheFile> findFileCacheById(String userId, Long cacheFileId) {
		return accountRepository.findByUsername(userId).map(account -> {
			CacheFile cache = this.cacheFileRepository.findOneByAccountUsernameAndId(userId, cacheFileId);
			if (null != cache) {
				log.info(String.format("found[%s] storage[%s]", cache.getFilename(), cache.getStoragePath()));
			} else {
				log.warn(String.format("Unable to find [%d]", cacheFileId.longValue()));
			}
			return cache;
		});
	}

	private Optional<CacheFile> findFileCacheByName(@PathVariable String userId, @PathVariable String hash) {
		return accountRepository.findByUsername(userId).map(account -> {
			CacheFile cache = this.cacheFileRepository.findOneByAccountUsernameAndFilename(userId, hash);
			if (null != cache) {
				log.info(String.format("found[%s] storage[%s]", cache.getFilename(), cache.getStoragePath()));
			} else {
				log.warn(String.format("Unable to find [%s]", hash));
			}
			return cache;
		});
	}

	@RequestMapping(value = "/id/{cacheFileId}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	CacheFileResource readFileCacheJson(@PathVariable String userId, @PathVariable Long cacheFileId) {
		log.info(String.format("JSON: user[%s] id[%d]", userId, cacheFileId.longValue()));
		this.validateUser(userId);
		return findFileCacheById(userId, cacheFileId).map(cache -> {
			return new CacheFileResource(cache);
		}).get();
	}

	@RequestMapping(value = "/id/{cacheFileId}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_XML_VALUE })
	CacheFile readFileCacheXML(@PathVariable String userId, @PathVariable Long cacheFileId) {
		log.info(String.format("XML: user[%s] id[%d]", userId, cacheFileId.longValue()));
		this.validateUser(userId);
		return findFileCacheById(userId, cacheFileId).get();
	}

	@RequestMapping(value = "/file/{hash}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
			"application/hal+json" })
	public CacheFileResource readFileJson(@PathVariable String userId, @PathVariable String hash) {
		log.info(String.format("JSON: user[%s] hash[%s]", userId, hash));
		this.validateUser(userId);
		return findFileCacheByName(userId, hash).map(cache -> {
			return new CacheFileResource(cache);
		}).get();
	}

	@RequestMapping(value = "/file/{hash}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE })
	public CacheFile readFileXML(@PathVariable String userId, @PathVariable String hash) {
		log.info(String.format("XML: user[%s] hash[%s]", userId, hash));
		this.validateUser(userId);
		return findFileCacheByName(userId, hash).get();
	}

	/*
	 * Download a file.
	 */
	@RequestMapping(value = "/download/file/{hash}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> downloadFile(@PathVariable String userId, @PathVariable("hash") String hash) {
		log.info(String.format("user[%s] hash[%s]", userId, hash));
		this.validateUser(userId);
		CacheFile cache = null;
		try {
			cache = this.cacheFileRepository.findByAccountUsernameAndFilename(userId, hash);
			log.info(String.format("found[%s] stored[%s]", cache.getFilename(), cache.getStoragePath()));
		} catch (Exception e) {
			try {
				cache = cacheResolver.fetchCacheFromUpstream(userId, hash);
				log.info(String.format("from upstream: found[%s] stored[%s]", cache.getFilename(),
						cache.getStoragePath()));
			} catch (Exception ex) {
				log.error(String.format("File [%s] not found", hash));
				return ResponseEntity.notFound().build();
			}
		}
		return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(cache.getStoragePath()).toString()));
	}

	@RequestMapping(value = "/file/{hash}", method = RequestMethod.POST)
	ResponseEntity<?> handleFileUpload(@PathVariable String userId, @PathVariable String hash,
			@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		log.info(String.format("user[%s] hash[%s]", userId, hash));
		this.validateUser(userId);
		if (!file.isEmpty()) {
			return accountRepository.findByUsername(userId).map(account -> {
				try {
					String filename = file.getOriginalFilename().isEmpty() ? hash : file.getOriginalFilename();
					String path = storageManager.allocatePath(account.getLocalRootPath(), filename);
					long sz = storageManager.storeFile(account.getLocalRootPath(), filename, file.getInputStream());
					log.info(String.format("Stored file [%s]", path));
					CacheFile cacheFile = cacheFileRepository.save(new CacheFile(account, hash, path, sz));
					log.info(
							String.format("saved[%s] stored[%s]", cacheFile.getFilename(), cacheFile.getStoragePath()));

					HttpHeaders httpHeaders = new HttpHeaders();

					Link forOneFileCache = new CacheFileResource(cacheFile).getLink("self");
					httpHeaders.setLocation(URI.create(forOneFileCache.getHref()));

					return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
				} catch (IOException | RuntimeException e) {
					log.error(String.format("Error: hash[%s] not saved [%s]", hash, e.getMessage()));
					// redirectAttributes.addFlashAttribute("message", "Failed
					// to upload " + file.getOriginalFilename() + " => " +
					// e.getMessage());
					HttpHeaders httpHeaders = new HttpHeaders();
					httpHeaders.setLocation(
							ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build().toUri());
					return new ResponseEntity<>(null, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}).get();

			// redirectAttributes.addFlashAttribute("message",
			// "You successfully uploaded " + file.getOriginalFilename() + "!");
		} else {
			log.error(String.format("Error: hash[%s] file empty, unable to store to cache", hash));
			// redirectAttributes.addFlashAttribute("message", "Failed to
			// upload " + file.getOriginalFilename() + " because it was
			// empty");
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build().toUri());
			return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
		}
		// return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE, "application/hal+json" })
	Resources<CacheFileResource> readFileCaches(@PathVariable String userId) {

		log.info(String.format("user[%s]", userId));
		this.validateUser(userId);

		List<CacheFileResource> cacheFileResourceList = cacheFileRepository.findByAccountUsername(userId).stream()
				.map(CacheFileResource::new).collect(Collectors.toList());
		log.info(String.format("found [%d] files", cacheFileResourceList.size()));
		for (CacheFileResource cfr : cacheFileResourceList) {
			log.info(String.format("  {%s}", cfr.toString()));
		}
		return new Resources<>(cacheFileResourceList);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE, "application/xml" })
	CacheFileList readFileCachesXML(@PathVariable String userId) {

		log.info(String.format("user[%s]", userId));
		this.validateUser(userId);

		CacheFileList cacheFileList = new CacheFileList(
				cacheFileRepository.findByAccountUsername(userId).stream().collect(Collectors.toList()));
		log.info(String.format("found [%d] files", cacheFileList.getCacheFile().size()));
		for (CacheFile cfr : cacheFileList.getCacheFile()) {
			log.info(String.format("  {%s}", cfr.toString()));
		}
		return cacheFileList;
	}

	@Autowired
	CacheFileRestController(CacheFileRepository cacheFileRepository, AccountRepository accountRepository,
			StorageManager storageManager, CacheResolver cacheResolver, ResourceLoader resourceLoader) {
		this.cacheFileRepository = cacheFileRepository;
		this.accountRepository = accountRepository;
		this.storageManager = storageManager;
		this.cacheResolver = cacheResolver;
		this.resourceLoader = resourceLoader;
	}

	private void validateUser(String userId) {
		this.accountRepository.findByUsername(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}

}
