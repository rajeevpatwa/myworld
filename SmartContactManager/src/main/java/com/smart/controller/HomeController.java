package com.smart.controller;

//import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.SessionHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private SessionHelper sessionHelper;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	//Handler for signup
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register-Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//Handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login-Smart Contact Manager");
		return "signin";
	}
	
	//Handler for do-register -processing
	
	@PostMapping("/do-register")
	@Transactional
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,
								@RequestParam(value="agreement",defaultValue = "false") boolean agreement,
								Model model,HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed to our terms and conditions");
				throw new Exception("You have not agreed to our terms and conditions");
			}
			
			if(result.hasErrors()) {
				System.out.println("ERROR "+result.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement: "+agreement);
			System.out.println("User "+user);
			//saving data in database
			this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message",new com.smart.helper.Message("Registred Successfully","alert-success"));
			//sessionHelper.removeMessageFromSession();

			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new com.smart.helper.Message("Something went wrong !!"+e.getMessage(),"alert-danger"));
			//sessionHelper.removeMessageFromSession();
			return "signup";
		}
		
		
	}
}
