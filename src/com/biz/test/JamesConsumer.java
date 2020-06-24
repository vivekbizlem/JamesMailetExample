package com.biz.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
 
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JamesConsumer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		callConsumer();
	}
	//URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = "tcp://bluealgo.com:61616";
     
    // default broker URL is : tcp://localhost:61616"
    private static String subject = "JAMES_QUEUE"; // Queue Na
	
    private static String activeMQPassword = "admin";
    
    private static String activeMQUsername = "admin";
    
    public static void callConsumer(){
		try{
			
			 ConnectionFactory connectionFactory  = new ActiveMQConnectionFactory(activeMQUsername,activeMQPassword,url);
			 Connection connection = connectionFactory.createConnection();
			 connection.start();
			
			 Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
			//queueName = queueName.toUpperCase();
			Destination destination = session.createQueue(subject);
			MessageConsumer consumer = session.createConsumer(destination);
			//MessageProducer producer = session.createProducer(null);  // no default queue
			

			// Listen for arriving messages 
			MessageListener listener = new MessageListener() { 

			@Override
			public void onMessage(Message message) {
				try {

				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
									
						System.out.println(textMessage.getText());
					}
			    }
				 catch (Exception e) {
						e.printStackTrace();
						System.out.print("error message :: "+e.getMessage());
					}
			} 
			};
			consumer.setMessageListener(listener);
		
			}catch(Exception e){
				System.out.print("error :: "+e.getMessage());
			}
	}
	


}
