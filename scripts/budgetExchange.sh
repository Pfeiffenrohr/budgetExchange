#!/bin/bash
cd /var/lib/budgetserver
sleep 20
/opt/java/openjdk/bin/java -jar /var/lib/budgetserver/budgetexchange.jar --budgetserverhost=$budgetserverhost --budgetserverport=$budgetserverport --databaseurl=$databaseurl