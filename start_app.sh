#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/
BUCKET_NAME=dropboxish_deploy

JAR_NAME=app-0.0.1-SNAPSHOT.jar
VM_NAME=dropboxish-app

gradle :app:jar

gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:8080 --target-tags ${VM_NAME}
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./app/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_app.sh

gcloud compute instances list --filter="name~'${VM_NAME}'" --format="value(networkInterfaces[0].accessConfigs[0].natIP)"
