# Using the API Locally

## Prerequisites 

* [AWS CLI](https://aws.amazon.com/cli/) installed and configured with administrator permissions 
* Java 8+

# Configure

The API can be configured by changing values on the `src/main/resources/application.properties` file.

Currently there are no settings that need to be modified to run the application however it is **strongly** recommended you change.

`token.secret=ASecretKeyForJWTToken` for a more secure and unique value.


Configuration for log output can also be modified in `src/main/resources/logback.xml`
# Using

## Build 

Windows
```bash
gradlew.bat bootJar
```
OSX/Unix/Linux
```bash
./gradlew bootJar

```

See build/libs/ for the assembled jar file and launch using the standard `java -jar  awsbuilder-x.x.x-SNAPSHOT.jar`
## Test
Windows
```bash
gradlew.bat test
```
OSX/Unix/Linux
```bash
./gradlew test

```
## Run
Windows
```bash
Access file location: /build/libs
run the jar file: java -jar medus-labs-api-0.0.1-SNAPSHOT.jar
```
OSX/Unix/Linux
```bash
./gradlew bootRun

```

## Docker

For deployment you can package the API into a docker container complete with Java already installed.

Windows
```bash
gradlew.bat docker
```
OSX/Unix/Linux
```bash
./gradlew docker

```

You can view your Docker Image with the command:

```bash
docker images
```
This will allow you to view you docker image id.
```

You must give the local docker image a tag in the form of: <Image ID> <DockerId> / <RepoName>: <TagName>

```bash
docker image tag <Image id> co.uk.deep3/deep3-medus-labs-api:latest
```
Ensuring to change <Image id> to the actual id of the image created
```

To push to docker hub  use the form: <DockerId> / <RepoName>: <TagName>
```bash
docker push co.uk.deep3/deep3-medus-labs-api:latest
```


### Docker-compose

There is a handy docker compose file in the root of the project. 

You will need to expose and export your AWS credentials as environment variables for it to work.

##Windows
# Run terminal as Administrator.
```bash
SETX AWS_ACCESS_KEY_ID "<Your Access Key Id>" /M
SETX AWS_SECRET_ACCESS_KEY "<Your AWS secret access Key>" /M

##Linux

```bash
EXPORT AWS_ACCESS_KEY_ID=<Your Access Key Id>
EXPORT AWS_SECRET_ACCESS_KEY=<Your AWS secret access Key>

```

## Modify the docker-compose-local.yml to reference environment variables:

##Linux
"AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}"
"AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
```

Then you can run both the api and ui by doing the following
```bash
docker-compose -f docker-compose-local.yml

```

The application should now be running on `http://localhost/` or you can use just the api on `http://localhost:8080`


## Packer

First ensure you have packer installed locally

https://www.packer.io/intro/getting-started/install.html

Packer provides a way to build a collection of AMI's that are 
- Based on ec2 amazon linux
- Have docker pre installed
- Have a swarm configured that has pulled the api/ui from dockerhub

Generally as part of a deployment once you have updated the ui and api docker images on dockerhub you will want to rebuild the AMI.

You will need to export your aws credentials for it to work
 - `AWS_ACCESS_KEY_ID`
 - `AWS_SECRET_ACCESS_KEY

##Windows
# Run terminal as Administrator.
```bash
SETX AWS_ACCESS_KEY_ID "<Your Access Key Id>" /M
SETX AWS_SECRET_ACCESS_KEY "<Your AWS secret access Key>" /M

##Linux

```bash
EXPORT AWS_ACCESS_KEY_ID=<Your Access Key Id>
EXPORT AWS_SECRET_ACCESS_KEY=<Your AWS secret access Key>
```

## Modify the docker-compose-local.yml to reference environment variables:

##Linux
"AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}"
"AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"

```
cd packer
./make.sh
```
If you are on windows you can easily copy the packer command and tweak it to run in powershell, you can also edit the `make.sh` script to update the version and other build time options.

All make.sh does is pre-fll in a number options for running the packer build command, you can modify these as you wish

Note the AMI ID and region, you will need to update the ami id if you change regions. Its also worth checking the AMI id is still current. AWS will continually update that AMI id but its feasible in the future it may
be replaced with a newer AMI ID.

```bash
packer build -force \
    -var source_ami=ami-0ff8a91507f77f867 \
    -var aws_region=us-east-1 \
    -var version=1 \
    -var revision=0 \
    -var aws_access_key=$AWS_ACCESS_KEY_ID \
    -var aws_secret_key=$AWS_SECRET_ACCESS_KEY \
    ./packer.json
```
