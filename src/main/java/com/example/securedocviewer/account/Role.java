package com.example.securedocviewer.account;

/**
 * What an account may do. Roles are cumulative in practice: every signed-in
 * user can read documents they have access to, publishers can also upload,
 * and admins can additionally manage accounts, sessions and the audit log.
 */
public enum Role {
    READER,
    PUBLISHER,
    ADMIN
}
