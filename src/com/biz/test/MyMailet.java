package com.biz.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.GenericMailet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
 
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class MyMailet extends GenericMailet {
	private static final Logger logger = LoggerFactory.getLogger(MyMailet.class);
	private String TemporaryAttachmentPath="/home/ubuntu/tomcat/apache-tomcat-8.5.55/webapps/ROOT/Attachments/";
	private String TemporaryAttachmentUrl="http://35.196.58.234:8080/Attachments/";
	
	//URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = "tcp://bluealgo.com:61616";
     
    // default broker URL is : tcp://localhost:61616"
    private static String subject = "JAMES_QUEUE"; // Queue Na
	
    private static String activeMQPassword = "admin";
    
    private static String activeMQUsername = "admin";
    
	@Override
	public void service(Mail mail) throws MessagingException {
		// mail.getMessage().getC
		try {
			String mailFrom = "";
			String mailRecipientTO = "";
			String mailSubject = "";
			String mailBody = "";
			JSONObject mainJson = new JSONObject();
			log("log via mailet logger with INFO level");
			MimeMessage objMimeMessage = mail.getMessage();
			Address[] to = objMimeMessage.getRecipients(Message.RecipientType.TO);
			JSONArray toArr = new JSONArray();
			for(int i=0;i<to.length;i++){
				toArr.put(to[i]);
			}
			log("MyMailet recipient to list :: " + toArr);
			mainJson.put("to", toArr);
			
			JSONArray ccArr = new JSONArray();
			if(objMimeMessage.getRecipients(Message.RecipientType.CC) != null){
			Address[] cc = objMimeMessage.getRecipients(Message.RecipientType.CC);
			for(int i=0;i<cc.length;i++){
				ccArr.put(cc[i]);
			}
			}
			log("MyMailet recipient cc list :: " + ccArr);
			mainJson.put("cc", ccArr);
			
//			Address[] cc = objMimeMessage.getRecipients(Message.RecipientType.CC);
//			log("MyMailet recipient to list :: " + objMimeMessage.getRecipients(Message.RecipientType.TO));
//			log("MyMailet recipient to list :: " + objMimeMessage.getRecipients(Message.RecipientType.CC));
			mailSubject = objMimeMessage.getSubject();
			mainJson.put("subject", mailSubject);
			log("MyMailet subject list :: " + mailSubject);
			//objMimeMessage.getContent().
			mailBody = getTextFromMessage(objMimeMessage);
			mainJson.put("body", mailBody);
			log("MyMailet body list :: " + mailBody);
			log("MyMailet body content type :: " + objMimeMessage.getContentType());
			// log("MyMailet From list :: " + mail.getFrom()[0]);
//			Collection<MailAddress> rcpt = mail.getRecipients();
//			// rcpt.forEach(action);
//			for (MailAddress mailAddress : rcpt) {
//				log("MyMailet From list :: " + mailAddress.asString());
//			}
			mailFrom = mail.getSender().asString();
			log("MyMailet sender list :: " + mailFrom);
			mainJson.put("from", mailFrom);
			// mail.
//			Enumeration headers = objMimeMessage.getAllHeaders();
//			while (headers.hasMoreElements()) {
//
//				log("header next :: " + headers.nextElement().toString());
//				// String param = (String) e.nextElement();
//			}
			JSONArray attachFileUrl = AttachmentSaveData(objMimeMessage);
			mainJson.put("attachments", attachFileUrl);
			log("mainJson produce :: " + mainJson);
			callMQProducer(mainJson.toString());
			// objMimeMessage.getC
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// logger.info("Log via slf4j with INFO level !!! Add
		// log4j.logger.com.test=INFO, CONS, FILE in the log4j.properties");
		// logger.debug("Log via slf4j with DEBUG level !!! Add
		// log4j.logger.com.test=DEBUG, CONS, FILE in the log4j.properties");
		// System.out.println("sysout java log via mailet logger with INFO
		// level");
		// logger.info("Log via slf4j with INFO level !!! Add
		// log4j.logger.com.test=INFO, CONS, FILE in the log4j.properties");
		// logger.debug("Log via slf4j with DEBUG level !!! Add
		// log4j.logger.com.test=DEBUG, CONS, FILE in the log4j.properties");
	}

	private String getTextFromMessage(MimeMessage message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("text/html")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				// result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
				result = html;
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	public JSONArray AttachmentSaveData(MimeMessage message) {
		JSONArray attachmentsUrl = new JSONArray();
		try {
			if (message.getContentType().contains("multipart")) {

				Multipart multiPart = (Multipart) message.getContent();
				int numberOfParts = multiPart.getCount();
				MimeBodyPart part = null;
				for (int partCount = 0; partCount < numberOfParts; partCount++) {
					part = (MimeBodyPart) multiPart.getBodyPart(partCount);
					String fileName = "";
					String originalName = "";
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
						originalName = part.getFileName();
						String tomcatfilepath=TemporaryAttachmentPath;
						 tomcatfilepath=tomcatfilepath+ originalName;
							part.saveFile(new File(tomcatfilepath));
							//strFilepath = tomcatfilepath;
							attachmentsUrl.put(TemporaryAttachmentUrl+originalName);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachmentsUrl;
	}
	
    public void callMQProducer(String jsonString) throws JMSException {        
        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQUsername, activeMQPassword,url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
         
        //Creating a non transactional session to send/receive JMS message.
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);  
         
        //Destination represents here our queue 'JCG_QUEUE' on the JMS server. 
        //The queue will be created automatically on the server.
        Destination destination = session.createQueue(subject); 
         
        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT); 
        // We will send a small text message saying 'Hello World!!!' 
//        TextMessage message = session
//                .createTextMessage("Hello !!! Welcome to the world of ActiveMQ.");
        TextMessage message = session.createTextMessage();
        message.setText(jsonString);
         
        // Here we are sending our message!
        producer.send(message);
         
        System.out.println("JCG printing@@ '" + message.getText() + "'");
        connection.close();
    }

}
