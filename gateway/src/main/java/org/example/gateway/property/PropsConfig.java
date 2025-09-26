package org.example.gateway.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "gateway")
@Component
public class PropsConfig {
    private final Jwt jwt = new Jwt();
    private final ApiKey apiKey = new ApiKey();

    public Jwt getJwt() {
        return jwt;
    }

    public ApiKey getApiKey() {
        return apiKey;
    }

    public static class Jwt {
        private String secret;
        private String issuer;
        private List<String> publicPaths;

        public String getSecret() {
            return secret;
        }
        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public List<String> getPublicPaths() {
            return publicPaths;
        }
        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }
    }

    public static class ApiKey {
        private String header;
        private String authUrl;
        private List<String> publicPaths;

        public String getHeader() {
            return header;
        }
        public void setHeader(String header) {
            this.header = header;
        }

        public String getAuthUrl() {
            return authUrl;
        }
        public void setAuthUrl(String authUrl) {
            this.authUrl = authUrl;
        }

        public List<String> getPublicPaths() {
            return publicPaths;
        }
        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }
    }
}
