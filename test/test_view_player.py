from typing import Any, List

from service.api.utils import make_json
from service.core.party import Party
from service.models.piece import Piece
from service.models.player import Player


async def test_players_get(server: Any, party: Party, player: Player) -> None:
    await party.set_player(player)

    response = await server.get('/api/v1/party')
    assert response.status == 200
    assert await response.text() == make_json(party.party, {}, 200)


async def test_players_get_with_filter(server: Any, party: Party, player: Player, player2: Player) -> None:
    await party.set_player(player)

    response = await server.get('/api/v1/party', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([player], {'nick': player.nick}, 200)

    response = await server.get('/api/v1/party', params={'nick': player2.nick})
    assert response.status == 200
    assert await response.text() == make_json([player2], {'nick': player2.nick}, 200)


async def test_players_post_add(server: Any, party: Party, player: Player) -> None:
    await party.remove_player(player.player_id)

    response = await server.get('/api/v1/party', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([], {'nick': player.nick}, 200)

    response = await server.post('/api/v1/party', json={
        'action': 'add',
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200

    assert player.player_id in party.players


async def test_players_post_remove(server: Any, party: Party, player: Player) -> None:
    response = await server.get('/api/v1/party', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([player], {'nick': player.nick}, 200)

    response = await server.post('/api/v1/party', json={
        'action': 'remove',
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200

    response = await server.get('/api/v1/party', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([], {'nick': player.nick}, 200)

    assert player.player_id not in party.players


async def test_players_post_add_with_link(server: Any, party: Party, player: Player,
                                          bis_link: str, bis_set: List[Piece]) -> None:
    await party.remove_player(player.player_id)

    response = await server.get('/api/v1/party', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([], {'nick': player.nick}, 200)

    response = await server.post('/api/v1/party', json={
        'action': 'add',
        'job': player.job.name,
        'nick': player.nick,
        'link': bis_link
    })
    assert response.status == 200

    assert party.players[player.player_id].bis.pieces == bis_set
