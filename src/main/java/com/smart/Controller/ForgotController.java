package com.smart.Controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random(1000);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private  EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		System.out.println("EMAIl: "+email);
		//generating otp of 4 digit
		int otp = random.nextInt(10000);
		System.out.println(otp);
		String subject = "OTP from SCM";
		String message = ""+
				"<div>"
				+"<p>"
				+"This is email from Smart Contact Manager application regarding your password changement."
				+"</p><br><br>"
				+"<h1 style='border:1px solid #e2e2e2; padding:20px; border-radius:25px;'>"
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			
			session.setAttribute("myotp",otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
		}
		else {
			
			session.setAttribute("message", "Check your email id!!");
			return "forgot_email_form";
		}
		
		
	}
	
	//verify otp
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int myOtp =(int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myOtp==otp) {
			//passowrd change form
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				//send error message;
				session.setAttribute("message", "User does not exits with this email");
				
				return "forgot_email_form";
			}
			else {
				//send change password form
				
				
			}
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message","You have enterd wrong otp");
			return "verify_otp";
		}
		
	}
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword")String newpassword,HttpSession session) {
		
		
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully..";
		
	}
}
