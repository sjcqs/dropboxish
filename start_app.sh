#! /bin/bash
# source http://cgrant.io/tutorials/gcp/compute/gce/how-to-deploy-a-java-application-to-google-compute-engine/
BUCKET_NAME=dropboxish_deploy

JAR_NAME=app-0.0.1-SNAPSHOT.jar
VM_NAME=dropboxish-app

gradle :app:jar

gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:8080 --target-tags ${VM_NAME}
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./app/build/libs/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

CONTROLLER0=dropboxish-controller-1
CONTROLLER1=dropboxish-controller-2
CONTROLLER2=dropboxish-controller-3


addr0="$(gcloud compute instances list --filter="name~'${CONTROLLER0}'" --format="value(networkInterfaces[0].networkIP)")"
addr1="$(gcloud compute instances list --filter="name~'${CONTROLLER1}'" --format="value(networkInterfaces[0].networkIP)")"
addr2="$(gcloud compute instances list --filter="name~'${CONTROLLER2}'" --format="value(networkInterfaces[0].networkIP)")"

echo "#! /bin/bash

BUCKET_NAME=dropboxish_deploy
JAR_NAME=app-0.0.1-SNAPSHOT.jar

sudo su -

# install OpenJDK
apt-get update

apt-get install openjdk-8-jdk -y

mkdir /opt/gcedeploy

gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /opt/gcedeploy/${JAR_NAME}
java -jar /opt/gcedeploy/${JAR_NAME} ${addr0}:8090 ${addr1}:8090 ${addr2}:8090 2>&1 /opt/gcedeploy/log.txt &
exit
" > install_app.sh

gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --machine-type f1-micro \
  --metadata-from-file startup-script=install_app.sh

gcloud compute instances list --filter="name~'${VM_NAME}'" --format="value(networkInterfaces[0].accessConfigs[0].natIP)"
