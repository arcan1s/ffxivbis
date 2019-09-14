#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from typing import Any, Dict, List, Type

from .serializable import Serializable


class PlayerEdit(Serializable):

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'action': {
                'description': 'action to perform',
                '$ref': cls.model_ref('Action')
            },
            'job': {
                'description': 'player job name to edit',
                '$ref': cls.model_ref('Job')
            },
            'nick': {
                'description': 'player nick name to edit',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['action', 'nick', 'job']