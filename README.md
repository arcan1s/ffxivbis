# FFXIV BiS

Service which allows to manage savage loot distribution easy.

## Installation and usage

In general installation process looks like:

```bash
sbt assembly
```

Service can be run by using command:

```bash
java -cp ./target/scala-2.13/ffxivbis-scala-assembly-0.1.jar me.arcanis.ffxivbis.ffxivbis 
```

## Web service

REST API documentation is available at `http://0.0.0.0:8000/swagger`. HTML representation is available at `http://0.0.0.0:8000`.

*Note*: host and port depend on configuration settings. 

and add new password to configuration.
