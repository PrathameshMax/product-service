# product-service

Static CRUD Spring Boot service (Java 21, Spring Boot 3.5) used to practise deploying to AWS ECS Fargate with Terraform and GitHub Actions.

```
GitHub Actions ──OIDC──► IAM (gha-plan / gha-deploy)
   ci  : test → docker build → Trivy → smoke test → terraform fmt/validate → lab plan (PRs)
   cd  : test → push ECR (git SHA tag) → deploy to ECS if lab is up → smoke test
   lab-up / lab-down : terraform apply / destroy infra/lab (lab-down also nightly 23:30 IST)

https://api.dev.nexacommerce.in ─► Route 53 ─► ALB (ACM TLS) ─► ECS Fargate Spot (2 AZs)
                                               logs/metrics/alarms ─► CloudWatch ─► SNS email
```

## Layout

| Path | Purpose |
|---|---|
| `src/` | Spring Boot app |
| `Dockerfile` | Multi-stage, layered jar, non-root, JRE 21 Alpine |
| `infra/scripts/bootstrap-state.sh` | One-time Terraform state bucket |
| `infra/foundation/` | Permanent: OIDC roles, ECR, Route 53 zone, ACM cert, SNS, budget (applied from laptop) |
| `infra/lab/` | On-demand: VPC, ALB, ECS, auto scaling, alarms, dashboard, SSM param, DNS alias (applied by Actions) |
| `.github/workflows/` | `ci`, `cd`, `lab-up`, `lab-down` |

## Endpoints

| Method | Path | Response |
|---|---|---|
| GET | `/api/v1/products` | 200, fixed list of 3 |
| GET | `/api/v1/products/{id}` | 200 for 1–3, else 404 ProblemDetail |
| POST | `/api/v1/products` | 201 + Location, echoes body with UUID |
| PUT | `/api/v1/products/{id}` | 200, echoes body with path id |
| DELETE | `/api/v1/products/{id}` | 204 |
| GET | `/api/v1/info` | version, hostname, ECS task ARN, AZ |
| GET | `/actuator/health/liveness`, `/actuator/health/readiness` | probes |
| GET | `/swagger-ui.html` | OpenAPI UI |

Every response carries `X-Trace-Id`. Logs are ECS-format JSON with `traceId`; set `LOG_FORMAT=` (empty) for plain-text logs locally.

## Local (Git Bash)

```bash
cd /d/product-service/product-service
mvn clean verify
docker build --build-arg APP_VERSION=local -t product-service:local .
docker run --rm -p 8080:8080 -e APP_ENV=docker --name product-service product-service:local
curl -s localhost:8080/api/v1/info
```

## GitHub configuration

Repository variables (Settings → Secrets and variables → Actions → Variables):

| Name | Value |
|---|---|
| `AWS_REGION` | `ap-south-1` |
| `AWS_PLAN_ROLE_ARN` | `arn:aws:iam::718533829466:role/product-service-gha-plan` |
| `AWS_DEPLOY_ROLE_ARN` | `arn:aws:iam::718533829466:role/product-service-gha-deploy` |
| `ECR_REPOSITORY` | `product-service` |

Environment `dev` (Settings → Environments) is created automatically on first use. Do not add required reviewers to it, or the nightly `lab-down` will wait for approval instead of running.

## Daily lab loop

1. Actions → **lab-up** → Run workflow (deploys the most recent ECR image).
2. Practise: `https://api.dev.nexacommerce.in/swagger-ui.html`, dashboard, logs, alarms.
3. Push to `main` → **cd** builds, pushes and rolls out the new version.
4. Actions → **lab-down** → Run workflow (also runs automatically at 23:30 IST).

## Practice exercises (lab up)

```bash
# which task/AZ answered (run several times once scaled to 2+ tasks)
for i in 1 2 3 4 5; do curl -s https://api.dev.nexacommerce.in/api/v1/info; echo; done

# tail application logs
aws logs tail /ecs/product-service-dev --follow --region ap-south-1

# scale manually and watch ALB register targets
aws ecs update-service --cluster product-service-dev --service product-service --desired-count 2 --region ap-south-1

# kill a task and watch ECS replace it
TASK=$(aws ecs list-tasks --cluster product-service-dev --service-name product-service --query 'taskArns[0]' --output text --region ap-south-1)
aws ecs stop-task --cluster product-service-dev --task "$TASK" --reason "chaos test" --region ap-south-1

# shell into the running container (needs the Session Manager plugin installed locally)
aws ecs execute-command --cluster product-service-dev --task "$TASK" --container product-service --interactive --command "/bin/sh" --region ap-south-1

# load test to trigger auto scaling (install hey: go install github.com/rakyll/hey@latest)
hey -z 3m -c 50 https://api.dev.nexacommerce.in/api/v1/products
```
