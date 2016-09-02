package monitor;/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class Agent {

    static String pid = "6102";

    //private static MemoryMonitor memoryMonitor = new MemoryMonitor();

    public static void main(String[] args) throws InterruptedException {

        /*
        if(args.length!=1) {
            System.err.println("Usage: java Agent <pid>");
        }
        else
        if (memoryMonitor.printStats(pid)) {
            return;
        }
        */

        System.out.println("Currently running");
        for (VirtualMachineDescriptor vmd : VirtualMachine.list())
            System.out.println(vmd.id() + "\t" + vmd.displayName());

    }

}