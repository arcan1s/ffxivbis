from typing import Any

from service.api.utils import make_json
from service.core.party import Party
from service.models.piece import Piece
from service.models.player import Player


async def test_loot_get(server: Any, party: Party, player: Player, player2: Player, weapon: Piece) -> None:
    await party.set_item(player.player_id, weapon)
    await party.set_item(player2.player_id, weapon)

    response = await server.get('/api/v1/party/loot')
    assert response.status == 200
    assert await response.text() == make_json([weapon, weapon])


async def test_loot_get_with_filter(server: Any, party: Party, player: Player, player2: Player, weapon: Piece) -> None:
    await party.set_item(player.player_id, weapon)
    await party.set_item(player2.player_id, weapon)

    response = await server.get('/api/v1/party/loot', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([weapon])

    response = await server.get('/api/v1/party/loot', params={'nick': player2.nick})
    assert response.status == 200
    assert await response.text() == make_json([weapon])


async def test_loot_post_add(server: Any, player: Player, weapon: Piece) -> None:
    response = await server.get('/api/v1/party/loot')
    assert response.status == 200
    assert await response.text() == make_json([])
    assert weapon not in player.loot

    response = await server.post('/api/v1/party/loot', json={
        'action': 'add',
        'name': weapon.name,
        'is_tome': weapon.is_tome,
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200
    assert weapon in player.loot


async def test_loot_post_remove(server: Any, player: Player, head_with_upgrade: Piece, weapon: Piece) -> None:
    assert weapon not in player.loot
    player.loot.append(weapon)
    player.loot.append(weapon)
    assert player.loot.count(weapon) == 2

    response = await server.post('/api/v1/party/loot', json={
        'action': 'remove',
        'name': weapon.name,
        'is_tome': weapon.is_tome,
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200
    assert player.loot.count(weapon) == 1

    player.loot.append(head_with_upgrade)

    response = await server.post('/api/v1/party/loot', json={
        'action': 'remove',
        'name': weapon.name,
        'is_tome': weapon.is_tome,
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200
    assert player.loot.count(weapon) == 0
    assert player.loot.count(head_with_upgrade) == 1


async def test_loot_put(server: Any, player: Player, player2: Player, head_with_upgrade: Piece) -> None:
    response = await server.put('/api/v1/party/loot', json={
        'is_tome': head_with_upgrade.is_tome,
        'name': head_with_upgrade.name
    })
    assert response.status == 200
    assert await response.text() == make_json(
        [player2.player_id_with_counters(head_with_upgrade), player.player_id_with_counters(head_with_upgrade)]
    )
