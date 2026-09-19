<!-- chapter: 29 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 29

### Exercise 29.1 ★ Idle state

`{ kind: 'active' }`. The timeout is 1,800 seconds and 600 remain. The warning window is min(300, 1800 / 2) = 300 seconds, and 600 is more than 300.

### Exercise 29.2 ★★ Keys while typing

Arrow keys move the text cursor in a field. If the viewer also turned pages, typing would be impossible. The shortcuts are ignored while typing and with Ctrl, Cmd or Alt held.

### Exercise 29.3 ★★★ Clamped spacing

The constructor uses `Math.min(6.0, spacing)`, so 20 becomes 6.0. A bad setting can't make the mark absurdly sparse or dense; opacity is clamped to 0.05 to 0.6 the same way.

