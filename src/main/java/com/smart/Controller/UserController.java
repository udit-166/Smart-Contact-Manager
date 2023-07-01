package com.smart.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.Helper.Message;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME" +userName);
		
		//get user by username
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		
		model.addAttribute("user", user);
		
	}
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String apenAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file ,
			Principal principal,HttpSession session) {
		
		
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		if(file.isEmpty()) {
			//if the file is empty then try your message
			System.out.println("File is empty");
			contact.setImage("contact.png");
			
		}else {
			//file the file to folder update the name to contact
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
		
		
		user.getContacts().add(contact);
		contact.setUser(user);
		
		
		this.userRepository.save(user);
		
		System.out.println("DATA "+contact);
		System.out.println("Added to data base");
		// message success........
		session.setAttribute("message", new Message("Your contact is added !! Add more...","success" ));
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(""+e.getMessage());
			e.printStackTrace();
			// error message......
			session.setAttribute("message", new Message("Something went wrong !! try again....","danger" ));
			
		}
		return "normal/add_contact_form";
	}
	//show contact handlers
	//per page =5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model m,Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		//contact ki list bhejni hai
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contact";
	}
	
	//showing particular show contact details
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		Optional<Contact> contactopOptional = this.contactRepository.findById(cid);
		Contact contact = contactopOptional.get();
		
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("title", contact.getName());
			model.addAttribute("contact", contact);
		}
		
		return "normal/contact_detail";
	}
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model,Principal principal) {
		Optional<Contact> contactoOptional = this.contactRepository.findById(cid);
		Contact contact = contactoOptional.get();
		User user2 = this.userRepository.getUserByUserName(principal.getName());
		user2.getContacts().remove(contact);
		this.userRepository.save(user2);
		// check...
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(contact.getUser().getId()==user.getId()) {
			contact.setUser(null);
			this.contactRepository.delete(contact);
			return "redirect:/user/show-contacts/0";
		}
		else {
			return "redirect:/user/show-contacts/0";
		}
		
	}
	// this is update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid,Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	// update contact handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model m,Principal principal) {
		 try {
			 //old contact detail
			 Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			 
			//image
			 if(!file.isEmpty()) {
				 //file work write...
				 //delete old photo
				 File deleteFile = new ClassPathResource("static/img").getFile();
				 File file1=new File(deleteFile,oldContactDetail.getImage());
				 file1.delete();
				 
				 
				 //update new photo
				 
					File saveFile = new ClassPathResource("static/img").getFile();
					
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					
					Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
					contact.setImage(file.getOriginalFilename());
			 }
			 else {
				 contact.setImage(oldContactDetail.getImage());
			 }
			 User user =this.userRepository.getUserByUserName(principal.getName());
			 contact.setUser(user);
			 this.contactRepository.save(contact);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		System.out.println("CONTACT NAME "+contact.getName());
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
		
	}
	//open setting handler
	@GetMapping("/settings")
	public String openSetting(){
		
		return "normal/settings";
	}
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal) {
		System.out.println("Old Password "+oldPassword);
		System.out.println("New Password "+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
		}
		else {
			
		}
		
		
		return "redirect:/user/index";
	}
}
