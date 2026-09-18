import { Role } from '../../core/session.service';

export interface SessionSummary {
  /** Opaque, revocation-only reference. Never the session id. */
  handle: string;
  username: string;
  role: Role;
  lastActiveEpochSeconds: number;
  current: boolean;
}

export interface RateLimitStatus {
  username: string;
  used: number;
  limit: number;
  windowSeconds: number;
}

export interface AuditEntry {
  sessionHandle: string;
  username: string;
  documentId: string;
  page: number;
  row: number;
  col: number;
  timestampEpochSeconds: number;
}

export interface UserSummary {
  username: string;
  role: Role;
  enabled: boolean;
  createdAtEpochSeconds: number;
}
