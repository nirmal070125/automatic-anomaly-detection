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

package communicator;

import jvmmonitor.model.UsageMonitorLog;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.net.*;
import java.util.Enumeration;


public class DASmemoryPublisher implements Runnable {

    private DataPublisher dataPublisher;
    private String dataStream;
    private EventPublisher eventAgent;
    private UsageMonitorLog usageLogObj;

    final static Logger logger = Logger.getLogger(DASmemoryPublisher.class);

    /**
     * Constructor
     * Need to set client-truststore.jks file located path
     *
     * @param defaultThriftPort
     * @param defaultBinaryPort
     * @param username
     * @param password
     * @throws SocketException
     * @throws UnknownHostException
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws TransportException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     */
    public DASmemoryPublisher(int defaultThriftPort, int defaultBinaryPort, String username, String password) throws SocketException,
            UnknownHostException,
            DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException,
            DataEndpointException,
            DataEndpointConfigurationException {

        logger.info("Starting DAS HttpLog Agent");
        String currentDir = System.getProperty("user.dir");

        //Set the client-truststore.jks file located path in here
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/jvm-monitor-agent/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        AgentHolder.setConfigPath(getDataAgentConfigPath());
        String host = getLocalAddress().getHostAddress();

        String type = getProperty("type", "Thrift");
        int receiverPort = defaultThriftPort;
        if (type.equals("Binary")) {
            receiverPort = defaultBinaryPort;
        }
        int securePort = receiverPort + 100;

        String url = getProperty("url", "tcp://" + host + ":" + receiverPort);
        String authURL = getProperty("authURL", "ssl://" + host + ":" + securePort);
        username = getProperty("username", username);
        password = getProperty("password", password);

        dataPublisher = new DataPublisher(type, url, authURL, username, password);

        //Set default Memory usage stream
        String HTTPD_LOG_STREAM = "MemoryUsageStream";
        String VERSION = "1.0.0";
        setDataStream(HTTPD_LOG_STREAM, VERSION);

        eventAgent = new EventPublisher();

    }

    /**
     * Need to set UsageMonitorLog before publish data to DAS
     *
     * @param usageLogObj
     */
    public void setUsageLogObj(UsageMonitorLog usageLogObj) {
        this.usageLogObj = usageLogObj;
    }

    /**
     * Generate StreamId for Memory data
     * <p>
     * Data format must be in the following order in given types in "MemoryUsageStream":-
     * <p>
     * long  MAX_HEAP_MEMORY
     * long  ALLOCATED_HEAP_MEMORY
     * long  USED_HEAP_MEMORY
     * long  MAX_NON_HEAP_MEMORY
     * long  ALLOCATED_NON_HEAP_MEMORY
     * long  USED_NON_HEAP_MEMORY
     * long  PENDING_FINALIZATIONS
     * long  Timestamp
     *
     * @param HTTPD_LOG_STREAM
     * @param VERSION
     */
    public void setDataStream(String HTTPD_LOG_STREAM, String VERSION) {
        dataStream = DataBridgeCommonsUtils.generateStreamId(HTTPD_LOG_STREAM, VERSION);
    }

    /**
     * Shutdown the DataPublisher
     *
     * @throws DataEndpointException
     */
    public void shutdownDataPublisher() throws DataEndpointException {
        dataPublisher.shutdown();
    }

    /**
     * Need to set resource files located path
     *
     * @return Data agent config path
     */
    public static String getDataAgentConfigPath() {
        File filePath = new File("jvm-monitor-agent" + File.separator + "src" + File.separator + "main" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-agent-conf.xml";
    }

    /**
     * @return LocalAddress
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
    }

    /**
     * @param name
     * @param def
     * @return
     */
    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0 || result == "") {
            result = def;
        }
        return result;
    }


    /**
     * @Override method
     */
    public void run() {

        try {
            eventAgent.publishLogEvents(dataPublisher, dataStream, usageLogObj.getDate(), usageLogObj.getMemoryUsageLog());
        } catch (DataEndpointConfigurationException e) {
            e.printStackTrace();
        } catch (DataEndpointAgentConfigurationException e) {
            e.printStackTrace();
        } catch (DataEndpointException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        } catch (DataEndpointAuthenticationException e) {
            e.printStackTrace();
        }

    }

}