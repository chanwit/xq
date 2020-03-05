#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cat $DIR/test_case_0001 \
| xq --json '[0].with{"[settings.kubernetes]\napi-server=\"${Endpoint}\"\ncluster-certificate=\"${CertificateAuthority.Data}\"\ncluster-name=\"cluster\""}' -o raw