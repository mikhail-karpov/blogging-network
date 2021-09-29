package com.mikhailkarpov.users;

import com.mikhailkarpov.users.config.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootTest
@EnableCaching
class UserServiceApplicationIT extends AbstractIT {

	@Test
	void contextLoads() {
	}

}
