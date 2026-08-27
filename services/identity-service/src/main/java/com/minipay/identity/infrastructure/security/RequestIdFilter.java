package com.minipay.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 让 Spring 在启动时创建并注册这个 Filter Bean。每个 HTTP 请求到 Controller 前会先经过它一次。
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    // ATTRIBUTE 存在服务端本次 request 对象中；HEADER 是 HTTP 请求/响应中使用的名字。
    public static final String ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";
    public static final String HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // 调用方带了 X-Request-Id 就沿用；没带则生成一个。它不是登录规则，只是一次请求的追踪编号。
        String requestId = normalize(request.getHeader(HEADER));
        // 后面的 Controller/Service 可以从 request 取到同一个编号，审计和日志因此能关联到同一次请求。
        request.setAttribute(ATTRIBUTE, requestId);
        // 同时写回响应头，前端或调用方也能看到并反馈这个编号。
        response.setHeader(HEADER, requestId);
        // 把请求继续交给过滤器链；之后才会到 Spring MVC 和 Controller。
        filterChain.doFilter(request, response);
    }

    public static String get(HttpServletRequest request) {
        // Controller 优先读取 Filter 已保存的编号；理论上没有时才从请求头读取/生成兜底值。
        Object value = request.getAttribute(ATTRIBUTE);
        return value instanceof String requestId ? requestId : normalize(request.getHeader(HEADER));
    }

    private static String normalize(String value) {
        // 调用方没有携带或只携带空白请求编号时，生成 UUID，保证每次请求仍可被追踪。
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }
        // 去掉前后空格，并限制长度，避免不可信请求头污染日志、审计或存储。
        String trimmed = value.trim();
        return trimmed.length() <= 128 ? trimmed : trimmed.substring(0, 128);
    }
}
