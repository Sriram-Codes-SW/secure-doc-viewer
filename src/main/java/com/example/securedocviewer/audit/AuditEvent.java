package com.example.securedocviewer.audit;

/**
 * One audit record. Username and document title are copied in rather than
 * referenced, so a record still reads correctly after the account or
 * document it mentions has been renamed or deleted.
 */
public record AuditEvent(
        long id,
        long occurredAtEpochMillis,
        AuditEventType type,
        String username,
        String sessionHandle,
        String clientIp,
        String documentId,
        String documentTitle,
        Integer page,
        Integer tileRow,
        Integer tileCol,
        String detail
) {

    /** Who did it and from where; attached to every event recorded during a request. */
    public record Actor(String username, String sessionHandle, String clientIp) {
        public static Actor anonymous(String clientIp) {
            return new Actor(null, null, clientIp);
        }
    }

    /** What an event is about; all fields optional. */
    public record Subject(String documentId, String documentTitle, Integer page, Integer tileRow, Integer tileCol,
                          String detail) {

        public static Subject none() {
            return new Subject(null, null, null, null, null, null);
        }

        public static Subject detail(String detail) {
            return new Subject(null, null, null, null, null, detail);
        }

        public static Subject document(String documentId, String documentTitle) {
            return new Subject(documentId, documentTitle, null, null, null, null);
        }

        public static Subject document(String documentId, String documentTitle, String detail) {
            return new Subject(documentId, documentTitle, null, null, null, detail);
        }

        public static Subject tile(String documentId, String documentTitle, int page, int row, int col) {
            return new Subject(documentId, documentTitle, page, row, col, null);
        }
    }
}
