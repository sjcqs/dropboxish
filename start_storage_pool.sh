#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/

if [[ $# -ne 1 ]]; then
    echo "Indicate number of pools"
    exit
fi

BUCKET_NAME=dropboxish_deploy

JAR_NAME=pool-0.0.1-SNAPSHOT.jar
VM_NAME=dropboxish-pool

gradle :pool:jar
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./pool/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

for (( i=0; i < $1; i++ ))
do
   name="${VM_NAME}-$i"
    gcloud compute instances create ${name} \
      --tags ${VM_NAME} \
      --machine-type f1-micro \
      --metadata-from-file startup-script=install_storage_pool.sh
done
