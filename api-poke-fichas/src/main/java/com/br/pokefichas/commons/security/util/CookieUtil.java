package com.br.pokefichas.commons.security.util;

import com.br.pokefichas.commons.config.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final CookieProperties cookieProperties;

    public CookieUtil(final CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public ResponseCookie createRefreshTokenCookie(final String token) {
        final ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(cookieProperties.getMaxAge()))
                .sameSite(cookieProperties.getSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        return builder.build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        final ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/api/auth")
                .maxAge(0)
                .sameSite(cookieProperties.getSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        return builder.build();
    }

    public Optional<String> getCookieValue(final HttpServletRequest request, final String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }

    public void addCookie(final HttpServletResponse response, final ResponseCookie cookie) {
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
