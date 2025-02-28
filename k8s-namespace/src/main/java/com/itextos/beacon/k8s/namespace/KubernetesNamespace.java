package com.itextos.beacon.k8s.namespace;

import java.util.ArrayList;
import java.util.List;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class KubernetesNamespace {
	
	
	private List<String> getNameSpace(){
		
		List<String> namespacelist=new ArrayList<String>();
		
		namespacelist.add("masterdb");
		namespacelist.add("billingdb");
		namespacelist.add("kanneldb");
		namespacelist.add("kafka");
		namespacelist.add("redis");
		namespacelist.add("elasticsearch");
		namespacelist.add("beacon");
		namespacelist.add("smsbox");
		namespacelist.add("bearerbox");

		return namespacelist;
	}

	
 
    public  void docreateNameSpaceIfNotExsits() throws Exception {
        // Load Kubernetes client configuration
        ApiClient client = io.kubernetes.client.util.Config.defaultClient();
        
        Configuration.setDefaultApiClient(client);
        
        String env=System.getenv("env");
        List<String> namespacelist=getNameSpace();

        
        	
        namespacelist.forEach((namespaceName)->{
        	
        	
        		
        		if(namespaceName.equals("beacon")) {
            		
        			List<String> clusterlist=getClusterList();
        			clusterlist.forEach((cluster)->{
        				
                		String np=env+"-"+namespaceName+"-"+cluster;
                    	creaetNS(np);

        			});
            	}else {
            		
            		String np=env+"-"+namespaceName;
            		
                	creaetNS(np);


            	}
        	
        	
        	 
        	 
        });

        
       
    }

    private List<String> getClusterList() {
		List<String> clusterlist=new ArrayList<String>();
		
		clusterlist.add("bulk");
		clusterlist.add("otp");
		clusterlist.add("trans");
		clusterlist.add("international");

		return clusterlist;
	}



	private void creaetNS(String np) {
	
    	 if (isNamespaceAvailable(np)) {
             System.out.println("Namespace '" + np + "' already exists.");
         } else {
             createNamespace(np);
             System.out.println("Namespace '" + np + "' created.");
         }
		
	}

	private boolean isNamespaceAvailable(String namespaceName) {
        CoreV1Api coreV1Api = new CoreV1Api();
        try {
            // Try to read the namespace
        	coreV1Api.readNamespace(namespaceName, namespaceName);
        	return true; // Namespace exists
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return false; // Namespace does not exist
            } else {
                throw new RuntimeException("Error checking namespace availability: " + e.getResponseBody(), e);
            }
        }
    }

    private void createNamespace(String namespaceName) {
        CoreV1Api coreV1Api = new CoreV1Api();

        V1Namespace namespace = new V1Namespace();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.name(namespaceName);
        namespace.metadata(metadata);

        try {
            coreV1Api.createNamespace(namespace, namespaceName, null, null, null);
        } catch (ApiException e) {
            throw new RuntimeException("Error creating namespace: " + e.getResponseBody(), e);
        }
    }
}
