#! /bin/bash

BUCKET_NAME=dropboxish_deploy

JAR_NAME=dropboxish-controller-0.0.1-SNAPSHOT.jar
VM_NAME=dropboxish-controller

gsutil mb gs://${BUCKET_NAME}
gsutil cp ./storage-controller/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_storage_controller.sh
