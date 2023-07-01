package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;



@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		// rest of the code
		
		boolean f = false;
		
		String from  = "uditshahi96@gmail.com";
		// Variable for gmail
		
		String host = "smtp.gmail.com";
		//get the system properties
		
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES "+properties);
		// setting importing information to properties object
		
		//host set
		
		properties.put("mail.smtp.host",host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		// to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("uditshahi96@gmail.com","ddrpyglpsxptbbto");
			}
		});
		session.setDebug(true);
		
		//Step 2:- compose the message[text,multimedia]
		
		MimeMessage m = new MimeMessage(session);
		//from email
		try {
		m.setFrom(from);
		
		// adding recipient to message
		
		m.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
		
		// adding subject to message
		
		m.setSubject(subject);
		// add text to message
		
		//m.setText(message);
		
		m.setContent(message,"text/html");
		
		
		
		//send
		// Step 3: send the message using Transport class
		
		Transport.send(m);
		
		System.out.println("send success................");
		 f=true;
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
		return f;
	}
}
