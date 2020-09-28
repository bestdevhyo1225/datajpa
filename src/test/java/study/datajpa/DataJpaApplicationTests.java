package study.datajpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/*
 * 실행할 때, Task Worker 관련 로그가 뜨는데 Gradle에 의해서 실행 되기 때문에 옵션을 변경해야한다.
 * Preference -> Build, Execution, Deployment -> Gradle로 이동한다.
 * Build and run using : Gradle -> IntelliJ IDEA 변경
 * Run tests using : Gradle -> IntelliJ IDEA 변경
 */
@SpringBootTest
class DataJpaApplicationTests {

	@Test
	void contextLoads() {
	}

}
