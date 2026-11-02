# BrightPath

A deliberately tiny course-enrollment web app used to practice **infrastructure,
deployment and upgrade** design. The app itself is not the point; it exists to
give you a WAR, a container image, a database migration and a version number
that changes between releases.

Stack: Java 17 servlets + JSP → **Tomcat 10.1**, fronted by **Apache httpd**
(`mod_proxy`), backed by **MySQL 8.4**, schema managed by **Flyway**.

## Layout

```
src/main/java/com/brightpath/   servlets + env-based config
src/main/webapp/                JSP page
db/migration/                   Flyway SQL (V1, V2, V3)
Dockerfile                      app image (multi-stage Maven build -> Tomcat)
Dockerfile.migrate              migrations image (one-off job, runs BEFORE deploy)
httpd/                          Apache reverse-proxy config + image
docker-compose.yml              local stack: mysql -> migrate -> tomcat -> httpd
```

## Run locally

```bash
docker compose up -d --build
open http://localhost:8081        # through Apache
curl localhost:8080/version       # Tomcat directly
docker compose down -v            # stop and wipe the DB
```

MySQL is published on host port **3307** (3306 is commonly taken).

## Endpoints

| Path | Purpose |
|---|---|
| `/courses` | the UI: list courses, add a course, enroll a student |
| `/health` | liveness, no DB call. Use for ALB / target group health checks |
| `/health/db` | readiness, runs `SELECT 1`. Use in post-deploy smoke tests |
| `/version` | `{"version","gitSha","customer"}`. Confirms what is live, and where |

## Configuration (one artifact, every customer)

Read from env vars, falling back to JVM system properties (`-DNAME=...` in
Tomcat's `setenv.sh` on a plain EC2 install).

| Name | Example |
|---|---|
| `CUSTOMER_NAME` | `acme` |
| `DB_URL` | `jdbc:mysql://acme-db.xxxx.rds.amazonaws.com:3306/brightpath` |
| `DB_USER` / `DB_PASSWORD` | from Secrets Manager / SSM in AWS |
| `GIT_SHA` | baked in at image build: `--build-arg GIT_SHA=$GITHUB_SHA` |

Migrations image takes `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`.

## Deployment contract

1. Build **once** per commit: the app image (or `target/brightpath.war`) plus the migrations image, both tagged with the git SHA.
2. For each customer: **run migrations first** (one-off task/job), then roll out the app.
3. Verify: `/health/db` returns 200 and `/version` shows the expected `gitSha`.
4. Schema changes must be **backward compatible** with the version still
   running (expand/contract). `V3` is an example of an expand step.

The app never migrates its own schema at startup. With N tasks starting at
once, that races and can lock tables mid-rollout.

## Exercises (you write these; keep infra in a separate `brightpath-infra` repo)

1. **CI**: a GitHub Actions workflow that builds and tests, then pushes both images to ECR via OIDC (no stored keys).
2. **CD to ECS**: run the migrations task, deploy the new task definition, wait for stability, smoke-test `/version`. Add a prod environment with required reviewers.
3. **Per-customer rollout**: a matrix over a customers list, in waves (canary customer first), with a concurrency group per customer.
4. **Terraform (`brightpath-infra`)**: VPC, ALB with host-based routing per customer, ECS cluster and service per customer, RDS MySQL, Secrets Manager. Use `ignore_changes = [task_definition]` so the pipeline owns releases.
5. **EC2 path**: a Packer template that bakes Java + Tomcat + httpd + the WAR into an AMI, then an ASG instance refresh to upgrade.
6. **EKS path**: a Helm chart with a pre-upgrade migration Job and per-customer values files.
7. **Expand/contract drill**: rename `title` → `name` across three releases with zero downtime.
