# FFXIV BiS

Service which allows to manage savage loot distribution easy.

## REST API

### Party API

* `GET /api/v1/party`
    
    Get party list. Parameters:
    
    * `nick`: player full nickname to filter, string, optional.
    
* `POST /api/v1/party`

    Add or remove party member. Parameters:
    
    * `action`: action to do, string, required. One of `add`, `remove`.
    * `job`: player job name, string, required.
    * `nick`: player nickname, string, required.
    * `link`: link to ariyala set to parse BiS, string, optional.
    
### BiS API

* `GET /api/v1/party/bis`

    Get party/player BiS. Parameters:
    
    * `nick`: player full nickname to filter, string, optional.
    
* `POST /api/v1/party/bis`

    Add or remove item to/from BiS. Parameters:
    
    * `action`: action to do, string, required. One of `add`, `remove`.
    * `job`: player job name, string, required.
    * `nick`: player nickname, string, required.
    * `is_tome`: is item tome gear or not, bool, required.
    * `piece`: item name, string, required.
    
* `PUT /api/v1/party/bis`

    Create BiS from ariyala link. Parameters:
    
    * `job`: player job name, string, required.
    * `nick`: player nickname, string, required.
    * `link`: link to ariyala set to parse BiS, string, required.
    
### Loot API

* `GET /api/v1/party/loot`

    Get party/player loot. Parameters:
    
    * `nick`: player full nickname to filter, string, optional.
    
* `POST /api/v1/party/loot`

    Add or remove item to/from loot list. Parameters:
    
    * `action`: action to do, string, required. One of `add`, `remove`.
    * `job`: player job name, string, required.
    * `nick`: player nickname, string, required.
    * `is_tome`: is item tome gear or not, bool, required.
    * `piece`: item name, string, required.
    
* `PUT /api/v1/party/loot`

    Suggest players to get loot. Parameters:
    
    * `is_tome`: is item tome gear or not, bool, required.
    * `piece`: item name, string, required.


## Configuration

* `settings` section

    General project settings.

    * `include`: path to include configuration directory, string, optional.
    * `logging`: path to logging configuration, see `logging.ini` for reference, string, optional.
    * `database`: database provide name, string, required. Allowed values: `sqlite`.
    * `priority`: methods of `Player` class which will be called to sort players for loot priority, space separated list of strings, required.
    
* `ariyala` section

    Settings related to ariyala parser.
    
    * `ariyala_url`: ariyala base url, string, required.
    * `request_timeout`: xivapi request timeout, float, optional, default 30.
    * `xivapi_key`: xivapi developer key, string, optional.
    * `xivapi_url`: xivapi base url, string, required.
    
* `sqlite` section

    Database settings for `sqlite` provider.
    
    * `database_path`: path to sqlite database, string, required.
    * `migrations_path`: path to database migrations, string, required.
    
* `web` section

    Web server related settings.
    
    * `host`: address to bind, string, required.
    * `port`: port to bind, int, required.
    * `templates`: path to directory with jinja templates, string, required.