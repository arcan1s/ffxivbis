# FFXIV BiS

[![Build status](https://github.com/arcan1s/ffxivbis/actions/workflows/run-tests.yml/badge.svg)](https://github.com/arcan1s/ffxivbis/actions/workflows/run-tests.yml) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/arcan1s/ffxivbis)

Service which allows managing savage loot distribution easy.

## Installation and usage

In general compilation process looks like:

```bash
sbt dist
```

Or alternatively you can download the latest distribution zip from the releases page. Service can be run by using command:

```bash
bin/ffxivbis
```

from the extracted archive root.

## Web service

REST API documentation is available at `http://0.0.0.0:8000/api-docs`. HTML representation is available at `http://0.0.0.0:8000`.

*Note*: host and port depend on configuration settings. 

## Public service

There is also public service which is available at https://ffxivbis.arcanis.me.
