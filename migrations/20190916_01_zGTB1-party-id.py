'''
party id
'''

import random
import string

from yoyo import step

__depends__ = {'20190830_01_sYYZL-init-tables', '20190910_01_tgBmx-users-table'}
party_id = ''.join(random.sample(string.ascii_letters, 16))

steps = [
    step('''create table players2 (
            party_id text not null,
            player_id integer primary key,
            created integer not null,
            nick text not null,
            job text not null,
            bis_link text,
            priority integer not null default 1
        )'''),
    # not safe for injections, but sqlite and psycopg have different placeholders for parameters
    step('''insert into players2 select '%s' as party_id, players.* from players''' % (party_id,)),
    step('''drop index if exists players_nick_job_idx'''),
    step('''create unique index players_nick_job_idx on players2(party_id, nick, job)'''),

    step('''create table loot2 (
            loot_id integer primary key,
            player_id integer not null,
            created integer not null,
            piece text not null,
            is_tome integer not null,
            foreign key (player_id) references players2(player_id) on delete cascade
        )'''),
    step('''insert into loot2 select * from loot'''),
    step('''drop index if exists loot_owner_idx'''),
    step('''create index loot_owner_idx on loot(player_id)'''),

    step('''create table bis2 (
            player_id integer not null,
            created integer not null,
            piece text not null,
            is_tome integer not null,
            foreign key (player_id) references players2(player_id) on delete cascade
        )'''),
    step('''insert into bis2 select * from bis'''),
    step('''drop index if exists bis_piece_player_id_idx'''),
    step('''create unique index bis_piece_player_id_idx on bis2(player_id, piece)'''),

    step('''create table users2 (
            party_id text not null,
            user_id integer primary key,
            username text not null,
            password text not null,
            permission text not null,
            foreign key (party_id) references players2(party_id) on delete cascade
        )'''),
    # not safe for injections, but sqlite and psycopg have different placeholders for parameters
    step('''insert into users2 select '%s' as party_id, users.* from users''' % (party_id,)),
    step('''drop index if exists users_username_idx'''),
    step('''create unique index users_username_idx on users2(party_id, username)'''),

    step('''drop table users'''),
    step('''alter table users2 rename to users'''),

    step('''drop table loot'''),
    step('''alter table loot2 rename to loot'''),

    step('''drop table bis'''),
    step('''alter table bis2 rename to bis'''),

    step('''drop table players'''),
    step('''alter table players2 rename to players''')
]
