from enum import Enum, auto
from typing import List


class Upgrade(Enum):
    NoUpgrade = auto()
    AccessoryUpgrade = auto()
    GearUpgrade = auto()
    WeaponUpgrade = auto()

    @staticmethod
    def dict_types() -> List[str]:
        return list(map(lambda t: t.name.lower(), Upgrade))