# Foundation stack (permanent, ~₹50/month)

State bucket (script) · GitHub OIDC provider · `gha-plan` / `gha-deploy` roles · ECR · Route 53 zone `dev.nexacommerce.in` · ACM cert (`api.dev` + `*.dev`) · SNS alerts · monthly budget.

Applied once from your laptop with your IAM admin credentials. Everything after this runs from GitHub Actions.

## 0. Verify credentials (Git Bash)

```bash
aws sts get-caller-identity
aws configure get region
```

If `region` is empty: `aws configure set region ap-south-1`.

## 1. State bucket

```bash
cd /d/product-service
chmod +x infra/scripts/bootstrap-state.sh
./infra/scripts/bootstrap-state.sh
```

## 2. Init + create the DNS zone only

```bash
cd /d/product-service/infra/foundation
terraform init -backend-config=backend.hcl
terraform apply -target=aws_route53_zone.dev
terraform output dns_name_servers
```

## 3. Delegate `dev` at Namecheap

Namecheap → Domain List → `nexacommerce.in` → **Manage** → **Advanced DNS** → **Add New Record** → type **NS Record**, host `dev`, value = each name server (4 records, drop the trailing dot if Namecheap complains). Nameservers for the root domain stay on Namecheap BasicDNS.

Check propagation (usually 5–30 min):

```bash
nslookup -type=NS dev.nexacommerce.in 8.8.8.8
```

All four `awsdns` servers must appear before step 4.

## 4. Full apply

```bash
cd /d/product-service/infra/foundation
terraform plan -out=foundation.tfplan
terraform apply foundation.tfplan
terraform output github_variables
```

The ACM validation step waits until the DNS record resolves (normally a few minutes once delegation works).

## 5. Confirm SNS email

Click **Confirm subscription** in the AWS Notifications email sent to the alert address.

## 6. Commit

Commit `backend.hcl` files and `.terraform.lock.hcl`. Keep the `github_variables` output for Phase 3.
