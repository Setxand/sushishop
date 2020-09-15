package config;

import com.sushishop.model.User;
import com.sushishop.repository.UserRepository;
import com.sushishop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AdminConfig {

	@Autowired
	UserRepository userRepo;

	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@Value("admin.email")
	private String adminEmail;

	@Value("admin.password")
	private String adminPassword;

	@PostConstruct
	public void setUp() {
		User user = new User();
		user.setEmail(adminEmail);
		user.setPassword(passwordEncoder.encode(adminPassword));
		user.setRole(User.UserRole.ROLE_ADMIN);
		userRepo.findByEmail(user.getEmail()).orElseGet(() -> userRepo.saveAndFlush(user));
	}

}
