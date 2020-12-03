drop index bis_piece_player_id_idx;
create index bis_piece_type_player_id_idx on bis(player_id, piece, piece_type);