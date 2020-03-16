alter table loot add column is_free_loot integer;

update loot set is_free_loot = 0;

create table loot_new (
    loot_id integer primary key autoincrement,
    player_id integer not null,
    created integer not null,
    piece text not null,
    piece_type text not null,
    job text not null,
    is_free_loot integer not null,
    foreign key (player_id) references players(player_id) on delete cascade);
insert into loot_new select loot_id, player_id, created, piece, piece_type, job, is_free_loot from loot;

drop index loot_owner_idx;
drop table loot;

alter table loot_new rename to loot;
create index loot_owner_idx on loot(player_id);