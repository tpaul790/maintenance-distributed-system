# utility-service (placeholder)

Utility & Billing Service — owns `index_logs` and `bills`. Not implemented yet.

## Plug it in

1. Create a Spring Boot project here (listening on port **8083**) with a `Dockerfile`.
   Its endpoints must live under `/api/utilities/**` (e.g. `GET /api/utilities/health`).
2. In the root `docker-compose.yml`, uncomment the `utility-service` block.
3. Nothing to change in the gateway: it already routes `/api/utilities/**` to
   `UTILITY_SERVICE_URL` (default `http://utility-service:8083`) and allows **ADMIN and TENANT**.
4. Tables already exist via Liquibase (`liquibase/changelog`); use `ddl-auto: none`.
   DB env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
5. TENANT users should only see their own data: use the `userId` / `sub` claims of the JWT
   (shared `JWT_SECRET`) to scope queries via `apartments.user_id`.

Until it is deployed the gateway answers `503 Downstream microservice is not yet deployed`.
