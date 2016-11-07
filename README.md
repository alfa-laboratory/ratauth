# OpenID Connect authorization server
[![Build Status](https://travis-ci.org/alfa-laboratory/ratauth.svg?branch=master)](https://travis-ci.org/alfa-laboratory/ratauth)
[![Coverage Status](https://coveralls.io/repos/github/alfa-laboratory/ratauth/badge.svg)](https://coveralls.io/github/alfa-laboratory/ratauth)
[![Download](https://api.bintray.com/packages/alfa-laboratory/passport/ratauth/images/download.svg)](https://bintray.com/alfa-laboratory/passport/ratauth/_latestVersion)
## Local run

`SERVER_PORT=8080 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`

or use `docker-compose` util. See details below

And then, see docs and try to use register API

## Build

Build docker image with prefix:

    ./gradlew buildImage -PimagePrefix=myDockerImagePrefix
    Using tag 'myDockerImagePrefix/ratauth:1.1.0-dev.3.uncommitted74f4acf' for image.

Build with docker compose:

    docker-compose -f docker-compose-build.yml up

It produce docker image with `latest` tag - `ratpack:latest` without prefix

Or without prefix:

    ./gradlew buildImage
    Using tag 'ratauth:1.1.0-dev.3.uncommitted74f4acf' for image.

## Run

Run built docker image. Image with statically added ratauth jar. Work only you already built image. See `Build with docker compose` in Build section

    docker-compose -f docker-compose-staticimage up

Run with jar file. Build jar before

    docker-compose up

## Advanced Configuration

If you need to change username for publish docker image to registry or change
registry name, follow next instruction:

1. make gradle.properties file (it not index by git, see `.gitignore`) in
project dir or your home directory
1. add next properties to gradle.properties:
```
systemProp.org.ajoberstar.grgit.auth.force=hardcoded # needed for nebula release plugin
DOCKER_USERNAME=USERNAME                             # docker registry username
DOCKER_REPOSITORY=DOCKER_REGISTRY_NAME               # docker registry name for correct image creation. Like <you_username>/ratauth:tag
DOCKER_PASSWORD=API_KEY                              # api key or password for authenticate in docker registry
```

And now, you can push image to registry by gradle command `./gradlew pushImage`

If you need gradle.properties in Travis CI, you will be able to encrypt gradle.properties by travis ci command `travis`
For example: `travis encrypt-file gradle.properties`

See TravisCI [Official Instruction](https://docs.travis-ci.com/user/encryption-keys/)
