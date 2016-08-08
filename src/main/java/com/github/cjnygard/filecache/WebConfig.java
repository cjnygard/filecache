/**
 *
 */
package com.github.cjnygard.filecache;

import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author carl
 *
 */
public class WebConfig extends WebMvcConfigurerAdapter {

	/**
	 * Total customization - see below for explanation.
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.ignoreAcceptHeader(false);
	}
}
