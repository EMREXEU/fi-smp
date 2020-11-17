FROM alpine:edge
MAINTAINER CSC
RUN apk add --no-cache openjdk11
COPY ./target/smp-0.0.1-SNAPSHOT.war /opt/fi_smp/lib/
#COPY ./fi_smp_entrypoint.sh /opt/fi_smp/bin/
ENV SPRING_APPLICATION_JSON='{"spring": "test_json"}'
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/opt/fi_smp/lib/smp-0.0.1-SNAPSHOT.war"]
VOLUME /var/lib/fi_smp_server
EXPOSE 9002
