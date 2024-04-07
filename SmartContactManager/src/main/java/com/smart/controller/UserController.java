package com.smart.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		//get the user using username(Email)
		User user = this.userRepository.getUserByUserName(userName);
		
		System.out.println("User: "+user);
		//sending to view
		m.addAttribute("user",user);
		
	}
	//dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dasboard");
		
		return "normal/user_dashboard";
	}
	
	//Open Add contact form Handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add contact"); 
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//Processing Add-contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
								 @RequestParam("profileImage") MultipartFile file, 
								 Principal principal,
								 HttpSession session) {
		
		try {
		String name=principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		//processing and uploading file
		if(file.isEmpty()) {
			System.out.println("No file found");
			contact.setImage("contact.png");
		}
		else {
			//file the file to folder and update the name in contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		
		
		this.userRepository.save(user);
		
		System.out.println("DATA"+contact);
		
		System.out.println("Added to data base");
		
		//success message
		session.setAttribute("message", new Message("Your contact is added!! Add more", "success"));
		
//		session.removeAttribute("message");
		
		}catch(Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message", new Message("Something went wrong! try again", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contact handler
	//per page ==5[n]
	//current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m,Principal principal) {
		
		m.addAttribute("title","Show Contacts");
		//Need to send contacts list to view
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//1.currentPage-page
		//2.Contact per Page
		Pageable pageable = PageRequest.of(page, 5);
		
		//getting all contacts by user id
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		//sending to view
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	//showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal) {
		
		Optional<Contact> contact_Detail = this.contactRepository.findById(cId);
		Contact contact = contact_Detail.get();
		//Applying security checks 
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		}
	
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model m,Principal principal, HttpSession session) {
		
		Contact contact = this.contactRepository.findById(cId).get();
		System.out.println("Contact "+contact.getcId());
		
		//apply security checks
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			//contact.setUser(null);
			//this.contactRepository.delete(contact);
			//this.contactRepository.deleteById(cId);
			user.getContacts().remove(contact);
			this.userRepository.save(user);
		}

		
		System.out.println("Deleted");
		
		session.setAttribute("message",new Message("Contact Deleted Successfully", "success"));
		
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	//Open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m) {
		
		m.addAttribute("title","update contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//process-update handler
		@PostMapping("/process-update")
		public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
			
			try {
				//old contact detail
				Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
				//image
				if(!file.isEmpty()) {
					//file work
					//rewrite
					//delete old photo
					File deleteFile = new ClassPathResource("static/image").getFile();
					File file1 = new File(deleteFile,oldContactDetail.getImage());
					file1.delete();
					//update new photo
					File saveFile = new ClassPathResource("static/image").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					contact.setImage(file.getOriginalFilename());
					
				}else {
					contact.setImage(oldContactDetail.getImage());
				}
				User user = this.userRepository.getUserByUserName(principal.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
				
				//message to view
				session.setAttribute("message",new Message("Your contact is updated", "success"));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("CONTACT NAME "+contact.getName());
			System.out.println("CONTACT ID " +contact.getcId());
			
			return "redirect:/user/"+contact.getcId()+"/contact";
		}
		
		//you profile handler
		@GetMapping("/profile")
		public String yourProfile(Model m) {
			
			m.addAttribute("title","Profile");
			
			return "normal/profile";
		}
		
		//Setting handler
		@GetMapping("/settings")
		public String settings(Model m) {
			m.addAttribute("title","Setting");
			return "normal/settings";
		}
}
