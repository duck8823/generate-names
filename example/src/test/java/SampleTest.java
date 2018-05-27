import com.github.duck8823.GenerateNames;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@GenerateNames(findMethods = true, findSuperclass = false)
class SampleTest {

	@ParameterizedTest
	@MethodSource(SampleTestNames.NAMES)
	void test(String arg) {
		assertEquals(arg, "a");
	}

	static Stream<String> names() {
		return Stream.of("a", "b");
	}
}
