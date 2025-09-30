#!/bin/bash
set -e

echo ">>> Creating S3 buckets..."
awslocal s3 mb s3://local-movie-videos
awslocal s3 mb s3://local-movie-posters