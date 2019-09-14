#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Application
from apispec import APISpec

from ffxivbis.core.version import __version__
from ffxivbis.models.action import Action
from ffxivbis.models.bis import BiS, BiSLink
from ffxivbis.models.error import Error
from ffxivbis.models.job import Job
from ffxivbis.models.loot import Loot
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player, PlayerId, PlayerIdWithCounters
from ffxivbis.models.player_edit import PlayerEdit
from ffxivbis.models.upgrade import Upgrade
from ffxivbis.models.user import User


def get_spec(app: Application) -> APISpec:
    spec = APISpec(
        title='FFXIV loot helper',
        version=__version__,
        openapi_version='3.0.2',
        info=dict(description='Loot manager for FFXIV statics'),
    )

    # routes
    for route in app.router.routes():
        path = route.get_info().get('path') or route.get_info().get('formatter')
        method = route.method.lower()

        spec_method = f'endpoint_{method}_spec'
        if not hasattr(route.handler, spec_method):
            continue
        operations = getattr(route.handler, spec_method)()
        if not operations:
            continue

        spec.path(path, operations={method: operations})

    # components
    spec.components.schema(Action.model_name(), Action.model_spec())
    spec.components.schema(BiS.model_name(), BiS.model_spec())
    spec.components.schema(BiSLink.model_name(), BiSLink.model_spec())
    spec.components.schema(Error.model_name(), Error.model_spec())
    spec.components.schema(Job.model_name(), Job.model_spec())
    spec.components.schema(Loot.model_name(), Loot.model_spec())
    spec.components.schema(Piece.model_name(), Piece.model_spec())
    spec.components.schema(Player.model_name(), Player.model_spec())
    spec.components.schema(PlayerEdit.model_name(), PlayerEdit.model_spec())
    spec.components.schema(PlayerId.model_name(), PlayerId.model_spec())
    spec.components.schema(PlayerIdWithCounters.model_name(), PlayerIdWithCounters.model_spec())
    spec.components.schema(Upgrade.model_name(), Upgrade.model_spec())
    spec.components.schema(User.model_name(), User.model_spec())

    # default responses
    spec.components.response('BadRequest', dict(
        description='Bad parameters applied or bad request was formed',
        content={'application/json': {'schema': {'$ref': Error.model_ref('Error')}}}
    ))
    spec.components.response('Forbidden', dict(
        description='User permissions do not allow this action'
    ))
    spec.components.response('ServerError', dict(
        description='Server was unable to process request',
        content={'application/json': {'schema': {'$ref': Error.model_ref('Error')}}}
    ))
    spec.components.response('Unauthorized', dict(
        description='User was not authorized'
    ))

    return spec