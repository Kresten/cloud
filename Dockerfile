#Get fresh image base
FROM henrikbaerbak/cloudarch:e16.1

#create dir /root/cave and make workdir
WORKDIR /root/cave

#Copy all except what's in .dockerignore
COPY . .

#Prepare for commands
RUN ant build.all

#Set entry-point
ENTRYPOINT ["/bin/bash","entry-point.sh"]