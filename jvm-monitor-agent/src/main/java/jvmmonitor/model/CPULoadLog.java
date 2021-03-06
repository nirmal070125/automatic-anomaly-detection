package jvmmonitor.model;

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
 * CPU load model class
 * Stores
 *  Process CPU Load
 *  System CPU Load
 */
public class CPULoadLog {

    private double processCPULoad;
    private double systemCPULoad;

    public double getProcessCPULoad() {
        return processCPULoad;
    }

    public void setProcessCPULoad(double processCPULoad) {
        this.processCPULoad = processCPULoad;
    }

    public double getSystemCPULoad() {
        return systemCPULoad;
    }

    public void setSystemCPULoad(double systemCPULoad) {
        this.systemCPULoad = systemCPULoad;
    }
}
