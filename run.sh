#!/usr/bin/env bash
java -jar -Djava.net.preferIPv4Stack=true -Djgroups.tcpping.initial_hosts=10.132.0.8[7800],10.132.0.9[7800],10.132.0.10[7800] controller-0.0.1-SNAPSHOT.jar > out.log 2> err.log -pools 6 10.132.0.2:8060 10.132.0.3:8060 10.132.0.4:8060 10.132.0.5:8060 10.132.0.6:8060 10.132.0.7:8060 &
exit
