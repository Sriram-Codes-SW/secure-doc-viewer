# Chapter 2 solutions

### Exercise 2.1 ★ Explore the repository

`cd src/main/resources`, then `cat application.yml`. The file is three levels below the project folder (`src`, `main`, `resources`).

### Exercise 2.2 ★ Set and use a variable

`export SCRATCH_SIZE=512` then `echo $SCRATCH_SIZE` prints 512. In a new window the print is empty, because the variable existed only in the first shell process.

### Exercise 2.3 ★★ Why is `.env` ignored?

The `.gitignore` line is `.env` under the "Local secrets" comment. Without it, `git add .` could commit the database password and signing key. Git history is permanent and shared, so the secrets would be exposed and would have to be replaced.

### Exercise 2.4 ★ Redirect and count

```bash
echo "tile one" > list.txt
echo "tile two" >> list.txt
echo "page three" >> list.txt
wc -l list.txt
grep "tile" list.txt
```

`wc -l` prints 3 (with the filename). `grep "tile"` prints the first two lines only. Using `>` for the first line and `>>` for the others matters: a second `>` would replace the file.

### Exercise 2.5 ★★ Who owns port 8080?

On macOS or Linux, `lsof -i :8080` shows a line for the Python process with its PID; on Windows (Git Bash or PowerShell), `netstat -ano | findstr :8080` shows the `LISTENING` line whose last column is the PID; in PowerShell, `Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess` shows the PID, and `Get-Process -Id <pid>` names the program. Press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the window running the server to stop it, and rerun the command: it prints nothing.

### Exercise 2.6 ★★★ Read the environment like the app does

With `DB_PORT=3307` in `.env`, the placeholder `${DB_PORT:3306}` resolves to 3307, so the URL becomes `jdbc:mysql://localhost:3307/securedocs?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true` (assuming the other variables are unset or default). If you also `export DB_PORT=3308`, the app connects to port 3308: the comment in `application.yml` says real environment variables win over the `.env` file. The reason is that the environment is the more specific, more deliberate source: whoever started this process chose it, whereas the file holds a machine's everyday defaults.
