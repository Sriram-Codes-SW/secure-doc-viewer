# Pandoc + XeLaTeX with real fonts and the LaTeX packages the book layout needs.
# Build once:  docker build -t sdv-book-pandoc book/build
FROM pandoc/extra:latest
RUN apk add --no-cache fontconfig font-dejavu font-liberation \
 && fc-cache -f
RUN tlmgr install newunicodechar tex-gyre \
 || (tlmgr update --self && tlmgr install newunicodechar tex-gyre)
