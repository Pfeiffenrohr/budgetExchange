FROM  eclipse-temurin:17
# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /opt/java/openjdk/bin/
RUN export JAVA_HOME

RUN apt-get update && apt-get install -y dos2unix
RUN mkdir /var/lib/budgetserver
RUN mkdir /var/lib/exchange
WORKDIR /var/lib/budgetserver/
ADD build/libs/budgetExchange-0.0.1-SNAPSHOT.jar /var/lib/budgetserver/budgetexchange.jar
ADD scripts/budgetExchange.sh /var/lib/budgetserver/budgetexchange.sh
RUN chmod 777 /var/lib/budgetserver/budgetexchange.sh
RUN dos2unix /var/lib/budgetserver/budgetexchange.sh
ENTRYPOINT ["/var/lib/budgetserver/budgetExchange.sh"]