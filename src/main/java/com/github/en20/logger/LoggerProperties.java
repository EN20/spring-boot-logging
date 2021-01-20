package com.github.en20.logger;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Encapsulates the logger configurations properties.
 *
 * @author Vahid Forghani
 */
@Configuration
@ConfigurationProperties("logger")
public class LoggerProperties {

    /**
     * Represents a set of path should be ignored in logger.
     */
    private Set<String> excludedPaths;

    /**
     * Represents a set of path should be logged by logger
     */
    private Set<String> includedPaths;

    public Set<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(Set<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    public Set<String> getIncludedPaths() {
        return includedPaths;
    }

    public void setIncludedPaths(Set<String> includedPaths) {
        this.includedPaths = includedPaths;
    }
}
