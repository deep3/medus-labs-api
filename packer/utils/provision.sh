#!/bin/bash

if [[ "$(whoami)" != "root" ]]
then
  echo "Switching to root..."
  sudo su -s "$0"
fi

echo "Updating system..."
yum update -y

echo "Updating to java 8..."
sudo yum install -y java-1.8.0-openjdk.x86_64

sudo /usr/sbin/alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java

sudo /usr/sbin/alternatives --set javac /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/javac

echo "Installing docker"
sudo yum install -y docker

echo "Starting docker"
sudo service docker start

echo "Adding ec2-user user to docker group..."
sudo usermod -a -G docker ec2-user

echo "Done."