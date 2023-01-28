timeTotal=0
successTotal=0
logTotal=0
totalRepairVariant=0 
bugsRepaired=0
timedout=0
touch variant_data
touch time_data
for project in $(ls $1) ; do
    for bug in $(ls $1\\$project) ; do
        found=0
        if $( grep -Fxq "$bug" $2 ) ; then
            continue
        fi
        for file in $(ls $1\\$project\\$bug) ; do
            if [[ "$file" == "log"* ]] ; then
                logTotal=$(( logTotal + 1 ))
                if (( $( cat $1\\$project\\$bug\\$file | grep "Total elapsed time" | wc -l ) == 0 )) ; then
                    timedout=$(( timedout + 1 ))
                fi
                if ( cat $1\\$project\\$bug\\$file | grep "Repair Found" ) ; then
                    if (($found == 0)); then
                        bugsRepaired=$(( bugsRepaired + 1 ))
                        echo $bug
                        found=1
                    fi
                    time=$( cat $1\\$project\\$bug\\$file | grep -a "Total elapsed time" | head -1 | cut -d " " -f4 )
                    currVar=$( cat $1\\$project\\$bug\\$file | grep "Repair Found" | grep  -o 'variant[0-9]*' | grep  -o '[0-9]*')
                    totalRepairVariant=$(( totalRepairVariant + currVar ))
                    timeTotal=$(( timeTotal + time ))
                    successTotal=$(( successTotal + 1 ))
                    echo $time >> time_data
                    echo $currVar >> variant_data
                fi
            fi
        done
    done
done
   
echo "Total succeded logs"
echo $successTotal

echo "Total logs"
echo $logTotal

echo "Bugs Fixed"
echo $bugsRepaired

echo "Average Time elapsed To Find Repair"
echo $(( timeTotal / successTotal ))

echo "Median Time elapsed To Find Repair"
sort -n time_data | awk -f median.awk

echo "On average repair is in variant #:"
echo $(( totalRepairVariant / successTotal ))

echo "Median for repair variant #"
sort -n variant_data | awk -f median.awk

echo "Timed Out:"
echo $timedout


rm time_data
rm variant_data