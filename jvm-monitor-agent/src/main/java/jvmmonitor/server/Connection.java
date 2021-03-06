package jvmmonitor.server;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.apache.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

/**
 * Provide the connection to running JVMs using PID or MBean URL
 */
public class Connection {

    private final static Logger logger = Logger.getLogger(Connection.class);

    private VirtualMachine vm;
    private static Connection connection;
    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";


    /**
     * Connection is singleton
     * <p>
     * Return single Connection obj
     * Make sure that VirtualMachine objs are destroyed before creating a new instance
     * Forced to maintain only one connection at once
     *
     * @return Connector obj
     */
    public static Connection getConnection() {
        try {
            synchronized (Connection.class) {
                if (connection != null) {
                    connection.disconnectFromVM();
                }
                connection = new Connection();
            }
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Create the MBeanServerConnection to a remote server using the JMX URL
     * This server connection can be used to get the UsageBean objects from the monitoring VM
     *
     * @param hostname
     * @param RMI_Server_Port
     * @param RMI_Registry_Port
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public MBeanServerConnection getRemoteMBeanServerConnection(String hostname, String RMI_Server_Port, String RMI_Registry_Port, String username, String password) throws IOException {

        if (hostname != null) {

            String connectorAddress, RMI_Server_Address, RMI_Registry_Address;
            JMXConnector con;

            //create JMX URL
            RMI_Server_Address = hostname.concat(":").concat(RMI_Server_Port);
            RMI_Registry_Address = hostname.concat(":").concat(RMI_Registry_Port);

            connectorAddress = "service:jmx:rmi://"
                    .concat(RMI_Server_Address)
                    .concat("/jndi/rmi://")
                    .concat(RMI_Registry_Address)
                    .concat("/jmxrmi");
            logger.info(connectorAddress);

            if (username != null && password != null) {
                Map<String, String[]> cred = new HashMap<String, String[]>();
                cred.put(JMXConnector.CREDENTIALS, new String[]{username, password});
                con = JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress), cred);
            } else {
                con = JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
            }

            return con.getMBeanServerConnection();
        }
        return null;
    }

    /**
     * Create the MBeanServerConnection to a local server using PID
     * This server connection can be used to get the UsageBean objects from the monitoring VM
     *
     * @param pid
     * @return
     */
    public MBeanServerConnection getLocalMBeanServerConnection(String pid) throws IOException,
            AttachNotSupportedException,
            AgentInitializationException,
            AgentLoadException {

        if (pid != null) {

            String connectorAddress;
            JMXConnector con;

            //attach VM using the pid given
            vm = VirtualMachine.attach(pid);

            //print properties of connected VM
            logger.info("Connected to " + vm.id());
            logger.debug("System Properties:");

            for (Map.Entry<?, ?> en : vm.getSystemProperties().entrySet())
                logger.debug("\t" + en.getKey() + " = " + en.getValue());

            logger.info("==================================================");

            //getting the connector address to the local JVM
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            if (connectorAddress == null) {
                logger.info("loading agent");

                Properties props = vm.getSystemProperties();
                String home = props.getProperty("java.home");
                String agent = home + File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent);

                connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                while (connectorAddress == null) {
                    try {
                        Thread.sleep(1000);
                        connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            logger.info("Connecting Address for given PID :" + pid + " is :" + connectorAddress);
            con = JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
            return con.getMBeanServerConnection();
        }
        return null;
    }


    /**
     * Properly dispose the Connector object
     */
    private void disconnectFromVM() throws IOException {
        vm.detach();
    }
}
