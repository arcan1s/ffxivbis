import json

from aiohttp.web import Response
from typing import Any, Mapping, List

from .json import HttpEncoder


def make_json(response: Any, args: Mapping[str, Any], code: int = 200) -> str:
    return json.dumps({
        'arguments': dict(args),
        'response': response,
        'status': code
    }, cls=HttpEncoder, sort_keys=True)


def wrap_exception(exception: Exception, args: Mapping[str, Any], code: int = 500) -> Response:
    return wrap_json({'message': repr(exception)}, args, code)


def wrap_invalid_param(params: List[str], args: Mapping[str, Any], code: int = 400) -> Response:
    return wrap_json({'message': 'invalid or missing parameters: `{}`'.format(params)}, args, code)


def wrap_json(response: Any, args: Mapping[str, Any], code: int = 200) -> Response:
    return Response(
        text=make_json(response, args, code),
        status=code,
        content_type='application/json'
    )
