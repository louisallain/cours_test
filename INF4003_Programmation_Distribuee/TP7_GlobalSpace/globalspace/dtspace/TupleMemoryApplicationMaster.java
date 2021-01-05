package dtspace;

import java.io.IOException;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.LocalResource;
import java.util.Map;
import java.util.HashMap;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.util.Apps;
import java.io.File;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TupleMemoryApplicationMaster {

    //public YarnConfiguration yarnConfiguration = new YarnConfiguration();
    public static Configuration conf = new YarnConfiguration();


    public static void main(String[] args) throws YarnException, IOException, InterruptedException {

        Configuration configuration = new YarnConfiguration();
        String jarPath = args[0];
        int numberOfContainers = Integer.parseInt(args[1]);

        System.out.println("Starting Application Master");
        AMRMClient<AMRMClient.ContainerRequest> resourceManagerClient = AMRMClient.createAMRMClient();
        resourceManagerClient.init(configuration);
        resourceManagerClient.start();
        System.out.println("Started AMRMClient");
        NMClient nodeManagerClient = NMClient.createNMClient();
        nodeManagerClient.init(configuration);
        nodeManagerClient.start();
        System.out.println("Started NMClient");
        resourceManagerClient.registerApplicationMaster("localhost", 80010, "tuplememoryappmaster_2246");
        System.out.println("Registration done");
        // Priority for worker containers - priorities are intra-application
        Priority priority = Records.newRecord(Priority.class);
        priority.setPriority(0);
        // Resource requirements for worker containers
        Resource capability = Records.newRecord(Resource.class);
        capability.setVirtualCores(1);


        for(int i=0; i < numberOfContainers; i++){
            //System.out.println("CONTAINER_ID = " + containerId);
            AMRMClient.ContainerRequest containerRequest = new AMRMClient.ContainerRequest(capability, null, null, priority);
            resourceManagerClient.addContainerRequest(containerRequest);
        }
        int completedContainers = 0;
        int containerId = 0;
        int nbInstance = 0;

        while(completedContainers < numberOfContainers) {
            
            AllocateResponse allocateResponse = resourceManagerClient.allocate(containerId);
            containerId++;
            for(Container container : allocateResponse.getAllocatedContainers()){
                // ecriture des hostnames dans zookeeper
                new PrintNode(nbInstance, container.getNodeId().getHost());
                ContainerLaunchContext shellContainerContext = Records.newRecord(ContainerLaunchContext.class);
                
                LocalResource applicationJar = Records.newRecord(LocalResource.class);
                Path jar_path = new Path(jarPath); // dans le HDFS
                String JAR_NAME= "dtspace.jar";
                setupJarFileForApplicationMaster(jar_path, applicationJar); //hdfs  
                shellContainerContext.setLocalResources(Collections.singletonMap(JAR_NAME, applicationJar));
                
                Map<String, String> appMasterEnv = new HashMap<>();
                setupEnvironmentForApplicationMaster(appMasterEnv);
                shellContainerContext.setEnvironment(appMasterEnv);
                
                // création d'un producteur et de 3 consommateurs
                if(nbInstance == 0) {
                    shellContainerContext.setCommands(
                        Collections.singletonList("$JAVA_HOME/bin/java dtspace.Test write " + numberOfContainers +
                                 " 1>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout "  +
                                 " 2>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
                }
                else if(nbInstance == 1){
                    shellContainerContext.setCommands(
                        Collections.singletonList("$JAVA_HOME/bin/java dtspace.Test blocking " + numberOfContainers +
                                 " 1>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout "  +
                                 " 2>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
                }
                else {
                    shellContainerContext.setCommands(
                        Collections.singletonList("$JAVA_HOME/bin/java dtspace.Test ifExists " + numberOfContainers +
                                 " 1>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout "  +
                                 " 2>"  +
                                  ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
                }
                
                nbInstance++;
                nodeManagerClient.startContainer(container, shellContainerContext);
            
            }
            for(ContainerStatus containerStatus : allocateResponse.getCompletedContainersStatuses()){
              completedContainers++;
              System.out.println("Completed Container " + completedContainers + " " + containerStatus);
            }
            Thread.sleep(500);    
        } 
        resourceManagerClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
    }

    public static void setupJarFileForApplicationMaster(Path jarPath, LocalResource localResource) throws IOException {
        FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
        localResource.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
        localResource.setSize(jarStat.getLen());
        localResource.setTimestamp(jarStat.getModificationTime());
        localResource.setType(LocalResourceType.FILE);
        localResource.setVisibility(LocalResourceVisibility.PUBLIC);
    }
    public static void setupEnvironmentForApplicationMaster(Map<String, String> environmentMap) {
        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
            YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
            Apps.addToEnvironment(environmentMap, ApplicationConstants.Environment.CLASSPATH.name(),c.trim());
        }
        Apps.addToEnvironment(environmentMap,
        ApplicationConstants.Environment.CLASSPATH.name(),
        ApplicationConstants.Environment.PWD.$() + File.separator + "*");
    }

}
