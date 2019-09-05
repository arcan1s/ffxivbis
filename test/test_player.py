from service.models.piece import Piece
from service.models.player import Player


def test_loot_count(player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert abs(player.loot_count(head_with_upgrade)) == 0
    assert abs(player.loot_count(weapon)) == 0

    player.loot.append(head_with_upgrade)
    assert abs(player.loot_count(head_with_upgrade)) == 1
    assert abs(player.loot_count(weapon)) == 0

    player.loot.append(weapon)
    assert abs(player.loot_count(head_with_upgrade)) == 1
    assert abs(player.loot_count(weapon)) == 1


def test_loot_count_bis(player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert abs(player.loot_count_bis(head_with_upgrade)) == 0
    assert abs(player.loot_count_bis(weapon)) == 0

    player.bis.set_item(head_with_upgrade)
    player.loot.append(head_with_upgrade)
    assert abs(player.loot_count_bis(head_with_upgrade)) == 1
    assert abs(player.loot_count_bis(weapon)) == 1

    player.bis.set_item(weapon)
    assert abs(player.loot_count_bis(head_with_upgrade)) == 1
    assert abs(player.loot_count_bis(weapon)) == 1


def test_loot_count_total(player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert abs(player.loot_count_total(head_with_upgrade)) == 0
    assert abs(player.loot_count_total(weapon)) == 0

    player.loot.append(head_with_upgrade)
    assert abs(player.loot_count_total(head_with_upgrade)) == 1
    assert abs(player.loot_count_total(weapon)) == 1

    player.loot.append(weapon)
    assert abs(player.loot_count_total(head_with_upgrade)) == 2
    assert abs(player.loot_count_total(weapon)) == 2


def test_loot_priority(player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert abs(player.priority) == abs(player.loot_priority(head_with_upgrade))
    assert abs(player.priority) == abs(player.loot_priority(weapon))

    player.loot.append(head_with_upgrade)
    assert abs(player.priority) == abs(player.loot_priority(head_with_upgrade))
    assert abs(player.priority) == abs(player.loot_priority(weapon))

    player.loot.append(weapon)
    assert abs(player.priority) == abs(player.loot_priority(head_with_upgrade))
    assert abs(player.priority) == abs(player.loot_priority(weapon))


def test_is_required(player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert not player.is_required(weapon)
    assert not player.is_required(head_with_upgrade)

    player.bis.set_item(weapon)
    assert player.is_required(weapon)
    assert not player.is_required(head_with_upgrade)

    player.loot.append(weapon)
    assert not player.is_required(weapon)
    assert not player.is_required(head_with_upgrade)

    player.bis.set_item(head_with_upgrade)
    assert not player.is_required(weapon)
    assert player.is_required(head_with_upgrade)
    assert player.is_required(head_with_upgrade.upgrade)