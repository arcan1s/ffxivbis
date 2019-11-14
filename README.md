# FFXIV BiS

Service which allows to manage savage loot distribution easy.

## Installation and usage

In general compliation process looks like:

```bash
sbt dist
```

Or alternatively you can download latest distribution zip from the releases page. Service can be run by using command:

```bash
bin/ffxivbis
```

from the extracted archive root.

## Web service

REST API documentation is available at `http://0.0.0.0:8000/swagger`. HTML representation is available at `http://0.0.0.0:8000`.

*Note*: host and port depend on configuration settings. 

## Public service

There is also public service which is available at https://ffxivbis.arcanis.me.