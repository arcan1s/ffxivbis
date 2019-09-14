#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import itertools

from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Type, Union

from .job import Job
from .piece import Piece
from .serializable import Serializable
from .upgrade import Upgrade


@dataclass
class BiSLink(Serializable):
    nick: str
    job: Job
    link: str

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'job': {
                'description': 'player job name',
                '$ref': cls.model_ref('Job')
            },
            'link': {
                'description': 'link to BiS set',
                'example': 'https://ffxiv.ariyala.com/19V5R',
                'type': 'string'
            },
            'nick': {
                'description': 'player nick name',
                'example': 'Siuan Sanche',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['job', 'link', 'nick']


@dataclass
class BiS(Serializable):
    weapon: Optional[Piece] = None
    head: Optional[Piece] = None
    body: Optional[Piece] = None
    hands: Optional[Piece] = None
    waist: Optional[Piece] = None
    legs: Optional[Piece] = None
    feet: Optional[Piece] = None
    ears: Optional[Piece] = None
    neck: Optional[Piece] = None
    wrist: Optional[Piece] = None
    left_ring: Optional[Piece] = None
    right_ring: Optional[Piece] = None

    @property
    def pieces(self) -> List[Piece]:
        return [piece for piece in self.__dict__.values() if isinstance(piece, Piece)]

    @property
    def upgrades_required(self) -> Dict[Upgrade, int]:
        return {
            upgrade: len(list(pieces))
            for upgrade, pieces in itertools.groupby(self.pieces, lambda piece: piece.upgrade)
        }

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'weapon': {
                'description': 'weapon part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'head': {
                'description': 'head part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'body': {
                'description': 'body part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'hands': {
                'description': 'hands part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'waist': {
                'description': 'waist part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'legs': {
                'description': 'legs part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'feet': {
                'description': 'feet part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'ears': {
                'description': 'ears part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'neck': {
                'description': 'neck part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'wrist': {
                'description': 'wrist part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'left_ring': {
                'description': 'left_ring part of BiS',
                '$ref': cls.model_ref('Piece')
            },
            'right_ring': {
                'description': 'right_ring part of BiS',
                '$ref': cls.model_ref('Piece')
            }
        }

    def has_piece(self, piece: Union[Piece, Upgrade]) -> bool:
        if isinstance(piece, Piece):
            return piece in self.pieces
        elif isinstance(piece, Upgrade):
            return self.upgrades_required.get(piece) is not None
        return False

    def set_item(self, piece: Union[Piece, Upgrade]) -> None:
        setattr(self, piece.name, piece)

    def remove_item(self, piece: Union[Piece, Upgrade]) -> None:
        setattr(self, piece.name, None)