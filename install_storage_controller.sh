#! /bin/bash

BUCKET_NAME=dropboxish_deploy
JAR_NAME=controller-0.0.1-SNAPSHOT.jar

sudo su -


# install OpenJDK
apt-get update

apt-get install openjdk-8-jdk -y

mkdir /opt/gcedeploy

gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /opt/gcedeploy/${JAR_NAME}
java -jar -Djava.net.preferIPv4Stack=true -Djgroups.tcpping.initial_hosts=10.132.0.2[7800],10.132.0.3[7800],10.132.0.4[7800] /opt/gcedeploy/controller-0.0.1-SNAPSHOT.jar
exit
