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
- Live test stack: srt-backend-si (us-east-1). Old dev tables are retained, not managed by CF.
- Migrating to ONE DynamoDB table `srt-data-<env>` (pk=DISH|ORDER|COUNTER). Code is done;
  template.yaml still has two tables; data copy from the -dev tables is not done yet.

## Gotchas we hit
- Authorizer IdentitySource must be a LIST, not a string, or CF fails with only
  "EarlyValidation::PropertyValidation". Debug with `aws cloudformation describe-change-set`.
- Stage must be $default, or the stage name is prepended to the path and Quarkus 404s.
- GET /dishes returns 404 when the table is empty (DishService throws NotFoundException).
- Don't import resources into stacks; create fresh stacks and copy data instead.
- The cloud shell has no aws or mvn; the user runs those locally and pastes output.
