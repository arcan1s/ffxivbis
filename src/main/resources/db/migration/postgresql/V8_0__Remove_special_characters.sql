update parties set party_alias = regexp_replace(party_alias, '[^A-Za-z0-9!@#$%^&*()\-_=+;:'',./? ]', '', 'g');
update players set nick = regexp_replace(nick, '[^A-Za-z0-9!@#$%^&*()\-_=+;:'',./? ]', '', 'g');
update users set username = regexp_replace(username, '[^A-Za-z0-9!@#$%^&*()\-_=+;:'',./? ]', '', 'g');