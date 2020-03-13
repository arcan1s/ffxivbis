-- loot
alter table loot add column piece_type text;

update loot set piece_type = 'Tome' where is_tome = 1;
update loot set piece_type = 'Savage' where is_tome = 0;

create table loot_new (
    loot_id integer primary key autoincrement,
    player_id integer not null,
    created integer not null,
    piece text not null,
    piece_type text not null,
    job text not null,
    foreign key (player_id) references players(player_id) on delete cascade);
insert into loot_new select loot_id, player_id, created, piece, piece_type, job from loot;

drop index loot_owner_idx;
drop table loot;

alter table loot_new rename to loot;
create index loot_owner_idx on loot(player_id);

-- bis
alter table bis add column piece_type text;

update bis set piece_type = 'Tome' where is_tome = 1;
update bis set piece_type = 'Savage' where is_tome = 0;

create table bis_new (
    player_id integer not null,
    created integer not null,
    piece text not null,
    piece_type text not null,
    job text not null,
    foreign key (player_id) references players(player_id) on delete cascade);
insert into bis_new select player_id, created, piece, piece_type, job from bis;

drop index bis_piece_player_id_idx;
drop table bis;

alter table bis_new rename to bis;
create unique index bis_piece_player_id_idx on bis(player_id, piece);