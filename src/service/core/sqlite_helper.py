#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
# because sqlite3 does not support context management
import sqlite3

from types import TracebackType
from typing  import Any, Dict, Optional, Type


def dict_factory(cursor: sqlite3.Cursor, row: sqlite3.Row) -> Dict[str, Any]:
    return {
        key: value
        for key, value in zip([column[0] for column in cursor.description], row)
    }


class SQLiteHelper():
    def __init__(self, database_path: str) -> None:
        self.database_path = database_path

    def __enter__(self) -> sqlite3.Cursor:
        self.conn = sqlite3.connect(self.database_path)
        self.conn.row_factory = dict_factory
        self.conn.execute('''pragma foreign_keys = on''')
        return self.conn.cursor()

    def __exit__(self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException],
                 traceback: Optional[TracebackType]) -> None:
        self.conn.commit()
        self.conn.close()