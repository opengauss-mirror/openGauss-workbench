#!/usr/bin/env bash
echo "begin uninstall..."
sh server.sh stop
rm -rf /ops
echo "end uninstall"