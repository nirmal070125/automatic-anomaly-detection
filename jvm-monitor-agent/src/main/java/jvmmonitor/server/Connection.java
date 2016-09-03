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
 * Provide the connection to running JVMs
 */
public class Connection {

    private String pid;
    private VirtualMachine vm;

    private static Connection connection;
    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    private final static Logger logger = Logger.getLogger(Connection.class);


    /**
     * Constructor to create Connector
     *
     * @param pid process id of monitoring VM
     */
    private Connection(String pid) throws IOException, AttachNotSupportedException {
        this.pid = pid;
        vm = VirtualMachine.attach(pid);
    }


    /**
     * Connector is singleton
     *
     * Return single Connector obj
     * Make sure that VirtualMachine objs are destroyed before creating a new instance
     * @param pid
     *
     * @return Connector obj
     */
    public static Connection getConnection(String pid){
        if (pid != null){

            try {
                if (connection != null){
                    connection.disconnectFromVM();
                }
                connection = new Connection(pid);
                return  connection;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (AttachNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Create the MBeanServerConnection
     * This server connection can be used to get the UsageBean objects from the monitoring VM
     *
     * @return {MBeanServerConnection} Server Connection
     * @throws AgentLoadException
     * @throws AgentInitializationException
     * @throws IOException
     */
    public MBeanServerConnection getServerConnection() throws IOException, AgentLoadException, AgentInitializationException {


        //print properties of connected VM
        logger.info("Connected to "+vm.id());
        logger.debug("System Properties:");

        for(Map.Entry<?,?> en:vm.getSystemProperties().entrySet())
            logger.debug("\t"+en.getKey()+" = "+en.getValue());

        logger.debug("==================================================");


        String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

        if(connectorAddress == null)
        {
            logger.info("loading agent");

            Properties props = vm.getSystemProperties();
            String home  = props.getProperty("java.home");
            String agent = home+ File.separator+"lib"+File.separator+"management-agent.jar";
            vm.loadAgent(agent);

            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            while(connectorAddress==null) try {
                Thread.sleep(1000);
                connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        JMXConnector c= JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
        return c.getMBeanServerConnection();
    }


    /**
     * Properly dispose the Connector object
     */
    private void disconnectFromVM() throws IOException {
        vm.detach();
    }
}