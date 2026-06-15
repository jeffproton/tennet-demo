# tennet-demo — Hello World Backstage Template

A Backstage software template that scaffolds a minimal "Hello {name}" app and deploys it to a local Kind cluster via ArgoCD.

## Stack

| Layer     | Technology              |
|-----------|-------------------------|
| Frontend  | React 18 + Vite + Nginx |
| Backend   | Java 21 + Spring Boot 3 |
| Database  | PostgreSQL 16           |
| Cache     | Redis 7                 |
| CI        | GitHub Actions          |
| Deploy    | Kind + ArgoCD           |

## How it works

1. On startup the Spring Boot backend seeds the `greeting` table in Postgres with the `GREETING_NAME` env var (set by Backstage at template instantiation).
2. The `/api/greeting` endpoint returns `{"message": "Hello {name}"}`, checking Redis first (via `@Cacheable`) and falling back to Postgres.
3. The React frontend fetches `/api/greeting` and displays the message.
4. Nginx proxies `/api/` to the backend service inside the same Kubernetes namespace.

## Using the template in Backstage

### Register the template

In your Backstage `app-config.yaml`, add this repo as a catalog location:

```yaml
catalog:
  locations:
    - type: url
      target: https://github.com/YOUR_ORG/tennet-demo/blob/main/catalog-info.yaml
```

Then navigate to **Create → Hello World App** and fill in:
- **Name** — the string shown in "Hello {name}"
- **GitHub Owner** — your GitHub username or org
- **Repository Name** — the new repo to create

Backstage will scaffold the repo, push it to GitHub, and register it in the catalog.

### Run the pipeline

Push to `main` triggers the GitHub Actions workflow which:
1. Builds and pushes `frontend` and `backend` images to GHCR.
2. Updates `k8s/frontend/deployment.yaml` and `k8s/backend/deployment.yaml` with the correct image paths.

### Deploy with ArgoCD

Apply the ArgoCD Application after updating the `repoURL` with your repo:

```bash
# Update repoURL in skeleton/k8s/argocd/application.yaml, then:
kubectl apply -f k8s/argocd/application.yaml
```

ArgoCD will sync all manifests under `k8s/` and create the `hello-world` namespace automatically.

### Access the app

```bash
# Port-forward (or use the NodePort 30080 on your Kind node IP)
kubectl port-forward -n hello-world svc/frontend 8080:80
open http://localhost:8080
```

You should see **Hello {name}**.

## Local development

### Backend

```bash
cd backend
docker run -d -p 5432:5432 -e POSTGRES_DB=hello -e POSTGRES_USER=hello -e POSTGRES_PASSWORD=hello postgres:16-alpine
docker run -d -p 6379:6379 redis:7-alpine
GREETING_NAME=World mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev   # proxies /api to localhost:8080
```

## Repository layout

```
tennet-demo/
├── catalog-info.yaml          # Registers this repo in Backstage
├── template.yaml              # Backstage template definition
└── skeleton/                  # Files copied into the generated repo
    ├── .github/workflows/ci.yaml
    ├── catalog-info.yaml
    ├── frontend/              # React + Vite + Nginx
    ├── backend/               # Spring Boot 3
    └── k8s/
        ├── namespace.yaml
        ├── postgres/
        ├── redis/
        ├── backend/
        ├── frontend/
        └── argocd/
```
