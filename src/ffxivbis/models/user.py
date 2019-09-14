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
class User(Serializable):
    username: str
    password: str
    permission: str

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'password': {
                'description': 'user password',
                'type': 'string'
            },
            'permission': {
                'default': 'get',
                'description': 'user action permissions',
                'type': 'string',
                'enum': ['admin', 'get', 'post']
            },
            'username': {
                'description': 'user name',
                'type': 'string'
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['password', 'username']