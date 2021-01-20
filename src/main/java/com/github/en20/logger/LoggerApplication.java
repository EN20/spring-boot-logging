package com.github.en20.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.DispatcherServlet;

import static org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME;

@SpringBootApplication
public class LoggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggerApplication.class, args);
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public LoggingDispatcherServlet loggingDispatcherServlet(ObjectMapper objectMapper, AntPathMatcher antPathMatcher, LoggerProperties loggerProperties){
        return new LoggingDispatcherServlet(objectMapper, antPathMatcher, loggerProperties);
    }

    @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet(LoggingDispatcherServlet loggingDispatcherServlet) {
        return loggingDispatcherServlet;
    }

}
