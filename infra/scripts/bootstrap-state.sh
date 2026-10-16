#!/usr/bin/env bash
# One-time: creates the Terraform state bucket and writes backend.hcl files.
# Run from the repo root in Git Bash:  ./infra/scripts/bootstrap-state.sh
set -euo pipefail

REGION="${AWS_REGION:-ap-south-1}"
PROJECT="product-service"

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
BUCKET="${PROJECT}-tfstate-${ACCOUNT_ID}"

echo "Account : ${ACCOUNT_ID}"
echo "Region  : ${REGION}"
echo "Bucket  : ${BUCKET}"

if aws s3api head-bucket --bucket "${BUCKET}" 2>/dev/null; then
  echo "Bucket already exists, re-applying settings"
else
  aws s3api create-bucket \
    --bucket "${BUCKET}" \
    --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}" >/dev/null
  echo "Bucket created"
fi

aws s3api put-public-access-block \
  --bucket "${BUCKET}" \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

aws s3api put-bucket-ownership-controls \
  --bucket "${BUCKET}" \
  --ownership-controls 'Rules=[{ObjectOwnership=BucketOwnerEnforced}]'

aws s3api put-bucket-versioning \
  --bucket "${BUCKET}" \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket "${BUCKET}" \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"},"BucketKeyEnabled":true}]}'

aws s3api put-bucket-lifecycle-configuration \
  --bucket "${BUCKET}" \
  --lifecycle-configuration \
  '{"Rules":[{"ID":"expire-old-state-versions","Status":"Enabled","Filter":{},"NoncurrentVersionExpiration":{"NoncurrentDays":90},"AbortIncompleteMultipartUpload":{"DaysAfterInitiation":7}}]}'

aws s3api put-bucket-tagging \
  --bucket "${BUCKET}" \
  --tagging "TagSet=[{Key=Project,Value=${PROJECT}},{Key=ManagedBy,Value=bootstrap-script}]"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(dirname "${SCRIPT_DIR}")"

for STACK in foundation lab; do
  mkdir -p "${INFRA_DIR}/${STACK}"
  cat > "${INFRA_DIR}/${STACK}/backend.hcl" <<EOF
bucket       = "${BUCKET}"
key          = "${STACK}/terraform.tfstate"
region       = "${REGION}"
encrypt      = true
use_lockfile = true
EOF
  echo "Wrote infra/${STACK}/backend.hcl"
done

echo "Done. Next: cd infra/foundation && terraform init -backend-config=backend.hcl"
