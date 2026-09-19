package com.example.securedocviewer.document;

/** What the per-tile check needs: the title (for the audit log) and which tile version to read. */
public record TileAccess(String title, int tileVersion, int tileSize) {
}
