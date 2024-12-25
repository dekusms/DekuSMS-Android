#!/bin/bash

./populate_db_debug.py && adb push data.json /sdcard/
