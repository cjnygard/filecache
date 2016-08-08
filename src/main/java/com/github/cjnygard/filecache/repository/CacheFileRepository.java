/**
 * 
 */
package com.github.cjnygard.filecache.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.cjnygard.filecache.model.CacheFile;

/**
 * @author carl
 *
 */
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
    Collection<CacheFile> findByAccountUsername(String username);
    CacheFile findByAccountUsernameAndFilename(String username, String filename);
    CacheFile findByAccountUsernameAndId(String username, Long id);
	CacheFile findOneByAccountUsernameAndId(String username, Long id);
	CacheFile findOneByAccountUsernameAndFilename(String username, String filename);
}
