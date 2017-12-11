#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/

BUCKET_NAME=dropboxish_deploy

JAR_NAME=pool-0.0.1-SNAPSHOT.jar
VM_NAME=dropboxish-pool

gradle :pool:jar
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./pool/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_storage_pool.sh