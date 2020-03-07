xq - Any Configuration Processor
================================

`xq` aims to be a processor for any configuration format. Currently, `xq` supports JSON (`--json`), YAML (`--yaml`), and line-by-line text processing `--line`.

`xq` is generally slower than other processors, like `jq`, because it comes with a full programming language support. So `xq` can solve many limitations we might face when using other processors.

Examples
========

Here's the `data.json` used as our examples.
```
{
    "apiVersion": "v1",
    "kind": "Example",
    "spec": {
        "containers": [
            {
                "commands": [
                    "kube-proxy",
                    "--v=2"
                ]
            }
        ]
    }
}
```

### Get field `kind`

`$ cat data.json | xq --json '.kind' -o raw` 

### Generate text wth template

`$ cat data.json | xq --json '.with{"apiVersion=${apiVersion}\nkind=${kind}\n"}' -o raw`

### Convert JSON to YAML

`$ cat data.json | xq --json '' -o yaml`

### Inplace edit and output as YAML

`$ cat data.json | xq --json '.tap{spec.containers[0].commands[1]="--v=4"}' -o yaml`

### Line replace with regexp

`$ cat data.json | xq --line '.replace(/kube-proxy/, "k8s-proxy")'`
