#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

from typing import Any, Dict, List, Optional, Type

from service.models.serializable import Serializable


class OpenApi(Serializable):

    @classmethod
    def endpoint_delete_spec(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {}

    @classmethod
    def endpoint_get_description(cls: Type[OpenApi]) -> Optional[str]:
        return None

    @classmethod
    def endpoint_get_parameters(cls: Type[OpenApi]) -> List[Dict[str, Any]]:
        return []

    @classmethod
    def endpoint_get_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {}

    @classmethod
    def endpoint_get_summary(cls: Type[OpenApi]) -> Optional[str]:
        return None

    @classmethod
    def endpoint_get_tags(cls: Type[OpenApi]) -> List[str]:
        return []

    @classmethod
    def endpoint_get_spec(cls: Type[OpenApi]) -> Dict[str, Any]:
        description = cls.endpoint_get_description()
        if description is None:
            return {}
        return {
            'description': description,
            'parameters': cls.endpoint_get_parameters(),
            'responses': cls.endpoint_with_default_responses(cls.endpoint_get_responses()),
            'summary': cls.endpoint_get_summary(),
            'tags': cls.endpoint_get_tags()
        }

    @classmethod
    def endpoint_post_consumes(cls: Type[OpenApi]) -> List[str]:
        return ['application/json']

    @classmethod
    def endpoint_post_description(cls: Type[OpenApi]) -> Optional[str]:
        return None

    @classmethod
    def endpoint_post_request_body(cls: Type[OpenApi], content_type: str) -> str:
        return ''

    @classmethod
    def endpoint_post_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {}

    @classmethod
    def endpoint_post_summary(cls: Type[OpenApi]) -> Optional[str]:
        return None

    @classmethod
    def endpoint_post_tags(cls: Type[OpenApi]) -> List[str]:
        return []

    @classmethod
    def endpoint_post_spec(cls: Type[OpenApi]) -> Dict[str, Any]:
        description = cls.endpoint_post_description()
        if description is None:
            return {}
        return {
            'consumes': cls.endpoint_post_consumes(),
            'description': description,
            'requestBody': {
                'content': {
                    content_type: {
                        'schema': {'$ref': cls.model_ref(cls.endpoint_post_request_body(content_type))}
                    }
                    for content_type in cls.endpoint_post_consumes()
                }
            },
            'responses': cls.endpoint_with_default_responses(cls.endpoint_post_responses()),
            'summary': cls.endpoint_post_summary(),
            'tags': cls.endpoint_post_tags()
        }

    @classmethod
    def endpoint_spec(cls: Type[OpenApi], operations: List[str]) -> Dict[str, Any]:
        return {
            operation.lower(): getattr(cls, f'endpoint_{operation.lower()}_spec')
            for operation in operations
        }

    @classmethod
    def endpoint_with_default_responses(cls: Type[OpenApi], responses: Dict[str, Any]) -> Dict[str, Any]:
        responses.update({
            '400': {'$ref': cls.model_ref('BadRequest', 'responses')},
            '401': {'$ref': cls.model_ref('Unauthorized', 'responses')},
            '403': {'$ref': cls.model_ref('Forbidden', 'responses')},
            '500': {'$ref': cls.model_ref('ServerError', 'responses')}
        })
        return responses
