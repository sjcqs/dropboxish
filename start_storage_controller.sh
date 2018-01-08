#!/usr/bin/env bash

VM_NAME0=dropboxish-controller-1
VM_NAME1=dropboxish-controller-2
VM_NAME2=dropboxish-controller-3

if [[ $# -eq 0 ]]; then
    # Start all the instances
    gcloud compute instances start ${VM_NAME0} ${VM_NAME1} ${VM_NAME2}
    else
     for arg in $*; do
        case "$arg" in
            0)
            gcloud compute instances start ${VM_NAME0};;
            1)
            gcloud compute instances start ${VM_NAME1};;
            2)
            gcloud compute instances start ${VM_NAME2};;
        esac
     done
fi