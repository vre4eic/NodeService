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
package eu.vre4eic.evre.nodeservice.modules.authentication;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.messages.AuthenticationMessage;
import eu.vre4eic.evre.core.messages.ControlMessage;
import eu.vre4eic.evre.nodeservice.nodemanager.ZKServer;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.comm.Subscriber;
import eu.vre4eic.evre.core.comm.SubscriberFactory;


/**
 * This class must be instanced to automatically receive information of the users authenticated with the system.
 * The method checkToken() can be used  to verify if the token received during the invocation of a service is valid.
 * 
 * example:
 * <br>
 * <code>
 * AuthModule module = AuthModule.getInstance(tcp://localhost:61616); <br>
 * module.checkToken(tkn);
 * </code>
 * 
 * <br>
 * If you don't specify a Broker URL ( i.e. AuthModule.getInstance() )
 * <br>then the default URL is tcp://v4e-lab.isti.cnr.it:61616
 * 
 * @author francesco
 *
 */

public class AuthModule {

	private static Logger log = LoggerFactory.getLogger(AuthModule.class);

	private static AuthModule instance = null;
	private Hashtable<String, AuthenticationMessage> AuthTable;
	Publisher<AuthenticationMessage> ap;

	private static String BROKER_URL = "tcp://localhost:61616";

	protected AuthModule() throws JMSException{		
		this(BROKER_URL);		
	}	

	protected AuthModule(String brokerURL) throws JMSException{


		//initialize data structure for tokens
		AuthTable = new  Hashtable<String, AuthenticationMessage> ();

		ap = PublisherFactory.getAuthenticationPublisher(brokerURL);

		Subscriber<ControlMessage> subcriber = SubscriberFactory.getControlSubscriber();
		subcriber.setListener(new ControlListener(this));

		log.info(" #### Authentication Module instantiated ####");
		log.info(" Connecting to Broker:: " + brokerURL);

		//subscribe Auth_channel
		doSubcribe(brokerURL);

	}

	/**
	 * The class constructor is protected and can be instantiated with this method.
	 * The default Broker URL is: v4e-lab.isti.cnr.it
	 * @return AuthModule - the singleton instance of the Class
	 */
	public static AuthModule getInstance() {
		if(instance == null) {
			try {
				instance = new AuthModule();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				log.info(e.getMessage()); 
				e.printStackTrace();
			}
		}
		return instance;

	}

	/**
	 * The class constructor is protected and can be instanced only by this method.
	 * @param brokerURL -  the URL of the Local or Remote Broker
	 * @return AuthModule - the singleton instance of the Class
	 */

	public static AuthModule getInstance(String brokerURL) {
		if(instance == null) {
			try {
				instance = new AuthModule(brokerURL);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				log.info(e.getMessage()); 
				e.printStackTrace();
			}
		}
		return instance;

	}

	/**
	 * It is a private method invoked during the class instantiation to register a listener to the authentication channel
	 * @param brokerURL - the URL of the Broker provider
	 * @throws JMSException - JMS interfaces are used to connect to the provider
	 */
	private void doSubcribe(String brokerURL) throws JMSException{	
		Subscriber<AuthenticationMessage> subscriber = SubscriberFactory.getAuthenticationSubscriber();
		subscriber.setListener(new AuthListener(this));

		// Forces thread switch to receive early notification on Auth_channel
		// TODO improve handshake
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 *  Method invoked by the authentication listener to register tokens of new authenticated users
	 * @param am - AuthenticationMessage received from the system
	 */
	protected void registerToken(AuthenticationMessage am){
		synchronized(AuthTable) {
			AuthTable.put(am.getToken(), am);
			doHousekeeping();
		}
		log.info(" #### registered authentication token ####");
		log.info(" token " + am.getToken() );
		log.info(" token timeout " + am.getTimeLimit() );
	}

	/**
	 *  Method invoked by the authentication listener to register tokens of new authenticated users
	 * @param am - AuthenticationMessage received from the system
	 */
	protected void cancelToken(AuthenticationMessage am) {
		synchronized(AuthTable) {
			AuthTable.remove(am.getToken());
			doHousekeeping();	
		}
		log.info(" #### cancelled authentication token ####");
		log.info(" token " + am.getToken() );
	}


	/**
	 * It must be used to check validity of the token receved with a service invocation
	 * @param token - the token received with a service invocation
	 * @return true - if the token is valid
	 */
	public boolean checkToken (String token) {
		if (AuthTable == null) {
			getInstance();
			return false;
		}

		// granularity on AuthTable Lock could be reduced when renewing
		synchronized(AuthTable) {
			if (AuthTable.containsKey(token)) {
				AuthenticationMessage am = AuthTable.get(token);
				ZoneId zone = ZoneId.of(am.getTimeZone());
				LocalDateTime now = LocalDateTime.now(zone);
				//if (now.isBefore(am.getTimeLimit())){ // token valid
				if (now.isBefore(am.getTimeLimit()) && checkSignature(token)){ // token valid
					doRenew(am, now);
					return true;
				}
				else { // token expired or signature wrong
					AuthTable.remove(token);
					return false;			
				}

			}
		}
		return false;				
	}

	private boolean checkSignature(String token){
		DecodedJWT jwt=null;
		try {
			Algorithm algorithm = Algorithm.HMAC256("fvsecret");
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer("NodeService")
					.build(); //Reusable verifier instance
			jwt = verifier.verify(token);
		} catch (UnsupportedEncodingException exception){
			//UTF-8 encoding not supported
			return false;
		} catch (JWTVerificationException exception){
			//Invalid signature/claims
			return false;
		}
		return true;
	}

	private void doRenew(AuthenticationMessage am, LocalDateTime now) {
		log.info("########### renew check ##########");
		log.info(am.getTimeLimit().toString());
		int renewable = Integer.valueOf(am.getRenewable());
		LocalDateTime halftime = am.getTimeLimit().minusMinutes(renewable/2);
		if (now.isAfter(halftime)) {
			am.setTimeLimit(now.plusMinutes(renewable));
			log.info("########### Renewd ##########");
			log.info(am.getTimeLimit().toString());
			ap.publish(am);
		}

	}

	//Cesare
	/**
	 * It must be used to check validity of a token with a specific role  
	 * @param token - the token received with a service invocation
	 * @param role - the role  required for a service invocation
	 * @return true - if the token is valid
	 */
	public boolean checkToken (String token, Common.UserRole  role) {
		if (AuthTable == null) {
			getInstance();
			return false;
		}

		// granularity on AuthTable Lock could be reduced when renewing
		synchronized(AuthTable) {
			if (AuthTable.containsKey(token)) {
				AuthenticationMessage am = AuthTable.get(token);
				if (am.getRole()!= role)
					return false;
				ZoneId zone = ZoneId.of(am.getTimeZone());
				LocalDateTime now = LocalDateTime.now(zone);
				if (now.isBefore(am.getTimeLimit())){ // token valid
					doRenew(am, now);
					return true;
				}
				else { // token expired
					AuthTable.remove(token);
					return false;			
				}

			}
		}
		return false;				
	}


	/**
	 *  helper method to remove the expired token
	 */
	private void doHousekeeping(){

		Iterator<Entry<String, AuthenticationMessage>> it = AuthTable.entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, AuthenticationMessage> entry = it.next();
			LocalDateTime timelimit = entry.getValue().getTimeLimit();
			ZoneId zone = ZoneId.of(entry.getValue().getTimeZone());
			LocalDateTime now = LocalDateTime.now(zone);	
			if (timelimit.isBefore(now)) {
				it.remove();
			}
		}

	}


	/**
	 * utility to print the table of the managed tokens
	 */
	public void listToken(){
		log.info("#### --------------------------- Token list ----------------------------------- ####" );
		//		LocalDateTime now = LocalDateTime.now();	
		for (Entry<String, AuthenticationMessage> entry : AuthTable.entrySet()) {
			ZoneId zone = ZoneId.of(entry.getValue().getTimeZone());
			LocalDateTime now = LocalDateTime.now(zone);	
			LocalDateTime timelimit = entry.getValue().getTimeLimit();
			Duration period = Duration.between(now, timelimit);
			if (period.isNegative())
				log.info("Token: "+ entry.getKey() + " EXPIRED:   " + period);
			else
				log.info("Token: "+ entry.getKey() + " VALID for: " + period);
		}
		log.info("#### --------------------------- Token list ----------------------------------- ####" );
	}


}
