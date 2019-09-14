#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from enum import auto
from typing import List

from .serializable import SerializableEnum


class Upgrade(SerializableEnum):
    NoUpgrade = auto()
    AccessoryUpgrade = auto()
    GearUpgrade = auto()
    WeaponUpgrade = auto()

    @staticmethod
    def dict_types() -> List[str]:
        return list(map(lambda t: t.name.lower(), Upgrade))