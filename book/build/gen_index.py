"""Generate appendices/index-terms.md: a back-of-the-book index (term, then chapters).

Terms come from the glossary (GLOSSARY.md) plus every bolded term in the chapters. For each term the index
lists the chapters where it is defined (the first bold use, shown in bold) and where it appears in the
prose. A term that appears in more than 10 chapters ends with "and others". Usage: python gen_index.py [output]
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
    text[n] = t

def norm(t):
    return re.sub(r"\s*\(.*?\)", "", t).strip()

terms = {}          # display -> set of search phrases
for l in open(os.path.join(BOOK, "GLOSSARY.md"), encoding="utf-8"):
    m = re.match(r"^\| \*\*(.+?)\*\* \|", l)
    if m:
        disp = m.group(1)
        terms[disp] = {norm(disp).lower(), disp.lower()}
SKIP = re.compile(r"[.:?!]$|^(Note|Tip|Warning|In this project|Listing|Example|Figure|Table|Exercise|Where|The |Step|Chapter|Beginner|Intermediate|Advanced|Level)")
defined = defaultdict(set)   # display -> chapters where bold
for n, t in text.items():
    for m in re.finditer(r"\*\*([^*\n]{2,45})\*\*", t):
        b = m.group(1).strip()
        if SKIP.search(b) or len(b.split()) > 4 or re.search(r"\d|`", b):
            continue
        key = None
        for disp in terms:
            if norm(disp).lower() == norm(b).lower():
                key = disp
                break
        if key is None:
            continue
        defined[key].add(n)

entries = {}
for disp, phrases in terms.items():
    chapters = []
    for n in sorted(text):
        low = text[n].lower()
        count = 0
        for p in phrases:
            if len(p) < 3:
                continue
            count += len(re.findall(r"(?<![a-z0-9])" + re.escape(p) + r"(?![a-z0-9])", low))
        # a chapter is listed when the term is defined there or is used at least twice
        if count >= 2 or n in defined.get(disp, ()):
            chapters.append(n)
    if chapters:
        entries[disp] = chapters

out = ["# Index", "",
       "Terms defined in this book, with the chapters where each is defined (in bold) and where it is used. "
       "The glossary (Appendix A) gives the definitions.", ""]
letters = defaultdict(list)
for disp in entries:
    first = disp[0].upper()
    letters[first if first.isalpha() else "#"].append(disp)
for L in sorted(letters):
    out.append("## " + L)
    out.append("")
    for disp in sorted(letters[L], key=lambda s: s.lower()):
        chs = entries[disp]
        d = defined.get(disp, set())
        shown = chs[:10]
        parts = [("**%d**" % c) if c in d else str(c) for c in shown]
        line = "- %s: Chapter%s %s" % (disp, "s" if len(chs) > 1 else "", ", ".join(parts))
        if len(chs) > 10:
            line += ", and others"
        out.append(line)
    out.append("")
open(OUT, "w", encoding="utf-8").write("\n".join(out))
print("entries:", len(entries), "->", OUT)
