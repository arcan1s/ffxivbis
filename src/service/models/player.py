#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from dataclasses import dataclass
from typing import List, Optional, Union

from .bis import BiS
from .job import Job
from .piece import Piece
from .upgrade import Upgrade


@dataclass
class PlayerId:
    job: Job
    nick: str

    def __hash__(self) -> int:
        return hash(str(self))


@dataclass
class PlayerIdWithCounters(PlayerId):
    loot_count: int
    loot_count_bis: int
    loot_count_total: int


@dataclass
class PlayerIdFull:
    jobs: List[Job]
    nick: str


@dataclass
class Player:
    job: Job
    nick: str
    bis: BiS
    loot: List[Union[Piece, Upgrade]]
    link: Optional[str] = None
    priority: int = 0

    @property
    def player_id(self) -> PlayerId:
        return PlayerId(self.job, self.nick)

    def player_id_with_counters(self, piece: Union[Piece, Upgrade]) -> PlayerIdWithCounters:
        return PlayerIdWithCounters(self.job, self.nick, self.loot_count(piece),
                                    self.loot_count_bis(piece), self.loot_count_total(piece))

    # ordering methods
    def is_required(self, piece: Union[Piece, Upgrade]) -> bool:
        # lets check if it is even in bis
        if not self.bis.has_piece(piece):
            return False

        if isinstance(piece, Piece):
            # alright it is in is, lets check if he even got it
            return self.loot_count(piece) == 0
        elif isinstance(piece, Upgrade):
            # alright it lets check how much upgrades does they need
            return self.bis.upgrades_required[piece] > self.loot_count(piece)
        return False

    def loot_count(self, piece: Union[Piece, Upgrade]) -> int:
        return self.loot.count(piece)

    def loot_count_bis(self, _: Union[Piece, Upgrade]) -> int:
        return len([piece for piece in self.loot if self.bis.has_piece(piece)])

    def loot_count_total(self, _: Union[Piece, Upgrade]) -> int:
        return len(self.loot)

    def loot_priority(self, _: Union[Piece, Upgrade]) -> int:
        return self.priority
