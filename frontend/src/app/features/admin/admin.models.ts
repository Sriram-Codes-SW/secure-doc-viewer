export interface SessionSummary {
  sessionId: string;
  username: string;
  expiresAtEpochSeconds: number;
}

export interface RateLimitStatus {
  sessionId: string;
  used: number;
  limit: number;
  windowSeconds: number;
}

export interface AuditEntry {
  sessionId: string;
  username: string;
  documentId: string;
  page: number;
  row: number;
  col: number;
  timestampEpochSeconds: number;
}
