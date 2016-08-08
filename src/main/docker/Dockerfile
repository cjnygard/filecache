FROM frolvlad/alpine-oraclejdk8:slim
ENV CACHE_ROOT_DIR /var/cache/filecache

VOLUME [ "$CACHE_ROOT_DIR", "/tmp" ]
COPY filecache-0.1.0.jar /
RUN sh -c 'touch /filecache-0.1.0.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dfilecache.rootdir=$CACHE_ROOT_DIR", "-jar","/filecache-0.1.0.jar"]

