from typing import Any, Mapping


class InvalidDatabase(Exception):

    def __init__(self, database_type: str) -> None:
        Exception.__init__(self, 'Unsupported database {}'.format(database_type))


class InvalidDataRow(Exception):

    def __init__(self, data: Mapping[str, Any]) -> None:
        Exception.__init__(self, 'Invalid data row `{}`'.format(data))


class MissingConfiguration(Exception):

    def __init__(self, section: str) -> None:
        Exception.__init__(self, 'Missing configuration section {}'.format(section))