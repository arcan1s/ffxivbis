from typing import Any, List

from service.api.utils import make_json
from service.core.party import Party
from service.models.piece import Piece
from service.models.player import Player


async def test_bis_get(server: Any, party: Party, player: Player, player2: Player,
                       head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_item_bis(player.player_id, weapon)
    party.set_item_bis(player2.player_id, weapon)
    party.set_item_bis(player2.player_id, head_with_upgrade)

    response = await server.get('/api/v1/party/bis')
    assert response.status == 200
    assert await response.text() == make_json([weapon, weapon, head_with_upgrade], {}, 200)


async def test_bis_get_with_filter(server: Any, party: Party, player: Player, player2: Player,
                                   head_with_upgrade: Piece, weapon: Piece) -> None:
    party.set_item_bis(player.player_id, weapon)
    party.set_item_bis(player2.player_id, weapon)
    party.set_item_bis(player2.player_id, head_with_upgrade)

    response = await server.get('/api/v1/party/bis', params={'nick': player.nick})
    assert response.status == 200
    assert await response.text() == make_json([weapon], {'nick': player.nick}, 200)

    response = await server.get('/api/v1/party/bis', params={'nick': player2.nick})
    assert response.status == 200
    assert await response.text() == make_json([weapon, head_with_upgrade], {'nick': player2.nick}, 200)


async def test_bis_post_add(server: Any, player: Player, head_with_upgrade: Piece) -> None:
    response = await server.post('/api/v1/party/bis', json={
        'action': 'add',
        'piece': head_with_upgrade.name,
        'is_tome': head_with_upgrade.is_tome,
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200
    assert player.bis.has_piece(head_with_upgrade)


async def test_bis_post_remove(server: Any, player: Player, player2: Player, weapon: Piece) -> None:
    response = await server.post('/api/v1/party/bis', json={
        'action': 'remove',
        'piece': weapon.name,
        'is_tome': weapon.is_tome,
        'job': player.job.name,
        'nick': player.nick
    })
    assert response.status == 200
    assert not player.bis.has_piece(weapon)
    assert player2.bis.has_piece(weapon)


async def test_bis_put(server: Any, player: Player, bis_link: str, bis_set: List[Piece]) -> None:
    response = await server.put('/api/v1/party/bis', json={
        'job': player.job.name,
        'link': bis_link,
        'nick': player.nick
    })
    assert response.status == 200
    assert player.bis.pieces == bis_set
