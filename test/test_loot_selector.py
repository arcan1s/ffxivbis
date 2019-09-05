from service.core.loot_selector import LootSelector
from service.models.piece import Piece
from service.models.player import Player


def test_suggest_by_need(selector: LootSelector, player: Player, player2: Player, head_with_upgrade: Piece) -> None:
    assert selector.suggest(head_with_upgrade) == \
           [player2.player_id_with_counters(head_with_upgrade), player.player_id_with_counters(head_with_upgrade)]


