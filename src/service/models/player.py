#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

import re

from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Type, Union

from .bis import BiS
from .job import Job
from .piece import Piece
from .serializable import Serializable
from .upgrade import Upgrade


@dataclass
class PlayerId(Serializable):
    job: Job
    nick: str

    @property
    def pretty_name(self) -> str:
        return f'{self.nick} ({self.job.name})'

    @classmethod
    def from_pretty_name(cls: Type[PlayerId], value: str) -> Optional[PlayerId]:
        matches = re.search('^(?P<nick>.*) \((?P<job>[A-Z]+)\)$', value)
        if matches is None:
            return None
        return PlayerId(Job[matches.group('job')], matches.group('nick'))

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return  {
            'job': {
                'description': 'player job name',
                '$ref': cls.model_ref('Job')
            },
            'nick': {
                'description': 'player nick name',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['job', 'nick']

    def __hash__(self) -> int:
        return hash(str(self))


@dataclass
class PlayerIdWithCounters(PlayerId):
    is_required: bool
    priority: int
    loot_count: int
    loot_count_bis: int
    loot_count_total: int
    bis_count_total: int


@dataclass
class Player(Serializable):
    job: Job
    nick: str
    bis: BiS
    loot: List[Union[Piece, Upgrade]]
    link: Optional[str] = None
    priority: int = 0

    @property
    def player_id(self) -> PlayerId:
        return PlayerId(self.job, self.nick)

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'bis': {
                'description': 'player BiS',
                '$ref': cls.model_ref('BiS')
            },
            'job': {
                'description': 'player job name',
                '$ref': cls.model_ref('Job')
            },
            'link': {
                'description': 'link to player BiS',
                'type': 'string'
            },
            'loot': {
                'description': 'player looted items',
                'type': 'array',
                'items': {
                    'anyOf': [
                        {'$ref': cls.model_ref('Piece')},
                        {'$ref': cls.model_ref('Upgrade')}
                    ]
                }
            },
            'nick': {
                'description': 'player nick name',
                'type': 'string'
            },
            'priority': {
                'description': 'player loot priority',
                'type': 'integer'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['bis', 'job', 'loot', 'nick', 'priority']

    def player_id_with_counters(self, piece: Union[Piece, Upgrade, None]) -> PlayerIdWithCounters:
        return PlayerIdWithCounters(self.job, self.nick, self.is_required(piece), self.priority,
                                    abs(self.loot_count(piece)), abs(self.loot_count_bis(piece)),
                                    abs(self.loot_count_total(piece)), abs(self.bis_count_total(piece)))

    # ordering methods
    def is_required(self, piece: Union[Piece, Upgrade, None]) -> bool:
        if piece is None:
            return False

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

    def loot_count(self, piece: Union[Piece, Upgrade, None]) -> int:
        if piece is None:
            return -self.loot_count_total(piece)
        return -self.loot.count(piece)

    def loot_count_bis(self, _: Union[Piece, Upgrade, None]) -> int:
        return -len([piece for piece in self.loot if self.bis.has_piece(piece)])

    def loot_count_total(self, _: Union[Piece, Upgrade, None]) -> int:
        return -len(self.loot)

    def bis_count_total(self, _: Union[Piece, Upgrade, None]) -> int:
        return len([piece for piece in self.bis.pieces if not piece.is_tome])

    def loot_priority(self, _: Union[Piece, Upgrade, None]) -> int:
        return self.priority
