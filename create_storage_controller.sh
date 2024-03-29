#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/


if [[ $# -ne 1 ]]; then
    echo "Indicate number of pools"
    exit
fi

BUCKET_NAME=dropboxish_deploy

JAR_NAME=controller-0.0.1-SNAPSHOT.jar

POOL_NAME=dropboxish-pool

VM_NAME0=dropboxish-controller-1
VM_NAME1=dropboxish-controller-2
VM_NAME2=dropboxish-controller-3

gradle :controller:jar

gcloud compute instances create ${VM_NAME0} \
  --tags ${VM_NAME1} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_storage_controller.sh
gcloud compute instances create ${VM_NAME1} \
  --tags ${VM_NAME1} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_storage_controller.sh
gcloud compute instances create ${VM_NAME2} \
  --tags ${VM_NAME2} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_storage_controller.sh

addr0="$(gcloud compute instances list --filter="name~'${VM_NAME0}'" --format="value(networkInterfaces[0].networkIP)")"
addr1="$(gcloud compute instances list --filter="name~'${VM_NAME1}'" --format="value(networkInterfaces[0].networkIP)")"
addr2="$(gcloud compute instances list --filter="name~'${VM_NAME2}'" --format="value(networkInterfaces[0].networkIP)")"

pools="-pools $1"
for (( i=0; i < $1; i++ ))
do
    addr="$(gcloud compute instances list --filter="name~'$POOL_NAME-$i'" --format="value(networkInterfaces[0].networkIP)")"
    pools="$pools ${addr}:8060"
done
cmd="#!/usr/bin/env bash
java -jar -Djava.net.preferIPv4Stack=true \
-Djgroups.tcpping.initial_hosts="${addr0}[7800],${addr1}[7800],${addr2}[7800]" \
${JAR_NAME} > out.log 2> err.log ${pools} &
exit"

echo -e "${cmd}" > run.sh

gcloud compute scp run.sh ${VM_NAME0}:~
gcloud compute scp run.sh ${VM_NAME1}:~
gcloud compute scp run.sh ${VM_NAME2}:~

gcloud compute scp ./controller/build/libs/${JAR_NAME} ${VM_NAME0}:~
gcloud compute scp ./controller/build/libs/${JAR_NAME} ${VM_NAME1}:~
gcloud compute scp ./controller/build/libs/${JAR_NAME} ${VM_NAME2}:~
