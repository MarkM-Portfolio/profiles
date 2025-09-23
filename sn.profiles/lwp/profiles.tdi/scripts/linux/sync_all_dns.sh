#!/bin/sh
# *****************************************************************
#
# HCL Confidential
#
# OCO Source Materials
#
# Copyright HCL Technologies Limited 2010, 2021
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# *****************************************************************

debug="false"; 

# cd to the dir where the script lives (I think)
cd `dirname $0`

# Note that certain characters prevent a properties file from being read here,
# so we must 'clean up' those characters.

# first, delete the temporary cleaned up properties file just in case.
rm -f profiles_tdi_clean_temp.properties

# clean up the undesirable characters by changing them to 'X'
cat profiles_tdi.properties | sed s'/[\&(),{} -]/X/g' > profiles_tdi_clean_temp.properties 
# load the properties.  We are mostly interested in:
#    sync_updates_size_model
#    sync_updates_hash_partitions
. ./profiles_tdi_clean_temp.properties

# delete the temporary cleaned up properties file
rm profiles_tdi_clean_temp.properties

# test for whether we got the properties we want. Note that
# empty string is considered the same as not existing

# test for sync_updates_hash_partitions from profiles_tdi.properties (default 10)
if [ -z "$sync_updates_hash_partitions" ]; then 
	echo "Error: sync_updates_hash_partitions is unset, exiting";
	exit 
else 
	if [ $debug == "true" ]; then echo "sync_updates_hash_partitions is set to '$sync_updates_hash_partitions' "; fi;
fi

# test for sync_updates_working_directory
if [ -z "$sync_updates_working_directory" ]; then 
	echo "Error: sync_updates_working_directory is unset, exiting";
	exit 
else 
	if [ $debug == "true" ]; then echo "sync_updates_working_directory is set to '$sync_updates_working_directory' "; fi;
fi

	sync_updates_dir=$sync_updates_working_directory; 
	if [ $debug == "true" ]; then echo "sync_updates_dir is set to '$sync_updates_dir' "; fi;

# if sync_updates_size_model not set, set it to bogus value, which will then be set to
# single shortly 
if [ -z "$sync_updates_size_model" ]; then 
	sync_updates_size_model="xxxx";
fi

# make sure model is valid value; default to single if invalid
bModelOk="false"; 
if [ $sync_updates_size_model == "single" ]; then
	bModelOk="true"; 
fi
if [ $sync_updates_size_model == "multi4" ]; then
	bModelOk="true"; 
fi
if [ $sync_updates_size_model == "multi6" ]; then
	bModelOk="true"; 
fi
if [ $sync_updates_size_model == "multi8" ]; then
	bModelOk="true"; 
fi

if [ $bModelOk == "false" ]; then 
	echo "sync_updates_size_model property is not a valid value [single, multi4, multi6, or multi8].  Using single";
	sync_updates_size_model="single";
fi;


if [ $debug == "true" ]; then echo "sync_updates_size_model is set to '$sync_updates_size_model' "; fi;

# always want profiles_tdi_partitions.properties file with property sync_updates_hash_partitions_if_large_model
# in base solution. Set prop to 0 indicates to run sync all the standard (single) way.
echo "sync_updates_hash_partitions_if_large_model=0" > profiles_tdi_partitions.properties

foundoption="true";
disableTs="false";

# check for single (single jvm) or multi (multiple jvm) model
if [ $sync_updates_size_model == "single" ]; then
# {
	# not multi, so use single approach - single jvm
	if [ $debug == "true" ]; then echo "using single, i.e., single jvm"; fi;

	# pick up disableTs argument if specified
	for i in $@; do
		if [ $debug == "true" ]; then echo input parameter - $i; fi;
		foundoption="false";

		# if disableTs set, disable timestamp processing for this invoke
		if [ $i == "disableTs" ]; then 
			disableTs="true";
			foundoption="true";
		fi;

		if [ $foundoption == "false" ]; then 
			echo Error: unknown command line argument in single mode: $i
			echo The only valid argument is disableTs
			exit 1;
		fi;

	done

	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	LOCK_FILE=./sync_all_dns.lck

	# test lock
	if [ -e ${LOCK_FILE} ]; then
		echo "Synchronization Lock file already exist. Please turn off other running sync process before performing this one.";
		exit;
	else
		echo "create synchronization lock";
		touch ${LOCK_FILE};
	fi

	#clear rc file
	./clearRC.sh

	#get TDI variables
	. ./tdienv.sh

	./netstore ping >/dev/null 2>/dev/null
	if [[ $? -ne 0 ]]; then
		./netstore start
	fi

	#set initial failure return code in case called task does not complete
	echo "1" >./_tdi.rc
	${TDIPATH}/ibmdisrv -s . -c profiles_tdi.xml -r sync_all_dns
	rc=`cat ./_tdi.rc`
	if [[ ! "${rc}" == "0" ]]; then
		echo "Synchronize of Database Repository failed"
		echo ""
	fi
	./clearRC.sh
	./clearLock.sh

	exit ${rc}

# }
else

# multi-process begins here

dateTimeStr=$(date +"%y%m%d%H%M%S")

refreshsols="false";
cleanlogs="false";
hashonly="false"; # for debugging
hashskipdb="false"; # for debugging
hashskipsrc="false"; # for debugging
updateonly="false"; #not implemented yet; would be for debugging

# pick up arguments
# note they only apply with multi model
for i in $@; do
	if [ $debug == "true" ]; then echo input parameter - $i; fi;
	foundoption="false";

	# if disableTs set, disable timestamp processing for this invoke
	if [ $i == "disableTs" ]; then 
		disableTs="true";
		foundoption="true";
	fi;

	# if refreshsols set, delete the parallel solutions dirs
	# must do this if one of the files is changed. 
	if [ $i == "refreshsols" ]; then 
		refreshsols="true";
		foundoption="true";
	fi;
	
	# if cleanlogs set, remove all files from logs dirs
	if [ $i == "cleanlogs" ]; then 
		cleanlogs="true";
		foundoption="true";
	fi;
	
	# if hashonly set, exit after generating hash files.  This is primarily for debugging.
	if [ $i == "hashonly" ]; then 
		hashonly="true";
		foundoption="true";
	fi;

	if [ $i == "hashskipdb" ]; then 
		hashskipdb="true";
		foundoption="true";
	fi;

	if [ $i == "hashskipsrc" ]; then 
		hashskipsrc="true";
		foundoption="true";
	fi;

	if [ $i == "updateonly" ]; then 
		updateonly="true";
		foundoption="true";
	fi;

	if [ $foundoption == "false" ]; then 
		echo Error: unknown command line argument in multi mode: $i
		echo valid arguments are disableTs, refreshsols, cleanlogs, hashonly, hashskipdb, hashskipsrc, updateonly
		exit 1;
	fi;

done

	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	if [ $debug == "true" ]; then
	    echo disable timestamp $disableTs
	    echo refresh $refreshsols
	    echo clean $cleanlogs
	    echo hash $hashonly
	    echo hash $hashskipdb
	    echo hash $hashskipsrc
	    echo hash $updateonly
	fi

	# make sure these args don't get in the way	of updateonly
	# note that the parallel solution dirs should exist
	if [ $updateonly == "true" ]; then
	    refreshsols="false";
	    hashonly="false";
	    hashskipdb="false";
	    hashskipsrc="false";
	fi

	# multi model, i.e., use multiple jvms/multi-processing
	if [ $debug == "true" ]; then echo "using multi model, i.e., multiple jvms"; fi

	nPartitions=$((0 + $sync_updates_hash_partitions));

	if [ $debug == "true" ]; then
		echo "nPartitions raw";
		echo $nPartitions;
	fi

	if [ $sync_updates_size_model = "multi4" ]; then
		numberOfJVMs=4;
	fi
	if [ $sync_updates_size_model = "multi6" ]; then
		numberOfJVMs=6;
	fi
	if [ $sync_updates_size_model = "multi8" ]; then
		numberOfJVMs=8;
	fi

	# each of the parallel tdi solution directories (or JVM's) needs to know which partitions to 
	# process.  Note you cannot change number this without changing code! 
	# make sure the number of partitions is a multiple of $numberOfJVMs, i.e., 4 at present

	numRemainder=$(($nPartitions % $numberOfJVMs));
	numOffset=$(($numberOfJVMs - $numRemainder));

	if [ $numRemainder == 0 ]; then
		numOffset=0;
	fi

	nPartitions=$(($nPartitions + $numOffset));

	if [ $debug == "true" ]; then
		echo "numRemainder final: $numRemainder";
		echo "numOffset final: $numOffset";
		echo "nPartitions final: $nPartitions";
	fi

	# get the name of the current directory (just the directory, e.g., TDI)
	currDirName=${PWD##*/}

	if [ $debug == "true" ]; then
		echo current directory name;
		echo $currDirName;
	fi

	if [ $refreshsols == "true" ]; then

		if [ $debug == "true" ]; then
			echo refreshing parallel sols;
		fi

		rm -rf ../${currDirName}_1/;
		rm -rf ../${currDirName}_2/;
		rm -rf ../${currDirName}_3/;
		rm -rf ../${currDirName}_4/;
		rm -rf ../${currDirName}_5/;
		rm -rf ../${currDirName}_6/;
		rm -rf ../${currDirName}_7/;
		rm -rf ../${currDirName}_8/;
	fi

	# go up to the parent directory
	cd ..

	if [ $debug == "true" ]; then
		echo test that parallel tdi solution dir 1 exists
	fi


	if [ -e ${currDirName}_1 ]; then
		if [ $debug == "true" ]; then
			echo "parallel dir _1 exists. "
		fi

		cp ./$currDirName/profiles_tdi.properties ./${currDirName}_1
		cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_1
		cp ./$currDirName/profiles_functions.js ./${currDirName}_1
		cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_1
	else
		if [ $debug == "true" ]; then
			echo "create parallel solution dir _1"
		fi
		cp -r ./$currDirName  ./${currDirName}_1
	fi

	cd ${currDirName}_1

	# set props in profiles_tdi_partitions.properties to specify hash db
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	echo "sync_updates_stage=hashdb" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	cd .. 
	if [ -e ${currDirName}_2 ]; then
		echo "parallel dir _2 exists. "

		cp ./$currDirName/profiles_tdi.properties ./${currDirName}_2
		cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_2
		cp ./$currDirName/profiles_functions.js ./${currDirName}_2
		cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_2
	else
		if [ $debug == "true" ]; then
			echo "create parallel solution dir _2"
		fi
		cp -r ./$currDirName  ./${currDirName}_2
	fi


	cd ${currDirName}_2

	# set props in profiles_tdi_partitions.properties to specify hash src
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	echo "sync_updates_stage=hashsrc" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	cd ..

	if [ -e ${currDirName}_3 ]; then
		echo "parallel dir _3 exists. "

		cp ./$currDirName/profiles_tdi.properties ./${currDirName}_3
		cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_3
		cp ./$currDirName/profiles_functions.js ./${currDirName}_3
		cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_3
	else
		if [ $debug == "true" ]; then
			echo "create parallel solution dir _3"
		fi
		cp -r ./$currDirName  ./${currDirName}_3
	fi

	if [ -e ${currDirName}_4 ]; then
		echo "parallel dir _4 exists. "

		cp ./$currDirName/profiles_tdi.properties ./${currDirName}_4
		cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_4
		cp ./$currDirName/profiles_functions.js ./${currDirName}_4
		cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_4
	else
		if [ $debug == "true" ]; then
			echo "create parallel solution dir _4"
		fi
		cp -r ./$currDirName  ./${currDirName}_4
	fi




	if [ $numberOfJVMs -ge 6 ]; then
		if [ -e ${currDirName}_5 ]; then
			echo "parallel dir _5 exists. "

			cp ./$currDirName/profiles_tdi.properties ./${currDirName}_5
			cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_5
			cp ./$currDirName/profiles_functions.js ./${currDirName}_5
			cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_5
		else
			if [ $debug == "true" ]; then
				echo "create parallel solution dir _5"
			fi
			cp -r ./$currDirName  ./${currDirName}_5
		fi

		if [ -e ${currDirName}_6 ]; then
			echo "parallel dir _6 exists. "

			cp ./$currDirName/profiles_tdi.properties ./${currDirName}_6
			cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_6
			cp ./$currDirName/profiles_functions.js ./${currDirName}_6
			cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_6
		else
			if [ $debug == "true" ]; then
				echo "create parallel solution dir _6"
			fi
			cp -r ./$currDirName  ./${currDirName}_6
		fi

	fi

	if [ $numberOfJVMs -ge 8 ]; then
		if [ -e ${currDirName}_7 ]; then
			echo "parallel dir _7 exists. "

			cp ./$currDirName/profiles_tdi.properties ./${currDirName}_7
			cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_7
			cp ./$currDirName/profiles_functions.js ./${currDirName}_7
			cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_7
		else
			if [ $debug == "true" ]; then
				echo "create parallel solution dir _7"
			fi
			cp -r ./$currDirName  ./${currDirName}_7
		fi

		if [ -e ${currDirName}_8 ]; then
			echo "parallel dir _8 exists. "

			cp ./$currDirName/profiles_tdi.properties ./${currDirName}_8
			cp ./$currDirName/map_dbrepos_from_source.properties ./${currDirName}_8
			cp ./$currDirName/profiles_functions.js ./${currDirName}_8
			cp ./$currDirName/collect_ldap_dns_by_chunks.js ./${currDirName}_8
		else
			if [ $debug == "true" ]; then
				echo "create parallel solution dir _8"
			fi
			cp -r ./$currDirName  ./${currDirName}_8
		fi

	fi



	cd ${currDirName}

	# don't clean sync_updates_dir if in updateonly mode 
	if [ $updateonly != "true" ]; then
		if [ $debug == "true" ]; then
			echo clean sync_updates
		fi
		rm -f ../${currDirName}/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_1/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_2/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_3/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_4/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_5/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_6/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_7/${sync_updates_dir}/*.*
		rm -f ../${currDirName}_8/${sync_updates_dir}/*.*
	fi

	# code below is needed to prevent permission issue
	echo "0" > ../${currDirName}_1/_tdisuccesscount.rc
	echo "0" > ../${currDirName}_1/_tdideletecount.rc
	echo "0" > ../${currDirName}_1/_tdiduplicatecount.rc
	echo "0" > ../${currDirName}_1/_tdifailcount.rc

	echo "0" > ../${currDirName}_2/_tdisuccesscount.rc
	echo "0" > ../${currDirName}_2/_tdideletecount.rc
	echo "0" > ../${currDirName}_2/_tdiduplicatecount.rc
	echo "0" > ../${currDirName}_2/_tdifailcount.rc

	echo "0" > ../${currDirName}_3/_tdisuccesscount.rc
	echo "0" > ../${currDirName}_3/_tdideletecount.rc
	echo "0" > ../${currDirName}_3/_tdiduplicatecount.rc
	echo "0" > ../${currDirName}_3/_tdifailcount.rc

	echo "0" > ../${currDirName}_4/_tdisuccesscount.rc
	echo "0" > ../${currDirName}_4/_tdideletecount.rc
	echo "0" > ../${currDirName}_4/_tdiduplicatecount.rc
	echo "0" > ../${currDirName}_4/_tdifailcount.rc

	if [ $numberOfJVMs -ge 6 ]; then
		echo "0" > ../${currDirName}_5/_tdisuccesscount.rc
		echo "0" > ../${currDirName}_5/_tdideletecount.rc
		echo "0" > ../${currDirName}_5/_tdiduplicatecount.rc
		echo "0" > ../${currDirName}_5/_tdifailcount.rc

		echo "0" > ../${currDirName}_6/_tdisuccesscount.rc
		echo "0" > ../${currDirName}_6/_tdideletecount.rc
		echo "0" > ../${currDirName}_6/_tdiduplicatecount.rc
		echo "0" > ../${currDirName}_6/_tdifailcount.rc
	fi

	if [ $numberOfJVMs -ge 8 ]; then
		echo "0" > ../${currDirName}_7/_tdisuccesscount.rc
		echo "0" > ../${currDirName}_7/_tdideletecount.rc
		echo "0" > ../${currDirName}_7/_tdiduplicatecount.rc
		echo "0" > ../${currDirName}_7/_tdifailcount.rc

		echo "0" > ../${currDirName}_8/_tdisuccesscount.rc
		echo "0" > ../${currDirName}_8/_tdideletecount.rc
		echo "0" > ../${currDirName}_8/_tdiduplicatecount.rc
		echo "0" > ../${currDirName}_8/_tdifailcount.rc
	fi


	if [ $cleanlogs == "true" ]; then
		echo cleaning logs
		rm -f ../${currDirName}_1/logs/*.*
		rm -f ../${currDirName}_2/logs/*.*
		rm -f ../${currDirName}_3/logs/*.*
		rm -f ../${currDirName}_4/logs/*.*
		rm -f ../${currDirName}_5/logs/*.*
		rm -f ../${currDirName}_6/logs/*.*
		rm -f ../${currDirName}_7/logs/*.*
		rm -f ../${currDirName}_8/logs/*.*
	fi

	# skip hash if updateonly mode 
	if [ $updateonly != "true" ]; then
	# {
		echo start hash db 
		cd ../${currDirName}_1
		echo "1" > ./_tdihdb.rc
		if [ $hashskipdb != "true" ]; then
			./sync_all_dns1.sh &
		fi

		echo start hash source
		cd ../${currDirName}_2
		echo "1" > ./_tdihsrc.rc
		if [ $hashskipsrc != "true" ]; then
			./sync_all_dns1.sh &
		fi

		if [ $hashskipsrc == "true" ]; then
			echo exiting due to skipped hash source
			exit 1
		fi

		if [ $hashskipdb == "true" ]; then
			echo exiting due to skipped hash db
			exit 1
		fi

		cd ../${currDirName}

		echo wait for hash assemblies
		wait
		echo wait for hash done

		# renamed the ibmdi.log files
		mv -f ../${currDirName}_1/logs/ibmdi.log  ../${currDirName}_1/logs/ibmdi_hash_db_$dateTimeStr.log
		mv -f ../${currDirName}_2/logs/ibmdi.log  ../${currDirName}_2/logs/ibmdi_hash_scr_$dateTimeStr.log

		echo check that hash db worked
		rcdb=`cat ../${currDirName}_1/_tdihdb.rc`
		if [[ ! "${rcdb}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Hash of Profiles database failed"
			echo ""
			exit
		fi

		# check that hash src worked
		rcsrc=`cat ../${currDirName}_2/_tdihsrc.rc`
		if [[ ! "${rcsrc}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Hash of source failed"
			echo ""
			exit
		fi

		if [ $debug == "true" ]; then
			echo got by check that both worked
		fi;

		# copy dbids from #1 to #0
		cp ../${currDirName}_1/${sync_updates_dir}/*.* ./${sync_updates_dir}
		rm -f ../${currDirName}_1/${sync_updates_dir}/*.*

		# copy ldif's from #2 to #0
		cp ../${currDirName}_2/${sync_updates_dir}/*.* ./${sync_updates_dir}
		rm -f ../${currDirName}_2/${sync_updates_dir}/*.*

		cp ./${sync_updates_dir}/*.*	../${currDirName}_1/${sync_updates_dir}
		cp ./${sync_updates_dir}/*.*	../${currDirName}_2/${sync_updates_dir}
		cp ./${sync_updates_dir}/*.*	../${currDirName}_3/${sync_updates_dir}
		cp ./${sync_updates_dir}/*.*	../${currDirName}_4/${sync_updates_dir}

		if [ $numberOfJVMs -ge 6 ]; then
			cp ./${sync_updates_dir}/*.*	../${currDirName}_5/${sync_updates_dir}
			cp ./${sync_updates_dir}/*.*	../${currDirName}_6/${sync_updates_dir}
		fi

		if [ $numberOfJVMs -ge 8 ]; then
			cp ./${sync_updates_dir}/*.*	../${currDirName}_7/${sync_updates_dir}
			cp ./${sync_updates_dir}/*.*	../${currDirName}_8/${sync_updates_dir}
		fi

		if [ $hashonly == "true" ]; then 
			echo exiting after hash - hashonly is set
			exit
		fi;

	# }
	fi

	if [ $debug == "true" ]; then
		echo "now sync batch 1 of partitions";
	fi;

	partitionsPerProcess=$(($nPartitions / $numberOfJVMs));

	if [ $debug == "true" ]; then
		echo "partitionsPerProcess: $partitionsPerProcess";
	fi

	cd ../${currDirName}_1
	echo "1" > ./_tdiupd.rc

	# set the props in profiles_tdi_partitions.properties, i.e., 
	#    sync_updates_hash_partitions_if_large_model=
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	countInit=0;
	echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
	countTo=$partitionsPerProcess;
	echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
	echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

		echo "countTo1: $countTo";
	./sync_all_dns1.sh &

	echo now sync batch 2 of partitions

	cd ../${currDirName}_2
	echo "1" > ./_tdiupd.rc

	# set the props in profiles_tdi_partitions.properties, i.e., 
	#    sync_updates_hash_partitions_if_large_model=
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	countInit=$countTo;
	echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties

	countTo=$(($countTo + $partitionsPerProcess));
		echo "countInit: $countInit";
		echo "countTo2: $countTo";

	echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
	echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	./sync_all_dns1.sh &


	echo now sync batch 3 of partitions

	cd ../${currDirName}_3
	echo "1" > ./_tdiupd.rc

	# set the props in profiles_tdi_partitions.properties, i.e., 
	#    sync_updates_hash_partitions_if_large_model=
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	countInit=$countTo;
	echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
	countTo=$(($countTo + $partitionsPerProcess));
	echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
	echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	./sync_all_dns1.sh &

	echo now sync batch 4 of partitions

	cd ../${currDirName}_4
	echo "1" > ./_tdiupd.rc

	# set the props in profiles_tdi_partitions.properties, i.e., 
	#    sync_updates_hash_partitions_if_large_model=
	echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
	countInit=$countTo;
	echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
	countTo=$(($countTo + $partitionsPerProcess));
	echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
	echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
	echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

	./sync_all_dns1.sh &


	if [ $numberOfJVMs -ge 6 ]; then
		echo now sync batch 5 of partitions

		cd ../${currDirName}_5
		echo "1" > ./_tdiupd.rc

		# set the props in profiles_tdi_partitions.properties, i.e., 
		#    sync_updates_hash_partitions_if_large_model=
		echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
		countInit=$countTo;
		echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
		countTo=$(($countTo + $partitionsPerProcess));
		echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
		echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
		echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

		./sync_all_dns1.sh &

		echo now sync batch 6 of partitions

		cd ../${currDirName}_6
		echo "1" > ./_tdiupd.rc

		# set the props in profiles_tdi_partitions.properties, i.e., 
		#    sync_updates_hash_partitions_if_large_model=
		echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
		countInit=$countTo;
		echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
		countTo=$(($countTo + $partitionsPerProcess));
		echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
		echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
		echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

		./sync_all_dns1.sh &
	fi

	if [ $numberOfJVMs -ge 8 ]; then
		echo now sync batch 7 of partitions

		cd ../${currDirName}_7
		echo "1" > ./_tdiupd.rc

		# set the props in profiles_tdi_partitions.properties, i.e., 
		#    sync_updates_hash_partitions_if_large_model=
		echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
		countInit=$countTo;
		echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
		countTo=$(($countTo + $partitionsPerProcess));
		echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
		echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
		echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

		./sync_all_dns1.sh &

		echo now sync batch 8 of partitions

		cd ../${currDirName}_8
		echo "1" > ./_tdiupd.rc

		# set the props in profiles_tdi_partitions.properties, i.e., 
		#    sync_updates_hash_partitions_if_large_model=
		echo "sync_updates_hash_partitions_if_large_model=$nPartitions" > profiles_tdi_partitions.properties
		countInit=$countTo;
		echo "sync_updates_count_init=$countInit" >> profiles_tdi_partitions.properties
		countTo=$(($countTo + $partitionsPerProcess));
		echo "sync_updates_count_to=$countTo" >> profiles_tdi_partitions.properties
		echo "sync_updates_stage=update" >> profiles_tdi_partitions.properties
		echo "sync_updates_hash_timestamp_disabled_by_command_arg=$disableTs" >> profiles_tdi_partitions.properties

		./sync_all_dns1.sh &
	fi

	cd ../${currDirName}

	echo wait for updates
	wait
	echo updates done

	if [ $debug == "true" ]; then 
		echo "now rename the log files";
	fi

	mv -f ../${currDirName}_1/logs/ibmdi.log  ../${currDirName}_1/logs/ibmdi_update1$dateTimeStr.log
	mv -f ../${currDirName}_2/logs/ibmdi.log  ../${currDirName}_2/logs/ibmdi_update2$dateTimeStr.log
	mv -f ../${currDirName}_3/logs/ibmdi.log  ../${currDirName}_3/logs/ibmdi_update3$dateTimeStr.log
	mv -f ../${currDirName}_4/logs/ibmdi.log  ../${currDirName}_4/logs/ibmdi_update4$dateTimeStr.log

	if [ $numberOfJVMs -ge 6 ]; then
		mv -f ../${currDirName}_5/logs/ibmdi.log  ../${currDirName}_5/logs/ibmdi_update5$dateTimeStr.log
		mv -f ../${currDirName}_6/logs/ibmdi.log  ../${currDirName}_6/logs/ibmdi_update6$dateTimeStr.log
	fi
	if [ $numberOfJVMs -ge 8 ]; then
		mv -f ../${currDirName}_7/logs/ibmdi.log  ../${currDirName}_7/logs/ibmdi_update7$dateTimeStr.log
		mv -f ../${currDirName}_8/logs/ibmdi.log  ../${currDirName}_8/logs/ibmdi_update8$dateTimeStr.log
	fi

	# check final results
	rcupd1=`cat ../${currDirName}_1/_tdiupd.rc`
	if [[ ! "${rcupd1}" == "0" ]]; then
		echo ""
		echo ""
		echo "Synchronize of Database Repository failed"
		echo "Update of batch 1 failed"
		echo ""
		exit 1
	fi

	rcupd2=`cat ../${currDirName}_2/_tdiupd.rc`
	if [[ ! "${rcupd2}" == "0" ]]; then
		echo ""
		echo ""
		echo "Synchronize of Database Repository failed"
		echo "Update of batch 2 failed"
		echo ""
		exit 1
	fi

	rcupd3=`cat ../${currDirName}_3/_tdiupd.rc`
	if [[ ! "${rcupd3}" == "0" ]]; then
		echo ""
		echo ""
		echo "Synchronize of Database Repository failed"
		echo "Update of batch 3 failed"
		echo ""
		exit 1
	fi

	rcupd4=`cat ../${currDirName}_4/_tdiupd.rc`
	if [[ ! "${rcupd4}" == "0" ]]; then
		echo ""
		echo ""
		echo "Synchronize of Database Repository failed"
		echo "Update of batch 4 failed"
		echo ""
		exit  1
	fi

	if [ $numberOfJVMs -ge 6 ]; then
		rcupd5=`cat ../${currDirName}_5/_tdiupd.rc`
		if [[ ! "${rcupd5}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Update of batch 5 failed"
			echo ""
			exit  1
		fi

		rcupd6=`cat ../${currDirName}_6/_tdiupd.rc`
		if [[ ! "${rcupd6}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Update of batch 6 failed"
			echo ""
			exit  1
		fi
	fi

	if [ $numberOfJVMs -ge 8 ]; then
		rcupd7=`cat ../${currDirName}_7/_tdiupd.rc`
		if [[ ! "${rcupd7}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Update of batch 7 failed"
			echo ""
			exit  1
		fi

		rcupd8=`cat ../${currDirName}_8/_tdiupd.rc`
		if [[ ! "${rcupd8}" == "0" ]]; then
			echo ""
			echo ""
			echo "Synchronize of Database Repository failed"
			echo "Update of batch 8 failed"
			echo ""
			exit  1
		fi
	fi


	success_1=`cat ../${currDirName}_1/_tdisuccesscount.rc`
	deleted_1=`cat ../${currDirName}_1/_tdideletecount.rc`
	unchanged_1=`cat ../${currDirName}_1/_tdiduplicatecount.rc`
	failure_1=`cat ../${currDirName}_1/_tdifailcount.rc`

	success_2=`cat ../${currDirName}_2/_tdisuccesscount.rc`
	deleted_2=`cat ../${currDirName}_2/_tdideletecount.rc`
	unchanged_2=`cat ../${currDirName}_2/_tdiduplicatecount.rc`
	failure_2=`cat ../${currDirName}_2/_tdifailcount.rc`

	success_3=`cat ../${currDirName}_3/_tdisuccesscount.rc`
	deleted_3=`cat ../${currDirName}_3/_tdideletecount.rc`
	unchanged_3=`cat ../${currDirName}_3/_tdiduplicatecount.rc`
	failure_3=`cat ../${currDirName}_3/_tdifailcount.rc`

	success_4=`cat ../${currDirName}_4/_tdisuccesscount.rc`
	deleted_4=`cat ../${currDirName}_4/_tdideletecount.rc`
	unchanged_4=`cat ../${currDirName}_4/_tdiduplicatecount.rc`
	failure_4=`cat ../${currDirName}_4/_tdifailcount.rc`

	success_5=0;
	success_6=0;
	success_7=0;
	success_8=0;
	deleted_5=0;
	deleted_6=0;
	deleted_7=0;
	deleted_8=0;
	unchanged_5=0;
	unchanged_6=0;
	unchanged_7=0;
	unchanged_8=0;
	failure_5=0;
	failure_6=0;
	failure_7=0;
	failure_8=0;

	if [ $numberOfJVMs -ge 6 ]; then
	   success_5=`cat ../${currDirName}_5/_tdisuccesscount.rc`
	   deleted_5=`cat ../${currDirName}_5/\_tdideletecount.rc`
	   unchanged_5=`cat ../${currDirName}_5/_tdiduplicatecount.rc`
	   failure_5=`cat ../${currDirName}_5/_tdifailcount.rc`

	   success_6=`cat ../${currDirName}_6/_tdisuccesscount.rc`
	   deleted_6=`cat ../${currDirName}_6/\_tdideletecount.rc`
	   unchanged_6=`cat ../${currDirName}_6/_tdiduplicatecount.rc`
	   failure_6=`cat ../${currDirName}_6/_tdifailcount.rc`
	fi

	if [ $numberOfJVMs -ge 8 ]; then
	   success_7=`cat ../${currDirName}_7/_tdisuccesscount.rc`
	   deleted_7=`cat ../${currDirName}_7/\_tdideletecount.rc`
	   unchanged_7=`cat ../${currDirName}_7/_tdiduplicatecount.rc`
	   failure_7=`cat ../${currDirName}_7/_tdifailcount.rc`

	   success_8=`cat ../${currDirName}_8/_tdisuccesscount.rc`
	   deleted_8=`cat ../${currDirName}_8/\_tdideletecount.rc`
	   unchanged_8=`cat ../${currDirName}_8/_tdiduplicatecount.rc`
	   failure_8=`cat ../${currDirName}_8/_tdifailcount.rc`
	fi

	success_total=$((success_1 + success_2 + success_3 + success_4 + success_5 + success_6 + success_7 + success_8));
	deleted_total=$((deleted_1 + deleted_2 + deleted_3 + deleted_4 + deleted_5 + deleted_6 + deleted_7 + deleted_8));
	unchanged_total=$((unchanged_1 + unchanged_2 + unchanged_3 + unchanged_4 + unchanged_5 + unchanged_6 + unchanged_7 + unchanged_8));
	fail_total=$((failure_1 + failure_2 + failure_3 + failure_4 + failure_5 + failure_6 + failure_7 + failure_8));

	echo "  "
	echo "  "
	echo After synchronzation, the final totals are:
	echo "    added or modifified records:    $success_total"
	echo "    deleted or inactivated records: $deleted_total"
	echo "    unchanged records:              $unchanged_total"
	echo "    failure records:                $fail_total"
	echo "  "
	echo "  "

	exit 0;

fi



