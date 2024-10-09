package job_agency.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import job_agency.model.User;
import job_agency.service.UserService;

@Controller
public class HomeController {
	@Autowired
    private UserService userService;
	
	@RequestMapping("/")
    public String showRegistrationForm() {
        
        return "index"; 
    }
	
	@GetMapping("/Profile")
	public String showProfile(Model model, HttpSession session) {
		if(isLoggedIn(session)){
			 User user = (User)session.getAttribute("loggedUser");
			 userService.findUserByEmail(user.getEmail());
			 model.addAttribute("user", user);
			 return "profile";
		}else {
			 return "login";
			
		}
		
		
	}
	
	private boolean isLoggedIn(HttpSession session) {
        User user = (User)session.getAttribute("loggedUser");
        return user != null;
    }
}
