
package com.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=TaskFlowTestSecretKeyForJwtAuthentication123456789"
})
class TaskflowApplicationTests {

	@Test
	void contextLoads() {
	}
}
