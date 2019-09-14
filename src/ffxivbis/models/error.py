#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from dataclasses import dataclass
from typing import Any, Dict, List, Type

from .serializable import Serializable


@dataclass
class Error(Serializable):
    message: str
    arguments: Dict[str, Any]

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'arguments': {
                'description': 'arguments passed to request',
                'type': 'object',
                'additionalProperties': True
            },
            'message': {
                'description': 'error message',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['arguments', 'message']