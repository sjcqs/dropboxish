#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/

BUCKET_NAME=dropboxish_deploy

JAR_NAME=controller-0.0.1-SNAPSHOT.jar

VM_NAME0=dropboxish-controller-1
VM_NAME1=dropboxish-controller-2
VM_NAME2=dropboxish-controller-3

gradle :controller:jar

# gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:80 --target-tags ${VM_NAME}
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./controller/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}


gcloud compute instances create ${VM_NAME0} \
  --tags ${VM_NAME0} \
  --machine-type f1-micro
gcloud compute instances create ${VM_NAME1} \
  --tags ${VM_NAME0} \
  --machine-type f1-micro
gcloud compute instances create ${VM_NAME2} \
  --tags ${VM_NAME0} \
  --machine-type f1-micro

addr0="$(gcloud compute instances list --filter="name~'${VM_NAME0}'" --format="value(networkInterfaces[0].networkIP)")"
addr1="$(gcloud compute instances list --filter="name~'${VM_NAME1}'" --format="value(networkInterfaces[0].networkIP)")"
addr2="$(gcloud compute instances list --filter="name~'${VM_NAME2}'" --format="value(networkInterfaces[0].networkIP)")"

cmd="java -jar -Djava.net.preferIPv4Stack=true \
-Djgroups.tcpping.initial_hosts="${addr0}[7800],${addr1}[7800],${addr2}[7800]" \
/opt/gcedeploy/${JAR_NAME}
exit"

echo -e "$(cat install_storage_controller.template)\n${cmd}" > install_storage_controller.sh

gcloud compute instances add-metadata ${VM_NAME0} \
  --metadata-from-file startup-script=install_storage_controller.sh
gcloud compute instances add-metadata ${VM_NAME1} \
  --metadata-from-file startup-script=install_storage_controller.sh
gcloud compute instances add-metadata ${VM_NAME2} \
  --metadata-from-file startup-script=install_storage_controller.sh