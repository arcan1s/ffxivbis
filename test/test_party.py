from ffxivbis.core.database import Database
from ffxivbis.core.party import Party
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player


async def test_set_player(party: Party, player: Player) -> None:
    assert len(party.players) == 0

    await party.set_player(player)
    assert len(party.players) == 1


async def test_remove_player(party: Party, player: Player) -> None:
    await party.remove_player(player.player_id)
    assert len(party.players) == 0

    await party.set_player(player)
    await party.remove_player(player.player_id)
    assert len(party.players) == 0


async def test_set_bis_link(party: Party, player: Player, bis_link: str) -> None:
    await party.set_player(player)

    await party.set_bis_link(player.player_id, bis_link)
    assert player.link == bis_link


async def test_set_item(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    await party.set_player(player)

    await party.set_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 1
    assert abs(player.loot_count(head_with_upgrade)) == 0


async def test_remove_item(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    await party.set_player(player)

    await party.remove_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 0
    assert abs(player.loot_count(head_with_upgrade)) == 0

    await party.set_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 1
    assert abs(player.loot_count(head_with_upgrade)) == 0

    await party.remove_item(player.player_id, weapon)
    assert abs(player.loot_count(weapon)) == 0
    assert abs(player.loot_count(head_with_upgrade)) == 0


async def test_set_item_bis(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    await party.set_player(player)

    await party.set_item_bis(player.player_id, head_with_upgrade)
    assert player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)


async def test_remove_item_bis(party: Party, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    await party.set_player(player)

    await party.remove_item_bis(player.player_id, head_with_upgrade)
    assert not player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)

    await party.set_item_bis(player.player_id, head_with_upgrade)
    assert player.bis.has_piece(head_with_upgrade)
    assert not player.bis.has_piece(weapon)

    await party.set_item_bis(player.player_id, weapon)
    assert player.bis.has_piece(head_with_upgrade)
    assert player.bis.has_piece(weapon)

    await party.remove_item_bis(player.player_id, head_with_upgrade)
    assert not player.bis.has_piece(head_with_upgrade)
    assert player.bis.has_piece(weapon)


async def test_get(party: Party, database: Database, player: Player, head_with_upgrade: Piece,
                   weapon: Piece, bis_link: str) -> None:
    await party.set_player(player)
    await party.set_bis_link(player.player_id, bis_link)
    await party.set_item_bis(player.player_id, head_with_upgrade)
    await party.set_item_bis(player.player_id, weapon)
    await party.set_item(player.player_id, weapon)

    new_party = await Party.get(database)
    assert party.party == new_party.party

    await party.remove_player(player.player_id)
    new_party = await Party.get(database)
    assert party.party == new_party.party
