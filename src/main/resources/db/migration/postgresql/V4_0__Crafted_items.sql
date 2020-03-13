-- loot
alter table loot add column piece_type text;

update loot set piece_type = 'Tome' where is_tome = 1;
update loot set piece_type = 'Savage' where is_tome = 0;

alter table loot alter column piece_type set not null;
alter table loot drop column is_tome;

-- bis
alter table bis add column piece_type text;

update bis set piece_type = 'Tome' where is_tome = 1;
update bis set piece_type = 'Savage' where is_tome = 0;

alter table bis alter column piece_type set not null;
alter table bis drop column is_tome;