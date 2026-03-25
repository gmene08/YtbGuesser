package com.gabmene.videoguesser;

import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VideoGuesserApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoGuesserApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(UserService userService) {
		return args -> {
			//createUser(userService);
		};
	}

	public void createUser(UserService userService){
		System.out.println("-- Initial Test: Saving user--");

		User user1 = User.builder()
				.nickname("Gabriel")
				.email("gab@gmail.com")
				.password("1234")
				.isGuest(false)
				.build();

		userService.save(user1);
		System.out.println("-- Initial Test: User saved --");

		System.out.println("-- Initial Test: All users --");
		userService.findAll().forEach(u -> System.out.println("Found: " + u.toString()));

		System.out.println("Test finished");
	}
}
