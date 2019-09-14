#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

from enum import auto
from typing import Tuple

from .piece import Piece, PieceAccessory, Weapon
from .serializable import SerializableEnum


class Job(SerializableEnum):
    PLD = auto()
    WAR = auto()
    DRK = auto()
    GNB = auto()
    WHM = auto()
    SCH = auto()
    AST = auto()
    MNK = auto()
    DRG = auto()
    NIN = auto()
    SAM = auto()
    BRD = auto()
    MCH = auto()
    DNC = auto()
    BLM = auto()
    SMN = auto()
    RDM = auto()

    @staticmethod
    def group_accs_dex() -> Tuple:
        return Job.group_ranges() + (Job.NIN,)

    @staticmethod
    def group_accs_str() -> Tuple:
        return Job.group_mnk() + (Job.DRG,)

    @staticmethod
    def group_casters() -> Tuple:
        return (Job.BLM, Job.SMN, Job.RDM)

    @staticmethod
    def group_healers() -> Tuple:
        return (Job.WHM, Job.SCH, Job.AST)

    @staticmethod
    def group_mnk() -> Tuple:
        return (Job.MNK, Job.SAM)

    @staticmethod
    def group_ranges() -> Tuple:
        return (Job.BRD, Job.MCH, Job.DNC)

    @staticmethod
    def group_tanks() -> Tuple:
        return (Job.PLD, Job.WAR, Job.DRK, Job.GNB)

    @staticmethod
    def has_same_loot(left: Job, right: Job, piece: Piece) -> bool:
        # same jobs, alright
        if left == right:
            return True

        # weapons are unique per class always
        if isinstance(piece, Weapon):
            return False

        # group comparison
        for group in (Job.group_casters(), Job.group_healers(), Job.group_mnk(), Job.group_ranges(), Job.group_tanks()):
            if left in group and right in group:
                return True

        # accessories group comparison
        if isinstance(Piece, PieceAccessory):
            for group in (Job.group_accs_dex(), Job.group_accs_str()):
                if left in group and right in group:
                    return True

        return False

