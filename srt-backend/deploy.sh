#!/usr/bin/env bash
# Usage: ./deploy.sh [env]   (default: si)   e.g. ./deploy.sh si
set -euo pipefail

ENV="${1:-si}"
case "$ENV" in dev|si|prod) ;; *) echo "env must be dev, si or prod" >&2; exit 1;; esac

REGION="us-east-1"
BUCKET="srt-deployments-bucket"
KEY="lambda/srt-backend-${ENV}.zip"
STACK="srt-backend-${ENV}"
POOL_ID="us-east-1_lEeDIOvEb"
CLIENT_ID="3ru9v2d905sdq0gamndmh0uprq"

cd "$(dirname "$0")"

if [ "$ENV" = "prod" ]; then
  read -r -p "Deploy to PROD? Type 'prod' to continue: " ANSWER
  [ "$ANSWER" = "prod" ] || { echo "Cancelled."; exit 1; }
fi

echo "==> 1/5 Tests";            mvn -q clean test
echo "==> 2/5 Package";          mvn -q package -DskipTests
echo "==> 3/5 Upload zip";       aws s3 cp target/function.zip "s3://${BUCKET}/${KEY}" --region "$REGION"
echo "==> 4/5 Deploy template";  aws cloudformation deploy --template-file template.yaml \
    --stack-name "$STACK" --region "$REGION" --capabilities CAPABILITY_NAMED_IAM \
    --no-fail-on-empty-changeset \
    --parameter-overrides Environment="$ENV" CognitoUserPoolId="$POOL_ID" CognitoAppClientId="$CLIENT_ID"
echo "==> 5/5 Update function code"
aws lambda update-function-code --function-name "srt-dishes-lambda-${ENV}" \
    --s3-bucket "$BUCKET" --s3-key "$KEY" --region "$REGION" --query LastModified --output text
aws lambda wait function-updated --function-name "srt-dishes-lambda-${ENV}" --region "$REGION"
echo "Done: ${STACK} deployed."
