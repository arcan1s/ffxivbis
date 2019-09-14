#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
# because sqlite3 does not support context management
import aiosqlite

from types import TracebackType
from typing  import Any, Dict, Optional, Type


def dict_factory(cursor: aiosqlite.Cursor, row: aiosqlite.Row) -> Dict[str, Any]:
    return {
        key: value
        for key, value in zip([column[0] for column in cursor.description], row)
    }


class SQLiteHelper():
    def __init__(self, database_path: str) -> None:
        self.database_path = database_path

    async def __aenter__(self) -> aiosqlite.Cursor:
        self.conn = await aiosqlite.connect(self.database_path)
        self.conn.row_factory = dict_factory
        await self.conn.execute('''pragma foreign_keys = on''')
        return await self.conn.cursor()

    async def __aexit__(self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException],
                        traceback: Optional[TracebackType]) -> None:
        await self.conn.commit()
        await self.conn.close()