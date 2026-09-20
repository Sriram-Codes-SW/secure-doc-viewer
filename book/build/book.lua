-- Pandoc Lua filter for the book build (used by the PDF, EPUB and HTML builds).
--   all formats:  cross-reference links that point at .md source files become plain text
--   html / epub:  table captions become real <caption> elements; the ★ level marker gets a text label
--   latex (pdf):  wide tables get proportional column widths (no clipping); long inline code may wrap;
--                 design hooks for header.tex (heading kinds, captions kept with their tables, listings and
--                 figures, callout boxes, part openers, roman/arabic numbering, two-column index).
--                 Nothing here changes the words of the book: it only adds raw LaTeX around existing blocks.

local stringify = pandoc.utils.stringify
local is_web = FORMAT:match('html') or FORMAT:match('epub')
local is_latex = FORMAT:match('latex')

local STAR_WORDS = { 'one star', 'two stars', 'three stars', 'four stars', 'five stars' }

function Link(el)
  local t = el.target
  if t:match('%.md$') or t:match('%.md#') then
    return el.content
  end
end

-- ★ runs get an accessible name for screen readers (HTML and EPUB only)
function Str(el)
  if not is_web then return nil end
  -- (a Lua pattern like '★+' would repeat only the last BYTE of the character, so walk the run by hand)
  local s = el.text
  local first = s:find('★', 1, true)
  if not first then return nil end
  local pos, n = first, 0
  while s:sub(pos, pos + 2) == '★' do
    n = n + 1
    pos = pos + 3
  end
  local pre, stars, post = s:sub(1, first - 1), s:sub(first, pos - 1), s:sub(pos)
  local label = STAR_WORDS[n] or (n .. ' stars')
  return {
    pandoc.Str(pre),
    pandoc.Span({ pandoc.Str(stars) }, pandoc.Attr('', {}, { { 'role', 'img' }, { 'aria-label', label } })),
    pandoc.Str(post),
  }
end

-- Long inline code is set as \texttt with break points, so it can wrap instead of running off the page (PDF only).
local BS = string.char(92)          -- a backslash
local AB = BS .. 'allowbreak{}'     -- a place where a line may break
local ESC = {
  [BS] = BS .. 'textbackslash{}',
  ['{'] = BS .. '{', ['}'] = BS .. '}', ['$'] = BS .. '$', ['#'] = BS .. '#', ['%'] = BS .. '%',
  ['^'] = BS .. '^{}', ['~'] = BS .. 'textasciitilde{}', ['<'] = BS .. 'textless{}', ['>'] = BS .. 'textgreater{}',
  ['|'] = BS .. 'textbar{}', ["'"] = BS .. 'textquotesingle{}', ['"'] = BS .. 'textquotedbl{}',
  ['`'] = BS .. 'textasciigrave{}', [' '] = BS .. ' ',
  ['&'] = BS .. '&' .. AB, ['_'] = BS .. '_' .. AB, ['-'] = '-' .. AB, ['.'] = '.' .. AB,
  [','] = ',' .. AB, ['/'] = '/' .. AB, ['='] = '=' .. AB, ['('] = '(' .. AB, [')'] = ')' .. AB,
  [':'] = ':' .. AB, [';'] = ';' .. AB, ['@'] = '@' .. AB, ['?'] = '?' .. AB, ['+'] = '+' .. AB,
}

local function to_code(el)
  local s = el.text
  if #s < 14 or s:find(string.char(10)) then return nil end
  local out, prev = {}, ''
  for ch in s:gmatch(utf8.charpattern) do
    if ch:match('^%u$') and prev:match('^%l$') then out[#out + 1] = AB end
    out[#out + 1] = ESC[ch] or ch
    prev = ch
  end
  return pandoc.RawInline('latex', BS .. 'texttt{' .. table.concat(out) .. '}')
end

local function cell_len(cell)
  local n = 0
  for _, b in ipairs(cell.contents) do
    n = n + #stringify(b)
  end
  return n
end

local function set_proportional_widths(tbl)
  local ncols = #tbl.colspecs
  if ncols < 2 then return end
  local maxlen = {}
  for j = 1, ncols do maxlen[j] = 0 end
  local function scan(rows)
    for _, row in ipairs(rows) do
      for j, cell in ipairs(row.cells) do
        local l = cell_len(cell)
        if l > (maxlen[j] or 0) then maxlen[j] = l end
      end
    end
  end
  scan(tbl.head.rows)
  for _, body in ipairs(tbl.bodies) do scan(body.body) end
  local total = 0
  for j = 1, ncols do total = total + maxlen[j] end
  -- the glossary (Appendix A): a narrow term column, a wide definition, a narrow chapter column
  if ncols == 3 and #tbl.head.rows > 0 then
    local h = tbl.head.rows[1].cells
    if stringify(h[1].contents) == 'Term' and stringify(h[3].contents) == 'First defined' then
      tbl.colspecs = { { tbl.colspecs[1][1], 0.25 }, { tbl.colspecs[2][1], 0.62 }, { tbl.colspecs[3][1], 0.09 } }
      return
    end
  end
  if total <= 60 then return end            -- fits on one line: leave the automatic layout
  local weights, sum = {}, 0
  for j = 1, ncols do
    local w = math.max(8, math.min(maxlen[j], 70))
    weights[j] = w
    sum = sum + w
  end
  local specs = {}
  for j = 1, ncols do
    specs[j] = { tbl.colspecs[j][1], (weights[j] / sum) * 0.96 }
  end
  tbl.colspecs = specs
end

local function is_table_label(b)
  if b.t ~= 'Para' then return false end
  local first = b.content[1]
  if first == nil then return false end
  if (first.t == 'Strong' or first.t == 'Emph') and stringify(first):match('^Table [%w%.]+') then
    return #b.content == 1
  end
  return false
end


-- ---- PDF design hooks (raw LaTeX around existing blocks; the words are never changed) ----------------
local function raw(s) return pandoc.RawBlock('latex', s) end

local function latex_escape(s)
  local map = { ['\\'] = '\\textbackslash{}', ['{'] = '\\{', ['}'] = '\\}', ['$'] = '\\$', ['&'] = '\\&',
                ['#'] = '\\#', ['^'] = '\\^{}', ['_'] = '\\_', ['%'] = '\\%', ['~'] = '\\textasciitilde{}' }
  return (s:gsub('[\\{}$&#^_%%~]', map))
end

-- Which kind of heading is this? header.tex styles each kind (see \sdvkind); some kinds also get a box.
local function header_kind(h)
  local t = stringify(h)
  if h.level == 2 then
    if t:match('^Beginner tier') or t:match('^Intermediate tier') or t:match('^Advanced tier') then return 'tier' end
    if t == 'Learning objectives' or t == 'Prerequisites' then return 'front' end
    if t == 'In this project' or t == 'Try it' or t == 'Summary' or t == 'Further reading' then return 'end' end
    if t:match('^%u$') then return 'letter' end
  elseif h.level == 3 then
    if t:match('^%d+%.%d+ Common mistakes') then return 'mistakes' end
  elseif h.level == 4 then
    if t:match('^A real incident') then return 'incident' end
  end
  return nil
end

local function header_boxed(h)
  local t = stringify(h)
  if h.level == 2 then return t == 'Learning objectives' or t == 'Summary' end
  if h.level == 3 then return t:match('^%d+%.%d+ Common mistakes') ~= nil end
  if h.level == 4 then return t:match('^A real incident') ~= nil end
  return false
end

-- What kind of paragraph is this (a caption, a text description, a path line, a figure)?
local function para_kind(b)
  if b.t ~= 'Para' or #b.content == 0 then return nil end
  local first = b.content[1]
  local only = #b.content == 1
  local text = stringify(b)
  if only and first.t == 'Strong' then
    if text:match('^Table [%w%.]+') then return 'table-cap' end
    if text:match('^Listing [%w%.]+') then return 'listing-cap' end
  elseif only and first.t == 'Emph' then
    if text:match('^Figure [%w%.]+') then return 'figure-cap' end
    if text:match('^Path:') then return 'path' end
  elseif only and first.t == 'Image' then
    return 'image'
  elseif first.t == 'Emph' and stringify(first):match('^Text description') then
    return 'desc'
  end
  return nil
end

local function has_table(list)
  for _, b in ipairs(list) do
    if b.t == 'Table' then return true end
  end
  return false
end

local function bold_head(tbl)
  for _, row in ipairs(tbl.head.rows) do
    for _, cell in ipairs(row.cells) do
      for _, blk in ipairs(cell.contents) do
        if (blk.t == 'Plain' or blk.t == 'Para') and #blk.content > 0 and blk.content[1].t ~= 'Strong' then
          blk.content = { pandoc.Strong(blk.content) }
        end
      end
    end
  end
end

local function process(blocks)
  local out = {}
  local skip = false
  for i, b in ipairs(blocks) do
    if skip then
      skip = false
    elseif b.t == 'Table' then
      if is_latex then set_proportional_widths(b) end
      if is_web and #b.caption.long == 0 then
        -- caption written above the table (house style) or below it
        local prev = out[#out]
        local nxt = blocks[i + 1]
        if prev and is_table_label(prev) then
          out[#out] = nil
          b.caption.long = { pandoc.Plain(prev.content[1].content) }
        elseif nxt and is_table_label(nxt) then
          b.caption.long = { pandoc.Plain(nxt.content[1].content) }
          skip = true
        end
      end
      if is_latex then
        bold_head(b)
        out[#out + 1] = raw('\\begingroup\\sdvtablestyle')
        out[#out + 1] = b
        out[#out + 1] = raw('\\endgroup')
      else
        out[#out + 1] = b
      end
    elseif is_latex and b.t == 'Para' and para_kind(b) then
      local k = para_kind(b)
      local inner = b.content[1].content
      if k == 'table-cap' then
        out[#out + 1] = raw('\\Needspace{7\\baselineskip}\\begingroup\\sdvcapstyle')
        out[#out + 1] = pandoc.Para(inner)
        out[#out + 1] = raw('\\endgroup\\nopagebreak[4]')
      elseif k == 'listing-cap' then
        out[#out + 1] = raw('\\Needspace{6\\baselineskip}\\begingroup\\sdvcapstyle')
        out[#out + 1] = pandoc.Para(inner)
        out[#out + 1] = raw('\\endgroup\\nopagebreak[4]')
      elseif k == 'image' then
        out[#out + 1] = raw('\\Needspace{14\\baselineskip}\\begingroup\\centering')
        out[#out + 1] = b
        out[#out + 1] = raw('\\endgroup\\nopagebreak[4]\\vspace{0.2\\baselineskip}')
      elseif k == 'figure-cap' then
        out[#out + 1] = raw('\\begingroup\\sdvfigstyle')
        out[#out + 1] = pandoc.Para(inner)
        out[#out + 1] = raw('\\endgroup\\nopagebreak[3]')
      elseif k == 'desc' then
        out[#out + 1] = raw('\\begingroup\\sdvdescstyle')
        out[#out + 1] = b
        out[#out + 1] = raw('\\endgroup\\vspace{0.3\\baselineskip}')
      elseif k == 'path' then
        out[#out + 1] = raw('\\nopagebreak[4]\\vspace{-0.4\\baselineskip}\\begingroup\\sdvpathstyle')
        out[#out + 1] = pandoc.Para(inner)
        out[#out + 1] = raw('\\endgroup')
      end
    else
      out[#out + 1] = b
    end
  end
  return out
end

function Blocks(blocks)
  if not is_latex then return process(blocks) end
  local out = {}
  local i = 1
  while i <= #blocks do
    local b = blocks[i]
    if b.t == 'Header' then
      local kind = header_kind(b)
      if kind then out[#out + 1] = raw('\\sdvkind{' .. kind .. '}') end
      out[#out + 1] = b
      i = i + 1
      if header_boxed(b) then
        -- the section that follows (up to the next heading) goes into a tinted callout box,
        -- unless it holds a table (a long table cannot sit inside a box)
        local seg = {}
        while i <= #blocks and blocks[i].t ~= 'Header' do
          seg[#seg + 1] = blocks[i]
          i = i + 1
        end
        local done = process(seg)
        if #done > 0 and not has_table(seg) then
          out[#out + 1] = raw('\\begin{sdvcallout}')
          for _, x in ipairs(done) do out[#out + 1] = x end
          out[#out + 1] = raw('\\end{sdvcallout}')
        else
          for _, x in ipairs(done) do out[#out + 1] = x end
        end
      end
    else
      local j = i
      local seg = {}
      while j <= #blocks and blocks[j].t ~= 'Header' do
        seg[#seg + 1] = blocks[j]
        j = j + 1
      end
      for _, x in ipairs(process(seg)) do out[#out + 1] = x end
      i = j
    end
  end
  return out
end

-- Applied last (after the table widths have been measured from the plain code text), and only in running
-- text and table cells, not in headings, whose text is reused in the contents and the bookmarks.
function Pandoc(doc)
  if not is_latex then return nil end
  local function conv(el) return pandoc.walk_block(el, { Code = to_code }) end
  doc.blocks = pandoc.walk_block(pandoc.Div(doc.blocks), { Para = conv, Plain = conv }).content

  -- title-page values from metadata.yaml, as LaTeX macros for the front matter in header.tex
  local defs = {}
  for _, k in ipairs({ { 'edition', 'sdvedition' }, { 'licence', 'sdvlicence' }, { 'code-version', 'sdvcodeversion' },
                       { 'book-version', 'sdvbookversion' } }) do
    local v = doc.meta[k[1]]
    if v then defs[#defs + 1] = '\\gdef\\' .. k[2] .. '{' .. latex_escape(stringify(v)) .. '}' end
  end
  -- (include-in-header is added by Pandoc after the filters, so the values go in include-before, which is
  -- typeset right after \maketitle; header.tex makes \maketitle empty and \sdvtitlepages the title pages)
  do
    local list = {}
    local ib = doc.meta['include-before']
    if ib then
      if ib.t == 'MetaList' then for _, x in ipairs(ib) do list[#list + 1] = x end else list[1] = ib end
    end
    list[#list + 1] = pandoc.MetaBlocks({ raw(table.concat(defs, string.char(10)) .. string.char(10) .. string.char(92) .. 'sdvtitlepages') })
    doc.meta['include-before'] = pandoc.MetaList(list)
  end

  -- part openers (with a list of their chapters), the switch to arabic page numbers, the two-column index
  local blocks = doc.blocks
  local out = {}
  local main_done, in_index, index_open = false, false, false
  for i, b in ipairs(blocks) do
    if b.t == 'RawBlock' and b.text == '\\sdvkind{letter}' and in_index and not index_open then
      out[#out + 1] = raw('\\begin{multicols}{2}\\sdvindexstyle')
      index_open = true
    end
    if b.t == 'Header' and b.level == 1 then
      local t = stringify(b)
      local is_part = t:match('^Part [IVX]+:') ~= nil
      local is_chapter = t:match('^Chapter %d+:') ~= nil
      in_index = (t == 'Index')
      if (is_part or is_chapter) and not main_done then
        out[#out + 1] = raw('\\sdvmainmatter')
        main_done = true
      end
      if is_part then
        out[#out + 1] = raw('\\cleardoublepage\\addtocontents{toc}{\\protect\\sdvnextpart}')
        out[#out + 1] = b
        local entries = {}
        for j = i + 1, #blocks do
          local h = blocks[j]
          if h.t == 'Header' and h.level == 1 then
            local ht = stringify(h)
            if ht:match('^Part [IVX]+:') then break end
            local num, title = ht:match('^Chapter (%d+): (.*)$')
            if num and h.identifier ~= '' then
              entries[#entries + 1] = '\\sdvpartentry{Chapter ' .. num .. '}{' .. latex_escape(title) .. '}{' ..
                  h.identifier .. '}'
            end
          end
        end
        if #entries > 0 then
          out[#out + 1] = raw('\\begingroup\\sffamily\\fontsize{11}{14}\\selectfont\n' .. table.concat(entries, '\n') ..
              '\n\\endgroup')
        end
        out[#out + 1] = raw('\\thispagestyle{empty}\\clearpage')
      else
        out[#out + 1] = b
      end
    else
      out[#out + 1] = b
    end
  end
  if index_open then out[#out + 1] = raw('\\end{multicols}') end
  doc.blocks = out
  return doc
end
