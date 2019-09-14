#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from typing import Any, Mapping


class InvalidDatabase(Exception):

    def __init__(self, database_type: str) -> None:
        Exception.__init__(self, f'Unsupported database {database_type}')


class InvalidDataRow(Exception):

    def __init__(self, data: Mapping[str, Any]) -> None:
        Exception.__init__(self, f'Invalid data row `{data}`')


class MissingConfiguration(Exception):

    def __init__(self, section: str) -> None:
        Exception.__init__(self, f'Missing configuration section {section}')