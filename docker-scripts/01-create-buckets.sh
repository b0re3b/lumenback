#!/bin/bash
set -ex

echo ">>> Waiting for LocalStack to start..."
sleep 5

echo ">>> Creating S3 buckets..."
awslocal s3 mb s3://local-movie-videos
awslocal s3 mb s3://local-movie-posters

echo ">>> Local S3 environment ready"
