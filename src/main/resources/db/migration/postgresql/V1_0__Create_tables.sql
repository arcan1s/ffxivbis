create table players (
    party_id text not null,
    player_id bigserial unique,
    created bigint not null,
    nick text not null,
    job text not null,
    bis_link text,
    priority integer not null default 1);
create unique index players_nick_job_idx on players(party_id, nick, job);

create table loot (
    loot_id bigserial unique,
    player_id bigint not null,
    created bigint not null,
    piece text not null,
    is_tome integer not null,
    job text not null,
    foreign key (player_id) references players(player_id) on delete cascade);
create index loot_owner_idx on loot(player_id);

create table bis (
    player_id bigint not null,
    created bigint not null,
    piece text not null,
    is_tome integer not null,
    job text not null,
    foreign key (player_id) references players(player_id) on delete cascade);
create unique index bis_piece_player_id_idx on bis(player_id, piece);

create table users (
    party_id text not null,
    user_id bigserial unique,
    username text not null,
    password text not null,
    permission text not null);
create unique index users_username_idx on users(party_id, username);
