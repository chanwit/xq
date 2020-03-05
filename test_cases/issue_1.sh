#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cat $DIR/issue_1.json \
| xq --json '.tap{containers[0].command=["kube","1","2"]}' -o yaml \
| xq --line '.replace(/kube/,"k8s")' \
| xq --yaml '.containers'
