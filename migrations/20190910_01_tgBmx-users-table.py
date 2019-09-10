'''
users table
'''

from yoyo import step

__depends__ = {}

steps = [
    step('''create table users (
        user_id integer primary key,
        username text not null,
        password text not null,
        permission text not null
    )'''),
    step('''create unique index users_username_idx on users(username)''')
]
