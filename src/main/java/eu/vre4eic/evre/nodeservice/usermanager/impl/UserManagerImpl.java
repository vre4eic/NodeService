/**
 * 
 */
package eu.vre4eic.evre.nodeservice.usermanager.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import eu.vre4eic.evre.core.Common;
import eu.vre4eic.evre.core.EvreEvent;
import eu.vre4eic.evre.core.EvreQuery;
import eu.vre4eic.evre.core.UserCredentials;
import eu.vre4eic.evre.core.UserProfile;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.Common.UserRole;
import eu.vre4eic.evre.core.impl.EVREUserProfile;
import eu.vre4eic.evre.core.messages.AuthenticationMessage;
import eu.vre4eic.evre.core.messages.Message;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.AuthenticationMessageImpl;
import eu.vre4eic.evre.core.messages.impl.MessageImpl;
import eu.vre4eic.evre.nodeservice.Utils;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.nodeservice.usermanager.UserManager;
import eu.vre4eic.evre.nodeservice.usermanager.dao.UserProfileRepository;

/**
 * @author cesare
 *
 */
@Configuration

public class UserManagerImpl implements UserManager {

	@Autowired
	private UserProfileRepository repository;
	/**
	 * 
	 */
	public UserManagerImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#createUserProfile(eu.vre4eic.evre.core.UserProfile)
	 */
	@Override
	public Message createUserProfile(EVREUserProfile profile) {
		
		if (repository.findByUserId(profile.getUserId())!=null)
			return( new MessageImpl("Operation not executed, User Id not unique", Common.ResponseStatus.FAILED));
			
		repository.save(profile);
		
		return( new MessageImpl("Operation completed", Common.ResponseStatus.SUCCEED));
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#updateUserProfile(java.lang.String, eu.vre4eic.evre.core.UserProfile)
	 */
	@Override
	public Message updateUserProfile(String userId, EVREUserProfile profile) {
		if (repository.findByUserId(userId)==null)
			return( new MessageImpl("Operation not executed, User profile not found", Common.ResponseStatus.FAILED));
			
		repository.save(profile);
		return( new MessageImpl("Operation completed", Common.ResponseStatus.SUCCEED));
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#removeUserProfile(java.lang.String)
	 */
	@Override
	public Message removeUserProfile(String userId) {
		EVREUserProfile profile= repository.findByUserId(userId);
		if (profile==null)
			return( new MessageImpl("Operation not executed, User profile not found", Common.ResponseStatus.FAILED));
			
		
		repository.delete(profile);
		return( new MessageImpl("Operation completed", Common.ResponseStatus.SUCCEED));
		
		
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#getUserProfile(java.lang.String)
	 */
	@Override
	public EVREUserProfile getUserProfile(String userId) {
		
		EVREUserProfile profile= repository.findByUserId(userId);
		return profile;	
		
		
		
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#getUserProfile(eu.vre4eic.evre.core.UserCredentials)
	 */
	@Override
	public EVREUserProfile getUserProfile(UserCredentials credentials) {
		EVREUserProfile profile= repository.findByUserId(credentials.getUserId());
		if (profile!=null && credentials.getPassword().equals(profile.getPassword()))
			return profile;
		return null;
		
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#getUserProfile(eu.vre4eic.evre.core.EvreQuery)
	 */
	@Override
	public List<UserProfile> getUserProfile(EvreQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#subscribeEvent(java.lang.String, java.util.List)
	 */
	@Override
	public Message subscribeEvent(String idUser, List<EvreEvent> events) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#unSubscribeEvent(java.lang.String, java.util.List)
	 */
	@Override
	public Message unSubscribeEvent(String idUser, List<EvreEvent> events) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#checkEvent(java.lang.String, java.lang.String)
	 */
	@Override
	public Message checkEvent(String userId, String eventId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#checkEvent(java.lang.String)
	 */
	@Override
	public List<Message> checkEvent(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#getSubscribedEvents(java.lang.String)
	 */
	@Override
	public List<EvreEvent> getSubscribedEvents(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#login(eu.vre4eic.evre.core.UserCredentials)
	 */
	@Override
	public AuthenticationMessage login(UserCredentials credentials) {
		Publisher<AuthenticationMessage> p =  PublisherFactory.getAuthenticationPublisher();

		String TTL = Utils.getNodeServiceProperties().getProperty("TOKEN_TIMEOUT");

		LocalDateTime timeLimit;
		AuthenticationMessage ame;

		ame = new AuthenticationMessageImpl(Common.ResponseStatus.FAILED, "Operation completed",
				"", null,LocalDateTime.MIN);
		ame.setTimeZone(ZoneId.systemDefault().getId());
	
		UserProfile profile= repository.findByUserId(credentials.getUserId());
		if (profile==null){
			ame.setTimeLimit(LocalDateTime.MIN);
			return ame;
		}
		
		if (credentials.getPassword().equals(profile.getPassword())){

			timeLimit = LocalDateTime.now().plusMinutes(Integer.valueOf(TTL));
			ame = new AuthenticationMessageImpl(Common.ResponseStatus.SUCCEED, "Operation completed",
					profile.getPassword(), profile.getRole(),timeLimit);
			ame.setTimeZone(ZoneId.systemDefault().getId())
			   .setRenewable(TTL);
			try {
				p.publish(ame);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return (ame);
		}
		ame.setTimeLimit(LocalDateTime.MIN);
		return ame;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#login(java.lang.String, java.lang.String)
	 */
	@Override
	public AuthenticationMessage login(String userId, String password, String authId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#logout(java.lang.String)
	 *change this implementation
	 */
	@Override
	public AuthenticationMessage logout(String token) {
		Publisher<AuthenticationMessage> p =  PublisherFactory.getAuthenticationPublisher();
		
		LocalDateTime timeLimit;
		AuthenticationMessage ame;
		
		timeLimit = LocalDateTime.MIN;
		ame = new AuthenticationMessageImpl(Common.ResponseStatus.SUCCEED, "Operation completed",
				token, Common.UserRole.ADMIN,timeLimit);
		ame.setTimeZone(ZoneId.systemDefault().getId());
		try {
			p.publish(ame);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ame;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#registerAuthenticator(eu.vre4eic.evre.core.UserCredentials, java.lang.String)
	 */
	@Override
	public Message registerAuthenticator(UserCredentials credentials,
			String authId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#removeAuthenticator(eu.vre4eic.evre.core.UserCredentials, java.lang.String)
	 */
	@Override
	public Message removeAuthenticator(UserCredentials credentials,
			String authId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#authenticate(eu.vre4eic.evre.core.UserCredentials, java.lang.String)
	 */
	@Override
	public Message authenticate(UserCredentials credentials, String authId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.vre4eic.evre.nodeservice.usermanager.UserManager#synchCredentials(java.lang.String, java.util.List)
	 */
	@Override
	public Message synchCredentials(String authId,
			List<UserCredentials> credentials) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message passwordRecovery(String email) {
		UserProfile profile= repository.findByEmail(email);
		if (profile !=null){
			//TO BE IMPLEMENTED: send an email message to recover password
			return (new MessageImpl("An email has been sent to the specified address", Common.ResponseStatus.SUCCEED));
		}
			
		return (new MessageImpl("No user found with the specified email", Common.ResponseStatus.EMPTY_RESULT));

	}
}
