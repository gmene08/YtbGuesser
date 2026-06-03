package com.gabmene.videoguesser.config;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedDataBase(CategoryRepository categoryRepository){
        return args -> {
            if (categoryRepository.count() ==0){
                System.out.println("Seeding database: inserting categories");

                Category cat1 = new Category(1, MatchCategory.GAMING, "gaming");
                Category cat2 = new Category(2, MatchCategory.SPORTS, "sports");
                Category cat3 = new Category(3, MatchCategory.MUSIC, "music");
                Category cat4 = new Category(4, MatchCategory.VLOG, "vlog");

                categoryRepository.saveAll(java.util.List.of(cat1, cat2, cat3, cat4));

                System.out.println("Database seeded: categories inserted");
            } else {
                System.out.println("Database already seeded");
            }
        };
    }
}
