#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/

if [[ $# -ne 1 ]]; then
    echo "Indicate number of pools"
    exit
fi

VM_NAME=dropboxish-pool
for (( i=0; i < $1; i++ ))
do
    #gcloud compute firewall-rules delete --quiet ${VM_NAME}-www
    gcloud compute instances delete --quiet ${VM_NAME}"-$i"
done