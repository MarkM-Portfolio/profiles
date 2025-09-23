#!/bin/sh
# ***************************************************************** 
#                                                                   
# IBM Confidential                                                  
#                                                                   
# OCO Source Materials                                              
#                                                                   
# Copyright IBM Corp. 2014                                          
#                                                                   
# The source code for this program is not published or otherwise    
# divested of its trade secrets, irrespective of what has been      
# deposited with the U.S. Copyright Office.                         
#                                                                   
# ***************************************************************** 

FIXUP_DIR=`dirname $0`
MIGRATION_DIR=`dirname ${FIXUP_DIR}`
MIGRATION_DIR=`dirname ${MIGRATION_DIR}`/migrate.lib

echo $DB2_JAVA_HOME/jdk64/bin/java -Dfile.encoding=UTF-8 -Xmx1024m -classpath \
${DB2_JAVA_HOME}/db2jcc.jar:${DB2_JAVA_HOME}/db2jcc_license_cu.jar:${MIGRATION_DIR}/profiles.migrate.jar:${MIGRATION_DIR}/commons-logging-1.0.4.jar:${MIGRATION_DIR}/lc.util.web-30.jar:${MIGRATION_DIR}/commons-lang-2.4.jar:${MIGRATION_DIR}/commons-codec-1.3-minus-mp.jar \
com.ibm.profiles.migrate.MigrateHashEmail \
jdbc:db2://${DB2_HOST}:${DB2_PORT}/${DB2_DB_NAME} \
"${DB2_USER}" "******"

$DB2_JAVA_HOME/jdk64/bin/java -Dfile.encoding=UTF-8 -Xmx1024m -classpath \
${DB2_JAVA_HOME}/db2jcc.jar:${DB2_JAVA_HOME}/db2jcc_license_cu.jar:${MIGRATION_DIR}/profiles.migrate.jar:${MIGRATION_DIR}/commons-logging-1.0.4.jar:${MIGRATION_DIR}/lc.util.web-30.jar:${MIGRATION_DIR}/commons-lang-2.4.jar:${MIGRATION_DIR}/commons-codec-1.3-minus-mp.jar \
com.ibm.profiles.migrate.MigrateHashEmail \
jdbc:db2://${DB2_HOST}:${DB2_PORT}/${DB2_DB_NAME} \
"${DB2_USER}" "${DB2_PASSWORD}"
