#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from service.core.config import Configuration

from .core import Application


def get_config(config_path: str) -> Configuration:
    config = Configuration()
    config.load(config_path, {})
    config.load_logging()

    return config


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='Simple loot recorder for FFXIV')
    parser.add_argument('-c', '--config', help='configuration path', default='ffxivbis.ini')
    args = parser.parse_args()

    config = get_config(args.config)
    app = Application(config)
    app.run()
