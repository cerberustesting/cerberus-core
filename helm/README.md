
## Introduction

This chart bootstraps a [Cerberus](https://github.com/cerberustesting/cerberus-source) deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.


## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+

## Installing the Chart

To install the chart with the release name `my-release`:

```console
$ cd app/
$ helm install my-release 
```

These commands deploy Cerberus on the Kubernetes cluster in the default configuration. You can override default values using -f parameter.

> **Tip**: List all releases using `helm list`

## Uninstalling the Chart

To uninstall/delete the `my-release` release:

```console
$ helm delete my-release
```

The command removes all the Kubernetes components associated with the chart and deletes the release. Remove also the chart using `--purge` option:

```console
$ helm delete --purge my-release
```
