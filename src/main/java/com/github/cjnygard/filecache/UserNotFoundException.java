/**
 * 
 */
package com.github.cjnygard.filecache;

/**
 * @author carl
 *
 */
public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String userId) {
		super("could not find user '" + userId + "'.");
	}

}
