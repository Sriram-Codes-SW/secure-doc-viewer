# Review: 10-docker-and-compose.md (window 4, LIGHT PASS)

Scope: automated listing check only (see other window-4 files). Result: all listings match book-m6-final except Listing 10.3 (`Dockerfile`), where the checker flagged three lines.

1. **minor** - Listing 10.3: `FROM ... @sha256:<digest>` is a placeholder for the real digests; the caption says "digests shortened", but the digest is replaced by `<digest>`, not shortened. Fix caption: "digests replaced by `<digest>`".
2. **minor** - Listing 10.3 shows `ENV STORAGE_ROOT=/data/storage` as a single line; the real file has a multi-line `ENV STORAGE_ROOT=/data/storage \` continuation (line 26) followed by more variables, and the same setting is repeated in `docker-compose.yml` (line 41). Fix: show the continuation or say "other ENV values omitted".
3. Not yet reviewed: prose, terms before use (image, container, layer, volume), exercises, analogy breakdown (file has one Analogy and one breakdown).
