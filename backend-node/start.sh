#!/bin/sh
node src/yahoo.js &
node src/poller.js &
node src/websocket.js
