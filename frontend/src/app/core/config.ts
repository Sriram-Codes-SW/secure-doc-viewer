/**
 * API calls use same-origin relative URLs. In development the Angular dev
 * server proxies /api to Spring Boot (proxy.conf.json); in production the
 * SPA and API are served from one origin. Same-origin is what lets the
 * httpOnly session cookie and the CSRF cookie work without CORS.
 */
export const API_BASE_URL = '';
