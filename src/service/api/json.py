#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from enum import Enum
from json import JSONEncoder
from typing import Any


class HttpEncoder(JSONEncoder):
    def default(self, obj: Any) -> Any:
        if isinstance(obj, dict):
            data = {}
            for key, value in obj.items():
                data[key] = self.default(value)
            return data
        elif isinstance(obj, Enum):
            return obj.name
        elif hasattr(obj, '_ast'):
            return self.default(obj._ast())
        elif hasattr(obj, '__iter__') and not isinstance(obj, str):
            return [self.default(value) for value in obj]
        elif hasattr(obj, '__dict__'):
            data = {
                key: self.default(value)
                for key, value in obj.__dict__.items()
                if not callable(value) and not key.startswith('_')}
            return data
        else:
            return obj
