# OpenID Connect authorization server
[![Build Status](https://travis-ci.org/alfa-laboratory/ratauth.svg?branch=master)](https://travis-ci.org/alfa-laboratory/ratauth)
[![Coverage Status](https://coveralls.io/repos/github/alfa-laboratory/ratauth/badge.svg)](https://coveralls.io/github/alfa-laboratory/ratauth)
## Local run

`SERVER_PORT=8080 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`

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
