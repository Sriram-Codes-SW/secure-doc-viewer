package com.example.securedocviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code secure-doc-viewer.*} block from application.yml.
 */
@Component
@ConfigurationProperties(prefix = "secure-doc-viewer")
public class ViewerProperties {

    private String storageRoot = "./storage";
    private int tileSize = 256;
    private int renderDpi = 150;
    private String signingSecret;
    private long urlTtlSeconds = 120;
    private long sessionTtlSeconds = 1800;

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

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    public void setSessionTtlSeconds(long sessionTtlSeconds) {
        this.sessionTtlSeconds = sessionTtlSeconds;
    }
}
