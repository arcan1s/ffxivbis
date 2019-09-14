from ffxivbis.models.piece import Piece


def test_parse_head(head_with_upgrade: Piece) -> None:
    assert Piece.get({'piece': 'head', 'is_tome': True}) == head_with_upgrade


def test_parse_weapon(weapon: Piece) -> None:
    assert Piece.get({'piece': 'weapon', 'is_tome': False}) == weapon


def test_parse_upgrade(head_with_upgrade: Piece) -> None:
    assert Piece.get({'piece': head_with_upgrade.upgrade.name, 'is_tome': True}) == head_with_upgrade.upgrade