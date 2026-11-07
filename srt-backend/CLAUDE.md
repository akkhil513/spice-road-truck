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
