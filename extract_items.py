import json
import requests

# NOTE: it does not cover all items, just workaround to extract most gear pieces from patches
MIN_ILVL = 580
MAX_ILVL = 605

TOME = (
    'radiant',
)
SAVAGE = (
    'asphodelos',
)


payload = {
    'queries': [
        {
            'slots': []
        },
        {
            'jobs': [],
            'minItemLevel': 580,
            'maxItemLevel': 605
        }
    ],
    'existing': []
}
# it does not support application/json
r = requests.post('https://ffxiv.ariyala.com/items.app', data=json.dumps(payload))
r.raise_for_status()

result = []

for item in r.json():
    item_id = item['itemID']
    source_dict = item['source']
    name = item['name']['en']
    if 'crafting' in source_dict:
        source = 'Crafted'
    elif 'gathering' in source_dict:
        continue  # some random shit
    elif 'purchase' in source_dict:
        if any(tome in name.lower() for tome in TOME):
            source = 'Tome'
        elif any(savage in name.lower() for savage in SAVAGE):
            source = 'Savage'
        else:
            source = None
            continue
    else:
        raise RuntimeError(f'Unknown source {source_dict}')
    result.append({'id': item_id, 'source': source, 'name': name})

output = {'cached-items': result}
print(json.dumps(output, indent=4, sort_keys=True))
