from service.models.bis import BiS
from service.models.piece import Piece
from service.models.upgrade import Upgrade


def test_set_item(bis: BiS, weapon: Piece) -> None:
    bis.set_item(weapon)
    assert bis.has_piece(weapon)


def test_remove_item(bis: BiS, weapon: Piece) -> None:
    test_set_item(bis, weapon)
    bis.remove_item(weapon)
    assert not bis.has_piece(weapon)


def test_upgrades_required(bis: BiS, weapon: Piece, head_with_upgrade: Piece) -> None:
    bis.set_item(weapon)
    bis.set_item(head_with_upgrade)
    assert bis.upgrades_required == {Upgrade.NoUpgrade: 1, Upgrade.GearUpgrade: 1}
