#! /bin/bash

BUCKET_NAME=dropboxish_deploy
JAR_NAME=pool-0.0.1-SNAPSHOT.jar

sudo su -


# install OpenJDK
apt-get update

apt-get install openjdk-8-jdk -y

mkdir /opt/gcedeploy

gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /opt/gcedeploy/${JAR_NAME}
java -jar /opt/gcedeploy/${JAR_NAME} &
exit
