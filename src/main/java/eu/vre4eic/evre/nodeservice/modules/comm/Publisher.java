/**
 * 
 */
package eu.vre4eic.evre.nodeservice.modules.comm;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.annotation.EnableJms;

import eu.vre4eic.evre.core.Common.Topics;
import eu.vre4eic.evre.core.messages.Message;

import eu.vre4eic.evre.nodeservice.modules.comm.CommModule;

/**
 * @author francesco
 *
 */

@EnableJms
public class Publisher<T extends Message>  {

	private  MessageProducer dipatcher;
	private Session session;

	public Publisher(Topics channel) {
		try {
			session  = CommModule.getInstance().getSession();
			Destination destination = session.createTopic(channel.toString());
			dipatcher = session.createProducer(destination);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void publish(T m) throws JMSException {
		ObjectMessage om = session.createObjectMessage(m);
		dipatcher.send(om);
	}




}