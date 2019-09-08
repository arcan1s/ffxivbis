#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import os
import requests

from typing import Dict, List, Optional

from service.models.piece import Piece

from .config import Configuration


class AriyalaParser:

    def __init__(self, config: Configuration) -> None:
        self.ariyala_url = config.get('ariyala', 'ariyala_url')
        self.xivapi_key = config.get('ariyala', 'xivapi_key', fallback=None)
        self.xivapi_url = config.get('ariyala', 'xivapi_url')
        self.request_timeout = config.getfloat('ariyala', 'request_timeout', fallback=30)

    def __remap_key(self, key: str) -> Optional[str]:
        if key == 'mainhand':
            return 'weapon'
        elif key == 'chest':
            return 'body'
        elif key == 'ringLeft':
            return 'left_ring'
        elif key == 'ringRight':
            return 'right_ring'
        elif key in ('head', 'hands', 'waist', 'legs', 'feet', 'ears', 'neck', 'wrist'):
            return key
        return None

    def get(self, url: str, job: str) -> List[Piece]:
        items = self.get_ids(url, job)
        return [
            Piece.get({'piece': slot, 'is_tome': self.get_is_tome(item_id)})  # type: ignore
            for slot, item_id in items.items()
        ]

    def get_ids(self, url: str, job: str) -> Dict[str, int]:
        norm_path = os.path.normpath(url)
        set_id = os.path.basename(norm_path)
        response = requests.get('{}/store.app'.format(self.ariyala_url), params={'identifier': set_id})
        response.raise_for_status()
        data = response.json()

        # it has job in response but for some reasons job name differs sometimes from one in dictionary,
        # e.g. http://ffxiv.ariyala.com/store.app?identifier=1AJB8
        api_job = data['content']
        try:
            bis = data['datasets'][api_job]['normal']['items']
        except KeyError:
            bis = data['datasets'][job]['normal']['items']

        result: Dict[str, int] = {}
        for original_key, value in bis.items():
            key = self.__remap_key(original_key)
            if key is None:
                continue
            result[key] = value
        return result

    def get_is_tome(self, item_id: int) -> bool:
        params = {'columns': 'IsEquippable'}
        if self.xivapi_key is not None:
            params['private_key'] = self.xivapi_key

        response = requests.get('{}/item/{}'.format(self.xivapi_url, item_id),
                                params=params, timeout=self.request_timeout)
        response.raise_for_status()
        data = response.json()

        return data['IsEquippable'] == 0  # don't ask
