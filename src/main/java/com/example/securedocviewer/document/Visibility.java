package com.example.securedocviewer.document;

/** Who, besides the owner and admins, may open a document. */
public enum Visibility {
    /** Only users the owner has explicitly shared it with. */
    PRIVATE,
    /** Every signed-in user. */
    EVERYONE
}
