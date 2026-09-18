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
    private int tileSize = 512;
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
    /** Uploads with more pages are rejected before anything is rendered. */
    private int maxPages = 500;
    /** Largest rendered page allowed (width x height at render DPI); stops decompression-bomb PDFs. */
    private long maxPagePixels = 40_000_000L;
    /** PDFs rendered at once; further uploads wait up to renderQueueTimeoutSeconds, then get 503. */
    private int maxConcurrentRenders = 2;
    private long renderQueueTimeoutSeconds = 30;
    /** Watermark ink opacity, 0.05-0.6. Lower is easier to read through; higher survives recompression better. */
    private float watermarkOpacity = 0.2f;
    /** Gap between watermark copies, as a multiple of the text height. Larger is lighter on the page. */
    private double watermarkSpacing = 1.5;
    private int tileRateLimitPerWindow = 180;
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

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public long getMaxPagePixels() {
        return maxPagePixels;
    }

    public void setMaxPagePixels(long maxPagePixels) {
        this.maxPagePixels = maxPagePixels;
    }

    public int getMaxConcurrentRenders() {
        return maxConcurrentRenders;
    }

    public void setMaxConcurrentRenders(int maxConcurrentRenders) {
        this.maxConcurrentRenders = maxConcurrentRenders;
    }

    public long getRenderQueueTimeoutSeconds() {
        return renderQueueTimeoutSeconds;
    }

    public void setRenderQueueTimeoutSeconds(long renderQueueTimeoutSeconds) {
        this.renderQueueTimeoutSeconds = renderQueueTimeoutSeconds;
    }

    public float getWatermarkOpacity() {
        return watermarkOpacity;
    }

    public void setWatermarkOpacity(float watermarkOpacity) {
        this.watermarkOpacity = watermarkOpacity;
    }

    public double getWatermarkSpacing() {
        return watermarkSpacing;
    }

    public void setWatermarkSpacing(double watermarkSpacing) {
        this.watermarkSpacing = watermarkSpacing;
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
