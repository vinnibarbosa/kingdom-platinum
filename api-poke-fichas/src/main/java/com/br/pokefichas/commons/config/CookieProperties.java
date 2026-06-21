package com.br.pokefichas.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.security.jwt.cookie")
public class CookieProperties {

    private String domain;
    private boolean secure = true;
    private boolean httpOnly = true;
    private String sameSite = "Lax";
    private int maxAge = 2592000;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public boolean isHttpOnly() { return httpOnly; }
    public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
    public String getSameSite() { return sameSite; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }
    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
}
