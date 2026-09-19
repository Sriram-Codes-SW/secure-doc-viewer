# Review: 01-the-big-picture.md (window 2)

Verified against book-m6-final: tile-size 512, render-dpi 150, url-ttl-seconds 120, rate limit 180 per 60 s, max-pages 500, README quotes ("ten seconds", "half an hour", right-click, ingest watermark, no MFA, no text layer), file paths TileGrid/SignedUrlService/TileRateLimiter all exist. Table 1.1 versions correct for m6. Solutions arithmetic correct (4x6=24; 464 and 440 px edges). No secrets. No blockers.

1. **minor** - 1.3 / STYLE 9: *rasterizing* and *tiles* are italic; STYLE says defined terms are bold (and glossary requests were made for them). Fix: bold at first definition (client, server, rasterize, tile, watermark, signed URL, rate limit, audit trail).
2. **major** - 1.1-1.3 / pedagogy: used before defined for a non-programmer: PDF, URL (defined only after use as "web address" in 1.3), cache, DevTools/"developer tools", cryptographic signature, session, CPU, "rate limited" (1.6), "audit"/"database" in Fig 1.2. Fix: one-clause definitions at first use or "Chapter N explains" pointers; "sign-in session" in 1.6 needs a phrase.
3. **minor** - 1.3 "That matches the README's 'about 12 tiles'": I did not find that exact phrase when grepping README at m6 (grep for "about 12" returned nothing). Fix: confirm the quote or drop the quotation marks; the math itself (3x4=12) is correct.
4. **minor** - 1.3 analogy (museum window): breakdown is given, good. But the STYLE 8.1 established analogies (wristband for signed URLs) are not used where signed URLs appear; consider a forward reference.
5. **minor** - 1.6: "tied to one sign-in session" is a claim about implementation not verified here; add a source comment (SignedUrlService / SessionService) or check.
6. **minor** - Figure 1.1 caption is bold above the figure; STYLE 13.1 says caption goes below in italics (`*Figure 1.1 — ...*`). Same for Figure 1.2 and Table 1.1 placement. Fix per STYLE 13. Also text should refer to Table 1.1 / Figure 1.2 before they appear (Fig 1.1 and 1.2 are ok; Table 1.1 ok).
7. **minor** - Exercise 1.2 lacks tag/scope; fine. Exercise 1.3 asks the reader to open README.md, which requires a checkout: say "at book-m6-final". Solutions file cites "Appendix C" for both; fine, but solution 1.3 "about 15 pages a minute" is derived (180/12) - say so.
8. **minor** - Further reading: MDN URL path may have moved; verify links resolve.
9. **minor** - Source comments present for claims; the metadata comment matches OUTLINE. Good.
