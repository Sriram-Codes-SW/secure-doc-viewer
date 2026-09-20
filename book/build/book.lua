-- Pandoc Lua filter for the book build (used by the PDF, EPUB and HTML builds).
--   all formats:  cross-reference links that point at .md source files become plain text
--   html / epub:  table captions become real <caption> elements; the ★ level marker gets a text label
--   latex (pdf):  wide tables get proportional column widths (no clipping); long inline code may wrap

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

-- Applied last (after the table widths have been measured from the plain code text), and only in running
-- text and table cells, not in headings, whose text is reused in the contents and the bookmarks.
function Pandoc(doc)
  if not is_latex then return nil end
  local function conv(el) return pandoc.walk_block(el, { Code = to_code }) end
  doc.blocks = pandoc.walk_block(pandoc.Div(doc.blocks), { Para = conv, Plain = conv }).content
  return doc
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

function Blocks(blocks)
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
      out[#out + 1] = b
    else
      out[#out + 1] = b
    end
  end
  return out
end
