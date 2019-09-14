#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import json

from aiohttp.web import HTTPException, Response
from typing import Any, Mapping, List

from .json import HttpEncoder


def make_json(response: Any) -> str:
    return json.dumps(response, cls=HttpEncoder, sort_keys=True)


def wrap_exception(exception: Exception, args: Mapping[str, Any], code: int = 500) -> Response:
    if isinstance(exception, HTTPException):
        raise exception  # reraise return
    return wrap_json({
        'message': repr(exception),
        'arguments': dict(args)
    }, code)


def wrap_invalid_param(params: List[str], args: Mapping[str, Any], code: int = 400) -> Response:
    return wrap_json({
        'message': f'invalid or missing parameters: `{params}`',
        'arguments': dict(args)
    }, code)


def wrap_json(response: Any, code: int = 200) -> Response:
    return Response(
        text=make_json(response),
        status=code,
        content_type='application/json'
    )
