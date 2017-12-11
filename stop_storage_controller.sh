#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/

VM_NAME0=dropboxish-controller-1
VM_NAME1=dropboxish-controller-2
VM_NAME2=dropboxish-controller-3

#gcloud compute firewall-rules delete --quiet ${VM_NAME}-www
gcloud compute instances delete --quiet ${VM_NAME0}
gcloud compute instances delete --quiet ${VM_NAME1}
gcloud compute instances delete --quiet ${VM_NAME2}
