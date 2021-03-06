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
package eu.vre4eic.evre.nodeservice.services;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.vre4eic.evre.core.comm.NodeLinker;
import eu.vre4eic.evre.core.messages.Message;
import eu.vre4eic.evre.nodeservice.Settings;
import eu.vre4eic.evre.nodeservice.nodemanager.ZKServer;
import io.swagger.annotations.ApiOperation;


/**
 * This class contains methods for managing users. 
 * @author Cesare
 *
 */

@Controller
public class NodeController {

	private static final String RELEASE = "release";
	private static final String WELCOME_PAGE = "welcome";
	NodeLinker node;
	//Properties property = new Properties();
	public NodeController()  {
		super();
		ZKServer.init();
		Properties defaultSettings = Settings.getProperties();
		
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		node = NodeLinker.init(ZkServer);
		
		/*InputStream in;
		
		try {
			in = this.getClass().getClassLoader()
					.getResourceAsStream("Nodeservice.properties");
			property.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}*/
	}

	//@JsonIgnore
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String WelcomePage(ModelMap model, HttpSession session) {

		session.setAttribute(RELEASE, node.getEvreVersion());
		
		return WELCOME_PAGE;
	}
	//list of services
	    
		/*@RequestMapping(value="/servicesdoc", method=RequestMethod.GET)
		public String userServices(Model model, HttpSession session, @RequestParam(value="component")String component) {
			session.setAttribute(RELEASE, "0.01a");

			return component;
		}*/
		@RequestMapping(value = "/doc", method = RequestMethod.GET)
		public String docs(ModelMap model, HttpSession session) {
			System.out.println("showing docs");
			return "doc/index"; 
		}
		
		@ApiOperation(value = "creates an id that can be used to register an e-VRE building block in the node", 
				notes = "A user with a valid Admin token can invoke this web service to get an id that can be used to register an e-VRE building block to the  e-VRE Node", 
				response = Message.class)

		@RequestMapping(value="node/getevrecomponentid", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
		public Message getEvreComponentID(@RequestParam(value="token") String token) {

			return null;
		}
		
		@ApiOperation(value = "returns the list of running e-VRE building block in the node", 
				notes = "A user with a valid Admin token can invoke this web service to get the list e-VRE building block active in the e-VRE Node", 
				response = Message.class)

		@RequestMapping(value="node/listcomponents", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
		public Message removeEvreComponentID(@RequestParam(value="token") String token) {

			return null;
		}

}
