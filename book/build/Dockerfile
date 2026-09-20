# Pandoc + LuaLaTeX with real fonts and the LaTeX packages the book layout needs (including tagged-PDF support).
# Build once:  docker build -t sdv-book-pandoc book/build
FROM pandoc/extra:latest
RUN apk add --no-cache fontconfig font-dejavu font-liberation \
 && fc-cache -f
# tlmgr talks to a CTAN mirror (signatures are verified). It must be updated before it installs anything,
# and mirrors are sometimes briefly out of sync, so use one fixed mirror and retry.
# "update --all" keeps the tagged-PDF packages (a fast-moving area) consistent with each other.
RUN tlmgr option repository https://ctan.math.illinois.edu/systems/texlive/tlnet \
 && for i in 1 2 3 4 5; do \
      tlmgr update --self \
      && tlmgr install newunicodechar tex-gyre tagpdf latex-lab pdfmanagement luaotfload luamml \
      && tlmgr update --all \
      && exit 0; \
      sleep 15; \
    done; exit 1
