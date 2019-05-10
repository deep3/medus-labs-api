#!/bin/bash
packer build -force \
    -var source_ami=ami-0ff8a91507f77f867 \
    -var aws_region=us-east-1 \
    -var version=1 \
    -var revision=0 \
    -var aws_access_key=$AWS_ACCESS_KEY_ID \
    -var aws_secret_key=$AWS_SECRET_ACCESS_KEY \
    ./packer.json