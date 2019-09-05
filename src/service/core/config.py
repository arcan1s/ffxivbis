#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import configparser
import os

from logging.config import fileConfig
from typing import Any, Dict, Mapping, Optional

from .exceptions import MissingConfiguration


class Configuration(configparser.RawConfigParser):

    def __init__(self) -> None:
        configparser.RawConfigParser.__init__(self, allow_no_value=True)
        self.path: Optional[str] = None
        self.root_path: Optional[str] = None

    @property
    def include(self) -> str:
        return self.__with_root_path(self.get('settings', 'include'))

    def __load_section(self, conf: str) -> None:
        self.read(os.path.join(self.include, conf))

    def __with_root_path(self, path: str) -> str:
        return os.path.join(self.root_path, path)

    def get_section(self, section: str) -> Dict[str, str]:
        if not self.has_section(section):
            raise MissingConfiguration(section)
        return dict(self[section])

    def load(self, path: str, values: Mapping[str, Mapping[str, Any]]) -> None:
        self.path = path
        self.root_path = os.path.dirname(self.path)

        self.read(self.path)
        self.load_includes()

        # don't use direct ConfigParser.update here, it overrides whole section
        for section, options in values.items():
            if section not in self:
                self.add_section(section)
            for key, value in options.items():
                self.set(section, key, value)

    def load_includes(self) -> None:
        try:
            include_dir = self.include
            for conf in filter(lambda p: p.endswith('.ini'), sorted(os.listdir(include_dir))):
                self.__load_section(conf)
        except (FileNotFoundError, configparser.NoOptionError, configparser.NoSectionError):
            pass

    def load_logging(self) -> None:
        fileConfig(self.__with_root_path(self.get('settings', 'logging')))
