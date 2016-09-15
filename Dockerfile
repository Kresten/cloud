#Get fresh image base
FROM henrikbaerbak/cloudarch:e16.1

#Create dir /root/cave and make workdir
WORKDIR /root/cave

#Copy all except what's in .dockerignore
COPY . .

#Prepare for future commands
RUN ant build.all

#Set entry-point
ENTRYPOINT ["/bin/bash","entry-point.sh"]