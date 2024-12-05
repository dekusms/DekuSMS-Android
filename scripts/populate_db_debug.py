#!/usr/bin/env python3

import json

data_array = []

for i in range((1000//50)):
    _len = i * 50
    for j in range(_len):
        data = { "_mk": None, 
                 "address": f"+16505556789_{i}", 
                 "data": None,
                "date": "1733237444789",
                "date_sent": None,
                "error_code": 0,
                "formatted_date": None,
                "id": j,
                "isIs_encrypted": False,
                "isIs_image": False,
                "isIs_key": False,
                "isRead": True,
                "message_id": f"{i}{j}",
                "num_segments": 0,
                "status": 32,
                "subscription_id": 0,
                "tag": None,
                "text": "sasdfasdfasdfasdf",
                "thread_id": str(i),
                "type": 3
              } 
        data_array.append(data)

print("+ writing:", len(data_array))
with open('data.json', 'w') as fd:
    json.dump(data_array, fd)
