from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Mapping, Type, Union

from .upgrade import Upgrade

from service.core.exceptions import InvalidDataRow


@dataclass
class Piece:
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

    @classmethod
    def get(cls: Type[Piece], data: Mapping[str, Any]) -> Union[Piece, Upgrade]:
        try:
            piece_type = data['piece']
            is_tome = bool(data['is_tome'])
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
