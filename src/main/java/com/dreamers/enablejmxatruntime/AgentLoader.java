package com.dreamers.enablejmxatruntime;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.dreamers.enablejmxatruntime.AgentParams.*;

public class AgentLoader {
    private static final String PORT_DEFAULT = "17777";

    private static final String JAR_SUFFIX = ".jar";

    private static final String CONF_PREFIX = "conf=";

    /**
     * Find the agent jar to load sun.management.Agent
     */
    private static File getAgentFile() {
        String codePath = AgentLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String agentPath;
        if (codePath.contains(JAR_SUFFIX)) {
            // Run in jar file, return this jar to load sun.management.Agent
            agentPath = codePath.substring(0, codePath.indexOf(JAR_SUFFIX) + JAR_SUFFIX.length());

        } else {
            // Run without a jar file, return management-agent.jar from jre to load sun.management.Agent
            agentPath = System.getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
        }

        System.out.println("Agent path: " + agentPath);
        return new File(agentPath);
    }

    private static Map<String, String> getParamMap(String[] args) {
        if (args == null || args.length == 0) return Collections.emptyMap();

        Map<String, String> paramMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] keyVal = arg.split("=", 2);
            if (keyVal == null || keyVal.length != 2) continue;
            paramMap.put(keyVal[0], keyVal[1]);
        }

        return paramMap;
    }

    private static String getJavaManConfPath() {
        return System.getProperty("java.home")
                + File.separator + "lib"
                + File.separator + "management"
                + File.separator + "management.properties";
    }

    private static void printUsage(String scriptName) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Usage]").append("\r\n")
                .append("1) ").append(scriptName).append(" PID=<PID> [PORT=<PORT>]").append("\n\r")
                .append("2) ").append(scriptName).append(" PID=<PID> CONF=<CONF>").append("\n\r")
                .append("\r\n")
                .append("\tPID  Process id").append("\r\n")
                .append("\tPORT JMX port number. Default ").append(PORT_DEFAULT).append(" if PORT param not specified").append("\r\n")
                .append("\tCONF JMX conf file. You can copy from the path below and modify it").append("\r\n")
                .append("\t\t").append(getJavaManConfPath()).append("\r\n")
                .append("\t\t").append("Full doc of the config: ").append("https://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html#gdeum").append("\r\n")
                .append("\r\n")
                .append("IMPORTANT:").append("\r\n")
                .append("Usage 1 enable JMX WITHOUT SSL and AUTHENTICATE, NEVER DO IT IN PRODUCTION ENV").append("\r\n");

        System.out.println(sb.toString());
    }

    private static void checkParamMap(Map<String, String> paramMap) throws IOException {
        if (StringUtils.isBlank(paramMap.get(PID.name()))) throw new RuntimeException("Wrong PID param");

        if (StringUtils.isNotBlank(paramMap.get(CONF.name()))) {
            File confFile = new File(paramMap.get(CONF.name()));
            if (!(confFile.exists() && confFile.isFile())) throw new RuntimeException("Wrong CONF param");

            paramMap.put(CONF.name(), confFile.getCanonicalPath());
        }

        if (paramMap.containsKey(PORT.name()) && paramMap.containsKey(CONF.name())) throw new RuntimeException("PORT param could be duplicated in CONF");

        if (!paramMap.containsKey(CONF.name()) && !paramMap.containsKey(PORT)) {
            System.out.println("NO PORT param specified, set default: " + PORT_DEFAULT);
            paramMap.put(PORT.name(), PORT_DEFAULT);
        }
    }

    private static String getAgentArgs(Map<String, String> paramMap) {
        String agentArgs;
        if (paramMap.containsKey(CONF.name())) {
            agentArgs = "com.sun.management.config.file=" + paramMap.get(CONF.name());
        } else {
            agentArgs = "com.sun.management.jmxremote.authenticate=false,"
                    + "com.sun.management.jmxremote.ssl=false,"
                    + "com.sun.management.jmxremote.port=" + paramMap.get(PORT.name());
        }
        System.out.println("AgentArgs: " + agentArgs);

        return agentArgs;
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> paramMap = getParamMap(args);
        try {
            checkParamMap(paramMap);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            printUsage(paramMap.get(SCRIPT_NAME.name()));
            System.exit(1);
            return;
        }


        File agentFile = getAgentFile();
        String agentArgs = getAgentArgs(paramMap);

        ByteBuddyAgent.attach(agentFile, paramMap.get(PID.name()), agentArgs);
        System.out.println("Enable Jmx succeeded.");
    }
}
