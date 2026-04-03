# 🌶️ Spice Road Truck

A full-stack food truck platform for **Spice Road Truck** — an Indo-American street food business in Atlanta, GA. The project consists of a **Next.js 16** frontend (`srt-ui`) and a **Quarkus 3** REST API backend, backed by **PostgreSQL on AWS RDS**.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Frontend (srt-ui)](#frontend-srt-ui)
  - [Pages](#pages)
  - [Components](#components)
  - [Lib](#lib)
  - [Config Files](#frontend-config-files)
  - [Public Assets](#public-assets)
- [Backend](#backend)
  - [Source Code](#backend-source-code)
  - [Resources](#backend-resources)
  - [Docker](#docker)
  - [Build Config](#backend-build-config)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Git & IDE Notes](#git--ide-notes)

---

## Architecture Overview

```
┌─────────────────┐       HTTP        ┌──────────────────────┐       JDBC        ┌──────────────┐
│   Next.js 16    │  ──────────────▶  │   Quarkus Backend    │  ──────────────▶  │  PostgreSQL  │
│   (srt-ui)      │                   │   (JAX-RS + CDI)     │                   │  (AWS RDS)   │
│   React 19      │  ◀──────────────  │   Port 8080          │  ◀──────────────  │              │
│   Port 3000     │                   │                      │                   └──────────────┘
└─────────────────┘                   └──────────────────────┘
                                               │
                                               │ (optional)
                                               ▼
                                      ┌──────────────────────┐
                                      │  AWS Secrets Manager  │
                                      └──────────────────────┘
```

The backend follows a **three-layer architecture**:

1. **API Layer** (`api/`) — JAX-RS REST resources handling HTTP requests/responses.
2. **Service Layer** (`service/`) — Business logic and transaction management.
3. **Repository Layer** (`repository/`) — Data access via Hibernate ORM with Panache.
4. **Entity Layer** (`entity/`) — JPA entity definitions mapping to database tables.

---

## Tech Stack

| Layer        | Technology                                                      |
| ------------ | --------------------------------------------------------------- |
| **Frontend** | Next.js 16, React 19, TypeScript 5, Tailwind CSS 4             |
| **Icons**    | Lucide React                                                    |
| **Email**    | EmailJS (newsletter subscription)                               |
| **Backend**  | Quarkus 3.19.2, Java 17, JAX-RS (REST), CDI (Dependency Inj.)  |
| **ORM**      | Hibernate ORM with Panache                                      |
| **Database** | PostgreSQL (AWS RDS)                                            |
| **Secrets**  | AWS Secrets Manager (via Quarkiverse extension)                 |
| **Docs**     | SmallRye OpenAPI + Swagger UI                                   |
| **Build**    | Maven (backend), npm (frontend)                                 |

---

## Project Structure

```
spice-road-truck/
├── README.md                          # This file — project documentation
├── .gitignore                         # Root gitignore (.idea, .DS_Store, .vscode)
│
├── srt-ui/                            # ── FRONTEND (Next.js 16 + React 19) ──
│   ├── package.json                   # Dependencies & scripts
│   ├── package-lock.json              # Lockfile
│   ├── next.config.ts                 # Next.js configuration
│   ├── tsconfig.json                  # TypeScript compiler options
│   ├── eslint.config.mjs              # ESLint flat config (Next.js + TS)
│   ├── postcss.config.mjs             # PostCSS with Tailwind CSS 4
│   ├── next-env.d.ts                  # Next.js TypeScript declarations
│   ├── .gitignore                     # Frontend gitignore (node_modules, .next, .env)
│   │
│   ├── app/                           # Next.js App Router pages
│   │   ├── layout.tsx                 # Root layout (Navbar + Footer wrapper)
│   │   ├── page.tsx                   # Home page (hero, stats, schedule, about, services, CTA)
│   │   ├── globals.css                # Global styles (Tailwind import + base colors)
│   │   ├── favicon.ico                # Favicon
│   │   ├── menu/page.tsx              # Menu page (fetches dishes from API, category filter)
│   │   ├── order/page.tsx             # Order page (Coming Soon placeholder)
│   │   ├── catering/page.tsx          # Catering page (Coming Soon placeholder)
│   │   ├── contact/page.tsx           # Contact page (form, hours, address, socials)
│   │   └── admin/                     # Admin section (empty — future feature)
│   │
│   ├── components/                    # Shared React components
│   │   ├── Navbar.tsx                 # Fixed top nav with mobile hamburger menu
│   │   ├── Footer.tsx                 # Footer with links, contact, EmailJS newsletter
│   │   └── ComingSoon.tsx             # Animated "Coming Soon" placeholder page
│   │
│   ├── lib/                           # Utility / API layer
│   │   └── api.ts                     # Typed fetch functions for Dish CRUD
│   │
│   └── public/                        # Static assets
│       ├── srt-logo-dark.svg          # Logo SVG
│       └── images/
│           ├── hero/
│           │   ├── truck.png          # Truck image used in hero & ComingSoon
│           │   ├── food-truck.jpg     # Photo used on contact page
│           │   └── image.png          # Additional hero image
│           └── logo/
│               └── srt-logo-dark.svg  # Logo copy for components
│
├── backend/                           # ── BACKEND (Quarkus 3 + Java 17) ──
│   ├── pom.xml                        # Maven POM (dependencies, plugins, Quarkus BOM)
│   ├── mvnw                           # Maven Wrapper (Unix)
│   ├── mvnw.cmd                       # Maven Wrapper (Windows)
│   ├── README.md                      # Quarkus-generated backend README
│   ├── .gitignore                     # Backend gitignore (target/, .idea, .env)
│   │
│   ├── src/main/java/com/spiceroad/
│   │   ├── api/
│   │   │   └── DishResource.java      # REST controller — /srt/v1/dishes endpoints
│   │   ├── service/
│   │   │   └── DishService.java       # Business logic — CRUD operations on dishes
│   │   ├── repository/
│   │   │   └── DishRepository.java    # Panache repository for Dish entity
│   │   └── entity/
│   │       └── Dish.java              # JPA entity — maps to "dish" table
│   │
│   ├── src/main/resources/
│   │   └── application.properties     # Quarkus config (DB, CORS, Swagger, Hibernate)
│   │
│   ├── src/main/docker/               # Docker build files
│   │   ├── Dockerfile.jvm             # JVM-based Docker image
│   │   ├── Dockerfile.legacy-jar      # Legacy JAR Docker image
│   │   ├── Dockerfile.native          # Native binary Docker image
│   │   └── Dockerfile.native-micro    # Micro native Docker image
│   │
│   └── target/                        # Maven build output (gitignored)
│       └── quarkus-app/
│           └── quarkus-run.jar        # Runnable application JAR
│
├── components/                        # (Empty — reserved for shared components)
└── lib/                               # (Empty — reserved for shared libraries)
```

---

## Frontend (srt-ui)

The frontend is a **Next.js 16** app using the **App Router**, **React 19**, **TypeScript**, and **Tailwind CSS 4**.

### Pages

| Route        | File                          | Description                                                                                                                 |
| ------------ | ----------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| `/`          | `app/page.tsx`                | **Home page** — Hero banner with truck image, stats bar (50+ items, 10K+ customers, 4.9 rating), weekly schedule, about us section, catering services grid, and CTA banner. |
| `/menu`      | `app/menu/page.tsx`           | **Menu page** — Fetches all dishes from the backend API via `getAllDishes()`. Displays a sticky category filter bar and groups dishes into cards showing name, price, description, and image. |
| `/order`     | `app/order/page.tsx`          | **Order page** — Uses `ComingSoon` component. Placeholder for future online ordering.                                       |
| `/catering`  | `app/catering/page.tsx`       | **Catering page** — Uses `ComingSoon` component. Placeholder for future catering booking.                                   |
| `/contact`   | `app/contact/page.tsx`        | **Contact page** — Contact info (phone, email, address), business hours, social links, and a contact form (currently simulated submission). |
| `/admin`     | `app/admin/`                  | **Admin section** — Empty directory, reserved for future admin dashboard.                                                    |

#### `app/layout.tsx` — Root Layout
- Wraps all pages with `<Navbar />` on top and `<Footer />` at the bottom.
- Uses **Geist** font from Google Fonts.
- Sets page metadata: title "SRT — Indian Street Food Atlanta", favicon as `srt-logo-dark.svg`.
- Adds `pt-16` padding to main content to account for the fixed navbar.

#### `app/globals.css` — Global Styles
- Imports Tailwind CSS 4 via `@import "tailwindcss"`.
- Sets base background color `#FFF8F0` (warm cream) and text color `#1A1A1A`.
- Enables smooth scrolling.

### Components

| Component          | File                          | Description                                                                                                  |
| ------------------ | ----------------------------- | ------------------------------------------------------------------------------------------------------------ |
| **Navbar**         | `components/Navbar.tsx`       | Fixed top navigation bar. Dark theme (`#1A1A1A`). Shows logo + brand name, nav links (Home, Menu, Order Online, Catering, Contact), and a red "Order Now" CTA button. Responsive with a hamburger menu on mobile. |
| **Footer**         | `components/Footer.tsx`       | Three-column footer: brand info with social links + **EmailJS newsletter subscription**, quick links, and contact details (phone, email, address). Uses `@emailjs/browser` to send welcome emails and admin notifications. |
| **ComingSoon**     | `components/ComingSoon.tsx`   | Animated placeholder page with a CSS-animated truck driving across the screen, smoke puffs, and road dashes. Shows page title, message, and call/email CTA buttons. Used by Order and Catering pages. |

### Lib

| File            | Description                                                                                          |
| --------------- | ---------------------------------------------------------------------------------------------------- |
| `lib/api.ts`    | Typed API client for the backend Dish REST API. Exports functions: `getAllDishes()`, `getDishesByCategory(category)`, `getDishById(id)`, `createDish(dish)`, `updateDish(id, dish)`, `deleteDish(id)`. Uses `NEXT_PUBLIC_API_URL` env var (defaults to `https://api.spiceroadtruck.com`). |

### Frontend Config Files

| File                  | Purpose                                                         |
| --------------------- | --------------------------------------------------------------- |
| `package.json`        | Dependencies: `next@16.2.1`, `react@19.2.4`, `lucide-react`, `@emailjs/browser`. Dev: `tailwindcss@4`, `typescript@5`, `eslint`. Scripts: `dev`, `build`, `start`, `lint`. |
| `next.config.ts`      | Configures allowed image domains: `api.spiceroadtruck.com`.     |
| `tsconfig.json`       | TypeScript config: ES2017 target, bundler module resolution, `@/*` path alias for imports. |
| `eslint.config.mjs`   | ESLint flat config with Next.js core web vitals + TypeScript rules. |
| `postcss.config.mjs`  | PostCSS plugin: `@tailwindcss/postcss` for Tailwind CSS 4.      |

### Public Assets

| Path                                | Description                          |
| ----------------------------------- | ------------------------------------ |
| `public/srt-logo-dark.svg`         | Main SVG logo (used as favicon)      |
| `public/images/logo/srt-logo-dark.svg` | Logo copy used in Navbar & Footer |
| `public/images/hero/truck.png`     | Truck PNG for hero section (~558KB)  |
| `public/images/hero/food-truck.jpg`| Food truck photo for contact page    |
| `public/images/hero/image.png`     | Additional hero image (~458KB)       |

---

## Backend

The backend is a **Quarkus 3.19.2** REST API using **Java 17**, **Hibernate ORM with Panache**, and **PostgreSQL**.

### Backend Source Code

All source code is under `src/main/java/com/spiceroad/`:

#### `entity/Dish.java` — JPA Entity
- Extends `PanacheEntity` (auto-generates `id` field).
- Maps to the `dish` database table.
- Fields: `name` (String), `description` (String, max 1000 chars), `price` (double), `category` (String), `imageUrl` (String).

#### `repository/DishRepository.java` — Data Access
- Implements `PanacheRepository<Dish>`.
- `@ApplicationScoped` CDI bean.
- Inherits all standard CRUD methods from Panache (`listAll`, `findById`, `persist`, `deleteById`, `list`).

#### `service/DishService.java` — Business Logic
- `@ApplicationScoped` CDI bean. Injects `DishRepository`.
- Methods:
  - `getAllDishes()` — returns all dishes.
  - `getDishesByCategory(category)` — filters dishes by category.
  - `getDish(id)` — finds a single dish by ID.
  - `createDish(dish)` — persists a new dish (`@Transactional`).
  - `updateDish(id, dish)` — updates all fields of an existing dish (`@Transactional`).
  - `deleteDish(id)` — deletes a dish by ID (`@Transactional`).

#### `api/DishResource.java` — REST Controller
- Base path: `/srt/v1/dishes`
- Produces/consumes: `application/json`
- Endpoints:

| Method   | Path                | Description                           | Query Params       |
| -------- | ------------------- | ------------------------------------- | ------------------ |
| `GET`    | `/srt/v1/dishes`    | List all dishes (or filter by category) | `?category=...`  |
| `GET`    | `/srt/v1/dishes/{id}` | Get a single dish by ID             | —                  |
| `POST`   | `/srt/v1/dishes`    | Create a new dish                     | —                  |
| `PUT`    | `/srt/v1/dishes/{id}` | Update an existing dish              | —                  |
| `DELETE` | `/srt/v1/dishes/{id}` | Delete a dish                        | —                  |

### Backend Resources

#### `application.properties`
- **Database**: PostgreSQL on AWS RDS (`srt-db.cyfyc46k01xl.us-east-1.rds.amazonaws.com:5432/srt_db`).
- **Hibernate**: `database.generation=update` (auto-updates schema), SQL logging enabled.
- **CORS**: Allows `https://spiceroadtruck.com` and `http://localhost:3000`.
- **OpenAPI**: Available at `/openapi`, Swagger UI at `/swagger`.
- **AWS**: Region `us-east-1`, Secrets Manager integration available.
- **Dev Services**: Disabled (using external RDS).

### Docker

Four Dockerfile variants in `src/main/docker/`:

| File                       | Description                                          |
| -------------------------- | ---------------------------------------------------- |
| `Dockerfile.jvm`           | Standard JVM-based image (recommended for production) |
| `Dockerfile.legacy-jar`    | Legacy uber-jar based image                          |
| `Dockerfile.native`        | GraalVM native binary image                          |
| `Dockerfile.native-micro`  | Minimal native image (smallest footprint)            |

### Backend Build Config

#### `pom.xml`
- **Group**: `com.spiceroad`, **Artifact**: `backend`, **Version**: `1.0.0-SNAPSHOT`
- **Quarkus BOM**: 3.19.2
- **Key Dependencies**:
  - `quarkus-rest` + `quarkus-rest-jackson` — RESTEasy Reactive with JSON serialization
  - `quarkus-hibernate-orm-panache` — ORM with active record pattern
  - `quarkus-jdbc-postgresql` — PostgreSQL JDBC driver
  - `quarkus-smallrye-openapi` — OpenAPI/Swagger generation
  - `quarkus-amazon-secretsmanager` (Quarkiverse 2.16.0) — AWS Secrets Manager
  - `quarkus-arc` — CDI dependency injection
  - `quarkus-junit5` + `rest-assured` — Testing (test scope)
- **Plugins**: quarkus-maven-plugin, maven-compiler-plugin (Java 17), surefire, failsafe

---

## API Reference

Base URL: `https://api.spiceroadtruck.com/srt/v1` (or `http://localhost:8080/srt/v1` locally)

### Dish Object

```json
{
  "id": 1,
  "name": "Chicken Biryani",
  "description": "Aromatic basmati rice with tender chicken and spices",
  "price": 14.99,
  "category": "Biryanis/Pulaos",
  "imageUrl": "https://..."
}
```

### Endpoints

```
GET    /srt/v1/dishes              → List all dishes
GET    /srt/v1/dishes?category=... → Filter by category
GET    /srt/v1/dishes/{id}         → Get dish by ID
POST   /srt/v1/dishes              → Create dish (body: Dish JSON)
PUT    /srt/v1/dishes/{id}         → Update dish (body: Dish JSON)
DELETE /srt/v1/dishes/{id}         → Delete dish
```

**Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
**OpenAPI spec**: [http://localhost:8080/openapi](http://localhost:8080/openapi)

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.9+** (or use the included `mvnw` wrapper)
- **Node.js 18+** and **npm**
- **PostgreSQL** database (or AWS RDS instance)

### Backend

```bash
cd backend

# Run in dev mode (hot reload)
./mvnw quarkus:dev

# Package for production
./mvnw clean package -DskipTests

# Run the packaged JAR
java -jar target/quarkus-app/quarkus-run.jar
```

> ⚠️ **Important**: Always run Maven commands from the `backend/` directory, not the project root. The `pom.xml` lives inside `backend/`.

### Frontend

```bash
cd srt-ui

# Install dependencies
npm install

# Run dev server (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Start production server
npm start
```

### Environment Variables (Frontend)

Create `srt-ui/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_EMAILJS_SERVICE_ID=your_service_id
NEXT_PUBLIC_EMAILJS_WELCOME_TEMPLATE_ID=your_welcome_template_id
NEXT_PUBLIC_EMAILJS_ADMIN_TEMPLATE_ID=your_admin_template_id
NEXT_PUBLIC_EMAILJS_PUBLIC_KEY=your_public_key
```

---

## Configuration

### Backend (`application.properties`)

| Property                                    | Description                                |
| ------------------------------------------- | ------------------------------------------ |
| `quarkus.datasource.jdbc.url`               | PostgreSQL JDBC connection URL             |
| `quarkus.datasource.username`               | Database username                          |
| `quarkus.datasource.password`               | Database password                          |
| `quarkus.hibernate-orm.database.generation`  | Schema strategy (`update`, `drop-and-create`, etc.) |
| `quarkus.http.cors.origins`                 | Allowed CORS origins                       |
| `quarkus.aws.region`                        | AWS region for Secrets Manager             |

### Frontend (Environment Variables)

| Variable                                     | Description                                |
| -------------------------------------------- | ------------------------------------------ |
| `NEXT_PUBLIC_API_URL`                        | Backend API base URL                       |
| `NEXT_PUBLIC_EMAILJS_SERVICE_ID`             | EmailJS service ID                         |
| `NEXT_PUBLIC_EMAILJS_WELCOME_TEMPLATE_ID`    | EmailJS welcome email template ID          |
| `NEXT_PUBLIC_EMAILJS_ADMIN_TEMPLATE_ID`      | EmailJS admin notification template ID     |
| `NEXT_PUBLIC_EMAILJS_PUBLIC_KEY`             | EmailJS public key                         |

---

## Git & IDE Notes

### .idea Files (IntelliJ / WebStorm)
- The `.idea/` directory contains IntelliJ IDEA project settings. These are **already gitignored** and should **NOT** be committed.
- The ☕ coffee cup icons you see in IntelliJ are the standard icon for **Java source files** — this is completely normal.

### Running Maven
- The `pom.xml` is inside `backend/`, so always `cd backend` before running Maven:
  ```bash
  cd backend
  ./mvnw clean package -DskipTests
  ```
- Running `mvn` or `./mvnw` from the project root will fail with "no POM in this directory".

### Git Push
- If you get `HTTP 400` errors when pushing, increase the HTTP buffer:
  ```bash
  git config http.postBuffer 524288000
  ```

---

## License

Private project — Spice Road Truck © 2025
