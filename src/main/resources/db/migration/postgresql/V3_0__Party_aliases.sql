create table parties (
    player_id bigserial unique,
    party_name text not null,
    party_alias text);
create unique index parties_party_name_idx on parties(party_name);