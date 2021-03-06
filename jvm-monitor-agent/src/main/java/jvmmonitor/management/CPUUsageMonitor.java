package jvmmonitor.management;

import com.sun.management.OperatingSystemMXBean;
import jvmmonitor.model.CPULoadLog;

import javax.management.MBeanServerConnection;
import java.io.IOException;

import static java.lang.management.ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

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
 * Collect the CPU load percentages from any connected JVM process
 */
public class CPUUsageMonitor {

    private OperatingSystemMXBean osMXBean;


    /**
     * Constructor
     * @param serverConnection
     */
    public CPUUsageMonitor(MBeanServerConnection serverConnection) throws IOException {
        this.osMXBean = newPlatformMXBeanProxy(serverConnection, OPERATING_SYSTEM_MXBEAN_NAME , OperatingSystemMXBean.class);
    }

    /**
     * Return CPU load percentages of the System and the process
     * @return {CPULoadLog}
     */
    public CPULoadLog getCPULoads(){

        if (osMXBean != null){
            CPULoadLog cpuLoadLog = new CPULoadLog();
            cpuLoadLog.setProcessCPULoad(osMXBean.getProcessCpuLoad());
            cpuLoadLog.setSystemCPULoad(osMXBean.getSystemCpuLoad());

            return cpuLoadLog;
        }else {
            throw new NullPointerException();
        }


    }

}
