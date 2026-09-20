"""Generate appendices/index-terms.md: a back-of-the-book index (term, then chapters).

Terms and their defining chapters come from GLOSSARY.md (the "First defined" column). For each term the index
lists every chapter that defines it (shown in bold) or uses it at least twice, with runs of consecutive
chapters written as ranges (for example 12-14). There is no cut-off. A few entries carry "See also" and
alternate-spelling cross-references. Usage: python gen_index.py [output]
"""
import glob
import os
import re
import sys
from collections import defaultdict

BOOK = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(BOOK, "appendices", "index-terms.md")

files = {}
for pat in ("part-*/[0-9][0-9]-*.md", "tradeoffs/[0-9][0-9]-*.md"):
    for f in glob.glob(os.path.join(BOOK, pat)):
        b = os.path.basename(f)
        if "solutions" in b:
            continue
        files[int(b[:2])] = f
text = {}
for n, f in files.items():
    t = open(f, encoding="utf-8").read()
    t = re.sub(r"<!--.*?-->", "", t, flags=re.S)
    t = re.sub(r"```.*?```", "", t, flags=re.S)
    text[n] = t.lower()

# term -> defining chapter, read from the glossary
terms = {}
for l in open(os.path.join(BOOK, "GLOSSARY.md"), encoding="utf-8"):
    m = re.match(r"^\| \*\*(.+?)\*\* \|.*\| Ch (\d+)(?:[^|]*)\|$", l)
    if m:
        terms[m.group(1)] = int(m.group(2))

def variants(term):
    m = re.match(r"^(.*?)\s*\((.+)\)\s*$", term)
    base, inner = (m.group(1), m.group(2)) if m else (term, None)
    v = {base.lower(), term.lower()}
    if inner:
        v.add(inner.lower())
    out = set()
    for x in v:
        out.add(x)
        if not x.endswith("s"):
            out.add(x + "s")
        if x.endswith("y") and len(x) > 3:
            out.add(x[:-1] + "ies")
    return out

def ranges(nums, bold):
    """Compress sorted chapter numbers into ranges; a bold (defining) chapter is always shown on its own."""
    out = []
    i = 0
    nums = sorted(nums)
    while i < len(nums):
        j = i
        while j + 1 < len(nums) and nums[j + 1] == nums[j] + 1 and nums[j + 1] not in bold and nums[j] not in bold:
            j += 1
        if nums[i] in bold:
            out.append("**%d**" % nums[i])
        elif j > i:
            out.append("%d–%d" % (nums[i], nums[j]))
        else:
            out.append(str(nums[i]))
        i = j + 1
    return out

entries = {}
for disp, dch in terms.items():
    phrases = variants(disp)
    chs = set([dch])
    for n, low in text.items():
        count = 0
        for p in phrases:
            if len(p) < 3:
                continue
            count += len(re.findall(r"(?<![a-z0-9])" + re.escape(p) + r"(?![a-z0-9])", low))
        if count >= 2:
            chs.add(n)
    entries[disp] = (sorted(chs), {dch})

# cross-references for the most looked-up terms and for alternate spellings
SEE_ALSO = {
    "session": ["session binding", "session cookie", "session fixation", "absolute lifetime", "idle timeout"],
    "tile": ["signed URL", "watermark", "rasterize", "ceiling division"],
    "Angular": ["Angular CLI", "signal", "interceptor", "route guard", "template"],
    "token": ["signed URL", "CSRF token", "JWT", "capability URL", "trace code"],
    "CSRF (cross-site request forgery)": ["CSRF token", "double-submit cookie", "SameSite"],
    "Flyway": ["migration", "schema"],
    "JPA": ["Hibernate", "entity", "Spring Data JPA", "JPQL"],
    "Docker": ["container", "image", "Docker Compose", "Dockerfile"],
    "Spring Boot": ["starter", "auto-configuration", "bean", "dependency injection"],
    "cookie": ["session cookie", "SameSite", "double-submit cookie"],
    "test": ["unit test", "integration test", "end-to-end test", "flaky test"],
    "Maven": ["Maven wrapper", "Maven Central", "parent POM", "dependency"],
}
ALT = {
    "log in": "authentication", "login": "authentication", "sign in": "authentication",
    "back end": "backend", "front end": "frontend", "web server": "server",
    "Spring": "Spring Boot", "Bcrypt": "BCrypt", "docker-compose": "Docker Compose",
    "Node": "Node.js", "PR": "pull request", "SSL": "TLS", "hashing": "hash",
    "e-mail": "email", "JS": "JavaScript", "TS": "TypeScript", "DTO": "data transfer object",
    "IaC": "infrastructure as code", "S3 bucket": "bucket", "load balancer": "Application Load Balancer (ALB)",
}
name_of = {t.lower(): t for t in terms}

def resolve(name):
    """find the glossary term whose display name matches (case-insensitive, ignoring parentheses)"""
    n = name.lower()
    if n in name_of:
        return name_of[n]
    for t in terms:
        if re.sub(r"\s*\(.*?\)", "", t).lower() == n:
            return t
    return None

out = ["# Index", "",
       "Each entry lists the chapters where the term is defined (in bold) and where it is used. "
       "Consecutive chapters are written as ranges. “See” and “see also” point to related entries. "
       "The glossary (Appendix A) gives the definitions.", ""]
rows = defaultdict(list)
for disp, (chs, dset) in entries.items():
    line = "- %s: Chapter%s %s" % (disp, "s" if len(chs) > 1 else "", ", ".join(ranges(chs, dset)))
    see = [resolve(s) for s in SEE_ALSO.get(disp, [])]
    see = [s for s in see if s]
    if see:
        line += ". See also " + ", ".join(sorted(see, key=str.lower))
    rows[disp[0].upper() if disp[0].isalpha() else "#"].append((disp.lower(), line))
for alt, target in ALT.items():
    t = resolve(target)
    if t:
        rows[alt[0].upper()].append((alt.lower(), "- %s: see %s" % (alt, t)))
for L in sorted(rows):
    out.append("## " + L)
    out.append("")
    for _, line in sorted(rows[L]):
        out.append(line)
    out.append("")
open(OUT, "w", encoding="utf-8").write("\n".join(out))
print("entries:", len(entries), "+", len(ALT), "alternate spellings ->", OUT)
