REST json API description to interact with FFXIV Best-in-slot service.

# Basic workflow

* Create party using `PUT /api/v1/party` endpoint. It consumes username and password of administrator (which can't be restored). As the result it returns unique id of created party.
* Create new users which have access to this party. Note that user belongs to specific party id and in scope of the specified party it must be unique.
* Add players with their best in slot sets (probably by using ariyala links).
* Add loot items if any.
* By using `PUT /api/v1/party/{partyId}/loot` API find players which are better for the specified loot.
* Add new loot item to the selected player.

# Limitations

No limitations for the API so far.

# Authentication

For the most party utils service requires user to be authenticated. User permission can be one of `get`, `post` or `admin`.

* `admin` permission means that the user is allowed to do anything, especially this permission is required to be able to add or modify users.
* `post` permission is required to deal with the most POST API endpoints, but to be precise only endpoints which modifies party content require this permission.
* `get` permission is required to have access to party.

`admin` permission includes any other permissions, `post` allows to perform get requests.

<security-definitions />