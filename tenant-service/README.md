# tenant-service (placeholder)

Tenant Management Service — owns `scari` and `apartments`. Not implemented yet.

## Plug it in

1. Create a Spring Boot project here (listening on port **8082**) with a `Dockerfile`.
   Its endpoints must live under `/api/tenants/**` (e.g. `GET /api/tenants/health`).
2. In the root `docker-compose.yml`, uncomment the `tenant-service` block.
3. Nothing to change in the gateway: it already routes `/api/tenants/**` to
   `TENANT_SERVICE_URL` (default `http://tenant-service:8082`) and enforces **ADMIN only**.
4. Tables already exist via Liquibase (`liquibase/changelog`); use `ddl-auto: none`.
   DB env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
5. The gateway validates the JWT; you can trust the forwarded request, or re-validate using the shared `JWT_SECRET`.

Until it is deployed the gateway answers `503 Downstream microservice is not yet deployed`.
