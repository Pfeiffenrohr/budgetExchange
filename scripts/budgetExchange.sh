#!/bin/bash
cd /var/lib/budgetserver
sleep 3600
/usr/bin/java -jar /var/lib/budgetserver/budgetexchange.jar --budgetserverhost=$budgetserverhost --budgetserverport=$budgetserverport