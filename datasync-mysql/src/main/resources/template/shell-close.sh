#!/usr/bin/env bash
echo "start"
source venv/bin/activate
chameleon stop_replica --config default --source mysql
echo "YES\n" | chameleon detach_replica --config default --source mysql
chameleon drop_replica_schema --config default
echo "end"