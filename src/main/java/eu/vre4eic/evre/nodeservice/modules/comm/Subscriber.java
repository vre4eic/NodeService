/**
 * 
 */
package eu.vre4eic.evre.nodeservice.modules.comm;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.vre4eic.evre.core.Common.Topics;
import eu.vre4eic.evre.nodeservice.modules.comm.CommModule;


/**
 * @author francesco
 *
 */


public class Subscriber<T extends eu.vre4eic.evre.core.messages.Message> {

	private static Logger log = LoggerFactory.getLogger(Subscriber.class);

	private  MessageConsumer messageConsumer;
	private Destination destination;
	private Session session;

	private class AMQListener implements javax.jms.MessageListener {

		private Logger log = LoggerFactory.getLogger(this.getClass());
		private MessageListener<T> listener;

		public  AMQListener(MessageListener<T> listener ) {
			this.listener = listener;
		}

		/* (non-Javadoc)
		 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
		 */
		@Override
		public void onMessage(Message message) {
			try {
				if (message instanceof ObjectMessage) {
					try {
						T m = (T) ((ObjectMessage) message).getObject();
						log.info("##### Broker message arrived #####");
						listener.onMessage(m);           		            	
					}
					catch (JMSException ex) {
						throw new RuntimeException(ex);
					}
				}
				else {
					throw new IllegalArgumentException("Message must be of type ObjectMessage serializing Message extentions ");
				}

			} catch (Exception e) {
				// MessageListener cannot throw exceptions
				e.printStackTrace();
			}
		}

	}

	public Subscriber(Topics topic) {
		try {
			session  = CommModule.getInstance().getSession();
			destination = session.createTopic(topic+"?consumer.retroactive=true");
			messageConsumer = session.createConsumer(destination);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void setListener(eu.vre4eic.evre.nodeservice.modules.comm.MessageListener<T> listener)  {
		try {
			if (listener == null)
				messageConsumer.setMessageListener(null);
			else {
				messageConsumer.setMessageListener(new AMQListener(listener));
				log.info(" subscribed to topic:: " + destination.toString());
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}




}
