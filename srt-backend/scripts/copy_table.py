"""Copy every item from one DynamoDB table to another (full scan, batched writes).

Usage: python3 scripts/copy_table.py <source-table> <destination-table> [region]
Re-running is safe: items with the same key are overwritten.
"""
import sys

import boto3

src_table = sys.argv[1]
dst_table = sys.argv[2]
region = sys.argv[3] if len(sys.argv) > 3 else "us-east-1"

dynamodb = boto3.resource("dynamodb", region_name=region)
src = dynamodb.Table(src_table)
dst = dynamodb.Table(dst_table)

count = 0
scan_kwargs = {}
while True:
    resp = src.scan(**scan_kwargs)
    with dst.batch_writer() as batch:
        for item in resp.get("Items", []):
            batch.put_item(Item=item)
            count += 1
    if "LastEvaluatedKey" not in resp:
        break
    scan_kwargs["ExclusiveStartKey"] = resp["LastEvaluatedKey"]

print(f"Copied {count} items from {src_table} to {dst_table}")
