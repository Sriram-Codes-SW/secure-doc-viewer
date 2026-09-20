# Chapter 10 solutions

### Exercise 10.1 ★ Image or container?

`mysql:8.4` is an image. `securedocs-mysql` is a container (its `container_name`). `mysql-data` is a volume.

### Exercise 10.2 ★ Start the database

`docker compose ps` shows the `mysql` service as `healthy` after the health check passes. The data is in the named volume `mysql-data`, which `docker compose down` doesn't remove, so a new container started from the same file reattaches it. (`down -v` would delete it.)

### Exercise 10.3 ★★ Read the ports

`"3306:3306"` would publish the database on every network interface of your computer, so other machines on the network could try to connect. The `127.0.0.1` prefix limits it to your own computer, in line with the file's comment: "the database is never exposed to the network."

### Exercise 10.4 ★★ Compare two health checks

Differences: (1) `test` uses `CMD` with a list of arguments in Listing 10.1 but `CMD-SHELL` with one string in Listing 10.2, because the final version needs a shell to set an environment variable for the command; (2) Listing 10.1 passes the password as an argument, `-p${DB_ROOT_PASSWORD}`, while Listing 10.2 sets `MYSQL_PWD` and refers to `$$MYSQL_ROOT_PASSWORD`, expanded inside the container; (3) Listing 10.2 adds `--silent`. The final version is safer because the password no longer appears in the command that `docker inspect` stores, or in the process's argument list, where other users of the machine could read it.

### Exercise 10.5 ★★ Trace a dependency

The lines are `depends_on:`, `mysql:` and `condition: service_healthy` under the `app` service. Without them, on a slow computer Compose would start the backend at the same time as MySQL; the backend would try to connect before MySQL accepts connections, fail at startup (its database migrations need the connection), and, with `restart: unless-stopped`, keep restarting until MySQL happened to be ready.

### Exercise 10.6 ★★★ Reorder the Dockerfile

Before the move: changing one Java file changes the layer created by `COPY src src`, so only that layer and those after it (the `package` build) run again; the layer that downloaded dependencies is reused from the cache, because `pom.xml` did not change. After the move: `COPY src src` now comes before the download step, so a code change invalidates the cache from that point on, and the dependency download runs again every time. The original order is faster: downloads dominate the build time, and they should repeat only when `pom.xml` changes.
