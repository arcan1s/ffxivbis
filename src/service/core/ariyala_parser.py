import os
import requests

from typing import Dict, List, Optional

from service.models.piece import Piece

from .config import Configuration


class AriyalaParser:

    def __init__(self, config: Configuration) -> None:
        self.ariyala_url = config.get('ariyala', 'ariyala_url')
        self.xivapi_url = config.get('ariyala', 'xivapi_url')
        self.request_timeout = config.getfloat('ariyala', 'request_timeout')

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

    def get(self, url: str) -> List[Piece]:
        items = self.get_ids(url)
        return [
            Piece.get({'piece': slot, 'is_tome': self.get_is_tome(item_id)})  # type: ignore
            for slot, item_id in items.items()
        ]

    def get_ids(self, url: str) -> Dict[str, int]:
        norm_path = os.path.normpath(url)
        set_id = os.path.basename(norm_path)
        response = requests.get('{}/store.app'.format(self.ariyala_url), params={'identifier': set_id})
        response.raise_for_status()
        data = response.json()

        job = data['content']
        bis = data['datasets'][job]['normal']['items']

        result: Dict[str, int] = {}
        for original_key, value in bis.items():
            key = self.__remap_key(original_key)
            if key is None:
                continue
            result[key] = value
        return result

    def get_is_tome(self, item_id: int) -> bool:
        response = requests.get('{}/item/{}'.format(self.xivapi_url, item_id),
                                params={'columns': 'IsEquippable'},
                                timeout=self.request_timeout)
        response.raise_for_status()
        data = response.json()

        return data['IsEquippable'] == 0  # don't ask
