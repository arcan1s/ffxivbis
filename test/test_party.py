from service.core.database import Database
from service.core.party import Party
from service.models.piece import Piece
from service.models.player import Player


def test_set_player(party: Party, player: Player) -> None:
    assert len(party.players) == 0

    party.set_player(player)
    assert len(party.players) == 1


def test_remove_player(party: Party, player: Player) -> None:
    party.remove_player(player.player_id)
    assert len(party.players) == 0

    party.set_player(player)
    party.remove_player(player.player_id)
    assert len(party.players) == 0


def test_set_bis_link(party: Party, player: Player, bis_link: str) -> None:
    party.set_player(player)

    party.set_bis_link(player.player_id, bis_link)
    assert player.link == bis_link


def test_set_item(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_player(player)

    party.set_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 1
    assert abs(player.loot_count(head_with_upgrade)) == 0


def test_remove_item(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_player(player)

    party.remove_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 0
    assert abs(player.loot_count(head_with_upgrade)) == 0

    party.set_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 1
    assert abs(player.loot_count(head_with_upgrade)) == 0

    party.remove_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 0
    assert abs(player.loot_count(head_with_upgrade)) == 0


def test_set_item_bis(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_player(player)

    party.set_item_bis(player.player_id, head_with_upgrade)
    assert player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)


def test_remove_item_bis(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_player(player)

    party.remove_item_bis(player.player_id, head_with_upgrade)
    assert not player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)

    party.set_item_bis(player.player_id, head_with_upgrade)
    assert player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)

    party.set_item_bis(player.player_id, weapon)
    assert player.bis.has_piece(head_with_upgrade)
    assert player.bis.has_piece(weapon)

    party.remove_item_bis(player.player_id, head_with_upgrade)
    assert not player.bis.has_piece(head_with_upgrade)
    assert player.bis.has_piece(weapon)


def test_get(party: Party, database: Database, player: Player, head_with_upgrade: Piece, weapon: Piece,
             bis_link: str) -> None:
    party.set_player(player)
    party.set_bis_link(player.player_id, bis_link)
    party.set_item_bis(player.player_id, head_with_upgrade)
    party.set_item_bis(player.player_id, weapon)
    party.set_item(player.player_id, weapon)

    new_party = Party.get(database)
    assert party.party == new_party.party

    party.remove_player(player.player_id)
    new_party = Party.get(database)
    assert party.party == new_party.party
