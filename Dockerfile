FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y temurin-17-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

RUN apt-get update && apt-get install -y dos2unix
RUN mkdir /var/lib/budgetserver
RUN mkdir /var/lib/exchange
WORKDIR /var/lib/budgetserver/
ADD build/libs/budgetExchange-0.0.1-SNAPSHOT.jar /var/lib/budgetserver/budgetExchange.jar
ADD scripts/budgetExchange.sh /var/lib/budgetserver/budgetExchange.sh
RUN chmod 777 /var/lib/budgetserver/budgetExchange.sh
RUN dos2unix /var/lib/budgetserver/budgetExchange.sh
ENTRYPOINT ["/var/lib/budgetserver/budgetExchange.sh"]