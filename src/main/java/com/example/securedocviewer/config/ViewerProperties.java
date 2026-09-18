package com.example.securedocviewer.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds the {@code secure-doc-viewer.*} block from application.yml.
 */
@Component
@ConfigurationProperties(prefix = "secure-doc-viewer")
@Validated
public class ViewerProperties {

    private String storageRoot = "./storage";
    private int tileSize = 256;
    private int renderDpi = 150;
    /**
     * HMAC key for tile tokens and session-derived values. Supplied via the
     * SIGNING_SECRET environment variable; startup fails if it is missing or
     * too short to be a real key, rather than failing on the first tile.
     */
    @NotBlank(message = "SIGNING_SECRET must be set")
    @Size(min = 32, message = "SIGNING_SECRET must be at least 32 characters")
    private String signingSecret;
    private long urlTtlSeconds = 120;
    private int tileRateLimitPerWindow = 120;
    private long tileRateLimitWindowSeconds = 60;

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public int getRenderDpi() {
        return renderDpi;
    }

    public void setRenderDpi(int renderDpi) {
        this.renderDpi = renderDpi;
    }

    public String getSigningSecret() {
        return signingSecret;
    }

    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
    }

    public long getUrlTtlSeconds() {
        return urlTtlSeconds;
    }

    public void setUrlTtlSeconds(long urlTtlSeconds) {
        this.urlTtlSeconds = urlTtlSeconds;
    }

    public int getTileRateLimitPerWindow() {
        return tileRateLimitPerWindow;
    }

    public void setTileRateLimitPerWindow(int tileRateLimitPerWindow) {
        this.tileRateLimitPerWindow = tileRateLimitPerWindow;
    }

    public long getTileRateLimitWindowSeconds() {
        return tileRateLimitWindowSeconds;
    }

    public void setTileRateLimitWindowSeconds(long tileRateLimitWindowSeconds) {
        this.tileRateLimitWindowSeconds = tileRateLimitWindowSeconds;
    }
}
