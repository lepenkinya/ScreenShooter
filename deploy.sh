#! /bin/bash

./gradlew distZip

ssh root@95.213.236.215 "rm -rf /root/web/*; mkdir web"
scp recognition-web/build/distributions/recognition-web.zip root@95.213.236.215:/root/web/
ssh root@95.213.236.215 "cd /root/web; unzip recognition-web.zip; rm -f recognition-web.zip"
ssh root@95.213.236.215 "fuser -k 4567/tcp; cd /root/web; nohup recognition-web/bin/recognition-web> out 2> error </dev/null &"
