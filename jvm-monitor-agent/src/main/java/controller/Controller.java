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

package controller;

import com.sun.tools.attach.AttachNotSupportedException;
import communicator.DAScpuPublisher;
import communicator.DASmemoryPublisher;
import communicator.DASPublisher;
import jvmmonitor.UsageMonitor;
import jvmmonitor.exceptions.MonitoringNotStartedException;
import jvmmonitor.model.GarbageCollectionLog;
import jvmmonitor.util.GarbageCollectionListener;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Controller implements GarbageCollectionListener {

    private final DASPublisher dasGCPublisher;
    private final DASmemoryPublisher dasMemoryPublisher;
    private final DAScpuPublisher dasCPUPublisher;


    public Controller() throws DataEndpointException,
            SocketException,
            UnknownHostException,
            DataEndpointConfigurationException,
            DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException {

        dasMemoryPublisher = new DASmemoryPublisher(7611, 9611, "admin", "admin");
        dasCPUPublisher = new DAScpuPublisher(7611, 9611, "admin", "admin");
        dasGCPublisher = new DASPublisher(7611, 9611, "admin", "admin");

    }

    public void sendUsageData(String pid, Controller controllerObj) throws IOException,
            AttachNotSupportedException,
            MalformedObjectNameException,
            InterruptedException,
            MonitoringNotStartedException,
            DataEndpointException {

        UsageMonitor usageObj = new UsageMonitor(pid);
        usageObj.stratMonitoring();
        usageObj.registerGCNotifications(controllerObj);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        dasMemoryPublisher.setUsageObj(usageObj);
        dasCPUPublisher.setUsageObj(usageObj);

        Runnable memory = dasMemoryPublisher;
        Runnable cpu = dasCPUPublisher;

        executor.execute(memory);
        executor.execute(cpu);

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        dasMemoryPublisher.shutdownDataPublisher();
        dasCPUPublisher.shutdownDataPublisher();
        dasGCPublisher.shutdownDataPublisher();

    }


    public void processGClogs(LinkedList<GarbageCollectionLog> gcLogList) {

        try {
            dasGCPublisher.publishGCData(gcLogList);
        } catch (DataEndpointAuthenticationException e) {
            e.printStackTrace();
        } catch (DataEndpointAgentConfigurationException e) {
            e.printStackTrace();
        } catch (DataEndpointException e) {
            e.printStackTrace();
        } catch (DataEndpointConfigurationException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        }

    }

}
