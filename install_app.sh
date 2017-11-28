#! /bin/bash

BUCKET_NAME=dropboxish_deploy
JAR_NAME=dropboxish-app-0.0.1-SNAPSHOT.jar

sudo su -

# install OpenJDK
apt-get update

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get install openjdk-8-jdk -y

mkdir /opt/gcedeploy

gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /opt/gcedeploy/${JAR_NAME}
java -jar /opt/gcedeploy/${JAR_NAME} &
exit
