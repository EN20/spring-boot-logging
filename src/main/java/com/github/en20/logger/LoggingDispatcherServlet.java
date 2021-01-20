package com.github.en20.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoggingDispatcherServlet extends DispatcherServlet {

    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher;
    private final LoggerProperties loggerProperties;

    public LoggingDispatcherServlet(ObjectMapper objectMapper, AntPathMatcher antPathMatcher, LoggerProperties loggerProperties) {
        this.objectMapper = objectMapper;
        this.antPathMatcher = antPathMatcher;
        this.loggerProperties = loggerProperties;
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        long startTime = System.currentTimeMillis();

        try {
            super.doDispatch(request, response);
        } finally {
            if (shouldLog(request)) {
                log((ContentCachingRequestWrapper) request, (ContentCachingResponseWrapper) response, System.currentTimeMillis() - startTime);
            }

            updateResponse(response);
        }
    }

    public String log(ContentCachingRequestWrapper requestToCache, ContentCachingResponseWrapper responseToCache, long executionTime) {

        try {
            Object requestContent = createObject(requestToCache.getContentAsByteArray());
            Object responseContent = createObject(responseToCache.getContentAsByteArray());

            Map<String, Object> map = new HashMap<>();
            map.put("executionTime", executionTime);
            map.put("httpMethod", requestToCache.getMethod());
            map.put("requestBody", requestContent);
            map.put("response", responseContent);
            map.put("requestParameters", requestToCache.getParameterMap());
            map.put("uri", requestToCache.getRequestURI());
            map.put("date", DATE_FORMAT.format(new Date()));
            map.put("logIdentifier", UUID.randomUUID().toString());

            List<String> headersNames = Collections.list(requestToCache.getHeaderNames());

            Map<String, String> headers = new HashMap<>();
            for (String header : headersNames) {
                headers.put(header, requestToCache.getHeader(header));
            }

            map.put("requestHeader", headers);

            var logString = objectMapper.writeValueAsString(map);

            logger.info(logString);

            return logString;

        } catch (Exception e){
            return "can not create log" + e.getMessage();
        }

    }

    private Object createObject(byte[] byteArray) throws IOException {
        if (byteArray == null || byteArray.length == 0)
            return null;
        return objectMapper.readValue(byteArray, Object.class);
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        responseWrapper.copyBodyToResponse();
    }


    protected boolean shouldLog(HttpServletRequest request) {
        var filter = Optional.ofNullable(loggerProperties);
        if (filter.isEmpty()) return false;

        if (shouldExcludePath(request, filter.get())) return false;

        return shouldIncludePath(request, filter.get());
    }

    protected boolean shouldExcludePath(HttpServletRequest request, LoggerProperties filterProperty) {
        var excludedPaths = Optional.ofNullable(filterProperty.getExcludedPaths());
        if (excludedPaths.isEmpty()) return false;

        return excludedPaths.get().stream().anyMatch(s -> antPathMatcher.match(s, request.getRequestURI()));
    }

    protected boolean shouldIncludePath(HttpServletRequest request, LoggerProperties filterProperty) {
        var includedPaths = Optional.ofNullable(filterProperty.getIncludedPaths());
        if (includedPaths.isEmpty()) return false;

        return includedPaths.get().stream().anyMatch(s -> antPathMatcher.match(s, request.getRequestURI()));
    }
}
