#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

from enum import Enum
from typing import Any, Dict, List, Type


class Serializable:

    @classmethod
    def model_name(cls: Type[Serializable]) -> str:
        return cls.__name__

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        raise NotImplementedError

    @staticmethod
    def model_ref(model_name: str, model_group: str = 'schemas') -> str:
        return f'#/components/{model_group}/{model_name}'

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return []

    @classmethod
    def model_spec(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'type': cls.model_type(),
            'properties': cls.model_properties(),
            'required': cls.model_required()
        }

    @classmethod
    def model_type(cls: Type[Serializable]) -> str:
        return 'object'


class SerializableEnum(Serializable, Enum):

    @classmethod
    def model_spec(cls: Type[SerializableEnum]) -> Dict[str, Any]:
        return {
            'type': cls.model_type(),
            'enum': [item.name for item in cls]
        }

    @classmethod
    def model_type(cls: Type[Serializable]) -> str:
        return 'string'