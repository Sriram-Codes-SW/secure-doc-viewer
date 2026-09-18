package com.example.securedocviewer.exception;

/**
 * The tile URL refers to a render that has since been replaced (the PDF was
 * swapped while someone was reading). Mapped to 410 so the viewer can tell
 * "reload this page" apart from "you lost access" (404).
 */
public class TileGoneException extends RuntimeException {
    public TileGoneException() {
        super("This page has changed. Reload it to see the latest version.");
    }
}
