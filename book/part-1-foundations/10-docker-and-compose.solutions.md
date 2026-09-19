# Chapter 10 solutions

### Exercise 10.1 ★ Image or container?

`mysql:8.4` is an image. `securedocs-mysql` is a container (its `container_name`). `mysql-data` is a volume.

### Exercise 10.2 ★ Start the database

`docker compose ps` shows the `mysql` service as `healthy` after the health check passes. The data is in the named volume `mysql-data`, which `docker compose down` doesn't remove, so a new container started from the same file reattaches it. (`down -v` would delete it.)

### Exercise 10.3 ★★ Read the ports

`"3306:3306"` would publish the database on every network interface of your computer, so other machines on the network could try to connect. The `127.0.0.1` prefix limits it to your own computer, in line with the file's comment: "the database is never exposed to the network."
