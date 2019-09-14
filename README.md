# FFXIV BiS

Service which allows to manage savage loot distribution easy.

## Installation and usage

This service requires python >= 3.7. For other dependencies see `setup.py`.

In general installation process looks like:

```bash
python setup.py build
python setup.py test  # if you want to run tests
```

Service can be run from `src` directory by using command:

```bash
python -m service.application.application
```

To see all available options type `--help`.

## Web service

REST API documentation is available at `http://0.0.0.0:8000/api-docs`. HTML representation is available at `http://0.0.0.0:8000`.

*Note*: host and port depend on configuration settings. 

## Configuration

* `settings` section

    General project settings.

    * `include`: path to include configuration directory, string, optional.
    * `logging`: path to logging configuration, see `logging.ini` for reference, string, optional.
    * `database`: database provide name, string, required. Allowed values: `sqlite`, `postgres`.
    * `priority`: methods of `Player` class which will be called to sort players for loot priority, space separated list of strings, required.
    
* `ariyala` section

    Settings related to ariyala parser.
    
    * `ariyala_url`: ariyala base url, string, required.
    * `request_timeout`: xivapi request timeout, float, optional, default 30.
    * `xivapi_key`: xivapi developer key, string, optional.
    * `xivapi_url`: xivapi base url, string, required.
    
* `auth` section

    Authentication settings.
    
    * `enabled`: whether authentication enabled or not, boolean, required.
    * `root_username`: username of administrator, string, required.
    * `root_password`: md5 hashed password of administrator, string, required.
    
* `postgres` section

    Database settings for `postgres` provider.
    
    * `database`: database name, string, required.
    * `host`: database host, string, required.
    * `password`: database password, string, required.
    * `port`: database port, int, required.
    * `username`: database username, string, required.
    * `migrations_path`: path to database migrations, string, required.
    
* `sqlite` section

    Database settings for `sqlite` provider.
    
    * `database_path`: path to sqlite database, string, required.
    * `migrations_path`: path to database migrations, string, required.
    
* `web` section

    Web server related settings.
    
    * `host`: address to bind, string, required.
    * `port`: port to bind, int, required.
    * `templates`: path to directory with jinja templates, string, required.