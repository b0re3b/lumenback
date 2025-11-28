#!/bin/bash
set -ex

echo ">>> Waiting for SES to be ready..."
sleep 5

echo ">>> Creating SES identities..."
awslocal ses verify-email-identity --email-address no-reply@lumen.local
awslocal ses verify-email-identity --email-address admin@lumen.local
awslocal ses verify-email-identity --email-address manager@lumen.local

echo ">>> Listing SES verified identities:"
awslocal ses list-identities

echo ">>> Local SES environment ready"
