/**
 * 
 */
package com.github.cjnygard.filecache.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.cjnygard.filecache.model.Account;

/**
 * @author carl
 *
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}
