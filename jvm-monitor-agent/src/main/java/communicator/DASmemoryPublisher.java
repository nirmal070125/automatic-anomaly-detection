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
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.SocketException;
import java.net.UnknownHostException;


public class DASmemoryPublisher extends DASPublisher implements Runnable {

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
    public DASmemoryPublisher(String hostname, int defaultThriftPort, int defaultBinaryPort, String username, String password) throws
            SocketException,
            UnknownHostException,
            DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException,
            DataEndpointException,
            DataEndpointConfigurationException {

        super(hostname, defaultThriftPort, defaultBinaryPort, username, password);

        /**
         * Set default Memory usage stream
         * <p>
         * Data format must be in the following order in given types in "MemoryUsageStream":-
         * <p>
         * long    Timestamp
         * String  AppID
         * long    MAX_HEAP_MEMORY
         * long    ALLOCATED_HEAP_MEMORY
         * long    USED_HEAP_MEMORY
         * long    MAX_NON_HEAP_MEMORY
         * long    ALLOCATED_NON_HEAP_MEMORY
         * long    USED_NON_HEAP_MEMORY
         * long    PENDING_FINALIZATIONS
         */
        String HTTPD_LOG_STREAM = "MemoryUsageStream";
        String VERSION = "1.0.0";
        setDataStream(HTTPD_LOG_STREAM, VERSION);

    }

    /**
     * Need to set UsageMonitorLog before publish data to DAS
     *
     * @param usageLogObj
     */
    public void setUsageLogObj(UsageMonitorLog usageLogObj) {
        this.usageLogObj = usageLogObj;
    }

    @Override
    public void run() {

        try {
            //Send data to EventPublisher
            eventAgent.publishLogEvents(dataPublisher, dataStream, usageLogObj.getTimeStamp(), appID, usageLogObj.getMemoryUsageLog());
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
