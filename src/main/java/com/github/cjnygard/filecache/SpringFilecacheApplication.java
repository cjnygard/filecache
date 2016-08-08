package com.github.cjnygard.filecache;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.github.cjnygard.filecache.model.Account;
import com.github.cjnygard.filecache.model.CacheFile;
import com.github.cjnygard.filecache.repository.AccountRepository;
import com.github.cjnygard.filecache.repository.CacheFileRepository;
import com.github.cjnygard.filecache.storage.StorageManager;

@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
public class SpringFilecacheApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringFilecacheApplication.class);

	private CacheFile buildCache(Account account, String hash, String user) {
		return CacheFile.build(account, hash, user);
	}

	// To resolve ${} in @Value
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
		p.setIgnoreResourceNotFound(true);
		p.setIgnoreUnresolvablePlaceholders(true);
		return p;
	}

	@Bean
	CommandLineRunner init(AccountRepository accountRepository, CacheFileRepository cacheFileRepository) {
		return (evt) -> Arrays.asList("jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
				.forEach(a -> {
					log.info(String.format("Create dummy account [%s]", a));
					Account account = accountRepository.save(new Account(a, "password"));
					cacheFileRepository.save(buildCache(account, "hash1", a));
					cacheFileRepository.save(buildCache(account, "hash2", a));
				});
	}

	@Bean
	CommandLineRunner cleanupCache(StorageManager storageManager) {
		return (args) -> {
			storageManager.clearCache();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringFilecacheApplication.class, args);
	}
}
