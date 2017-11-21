#! /bin/bash
VM_NAME=dropboxish-controller

#gcloud compute firewall-rules delete --quiet ${VM_NAME}-www
gcloud compute instances delete --quiet ${VM_NAME}
