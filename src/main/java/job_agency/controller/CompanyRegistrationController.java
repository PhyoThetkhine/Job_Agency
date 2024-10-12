package job_agency.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import job_agency.emailService.EmailService;
import job_agency.emailService.OtpService;
import job_agency.emailService.OtpStorageService;
import job_agency.model.Company;
import job_agency.service.CompanyService;

@Controller
public class CompanyRegistrationController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpStorageService otpStorageService;

    @Autowired
    private OtpService otpService;

    // Display company registration form
    @RequestMapping("/showcompanyregister")
    public String showRegistrationForm(Model model) {
        model.addAttribute("company", new Company());
        return "company_register"; 
    }

    // Handle company registration
    @PostMapping("/docompanyRegister")
    public ModelAndView registerCompany(@ModelAttribute("company") Company company, HttpSession session) {
        ModelAndView mav = new ModelAndView();
       // System.out.println(company.getCompany_password());

        // Validate email format
//        if (!isValidEmail(company.getCompany_email())) {
//            mav.setViewName("company_register");
//            mav.addObject("EmailInvalid", "Invalid email format.");
//            return mav;
//        }

        // Check if email is already registered
        if (companyService.checkEmail(company.getCompany_email())) {
            mav.setViewName("company_register");
            mav.addObject("EmailExist", "Email is already registered.");
            return mav;
        }

        // Validate phone number format
        if (!isValidPhone(company.getPhone())) {
            mav.setViewName("company_register");
            mav.addObject("PhoneInvalid", "Invalid phone number format.");
            return mav;
        }

        // Check if phone number is already registered
        if (companyService.checkPhone(company.getPhone())) {
            mav.setViewName("company_register");
            mav.addObject("PhoneExist", "This phone number is already registered.");
            return mav;
        }

        // Store company details in session
        session.setAttribute("companyDetails", company);

        // Generate and send OTP
        String companyotp = otpService.generateOtp();
        otpStorageService.storeOtp(company.getCompany_email(), companyotp);
        emailService.sendOtpEmail(company.getCompany_email(), companyotp);
        System.out.println("OTP "+companyotp);

        // Store email for OTP verification
        session.setAttribute("pendingEmail", company.getCompany_email());

        mav.setViewName("verify-companyotp");
        mav.addObject("message", "OTP has been sent to your email.");
        return mav;
    }

    // Handle OTP verification
    @PostMapping("/verify-companyotp")
    public ModelAndView verifyOtp(@RequestParam("companyotp") String companyotp, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String pendingEmail = (String) session.getAttribute("pendingEmail");
        Company company = (Company) session.getAttribute("companyDetails");

        // Check if session data exists
        if (company == null || pendingEmail == null) {
            mav.setViewName("company_register");
            mav.addObject("sessionExpired", "Session expired. Please register again.");
            return mav;
        }

        // Validate OTP
        boolean isValid = otpStorageService.validateOtp(pendingEmail, companyotp);
        if (isValid) {
        	if (company.getCompany_password() != null) {
                companyService.insertCompany(company); // Save the company details in the database
                mav.setViewName("success");// Save the company details in the database
           
        	}
        } else {
            mav.setViewName("verify-companyotp");
            mav.addObject("errorOtp", "Invalid or expired OTP.");
        }

        return mav;
    }

    // Handle OTP resend request
    @PostMapping("/resendcomapnyOtp")
    public ModelAndView resendcomapnyOtp(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String pendingEmail = (String) session.getAttribute("pendingEmail");

        // Check if session data exists
        if (pendingEmail == null) {
            mav.setViewName("company_register");
            mav.addObject("sessionExpired", "Session expired. Please register again.");
            return mav;
        }

        // Generate a new OTP
        String newOtp = otpService.generateOtp();
        otpStorageService.storeOtp(pendingEmail, newOtp);

        // Resend the OTP to the user's email
        emailService.sendOtpEmail(pendingEmail, newOtp);

        mav.setViewName("verify-companyotp");
        mav.addObject("message", "A new OTP has been sent to your email.");
        return mav;
    }

    // Utility method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$";
        return email.matches(emailRegex);
    }

    // Utility method to validate phone number format
    private boolean isValidPhone(String phone) {
        String phoneRegex = "\\d{11}"; // Adjust the regex as needed for your phone number format
        return phone.matches(phoneRegex);
    }
}
