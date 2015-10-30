FROM java:8
VOLUME /tmp
ADD build/libs/server.jar server.jar
RUN bash -c 'touch /server.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/server.jar"]