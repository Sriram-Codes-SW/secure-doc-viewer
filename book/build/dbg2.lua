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
  local pre, stars, post = el.text:match('^(.-)(★+)(.*)$')
  if not stars then return nil end
  local n = utf8.len(stars)
  local label = STAR_WORDS[n] or (n .. ' stars')
  return {
    pandoc.Str(pre),
    pandoc.Span({ pandoc.Str(stars) }, pandoc.Attr('', {}, { { 'role', 'img' }, { 'aria-label', label } })),
    pandoc.Str(post),
  }
end

-- Inline code that is long enough to overflow a line: let LaTeX break it (PDF only)
local function to_path(el)
  local s = el.text
  if #s >= 22 and not s:find('[{}\\%%#~^%s\'"`$&]') then
    return pandoc.RawInline('latex', '\\path{' .. s .. '}')
  end
end

-- only in running text and table cells (not in headings, whose text is reused in the contents and bookmarks)
function Para(el)
  if not is_latex then return nil end
  return pandoc.walk_block(el, { Code = to_path })
end

function Plain(el)
  if not is_latex then return nil end
  return pandoc.walk_block(el, { Code = to_path })
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
  io.stderr:write('set_prop ncols=' .. ncols .. '
')
  if ncols < 2 then return end
  local maxlen, longest_word = {}, {}
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
  io.stderr:write('total=' .. total .. '
')
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
