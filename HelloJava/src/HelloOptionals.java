import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public class HelloOptionals {

	public static void main(String[] args) {
		helloOptionalConstructors();

		// basic access to value in Optional
		helloOptionalIsPresentGet();
		helloOptionalEmptyGetFails();
		helloOptionalOrElse();
		helloOptionalOrElseGet();
		helloOptionalOrElseThrow();
		
		// using Optionals similar to Stream
		helloOptionalMap();
		helloOptionalFlatMap();
		helloOptionalFilter();
		
		// examples for Optionals in Stream API
		helloExampleOptionalStreamFindAny();
		helloExampleOptionalStreamMin();
		
		// javafx Dialog also returns Optional
	}

	private static void helloOptionalConstructors() {
		Optional<String> noString = Optional.empty();
		Optional<String> helloString = Optional.of("Hello");
		
		Optional<Object> helloString2 = Optional.ofNullable("Hello");
		Optional<Object> noString2 = Optional.ofNullable(null);
		
		System.out.println(noString);
		System.out.println(helloString);
		System.out.println(noString2);
		System.out.println(helloString2);
	}
	
	private static void helloOptionalIsPresentGet() {
		Optional<String> optionalString = Optional.of("Hello");
		
		if (optionalString.isPresent()) {
			System.out.println(optionalString.get());
		}
	}
	
	private static void helloOptionalEmptyGetFails() {
		Optional<String> optionalString = Optional.empty();
	
		try {
			System.out.println(optionalString.get());
		} catch (NoSuchElementException ex) {
			System.out.println("Failed with " + ex);
		}
	}
	
	private static void helloOptionalOrElse() {
		Optional<String> optionalString = Optional.empty();

		System.out.println(optionalString.orElse("<Undefined>"));
	}
	
	private static void helloOptionalOrElseGet() {
		Optional<String> optionalString = Optional.empty();

		System.out.println(optionalString.orElseGet(() -> "<Undefined " + System.currentTimeMillis() + ">"));
	}

	private static void helloOptionalOrElseThrow() {
		Optional<String> optionalString = Optional.empty();
	
		try {
			System.out.println(optionalString.orElseThrow(() -> new RuntimeException("<undef>")));
		} catch (RuntimeException ex) {
			System.out.println("Failed with " + ex);
		}
	}

	private static void helloOptionalMap() {
		Optional<String> optionalString = Optional.of("hello");
		
		Optional<Integer> optionalLength = optionalString.map(str -> str.length());
		System.out.println(optionalLength);
	}

	private static void helloOptionalFlatMap() {
		Optional<String> optionalString = Optional.of("hello");
		
		Optional<Integer> optionalLength = optionalString.<Integer> flatMap(str -> Optional.of(str.length()));
		System.out.println(optionalLength);
	}

	private static void helloOptionalFilter() {
		Optional<String> optionalString = Optional.of("hello");
		
		Optional<String> optionalStringIfContainsE = optionalString.filter(str -> str.contains("e"));
		System.out.println(optionalStringIfContainsE);
	}

	private static void helloExampleOptionalStreamFindAny() {
		Optional<String> optionalString = Stream.of("hello", "world").findAny();
		
		System.out.println(optionalString.orElse("<Undefined>"));

		// similar Stream functions: findFirst(), findAny()
	}
	
	private static void helloExampleOptionalStreamMin() {
		Optional<String> optionalString = Stream.of("hello", "world").min((str1, str2) -> str1.compareTo(str2));
		
		System.out.println(optionalString.orElse("<Undefined>"));
		
		// similar Stream functions: min(), max()
	}
	
}
