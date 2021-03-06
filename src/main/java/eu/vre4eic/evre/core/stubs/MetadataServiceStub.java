/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.vre4eic.evre.core.stubs;

import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.vre4eic.evre.nodeservice.Settings;

public class MetadataServiceStub {
	
	private static Logger log = LoggerFactory.getLogger(MetadataServiceStub.class.getClass());
	private static MetadataServiceStub instance;
	
	private static final String MESSAGE_BROKER = "/MetadataService";
	private static final String URL = "/url";
	

	private static Properties defaultSettings;
	
	private static String basePath;
	private static String url;


	
	private MetadataServiceStub(String nodeServiceURL, String serviceName, String env) {
		basePath = env + serviceName;
		loadRemoteProperties(nodeServiceURL, basePath);
	}

		
	public  static MetadataServiceStub  getInstance(String nodeServiceURL){
		if (instance == null)
			try {
				instance = new MetadataServiceStub(nodeServiceURL, MESSAGE_BROKER, Settings.EVRE_PRODUCTION);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return instance;
	}
	
	public  static MetadataServiceStub  getInstance(String nodeServiceURL, String env){
		if (instance == null)
			try {
				instance = new MetadataServiceStub(nodeServiceURL, MESSAGE_BROKER, env);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return instance;
	}
	
	
	private  static void loadRemoteProperties(String nodeServiceURL, String basePath){



		defaultSettings = Settings.getProperties();
		
		if (nodeServiceURL == null)
			nodeServiceURL = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);


		
		CuratorFramework client = CuratorFrameworkFactory
				.newClient(nodeServiceURL,new RetryOneTime(1));
		client.start();
		try
		{
			url = getZKProperty(basePath + URL,client);
		}
		
		finally
		{
			if (client != null)
				client.close();
		}
	}
	
	private static String getZKProperty(String property_name, CuratorFramework client){
		String property_path = defaultSettings.getProperty(property_name);

		try {
			byte[] data = client.getData().forPath(property_path);
			if (data != null) 
				return new String(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	
	public  String getURL() {
		return url;
		
	}




}
