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
package eu.vre4eic.evre;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.UserCredentials;
import eu.vre4eic.evre.core.UserProfile;
import eu.vre4eic.evre.core.comm.NodeLinker;
import eu.vre4eic.evre.core.comm.Subscriber;
import eu.vre4eic.evre.core.comm.SubscriberFactory;
import eu.vre4eic.evre.core.impl.EVREUserProfile;
import eu.vre4eic.evre.core.impl.UserCredentialsImpl;
import eu.vre4eic.evre.core.messages.AuthenticationMessage;
import eu.vre4eic.evre.core.messages.Message;
import eu.vre4eic.evre.core.messages.MultiFactorMessage;
import eu.vre4eic.evre.nodeservice.Settings;
import eu.vre4eic.evre.nodeservice.Utils;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import eu.vre4eic.evre.nodeservice.nodemanager.NodeServiceContextListener;
import eu.vre4eic.evre.nodeservice.usermanager.dao.UserProfileRepository;
import eu.vre4eic.evre.nodeservice.usermanager.impl.UserManagerImpl;


//@ContextConfiguration ("/Test-context.xml")
@ComponentScan
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringMongoConfig.class, UserManagerImpl.class, NodeServiceContextListener.class}, 
loader = SpringBootContextLoader.class)

public class UserDaoTest {

	
	
	
	@Autowired
	private UserProfileRepository repository;
	
	@Autowired
	private UserManagerImpl userMI;
	

	private AuthModule module;
	
	private static Vector <String> mfmCode;
	private static Subscriber<MultiFactorMessage> subscriber;
	//@Autowired
	//private CustomerPreferences customerPreference;
	
	@Before
	public void setUp() throws Exception {
		//repository.deleteAll();
		//userMI.removeUserProfile("userId0");
		//userMI.removeUserProfile("userId1");
		//repository.deleteAll();
		subscriber = SubscriberFactory.getMFASubscriber();
		subscriber.setListener(new eu.vre4eic.evre.util.TGBotMFAListener(this));
	}

	private void cleanTestEnv(){
		userMI.removeUserProfile("userId0");
		userMI.removeUserProfile("userId1");
		repository.deleteAll();
	}

	/*
	 * Test the login logout
	 */
	@Test
	public final void testLoginLogout() {	
		
		cleanTestEnv();
		Properties defaultSettings = Settings.getProperties();
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		NodeLinker node = NodeLinker.init(ZkServer);
		
		module = AuthModule.getInstance(node.getMessageBrokerURL());
		//save a user profile
		Message mes=userMI.createUserProfile(new EVREUserProfile("userId0", "userPWD", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER, 
				"email@domain","snsId", "11"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		// execute a login
		UserCredentials uc= new UserCredentialsImpl("userId0", "userPWD");
	
		AuthenticationMessage ame= userMI.login(uc);
		//wait for authentication message being dispatched
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		Boolean auth=module.checkToken(ame.getToken());
		assertEquals(auth, true);
		
		//logout
		userMI.logout(ame.getToken());
		//wait for authentication message being dispatched
		
		try {
			
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is no longer valid
		auth=module.checkToken(ame.getToken());
		assertEquals(auth, false);
		
		
	}
	/*
	 * Test: create a profile, login, get the User Profile, logout
	 */
	@Test
	public final void testCreateGetProfile() {	
		cleanTestEnv();
		Properties defaultSettings = Settings.getProperties();
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		NodeLinker node = NodeLinker.init(ZkServer);		
		String messageBrokerURL =  node.getMessageBrokerURL();
		
		module = AuthModule.getInstance(messageBrokerURL);
		//save a user profile
		Message mes=userMI.createUserProfile(new EVREUserProfile("userId0", "userPWD", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER, 
				"email@domain","snsId", "11"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		// execute a login
		UserCredentials uc= new UserCredentialsImpl("userId0", "userPWD");
	
		AuthenticationMessage ame= userMI.login(uc);
		//wait for authentication message being dispatched
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		Boolean auth=module.checkToken(ame.getToken());
		assertEquals(auth, true);
		UserProfile up=userMI.getUserProfile("userId0");
		assertEquals(up.getUserId(), "userId0");
		//logout
		userMI.logout(ame.getToken());
		//wait for authentication message being dispatched
		
		try {
			
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is no longer valid
		auth=module.checkToken(ame.getToken());
		assertEquals(auth, false);
		
	}
	
	/*
	 * Test: create an admin profile, login, get the User Profile, logout
	 */
	@Test
	public final void testCreateGetAllProfiles() {	
		
		cleanTestEnv();
		
		Properties defaultSettings = Settings.getProperties();
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		NodeLinker node = NodeLinker.init(ZkServer);		
		String messageBrokerURL =  node.getMessageBrokerURL();
		
		module = AuthModule.getInstance(messageBrokerURL);
		//save a user profile
		Message mes=userMI.createUserProfile(new EVREUserProfile("userId0", "userPWD", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.ADMIN, 
				"email@domain","snsId", "11"));
		Message mes1=userMI.createUserProfile(new EVREUserProfile("userId1", "userPWD", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER, 
				"email@domain","snsId", "11"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		// execute a login
		UserCredentials uc= new UserCredentialsImpl("userId0", "userPWD");
	
		
		UserCredentials ucR= new UserCredentialsImpl("userId1", "userPWD");
		AuthenticationMessage ame= userMI.login(uc);
		//wait for authentication message being dispatched
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		Boolean auth=module.checkToken(ame.getToken(), Common.UserRole.ADMIN);
		assertEquals(auth, true);
		List<EVREUserProfile> up=userMI.getAllUserProfiles();
		
		assertEquals(up.size(), 2);
		AuthenticationMessage ameR= userMI.login(ucR);
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		Boolean authR=module.checkToken(ameR.getToken(), Common.UserRole.ADMIN);
		assertEquals(authR, false);
		
		//logout
		userMI.logout(ame.getToken());
		userMI.logout(ameR.getToken());
		//wait for authentication message being dispatched
		
		try {
			
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is no longer valid
		auth=module.checkToken(ame.getToken());
		assertEquals(auth, false);
		authR=module.checkToken(ameR.getToken());
		assertEquals(authR, false);
		
	}
	
	//@Test
	public final void testLoginMFALogout() {	
		cleanTestEnv();
		
		this.mfmCode= new Vector<String>();
		
		Properties defaultSettings = Settings.getProperties();
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		NodeLinker node = NodeLinker.init(ZkServer);		
		String messageBrokerURL =  node.getMessageBrokerURL();
		
		module = AuthModule.getInstance(messageBrokerURL);
		//save a user profile
		Message mes=userMI.createUserProfile(new EVREUserProfile("userId0", "userPWD", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER, 
				"email@domain","snsId", "11"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		// execute a login
		
	
		AuthenticationMessage ame= userMI.loginMFA("userId0", "userPWD");
		//wait for authentication message being dispatched
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		
		assertEquals(UserDaoTest.mfmCode.size(), 1);
		
		userMI.loginMFACode(ame.getToken(), UserDaoTest.mfmCode.get(0));
		
	}
	public void updateMFMQueue(int id, String code){
		
		UserDaoTest.mfmCode.add(code);
	}
	
	@Test
	public final void testUpdateUserProfile() {	
		
		cleanTestEnv();
		
		Properties defaultSettings = Settings.getProperties();
		String ZkServer = defaultSettings.getProperty(Settings.ZOOKEEPER_DEFAULT);
		NodeLinker node = NodeLinker.init(ZkServer);		
		String messageBrokerURL =  node.getMessageBrokerURL();
		
		module = AuthModule.getInstance(messageBrokerURL);
		//save a user profile
		Message mes=userMI.createUserProfile(new EVREUserProfile("userId1", "userPWD1", "Name", "my_organization", eu.vre4eic.evre.core.Common.UserRole.RESEARCHER, 
				"email@domain","snsId", "authId"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		// execute a login
		UserCredentials uc= new UserCredentialsImpl("userId1", "userPWD1");
	
		AuthenticationMessage ame=userMI.login(uc);
		//wait for authentication message being dispatched
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is valid
		Boolean auth=module.checkToken(ame.getToken());
		assertEquals(true, auth);
		
		//logout
		userMI.logout(ame.getToken());
		//wait for authentication message being dispatched
		
		try {
			
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		//check if token is no longer valid
		auth=module.checkToken("userPWD");
		assertEquals(auth, false);
		mes=userMI.updateUserProfile("userId1", new EVREUserProfile("userId1", "userPWDupdate", "Nameupdate", "my_organization", eu.vre4eic.evre.core.Common.UserRole.CONTROLLER, 
				"email@domain","snsId", "authId"));
		assertEquals(Common.ResponseStatus.SUCCEED, mes.getStatus());
		EVREUserProfile eup= userMI.getUserProfile("userId1");
		assertEquals("userPWDupdate", eup.getPassword());
		
	}

}
