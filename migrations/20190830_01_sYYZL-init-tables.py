'''
init tables
'''

from yoyo import step

__depends__ = {}

steps = [
    step('''create table players (
        player_id integer primary key,
        created integer not null,
        nick text not null,
        job text not null,
        bis_link text,
        priority integer not null default 1
    )'''),
    step('''create unique index players_nick_job_idx on players(nick, job)'''),

    step('''create table loot (
        loot_id integer primary key,
        player_id integer not null,
        created integer not null,
        piece text not null,
        is_tome integer not null,
        foreign key (player_id) references players(player_id) on delete cascade
    )'''),
    step('''create index loot_owner_idx on loot(player_id)'''),

    step('''create table bis (
        player_id integer not null,
        created integer not null,
        piece text not null,
        is_tome integer not null,
        foreign key (player_id) references players(player_id) on delete cascade
    )'''),
    step('''create unique index bis_piece_player_id_idx on bis(player_id, piece)''')
]
