#!/usr/bin/env bash

if [[ $# -ne 1 ]]; then
    echo "Indicate number of pools"
    exit
fi

./start_storage_pool.sh $1

./create_storage_controller.sh $1

./start_app.sh