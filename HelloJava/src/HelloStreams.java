import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is a collection of code snippets using Java streams.
 */
public class HelloStreams {

	/**
	 * Simple Person useful to demonstrate Java streams.
	 */
	public static class Person {
		public final String name;
		public final int age;
		public final List<String> hobbies;

		public Person(String name, int age, List<String> hobbies) {
			this.name = name;
			this.age = age;
			this.hobbies = hobbies;
		}

		@Override
		public String toString() {
			return "Person [name=" + name + ", age=" + age + ", hobbies=" + hobbies + "]";
		}
	}

	/**
	 * Example data containing several {@link Person}s.
	 */
	public static final List<Person> PERSONS = Arrays.asList(
			new Person("Alice", 51, Arrays.asList("Hiking", "Photography")),
			new Person("Bob", 18, Arrays.asList("Astronomy", "Photography")),
			new Person("Charlie", 25, Arrays.asList("Programming")),
			new Person("Doris", 25, Arrays.asList("Programming", "Karate", "Dancing")),
			new Person("Edward", 51, Arrays.asList("Karate"))
			);
	
	/**
	 * Tuple containing two typed values.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 */
	private static class Pair<T1, T2> {
		public final T1 value1;
		public final T2 value2;

		public Pair(T1 value1, T2 value2) {
			this.value1 = value1;
			this.value2 = value2;
		}

		@Override
		public String toString() {
			return "(" + value1 + ", " + value2 + ")";
		}
	}

	public static void main(String[] args) {
		helloFilter();
		helloCollectToList();
		helloPeek();
		helloCollectToMyList();
		helloCollectToMyListParallel();
		helloCollectToMap1();
		helloCollectToMap2();
		helloCollectToMap3();
		helloCollectToMap4();
		helloMapToCollection();
		helloFlatMap();
		helloFlatMapDistinct();
		helloGroupingBy1();
		helloGroupingBy2();
		helloGroupingBy3();
		helloPartitioningBy();
		helloMapToInt();
		helloMapToIntSum();
		helloCollectSummingInt();
		helloMapReduceSum1();
		helloMapReduceSum2();
		helloMapReduceSum3();
		helloMapCollectAtomicIntSum();
		helloMapCollectBigDecimalArraySum();
		
		helloIntStream();
		helloArrayStream();
		
		helloExampleParallelWithNumberOfThreads();
		helloExampleFindNamesOfPersonsWithSameAge();
		helloExampleFindNameOfPersonWithMostHobbies1();
		helloExampleFindNameOfPersonWithMostHobbies2();
		helloExampleCalculatePi();
	}

	private static void helloFilter() {
		PERSONS.stream()
			.filter(person -> person.age == 25) // filter only matching elements
			.forEach(System.out::println);
	}

	private static void helloCollectToList() {
		List<Person> result = PERSONS.stream()
			.filter(person -> person.age == 25)
			.collect(Collectors.toList());
		System.out.println(result);
	}

	private static void helloPeek() {
		List<Person> result = PERSONS.stream()
			.peek(person -> System.out.println("Before filter: " + person))
			.filter(person -> person.age == 25) // filter only matching elements
			.peek(person -> System.out.println("After filter:  " + person))
			.collect(Collectors.toList());
		System.out.println(result);
	}

	private static void helloCollectToMyList() {
		List<Person> result = PERSONS.stream()
			.filter(person -> person.age == 25)
			.collect(
					() -> new ArrayList<>(), // supplies an empty container to collect into
					(list, person) -> list.add(person), // adds a value to a container
					(list1, list2) -> System.out.println("MERGING")); // only called if parallel() -- correct impl is below!
		System.out.println(result);
	}

	private static void helloCollectToMyListParallel() {
		List<Person> result = PERSONS.stream()
			.parallel()
			.filter(person -> person.age == 25)
			.collect(
					() -> new ArrayList<>(), // supplies an empty container to collect into
					(list, person) -> list.add(person), // adds a value to a container
					(list1, list2) -> list1.addAll(list2)); // merges the second container into the first one
		System.out.println(result);
	}

	private static void helloCollectToMap1() {
		Map<String, Integer> result = PERSONS.stream()
			.collect(Collectors.toMap(
					person -> person.name, // map to key
					person -> person.age)); // map to value
		System.out.println(result);
	}

	private static void helloCollectToMap2() {
		try {
			Map<Integer, String> result = PERSONS.stream()
				.collect(Collectors.toMap(
						person -> person.age, // map to key - throws IllegalStateException if conflicting keys!
						person -> person.name)); // map to value
			System.out.println(result);
		} catch (IllegalStateException ex) {
			// note: the IllegalStateException has wrong message "Duplicate key Charlie" - but 'Charlie' is a value not a key
			ex.printStackTrace();
		}
	}

	private static void helloCollectToMap3() {
		Map<Integer, String> result = PERSONS.stream()
			.collect(Collectors.toMap(
					person -> person.age, // map to key
					person -> person.name, // map to value
					(name1, name2) -> name1 + "/" + name2)); // merge two values into one
		System.out.println(result);
	}

	private static void helloCollectToMap4() {
		Map<Integer, List<String>> result = PERSONS.stream()
			.collect(Collectors.<Person, Integer, List<String>> toMap( // note: type inference seems to fail here - so we provide the generic types
					person -> person.age / 10 * 10, // map age group (10 years) to key
					person -> Arrays.asList(person.name), // map to value
					(list1, list2) -> {
						List<String> merged = new ArrayList<>(list1);
						merged.addAll(list2);
						return merged; // merge two List<String> into a single List<String>
					})); // merge two values into one
		System.out.println(result);
	}

	private static void helloMapToCollection() {
		List<List<String>> result = PERSONS.stream()
			.parallel()
			.filter(person -> person.age == 25)
			.map(person -> person.hobbies) // map to List<String>
			.collect(Collectors.toList()); // collects to List of List of String
		System.out.println(result);
	}

	private static void helloFlatMap() {
		List<String> result = PERSONS.stream()
			.parallel()
			.filter(person -> person.age == 25)
			.flatMap(person -> person.hobbies.stream()) // map to Stream<String> then flatten the streams into one
			.collect(Collectors.toList()); // collects to List of String
		System.out.println(result);
	}

	private static void helloFlatMapDistinct() {
		PERSONS.stream()
			.parallel()
			.filter(person -> person.age == 25)
			.flatMap(person -> person.hobbies.stream()) // map to Stream<String> then flatten the streams into one
			.distinct() // only distinct elements - uses equals()
			.forEach(System.out::println); // collects to List of String
		
		// other interesting methods similar to distinct(): limit(), sorted(), skip()
	}

	private static void helloGroupingBy1() {
		Map<Integer, List<Person>> result = PERSONS.stream()
			.parallel()
			.collect(Collectors.groupingBy(person -> person.age)); // collect to Map - map to key - elements will be collected in a List of elements
		result.entrySet().stream()
			.forEach(System.out::println);
	}

	private static void helloGroupingBy2() {
		PERSONS.stream()
			.parallel()
			.collect(Collectors.groupingBy(person -> person.age)) // collect to Map<Integer, List<Person>>
			.entrySet().stream() // stream over the entries of the Map
			.forEach(System.out::println);
	}

	private static void helloGroupingBy3() {
		PERSONS.stream()
			.parallel()
			.collect(Collectors.groupingBy(
					person -> person.age, // map to key
					() -> new TreeMap<>(), // supply empty Map
					Collectors.toList() // collector for the value
					))
			.entrySet().stream() // stream over the entries of the Map
			.forEach(System.out::println);
	}

	private static void helloPartitioningBy() {
		Map<Boolean, List<Person>> result = PERSONS.stream()
			.parallel()
			.collect(Collectors.partitioningBy(person -> person.age >= 40)); // partition into Map<Boolean, List<Person>>
		System.out.println("Young: " + result.get(false));
		System.out.println("Old:   " + result.get(true));
	}

	private static void helloMapToInt() {
		PERSONS.stream()
			.mapToInt(person -> person.age) // now a stream of primitive int type
			.forEach(System.out::println);
	}

	private static void helloMapToIntSum() {
		int sum = PERSONS.stream()
			.mapToInt(person -> person.age) // now a stream of primitive int type
			.sum();
		System.out.println(sum);
		
		// Similar special int reduction methods: sum(), average(), summaryStatistics()
	}

	private static void helloCollectSummingInt() {
		int sum = PERSONS.stream()
			.collect(Collectors.summingInt(person -> person.age)); // collects to an int sum - map to primitive int
		System.out.println(sum);
	}

	private static void helloMapReduceSum1() {
		// reduce() works with immutable accumulators
		BigDecimal sum = PERSONS.stream()
			.map(person -> BigDecimal.valueOf(person.age)) // map to BigDecimal
			.reduce(
					BigDecimal.ZERO, // the empty accumulator
					(accu, value) -> accu.add(value)); // reduces an accumulator and a value into another accumulator
		System.out.println(sum);
	}

	private static void helloMapReduceSum2() {
		BigDecimal sum = PERSONS.stream()
			.map(person -> person.age) // map to BigDecimal
			.reduce(
					BigDecimal.ZERO, // the empty accumulator 
					(accu, value) -> accu.add(BigDecimal.valueOf(value)),  // reduces an accumulator and a value into another accumulator
					(accu1, accu2) -> accu1.add(accu2)); // merges two accumulators into another accumulator - for parallel()
		System.out.println(sum);
	}

	private static void helloMapReduceSum3() {
		Optional<BigDecimal> sumOptional = PERSONS.stream()
			.map(person -> BigDecimal.valueOf(person.age))
			.reduce((value1, value2) -> value1.add(value2)); // reduces two accumulators into another accumulator 
		BigDecimal sum = sumOptional.orElse(BigDecimal.ZERO);
		System.out.println(sum);
	}

	private static void helloMapCollectAtomicIntSum() {
		// collect() needs a mutable accumulator
		AtomicInteger sum = PERSONS.stream()
			.map(person -> person.age)
			.collect(
					() -> new AtomicInteger(0), // the empty accumulator
					(accu, value) -> accu.addAndGet(value), // adds a value to the accumulator
					(accu, accuValue) -> accu.addAndGet(accuValue.get())); // merges the second accumulator into the first one - for parallel()
		System.out.println(sum.get());
	}

	private static void helloMapCollectBigDecimalArraySum() {
		// look at impl of Collectors.summingInt() - same trick
		BigDecimal sum[] = PERSONS.stream()
			.map(person -> BigDecimal.valueOf(person.age))
			.collect(
					() -> new BigDecimal[] { BigDecimal.ZERO }, // the empty accumulator
					(accu, value) -> accu[0] = accu[0].add(value), // adds a value to the accumulator
					(accu1, accu2) -> accu1[0] = accu1[0].add(accu2[0])); // merges the second accumulator into the first one - for parallel()
		System.out.println(sum[0]);
	}

	private static void helloExampleFindNamesOfPersonsWithSameAge() {
		Map<Integer, List<String>> result = PERSONS.stream()
			.collect(Collectors.groupingBy(person -> person.age)) // group into Map<Integer, List<Person>> with the age as key
			.entrySet().stream() // stream of Entry<Integer, Person>
			.filter(entry -> entry.getValue().size() > 1) // filter entries with multiple persons
			.collect(Collectors.toMap(
					entry -> entry.getKey(), // map age to key
					entry -> entry.getValue().stream() // stream over the Persons
						.map(person -> person.name) // map to name
						.collect(Collectors.toList()))); // collect to List<String> containing the names -> put into values of the Map
		System.out.println(result);
	}
	
	private static void helloIntStream() {
		IntStream.of(2, 4, 6, 8).forEach(System.out::println); // int stream of explicit values
		IntStream.range(0, 10).forEach(System.out::println); // int stream similar to for(int i=0 ; i<10 ; i++) (begin is inclusive, end is exclusive)
		IntStream.rangeClosed(0, 9).forEach(System.out::println); // int stream similar to for(int i=0 ; i<=9 ; i++) (begin is inclusive, end is inclusive)
	}
	
	private static void helloArrayStream() {
		double[] array =  new double[] { 1.1, 2.2, 3.3 };
		Arrays.stream(array).forEach(System.out::println); // stream over double array
		
		// Similar stream() methods for: int[], long[], double[], T[]
	}

	private static void helloExampleParallelWithNumberOfThreads() {
		ForkJoinPool forkJoinPool = new ForkJoinPool(2);
		try {
			List<Person> result = forkJoinPool.submit(() ->
				PERSONS.stream()
					.parallel()
					.filter(person -> person.age == 25)
					.collect(Collectors.toList())
			).get();
			System.out.println(result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}		
	}
	
	private static void helloExampleFindNameOfPersonWithMostHobbies1() {
		Optional<String> result = PERSONS.stream()
			.map(person -> new Pair<String, Integer>(person.name, person.hobbies.size())) // map to Pair<String, Integer> containing name and number of hobbies 
			.sorted((nameAndCount1, nameAndCount2) -> -Integer.compare(nameAndCount1.value2, nameAndCount2.value2)) // sort by number of hobbies (descending)
			.map(nameAndCount -> nameAndCount.value1) // map to name
			.findFirst();
		System.out.println(result.orElseThrow(() -> new RuntimeException("Nobody found")));
		
		// Similar short-circuiting methods: findFirst(), findAny(), allMatch(), anyMatch(), noneMatch()
	}
	
	private static void helloExampleFindNameOfPersonWithMostHobbies2() {
		Optional<Pair<String, Integer>> result = PERSONS.stream()
			.map(person -> new Pair<String, Integer>(person.name, person.hobbies.size())) // map to Pair<String, Integer> containing name and number of hobbies 
			.max((nameAndCount1, nameAndCount2) -> Integer.compare(nameAndCount1.value2, nameAndCount2.value2)); // find the max count
		System.out.println(result.orElseThrow(() -> new RuntimeException("Nobody found")));
		
		// Similar special reduction methods: min(), max()
	}

	private static void helloExampleCalculatePi() {
		int n = 10000000;
		long startMillis = System.currentTimeMillis();
		MathContext mc = new MathContext(100, RoundingMode.HALF_UP);
		BigDecimal value4 = BigDecimal.valueOf(4);
		BigDecimal result = IntStream.range(1, n)
			.parallel()
			.mapToObj(value -> {
				int sign = value % 2 == 0 ? -1 : 1;
				return BigDecimal.valueOf(sign * (value * 2 - 1));
			})
			.reduce(
					BigDecimal.ZERO,
					(accu, value) -> accu.add(value4.divide(value, mc)),
					(accu1, accu2) -> accu1.add(accu2));
		long endMillis = System.currentTimeMillis();
		System.out.println(result);
		System.out.println("in " + (endMillis - startMillis) + " ms");
	}
}
