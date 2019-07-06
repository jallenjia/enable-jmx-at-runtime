#!/bin/bash
java -cp ./lib/enable-jmx-at-runtime-jar-with-dependencies.jar com.dreamers.enablejmxatruntime.AgentLoader $@ SCRIPT_NAME=$0
