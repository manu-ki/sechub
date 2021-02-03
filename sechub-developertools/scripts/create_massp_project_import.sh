#!/bin/bash
#create csv with headers
echo "ProjectId;Owner;Users;Profiles" >> massp_projects_massimport.csv
#add projects
#CAVE: i="first new massp project"   i<="last new massp project"
for ((i=201;i<=300;i++))
do
        echo "massp_${i};szwaller;external_pichlerm,pid7183;sast-gold-standard"  >> massp_projects_massimport.csv
done
