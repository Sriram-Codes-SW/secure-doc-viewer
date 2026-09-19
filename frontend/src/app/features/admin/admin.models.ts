import { Role } from '../../core/session.service';

export interface SessionSummary {
  /** Opaque, revocation-only reference. Never the session id. */
  handle: string;
  username: string;
  role: Role;
  lastActiveEpochSeconds: number;
  current: boolean;
  clientIp: string | null;
  /** e.g. "Chrome on Windows" (the raw User-Agent is not kept). */
  device: string | null;
  startedAtEpochSeconds: number | null;
}

export interface RateLimitStatus {
  username: string;
  used: number;
  limit: number;
  windowSeconds: number;
}

export const AUDIT_EVENT_TYPES = [
  'PAGE_VIEWED',
  'TILE_VIEWED',
  'SIGN_IN',
  'SIGN_IN_FAILED',
  'SIGN_IN_LOCKED',
  'SIGN_OUT',
  'PASSWORD_CHANGED',
  'SESSION_REVOKED',
  'USER_CREATED',
  'USER_UPDATED',
  'USER_PASSWORD_RESET',
  'USER_UNLOCKED',
  'DOCUMENT_UPLOADED',
  'DOCUMENT_REPLACED',
  'DOCUMENT_UPDATED',
  'DOCUMENT_DELETED',
  'DOCUMENT_SHARED',
  'DOCUMENT_UNSHARED',
  'DOCUMENT_OWNER_CHANGED',
  'ACCESS_DENIED',
  'RATE_LIMITED',
] as const;

export type AuditEventType = (typeof AUDIT_EVENT_TYPES)[number];

export interface AuditEvent {
  id: number;
  occurredAtEpochMillis: number;
  type: AuditEventType;
  username: string | null;
  sessionHandle: string | null;
  clientIp: string | null;
  documentId: string | null;
  documentTitle: string | null;
  page: number | null;
  tileRow: number | null;
  tileCol: number | null;
  detail: string | null;
}

export interface AuditPage {
  items: AuditEvent[];
  total: number;
  page: number;
  size: number;
}

export interface AuditFilter {
  type?: AuditEventType | '';
  username?: string;
  documentId?: string;
  /** Trace code read off a watermark; matches the start of the session handle. */
  trace?: string;
}

export interface UserSummary {
  username: string;
  role: Role;
  enabled: boolean;
  createdAtEpochSeconds: number;
  lastSignInEpochSeconds: number | null;
  mustChangePassword: boolean;
  ownedDocuments: number;
  /** A sign-in lockout currently applies to this account (from some address, or account-wide). */
  locked: boolean;
}
