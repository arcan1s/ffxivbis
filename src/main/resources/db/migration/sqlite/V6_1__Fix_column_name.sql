create table parties_new (
    party_id integer primary key autoincrement,
    party_name text not null,
    party_alias text);
insert into parties_new select player_id, party_name, party_alias from parties;

drop index parties_party_name_idx;
drop table parties;

alter table parties_new rename to parties;
create unique index parties_party_name_idx on parties(party_name);