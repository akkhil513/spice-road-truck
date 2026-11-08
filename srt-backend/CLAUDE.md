# SRT backend

## Stack
Quarkus 3.19.2 on Java 21, running as an AWS Lambda behind an API Gateway HTTP API
(quarkus-amazon-lambda-http). Infra is one CloudFormation file: template.yaml.
JWT auth via Cognito; the authorizer is defined in template.yaml.

## Working rules
- Show proposed changes before writing code. Never add code without approval.
- Prefer small steps; run `mvn clean test` before any deploy.
- No Scan; Query/GetItem only. Config via env vars, no hardcoded names.

## Build and deploy (env = dev | si | prod)
1. mvn clean package -DskipTests      -> target/function.zip
2. aws s3 cp target/function.zip s3://srt-deployments-bucket/lambda/srt-backend-<env>.zip
3. aws cloudformation deploy --template-file template.yaml --stack-name srt-backend-<env> \
     --parameter-overrides Environment=<env> CognitoUserPoolId=<pool> CognitoAppClientId=<client> \
     --capabilities CAPABILITY_NAMED_IAM --region us-east-1
4. aws lambda update-function-code (same bucket/key)

## Current state
- Live stack: srt-backend-si (us-east-1), serving https://api.spiceroadtruck.com
  (custom domain mapped to the $default stage; mapping created by CLI, NOT in CloudFormation).
- Single DynamoDB table `srt-data-<env>` (pk=DISH|ORDER|COUNTER) is live in si; data was
  copied from the old -dev tables. Old srt-dishes-si / srt-orders-si / *-dev tables are
  retained and unmanaged; delete only with the owner's OK.
- Not done yet: real dev and prod stacks (si is the temporary live env), UI sends no
  Authorization header yet, www.spiceroadtruck.com redirect, hardening pass.

## Gotchas we hit
- Authorizer IdentitySource must be a LIST, not a string, or CF fails with only
  "EarlyValidation::PropertyValidation". Debug with `aws cloudformation describe-change-set`.
- Stage must be $default, or the stage name is prepended to the path and Quarkus 404s.
- GET /dishes returns 404 when the table is empty (DishService throws NotFoundException).
- Don't import resources into stacks; create fresh stacks and copy data instead.
- The cloud shell has no aws or mvn; the user runs those locally and pastes output.
- Public routes (GET dishes, GET dishes/{id}, POST orders) must declare
  `AuthorizationType: NONE` explicitly. A route flipped to JWT outside CloudFormation is NOT
  corrected by a deploy unless the template differs.
- The ACM cert for api.spiceroadtruck.com expired because its DNS validation CNAMEs were
  missing. DNS is on CLOUDFLARE (registrar is Squarespace; its DNS page is inactive).
  Add validation CNAMEs in Cloudflare as DNS only (not proxied).
- Deploy is template-only when just infra changes; rebuild the zip only when Java changes.

## Order data model (P1.1)
- Business day starts at 04:00 local (`srt.orders.business-day-start-hour`): earlier orders count toward the previous day. Order numbers restart at 001 each business day; one shared counter `COUNTER / order-counter#<date>`, prefix by source (`OrderSource`: SRT- own channels, UE- Uber Eats, DD- DoorDash).
- Order item (`pk=ORDER`, `sk=<businessDate>_<orderNumber>`) new optional fields: `source`, `orderType`, `tableNumber`, `paymentStatus`, `payments[]` (whole cents), `sentToKitchenAt`, `orderVersion`, `customerPhone` (E.164), `externalOrderId`, `gsiOpen`; each line has `lineId` + `itemStatus`. `entityVersion` is now 2 for new orders; old orders (v1) have these fields null.
- `gsiOpen="OPEN"` only while an order is not final; it is REMOVEd when COMPLETED/CANCELLED (sparse GSI `open-index`, `getOpenOrders`). Adding the GSI needs a template deploy BEFORE the new Lambda code is used.
- Customers: `pk=CUSTOMER`, `sk=<E.164 phone>` (`SRTCustomerRepository`, used by P1.6).
- Public `POST /orders` never trusts client-sent source, order type, payment, item status or version: `OrderAssembler` sets them (ONLINE, TO_GO, UNPAID, PENDING). New rules live in small pure classes (`BusinessDay`, `PhoneNumbers`, `OrderSource`, `OrderAssembler`) with plain JUnit tests; there is no Mockito.

## Kitchen endpoints (P1.2)
- `GET /srt/v1/kitchen/orders` (staff/manager/admin): open orders already sent to the kitchen (`sentToKitchenAt` set), oldest first, via `open-index` + filter. `?completed=true[&date=]` lists finished kitchen orders of a business day instead. Returns `KitchenOrder` (no email, phone, card ref, prices or payments).
- `PUT /srt/v1/kitchen/orders/{sk}/items/{lineId}/status` body `{"from":"PENDING","to":"TICKED"}`. One step forward (tick, clear) or one step back (recall) only; 409 if the item is no longer in `from`, the order is final or not sent to the kitchen. `ItemTransition` decides, `SRTOrderRepository.updateItemStatus` writes conditionally (line status, order status and `kitchenRevision` must be unchanged).
- Order status follows the lines while open: all PENDING = NEW, all CLEARED = READY, anything else = PREPARING (`KitchenProgress`). Counter staff still mark COMPLETED at pickup via `PUT /orders/{sk}/status`, which does not touch item statuses.
- Website orders (`source=ONLINE`) are sent to the kitchen when placed; orders created before P1.2 have no `sentToKitchenAt` and never appear in the kitchen list. Counter send/hold rules come in P1.3.
- Two new JWT routes in `template.yaml` must be deployed before the new code is used.

## Counter endpoints (P1.3)
All under `/srt/v1/counter/orders`, JWT + `@RolesAllowed(admin, manager, staff)` (`CounterResource` -> `CounterService`). Rules are in the pure `CounterOrderPolicy`; every write is a conditional update on the state that was read (409 if someone else changed the order).
- `POST /` create: body `{source: COUNTER|KIOSK, orderType: DINE_IN|TO_GO, tableNumber (dine-in only, 1-99), customerName (blank = "Walk-in"), customerPhone, items:[{id, quantity}]}`. Server prices it; saved NEW, UNPAID, NOT sent to the kitchen.
- `GET /?state=open|past[&date=]` list (open uses `open-index` and includes held/unsent orders); `GET /{sk}` one order with full detail.
- `PUT /{sk}/items` body `{orderVersion, items}`: only while unsent, NEW or HELD, and UNPAID; stale `orderVersion` = 409; bumps `orderVersion`.
- `POST /{sk}/hold` (NEW -> HELD), `/resume` (HELD -> NEW), `/discard` (soft cancel to CANCELLED, unsent + unpaid only), `/send` body `{orderVersion}` (sets `sentToKitchenAt`; to-go may be unpaid, dine-in must be PAID; no items edits afterwards), `/pickup` (READY -> COMPLETED, closes the order).
- Pickup uses COMPLETED (no separate PICKED_UP status). `HELD` is not final and stays in `open-index`.
- The generic `PUT /orders/{sk}/status` now refuses HELD and orders that were never sent to the kitchen (counter orders must use the routes above).
- Nine new JWT routes in `template.yaml` must be deployed before the new code is used (`./deploy.sh` does both).
- Dine-in cannot be sent until payments exist (P1.5).

## Decision: API definition
- Routes stay as `AWS::ApiGatewayV2::Route` resources in `template.yaml` on the live `si` stack (switching an existing API to an imported OpenAPI body risks the routes and the CLI-made custom domain mapping).
- When the real dev and prod stacks are created, move to an `openapi.yaml` pulled in with `Fn::Transform: AWS::Include` (needs `aws cloudformation package` in `deploy.sh`), and generate the Postman collection and UI client types from it.
