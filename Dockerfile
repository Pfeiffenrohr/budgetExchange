FROM  eclipse-temurin:17

RUN apt-get update && apt-get install -y dos2unix
RUN mkdir /var/lib/budgetserver
RUN mkdir /var/lib/exchange
WORKDIR /var/lib/budgetserver/
ADD build/libs/budgetExchange-0.0.1-SNAPSHOT.jar /var/lib/budgetserver/budgetExchange.jar
ADD scripts/budgetExchange.sh /var/lib/budgetserver/budgetExchange.sh
RUN chmod 777 /var/lib/budgetserver/budgetExchange.sh
RUN dos2unix /var/lib/budgetserver/budgetExchange.sh
ENTRYPOINT ["/var/lib/budgetserver/budgetExchange.sh"]