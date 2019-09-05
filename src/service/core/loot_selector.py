from typing import Iterable, List, Tuple, Union

from service.models.player import Player, PlayerIdWithCounters
from service.models.piece import Piece
from service.models.upgrade import Upgrade

from .party import Party


class LootSelector:

    def __init__(self, party: Party, order_by: List[str] = None) -> None:
        self.party = party
        self.order_by = order_by or ['is_required', 'loot_count_bis', 'loot_count_total', 'loot_count', 'loot_priority']

    def __order_by(self, player: Player, piece: Union[Piece, Upgrade]) -> Tuple:
        return tuple(map(lambda method: getattr(player, method)(piece), self.order_by))

    def __sorted_by(self, piece: Union[Piece, Upgrade]) -> Iterable[Player]:
        # pycharm is lying, don't trust it
        return sorted(self.party.players.values(), key=lambda player: self.__order_by(player, piece), reverse=True)

    def suggest(self, piece: Union[Piece, Upgrade]) -> List[PlayerIdWithCounters]:
        return [player.player_id_with_counters(piece) for player in self.__sorted_by(piece)]