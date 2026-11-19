# Distributed Maintenance Management System

Microservices: `auth-service` (8081), `api-gateway` (8080), `frontend` (3000), PostgreSQL 16 + Liquibase.
`tenant-service` (8082) and `utility-service` (8083) are placeholders with plug-in instructions in their READMEs.

## Run

```bash
cp .env.example .env   # optional; defaults are built into docker-compose.yml
docker compose up --build
```

Open http://localhost:3000.

| User | Password | Role |
|------|----------|------|
| admin@asociatie.ro | Admin123! | ADMIN |
| locatar@asociatie.ro | Locatar123! | TENANT |

There is no registration endpoint; users come from the Liquibase seed (`liquibase/changelog/002-seed-initial-data.yaml`).

## Behaviour

- `POST /api/auth/login` returns `{token,email,role}` and sets the `jwt_token` cookie. `POST /api/auth/logout` clears it.
- Gateway: `/api/auth/**` public; `/api/tenants/**` ADMIN only (403 for TENANT); `/api/utilities/**` ADMIN + TENANT.
  Missing/invalid JWT → 401 JSON for `/api/**`; browser HTML navigation to other protected paths → 302 to `FRONTEND_URL/login`.
- Until tenant/utility containers exist, authorized calls return `503 {"message":"Downstream microservice is not yet deployed"}`.

```bash
curl -i -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@asociatie.ro","password":"Admin123!"}'
```

## Notes

- `JWT_SECRET` (≥ 32 chars) is shared by auth-service and gateway. **Change it outside local development.**
- The cookie is intentionally not HttpOnly because the SPA also writes and reads it, as specified.
- Cookies are scoped per host, not port, so `localhost:3000` and `localhost:8080` share it.