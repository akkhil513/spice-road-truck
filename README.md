# Spice Road Truck

Website, online ordering and (in progress) in-truck POS for **Spice Road Truck**, an Indo-American street food truck in Atlanta, GA.

Live site: https://spiceroadtruck.com
API: https://api.spiceroadtruck.com

## Architecture

```
Browser ──▶ Cloudflare (DNS, SSL) ──▶ S3 static site (Next.js export)
   │
   └──▶ api.spiceroadtruck.com ──▶ API Gateway (HTTP API) ──▶ Lambda (Quarkus, Java 21) ──▶ DynamoDB
                                                         Cognito (admin sign-in)
```

| Part | Folder | Stack |
|---|---|---|
| Frontend | `srt-ui` | Next.js 16 (static export), React 19, TypeScript, Tailwind 4, AWS Amplify (Cognito) |
| Backend | `srt-backend` | Quarkus 3.19 on AWS Lambda, Java 21, DynamoDB enhanced client, Lombok |
| Infrastructure | `srt-backend/template.yaml` | CloudFormation: DynamoDB tables, Lambda, API Gateway routes, IAM |

## Folder structure

```
srt-ui/                 Next.js app (app/, components/, lib/)
srt-backend/
  src/main/java/com/spiceroad/
    api/                REST resources (DishResource, OrderResource)
    service/            business logic
    repository/         DynamoDB access
    model/              DynamoDB beans (SRTDish, SRTOrder)
    config/ constant/   configuration and constants
  template.yaml         CloudFormation stack (srt-<env>)
```

## API

All routes are under `/srt/v1`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/dishes` | List dishes (optional `?category=`) |
| GET | `/dishes/{id}` | Get one dish |
| POST / PUT / DELETE | `/dishes`, `/dishes/{id}` | Manage dishes (admin) |
| POST | `/orders` | Place an order |
| GET | `/orders` | List orders (admin) |
| PUT | `/orders/{sk}/status` | Update order status (admin) |

Data lives in two DynamoDB tables, `srt-dishes-<env>` and `srt-orders-<env>`, using generic `pk` / `sk` keys. Order numbers come from an atomic counter item in the orders table.

## Run locally

Frontend:

```bash
cd srt-ui
npm install
npm run dev        # http://localhost:3000
```

Set `NEXT_PUBLIC_API_URL` to point at a different API.

Backend (needs AWS credentials with access to the dev tables):

```bash
cd srt-backend
mvn quarkus:dev    # http://localhost:8080
```

## Build and deploy

Backend: `mvn package` in `srt-backend` produces `target/function.zip`. Upload it to the deployment bucket and deploy `template.yaml` (see the `Code` section of the Lambda resource for the bucket and key).

Frontend: `npm run build` in `srt-ui` produces a static export in `out/`, which is synced to the S3 site bucket.

## Roadmap

The in-truck POS (kiosk, counter, kitchen and status screens, staff PINs, refunds, delivery-app orders) is being planned. Build output (`target/`) and secrets are not committed; see `.gitignore`.
