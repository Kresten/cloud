#Get fresh image base
FROM henrikbaerbak/cloudarch:e16.1

#Create dir /root/cave and make workdir
WORKDIR /root/cave

#Copy to image
COPY lib-core lib-core
COPY resource resource
COPY src src
COPY test test
COPY entry-point.sh ./
COPY build.xml ./
COPY ivy.xml ./
COPY *.cpf ./
COPY *.yml ./

#Set entry-point
ENTRYPOINT ["/bin/bash","entry-point.sh"]