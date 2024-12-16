#!/usr/bin/env python3

import json

data_array = []

threads = set()
ids = 1
_type = 1
for i in range((1000//50)):
    _len = i+1 * 50
    threads.add(str(i+1))
    for j in range(_len):
        data = { "_mk": None, 
                 "address": f"+{_len}-6505556789", 
                 "data": None,
                "date": "1733237444789",
                "date_sent": None,
                "error_code": 0,
                "formatted_date": None,
                "id": ids,
                "isIs_encrypted": False,
                "isIs_image": False,
                "isIs_key": False,
                "isRead": True,
                "message_id": str(f"{i}{j}"),
                "num_segments": 0,
                "status": 32,
                "subscription_id": 0,
                "tag": None,
                "text": "sasdfasdfasdfasdf",
                "thread_id": str(i+1),
                "type": 1 if _type == 2 else 2
              } 
        ids += 1
        _type = 1 if _type == 2 else 2
        data_array.append(data)

for thread_id in threads:
    count = 0
    for data in data_array:
        if data['thread_id'] == thread_id:
            count += 1
    print(thread_id, ":", count)

print("+ writing:", len(data_array))
with open('data.json', 'w') as fd:
    json.dump(data_array, fd)
