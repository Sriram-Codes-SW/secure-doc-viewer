package com.example.securedocviewer.service;

import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Directory moves and deletes that tolerate transient locks. On Windows,
 * antivirus scanners and sync clients (OneDrive, Dropbox) briefly hold files
 * that were just written or are being synced, which makes a rename or delete
 * fail with AccessDenied for a moment. Each operation retries for about two
 * seconds before giving up.
 */
final class FileOperations {

    private static final int ATTEMPTS = 8;

    private FileOperations() {
    }

    /** Renames a directory; falls back to copy-and-delete if it stays locked. */
    static void moveDirectory(Path from, Path to) throws IOException {
        FileSystemException last = null;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            try {
                Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch (FileSystemException e) { // includes AccessDeniedException
                last = e;
                backOff(attempt);
            }
        }
        try {
            FileSystemUtils.copyRecursively(from, to);
            deleteDirectory(from);
        } catch (IOException copyFailure) {
            FileSystemUtils.deleteRecursively(to);
            copyFailure.addSuppressed(last);
            throw copyFailure;
        }
    }

    /** Deletes a directory tree; returns false if it didn't exist. */
    static boolean deleteDirectory(Path dir) throws IOException {
        FileSystemException last = null;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            try {
                return FileSystemUtils.deleteRecursively(dir);
            } catch (FileSystemException e) {
                last = e;
                backOff(attempt);
            }
        }
        throw last;
    }

    private static void backOff(int attempt) {
        try {
            Thread.sleep(50L << Math.min(attempt, 4));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
