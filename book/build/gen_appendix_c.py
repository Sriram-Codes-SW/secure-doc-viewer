"""Generate appendices/appendix-c-exercise-solutions.md from the per-chapter *.solutions.md files.

Heading structure of the output: one H1 ("Appendix C"), one H2 per chapter ("Chapter N solutions"),
one H3 per exercise. The solutions files' own H1 and any "solutions" H2 are dropped; other H2 lines are
demoted to H3; lines inside code fences are never touched. Usage: python gen_appendix_c.py [output]
"""
import glob
import os
import re
import sys

BOOK = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(BOOK, "appendices", "appendix-c-exercise-solutions.md")

files = {}
for pat in ("part-*/*.solutions.md", "tradeoffs/*.solutions.md"):
    for f in glob.glob(os.path.join(BOOK, pat)):
        n = int(os.path.basename(f)[:2])
        files[n] = f

out = [
    "# Appendix C: Exercise solutions",
    "",
    "Try each exercise before you look. Solutions are grouped by chapter. Each solution repeats the exercise's",
    "title so you can find it; chapters without a section have no solutions written.",
    "",
]
for n in sorted(files):
    out.append("## Chapter %d solutions" % n)
    out.append("")
    infence = False
    body = []
    for line in open(files[n], encoding="utf-8").read().splitlines():
        if line.lstrip().startswith("```"):
            infence = not infence
            body.append(line)
            continue
        if infence:
            body.append(line)
            continue
        if line.startswith("<!--"):
            continue
        if re.match(r"^# ", line):
            continue
        if re.match(r"^## .*[Ss]olutions?\b", line):
            continue
        if re.match(r"^## ", line):
            line = "#" + line
        body.append(line)
    text = "\n".join(body).strip("\n")
    out.append(text)
    out.append("")

open(OUT, "w", encoding="utf-8").write("\n".join(out))
print("chapters:", len(files), "->", OUT)
