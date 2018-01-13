#! /bin/bash

BUCKET_NAME=dropboxish_deploy
JAR_NAME=app-0.0.1-SNAPSHOT.jar

sudo su -

# install OpenJDK
apt-get update

apt-get install openjdk-8-jdk -y

mkdir /opt/gcedeploy

gsutil cp gs://dropboxish_deploy/app-0.0.1-SNAPSHOT.jar /opt/gcedeploy/app-0.0.1-SNAPSHOT.jar
java -jar /opt/gcedeploy/app-0.0.1-SNAPSHOT.jar 10.132.0.8:8090 10.132.0.9:8090 10.132.0.10:8090 &
exit

