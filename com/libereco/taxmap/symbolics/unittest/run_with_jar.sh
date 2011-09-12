#!/bin/bash
SOURCE_DIR=.
JWNL_JAR_DIR=/home/cacharya/sandbox/java/jwnl/lib
APACHE_JAR_DIR=/home/cacharya/sandbox/java/apache/lib
JWNL_CONFIG_DIR=/home/cacharya/sandbox/java/jwnl/config
java -cp $SOURCE_DIR:$JWNL_JAR_DIR/jwnl-1.4_rc3.jar:$APACHE_JAR_DIR/commons-logging-1.1.1.jar:$APACHE_JAR_DIR/commons-logging-adapters-1.1.1.jar:$APACHE_JAR_DIR/commons-logging-api-1.1.1.jar ConceptAnalyzer $JWNL_CONFIG_DIR/file_properties.xml

