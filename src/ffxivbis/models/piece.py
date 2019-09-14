#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, List, Mapping, Type, Union

from ffxivbis.core.exceptions import InvalidDataRow

from .serializable import Serializable
from .upgrade import Upgrade



@dataclass
class Piece(Serializable):
    is_tome: bool
    name: str

    @property
    def upgrade(self) -> Upgrade:
        if not self.is_tome:
            return Upgrade.NoUpgrade
        elif isinstance(self, Waist) or isinstance(self, PieceAccessory):
            return Upgrade.AccessoryUpgrade
        elif isinstance(self, Weapon):
            return Upgrade.WeaponUpgrade
        elif isinstance(self, PieceGear):
            return Upgrade.GearUpgrade
        return Upgrade.NoUpgrade

    @staticmethod
    def available() -> List[str]:
        return [
            'weapon',
            'head', 'body', 'hands', 'waist', 'legs', 'feet',
            'ears', 'neck', 'wrist', 'left_ring', 'right_ring'
        ]

    @classmethod
    def get(cls: Type[Piece], data: Mapping[str, Any]) -> Union[Piece, Upgrade]:
        try:
            piece_type = data.get('piece') or data.get('name')
            if piece_type is None:
                raise KeyError
            is_tome = data['is_tome'] in ('yes', 'on', '1', 1, True)
        except KeyError:
            raise InvalidDataRow(data)
        if piece_type.lower() == 'weapon':
            return Weapon(is_tome)
        elif piece_type.lower() == 'head':
            return Head(is_tome)
        elif piece_type.lower() == 'body':
            return Body(is_tome)
        elif piece_type.lower() == 'hands':
            return Hands(is_tome)
        elif piece_type.lower() == 'waist':
            return Waist(is_tome)
        elif piece_type.lower() == 'legs':
            return Legs(is_tome)
        elif piece_type.lower() == 'feet':
            return Feet(is_tome)
        elif piece_type.lower() == 'ears':
            return Ears(is_tome)
        elif piece_type.lower() == 'neck':
            return Neck(is_tome)
        elif piece_type.lower() == 'wrist':
            return Wrist(is_tome)
        elif piece_type.lower() in ('left_ring', 'right_ring', 'ring'):
            return Ring(is_tome, piece_type.lower())
        elif piece_type.lower() in Upgrade.dict_types():
            return Upgrade[piece_type]
        else:
            raise InvalidDataRow(data)

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'is_tome': {
                'description': 'is this piece tome gear or not',
                'type': 'boolean'
            },
            'name': {
                'description': 'piece name',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['is_tome', 'name']


@dataclass
class PieceAccessory(Piece):
    pass


@dataclass
class PieceGear(Piece):
    pass


@dataclass
class Weapon(Piece):
    name: str = 'weapon'


@dataclass
class Head(PieceGear):
    name: str = 'head'


@dataclass
class Body(PieceGear):
    name: str = 'body'


@dataclass
class Hands(PieceGear):
    name: str = 'hands'


@dataclass
class Waist(PieceGear):
    name: str = 'waist'


@dataclass
class Legs(PieceGear):
    name: str = 'legs'


@dataclass
class Feet(PieceGear):
    name: str = 'feet'


@dataclass
class Ears(PieceAccessory):
    name: str = 'ears'


@dataclass
class Neck(PieceAccessory):
    name: str = 'neck'


@dataclass
class Wrist(PieceAccessory):
    name: str = 'wrist'


@dataclass
class Ring(PieceAccessory):
    name: str = 'ring'

    # override __eq__method to be able to compare left/right rings
    def __eq__(self, other: Any) -> bool:
        if not isinstance(other, Ring):
            return False
        return self.is_tome == other.is_tome
