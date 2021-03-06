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
/**
 * 
 */
package eu.vre4eic.evre.nodeservice.usermanager;

import java.util.List;

import eu.vre4eic.evre.core.EvreEvent;
import eu.vre4eic.evre.core.EvreQuery;
import eu.vre4eic.evre.core.UserCredentials;
import eu.vre4eic.evre.core.UserProfile;
import eu.vre4eic.evre.core.impl.EVREUserProfile;
import eu.vre4eic.evre.core.messages.AuthenticationMessage;
import eu.vre4eic.evre.core.messages.Message;

/**
 * This interface defines methods for managing User profiles. 
 * 
 * 
 * @author cesare
 *
 */
public interface UserManager {
	
	
	/**
	 * Creates a User profile
	 * @param profile EVREUserProfile
	 * @return Message 
	 */
	
	public Message createUserProfile(EVREUserProfile profile);
	
	/**
	 * Updates a User profile  
	 * @param userId String - the User identifier
	 * @param profile EVREUserProfile - the User profile
	 * @return Message 
	 */
	
	public Message updateUserProfile(String userId, EVREUserProfile profile);
	
	/**
	 * Used to recovery forgotten credentials 
	 * @param email String 
	 * @return Message 
	 */
	
	public Message passwordRecovery(String email);
	
	/**
	 * Removes the profile of a specific User
	 * @param userId String  
	 * @return Message 
	 */
	
	public Message removeUserProfile(String userId);
	
	/**
	 * Returns a profile for a specific User id
	 * @param userId String
	 *   
	 * @return EVREUserProfile 
	 */
	
	public EVREUserProfile getUserProfile(String userId);
	
	/**
	 * Returns a profile for a specific User id that provides credentials
	  * @param credentials UserCredentials 
	 * @return EVREUserProfile 
	 */
	
	public EVREUserProfile getUserProfile(UserCredentials credentials);
	

	/**
	 * Returns a list of User profiles
	 * @param query EvreQuery    
	 * @return the list of User Profiles matching the query conditions
	 */
	
	public List <EVREUserProfile> getUserProfile(EvreQuery query);
	
	/**
	 * Returns all User profiles
	 *  
	 * @return all User Profiles
	 */
	
	public List <EVREUserProfile> getAllUserProfiles();
	
	public Message subscribeEvent(String idUser, List <EvreEvent> events);
	
	
	public Message unSubscribeEvent(String idUser, List <EvreEvent> events);
	
	public Message checkEvent(String userId, String eventId);
	
	public List <Message> checkEvent(String userId);
	
	
	public List<EvreEvent> getSubscribedEvents (String userId);
	
	/**
	 * Authenticates a User
	 * @param credentials UserCredentials
	 * @return AuthenticationMessage
	 */
	
	public AuthenticationMessage login (UserCredentials credentials);
	
	/**
	 * Authenticates a User using the 2FA protocol
	 *  @param userId String
	 *  @param password String
	 * @return AuthenticationMessage
	 */
	
	public AuthenticationMessage loginMFA (String userId, String password);
	
	/**
	 * Authenticates a User using the 2FA protocol
	 * @param token String
	 * @param code String
	 * @return AuthenticationMessage
	 */
	
	public AuthenticationMessage loginMFACode (String token, String code);
	
	/**
	 * Logs out a User
	 * @param token String
	 * @return AuthenticationMessage
	 */
	
	public AuthenticationMessage logout(String token);
	
	/**
	 * Registers an Authenticator for a specific User
	 * @param credentials UserCredentials
	 * @param authId String
	 * @return Message
	 */
	
	public Message registerAuthenticator(UserCredentials credentials, String authId);
	
	/**
	 * removes an Authenticator for a User
	 * @param credentials UserCredentials
	 * @param authId String 
	 * @return Message
	 */
	
	public Message removeAuthenticator(UserCredentials credentials, String authId);
	
	/*
	 * signature changed from the original deliverable description, need to verify.
	 */
	public Message authenticate(UserCredentials credentials, String authId);
	
	public Message  synchCredentials (String authId, List <UserCredentials> credentials);

}
