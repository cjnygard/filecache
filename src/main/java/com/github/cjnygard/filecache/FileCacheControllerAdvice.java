/**
 * 
 */
package com.github.cjnygard.filecache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author carl
 *
 */
@ControllerAdvice
public class FileCacheControllerAdvice {
	private static final Logger log = LoggerFactory.getLogger(FileCacheControllerAdvice.class);

	@ResponseBody
	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	VndErrors userNotFoundExceptionHandler(UserNotFoundException ex) {
		log.error(String.format("Error: User not found [%s]", ex.getMessage()));
		return new VndErrors("error", ex.getMessage());
	}

}
