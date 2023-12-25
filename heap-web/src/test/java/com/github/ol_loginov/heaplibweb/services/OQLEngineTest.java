package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.TestTool;
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest;
import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepository;
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader;
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.lib.profiler.heap.FastHprofHeap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import javax.script.ScriptEngineManager;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author Jaroslav Bachorik
 */
@ContextConfiguration(classes = {InputLoaderBTest.TestContext.class})
@Disabled("manual run")
@Rollback(false)
@Slf4j
public class OQLEngineTest extends DatabaseTest {
	static {
		if (null == new ScriptEngineManager().getEngineByName("JavaScript")) {
			throw new IllegalStateException("JavaScript script engine is not available");
		}
	}

	private OQLEngine instance;
	@TempDir
	private Path tempDir;

	@Inject
	private HeapFileRepository heapFileRepository;
	@Inject
	private HeapRepository heapRepository;
	@Inject
	private HeapRepositories heapRepositories;
	@Inject
	private InputFilesManager inputFilesManager;
	@Inject
	private ObjectProvider<InputLoader> inputLoaderProvider;

	private Instant startTime;

	@BeforeEach
	public void startTIme() {
		startTime = Instant.now();
	}

	@AfterEach
	public void endTime() {
		log.info("test complete in {} secs", Duration.between(startTime, Instant.now()).toSeconds());
	}

	@Test
	public void loadData() {
		var inputFileName = "1703107559883.hprof";
		var inputFileDump = tempDir.resolve(inputFileName);
		TestTool.copyResourceTo("heapdumps/remote-jdbc-1703107559883.sanitized.hprof", inputFileDump);
		when(inputFilesManager.getInputFile(inputFileName)).thenReturn(inputFileDump);

		var heapFile = new HeapFile();
		heapFile.setRelativePath(inputFileName);
		heapFileRepository.save(heapFile);

		var work = inputLoaderProvider.getObject();
		work.withEntityId(heapFile.getId());
		work.run();
	}

	@Test
	public void testSuite() throws Exception {
		var heap = entityManager.createQuery("from HeapEntity order by id desc", HeapEntity.class)
			.setMaxResults(1)
			.getSingleResult();
		var heapProxy = new HeapProxy(heap, heapRepositories);
		instance = new OQLEngine(heapProxy);

		log.info("testAltTypeNames");
		testAltTypeNames();
		log.info("testIntResult");
		testIntResult();
		log.info("testClassFields");
		testClassFields();
		log.info("testObjectClass");
		testObjectClass();
		log.info("testHeapForEachClass");
		testHeapForEachClass();
		log.info("testHeapForEachObject");
		testHeapForEachObject();
		log.info("testHeapFindObject");
		testHeapFindObject();
		log.info("testHeapRoots");
		testHeapRoots();
		log.info("testHeapClasses");
		testHeapClasses();
		log.info("testHeapFinalizables");
		testHeapFinalizables();
		log.info("testHeapLivePaths");
		testHeapLivePaths();
		log.info("testHeapObjects");
		testHeapObjects();
		log.info("testSubclasses");
		testSubclasses();
		log.info("testSuperclasses");
		testSuperclasses();
		log.info("testForEachReferrer");
		testForEachReferrer();
		log.info("testForEachReferee");
		testForEachReferee();
		log.info("testReferrersInstance");
		testReferrersInstance();
		log.info("testRefereesInstance");
		testRefereesInstance();
		testRefereesClass();
		testRefers();
		testReachables();
		testInstanceOf();
		testSizeOf();
		testRoot();
		testContains();
		testMap();
		testFilter();
		testSort();
		testLength();
		testCountNoClosure();
		testCount();
		testMultivalue();
		testComplexStatement1();
		testComplexStatement2();
		testMapWrapping();
		testUnwrapIterator();
		testUnwrapIteratorComplex();
		testTop();
	}

	protected void testAltTypeNames() throws Exception {
		instance.executeQuery("select a from [I a", null);
		instance.executeQuery("select a from [B a", null);
		instance.executeQuery("select a from [C a", null);
		instance.executeQuery("select a from [S a", null);
		instance.executeQuery("select a from [J a", null);
		instance.executeQuery("select a from [F a", null);
		instance.executeQuery("select a from [Z a", null);

		instance.executeQuery("select a from [java.lang.String a", null);
	}

	protected void testIntResult() throws Exception {
		AtomicBoolean tmp = new AtomicBoolean(true);
		instance.executeQuery("select a.count from java.lang.String a", o -> {
			if (!(o instanceof Integer)) {
				tmp.set(false);
				return true;
			}
			return false;
		});
		assertThat(tmp).isTrue();
	}

	protected void testClassFields() throws Exception {
		System.out.println("test class fields");

		final String[] values = new String[]{"", "prefixLength = int"};

		instance.executeQuery("select map(heap.findClass(\"java.io.File\").fields, 'toHtml(it.name) + \" = \" + toHtml(it.signature)')", o -> {
			values[0] = o.toString();
			return true;
		});

		assertThat(values[1]).isEqualTo(values[0]);
	}

	protected void testObjectClass() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("test object class accessor");

		instance.executeQuery("select map(a.clazz.statics, 'toHtml(it)') from java.lang.String a", o -> true);
	}

	protected void testHeapForEachClass() throws Exception {
		System.out.println("heap.forEachClass");
		String query = "select heap.forEachClass(function(xxx) { print(xxx.name); print(\"\\n\");})";

		instance.executeQuery(query, null);
	}

	protected void testHeapForEachObject() throws Exception {
		System.out.println("heap.forEachObject");
		String query = "select heap.forEachObject(function(xxx) { print(xxx.id); print(\"\\n\");}, \"java.io.File\")";

		instance.executeQuery(query, null);
	}

	protected void testHeapFindObject() throws Exception {
		System.out.println("heap.findObject");
		final int[] counter = new int[1];
		String query = "select heap.findObject(1684166976)";

		instance.executeQuery(query, o -> {
			counter[0]++;
			return true;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testHeapRoots() throws Exception {
		System.out.println("heap.roots");
		final int[] counter = new int[1];

		String query = "select heap.roots";

		instance.executeQuery(query, o -> {
			counter[0]++;
			return true;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testHeapClasses() throws Exception {
		System.out.println("heap.classes");
		final int[] counter = new int[1];

		String query = "select heap.classes";

		instance.executeQuery(query, o -> {
			counter[0]++;
			return true;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testHeapFinalizables() throws Exception {
		System.out.println("heap.finalizables");
		final int[] counter = new int[1];

		String query = "select heap.finalizables";

		instance.executeQuery(query, o -> {
			counter[0]++;
			return true;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testHeapLivePaths() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("heap.livepaths");
		final int[] counter = new int[1];

		String query = "select heap.livepaths(s) from java.lang.String s";

		instance.executeQuery(query, o -> {
			if (o != null) {
				counter[0]++;
				return true;
			} else {
				return false;
			}
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testHeapObjects() throws Exception {
		System.out.println("heap.objects");

		final int[] count = new int[]{0, 0};

		instance.executeQuery("select heap.objects(\"java.io.InputStream\", true)", o -> {
			count[0]++;
			return false;
		});
		instance.executeQuery("select heap.objects(\"java.io.InputStream\", false)", o -> {
			count[1]++;
			return false;
		});

		assertThat(count[0]).isNotSameAs(count[1]);
		assertThat(count[0]).isEqualTo(4);
		assertThat(count[1]).isEqualTo(0);
	}

	protected void testSubclasses() throws Exception {
		System.out.println("subclasses");
		final int[] counter = new int[1];

		String query = "select heap.findClass(\"java.io.InputStream\").subclasses()";

		instance.executeQuery(query, o -> {
			System.out.println(((JavaClass) o).getName());
			counter[0]++;
			return false;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testSuperclasses() throws Exception {
		System.out.println("superclasses");
		final int[] counter = new int[1];

		String query = "select heap.findClass(\"java.io.DataInputStream\").superclasses()";

		instance.executeQuery(query, o -> {
			System.out.println(((JavaClass) o).getName());
			counter[0]++;
			return true;
		});
		assertThat(counter[0]).isGreaterThan(0);
	}

	protected void testForEachReferrer() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("forEachReferrer");

		String query = "select forEachReferrer(function(xxx) { print(\"referrer: \" + xxx.id); print(\"\\n\");}, heap.findObject(1684166976))";

		instance.executeQuery(query, null);
	}

	protected void testForEachReferee() throws Exception {
		System.out.println("forEachReferee");

		String query = "select forEachReferee(function(xxx) { print(\"referee: \" + xxx.id); print(\"\\n\");}, heap.findObject(1684166976))";

		instance.executeQuery(query, null);
	}

	protected void testReferrersInstance() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("referrers-instance");

		String query = "select referrers(heap.findObject(1684166976))";
		long[] referrersTest = new long[]{1684166952};
		final List<Long> referrers = new ArrayList<>();

		instance.executeQuery(query, o -> {
			referrers.add(((Instance) o).getInstanceId());
			return false;
		});


		assertThat(referrersTest).hasSize(referrers.size());
		for (long referee : referrersTest) {
			if (!referrers.contains(referee)) fail();
		}
	}

	protected void testRefereesInstance() throws Exception {
		System.out.println("referees-instance");

		String query = "select referees(heap.findObject(1684166976))";
		long[] refereesTest = new long[]{1684166992};
		final List<Long> referees = new ArrayList<>();

		instance.executeQuery(query, o -> {
			referees.add(((Instance) o).getInstanceId());
			return false;
		});

		assertThat(refereesTest).hasSize(referees.size());
		for (long referee : refereesTest) {
			if (!referees.contains(referee)) fail();
		}
	}

	protected void testRefereesClass() throws Exception {
		System.out.println("referees-class");

		String query = "select referees(heap.findClass(\"java.io.File\"))";
		long[] refereesTest = new long[]{1684106928, 1684106888, 1684106848, 1684106408};
		final List<Long> referees = new ArrayList<>();

		instance.executeQuery(query, o -> {
			referees.add(((Instance) o).getInstanceId());
			return false;
		});

		assertThat(refereesTest).hasSize(referees.size());
		for (long referee : refereesTest) {
			if (!referees.contains(referee)) fail();
		}
	}

	protected void testRefers() throws Exception {
		System.out.println("refers");

		String query = "select refers(heap.findObject(1684166976), heap.findObject(1684166992))";

		final boolean[] result = new boolean[1];

		instance.executeQuery(query, o -> {
			result[0] = (Boolean) o;
			return true;
		});

		assertThat(result[0]).isTrue();

		query = "select refers(heap.findObject(1684166992), heap.findObject(1684166976))";
		instance.executeQuery(query, o -> {
			result[0] = (Boolean) o;
			return true;
		});
		assertThat(result[0]).isFalse();
	}


	protected void testReachables() throws Exception {
		System.out.println("reachables");
		final int[] count = new int[1];

		String query = "select reachables(p) from java.util.Properties p";

		instance.executeQuery(query, o -> {
			count[0]++;
			return false;
		});
		assertThat(count[0]).isEqualTo(352);
	}


	protected void testInstanceOf() throws Exception {
		System.out.println("instanceof");

		String query = "select classof(cl).name from instanceof java.lang.ClassLoader cl";
		final int[] counter = new int[1];

		instance.executeQuery(query, o -> {
			System.out.println(o);
			counter[0]++;
			return false;
		});
		assertThat(counter[0]).isEqualTo(2); // although there is 8 subclasses of ClassLoader only 2 of them have instances
	}


	protected void testSizeOf() throws Exception {
		System.out.println("sizeof");
		final int[] counter = new int[1];

		instance.executeQuery("select sizeof(o) from [I o", o -> {
			if (o instanceof Number) counter[0]++;
			return false;
		});

		assertThat(counter[0]).isGreaterThan(0);
	}


	protected void testRoot() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("root");

		final int[] count = new int[1];

		instance.executeQuery("select root(heap.findObject(1684166976))", o -> {
			count[0]++;
			return false;
		});

		assertThat(count[0]).isGreaterThan(0);
	}


	protected void testContains() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("contains");

		final int[] count = new int[1];

		instance.executeQuery("select s from java.lang.String s where contains(referrers(s), \"classof(it).name == 'java.lang.Class'\")", o -> {
			count[0]++;
			return false;
		});

		assertThat(count[0]).isGreaterThan(0);
	}


	protected void testMap() throws Exception {
		assertThat(instance.getHeap()).isNotInstanceOf(FastHprofHeap.class);

		System.out.println("map");

		final String[] output = new String[]{"", "$assertionsDisabled=true\nserialVersionUID=301077366599181570\ntmpdir=null\ncounter=-1\ntmpFileLock=<a href='file://instance/1684106928' name='1684106928'>java.lang.Object#6</a>\npathSeparator=<a href='file://instance/1684106888' name='1684106888'>java.lang.String#101</a>\npathSeparatorChar=:\nseparator=<a href='file://instance/1684106848' name='1684106848'>java.lang.String#100</a>\nseparatorChar=/\nfs=<a href='file://instance/1684106408' name='1684106408'>java.io.UnixFileSystem#1</a>\n<classLoader>=null\n"};

		instance.executeQuery("select map(heap.findClass(\"java.io.File\").statics, \"index + '=' + toHtml(it)\")", o -> {
			output[0] += o.toString() + "\n";
			return false;
		});
		assertThat(output[1]).isEqualTo(output[0]);
	}


	protected void testFilter() throws Exception {
		System.out.println("filter");

		final int[] size = new int[]{0};
		final boolean[] sorted = new boolean[]{true};


		instance.executeQuery("select map(sort(filter(heap.objects('[C'), 'it.length > 0'), 'sizeof(lhs) - sizeof(rhs)'), \"sizeof(it)\")", o -> {
			int aSize = ((Number) o).intValue();
			if (aSize < size[0]) {
				sorted[0] = false;
				return true;
			}
			size[0] = aSize;
			return false;
		});

		assertThat(sorted[0]).isTrue();
	}


	protected void testSort() throws Exception {
		System.out.println("sort");

		final int[] size = new int[]{0};
		final boolean[] sorted = new boolean[]{true};


		instance.executeQuery("select map(sort(heap.objects('[C'), 'sizeof(lhs) - sizeof(rhs)'), \"sizeof(it)\")", o -> {
			int aSize = ((Number) o).intValue();
			if (aSize < size[0]) {
				sorted[0] = false;
				return true;
			}
			size[0] = aSize;
			return false;
		});

		assertThat(sorted[0]).isTrue();
	}


	protected void testLength() throws Exception {
		System.out.println("length");

		final Class<?>[] rsltClass = new Class<?>[1];
//        final boolean sorted[] = new boolean[] {true};


		instance.executeQuery("select length(a.value) from java.lang.String a", o -> {
			rsltClass[0] = o.getClass();
			return true;
		});

		assertThat(rsltClass[0]).isAssignableTo(Number.class);
	}


	protected void testCountNoClosure() throws Exception {
		System.out.println("count - no closure");

		final Class<?>[] rsltClass = new Class<?>[1];
//        final boolean sorted[] = new boolean[] {true};


		instance.executeQuery("select count(a.value) from java.lang.String a", o -> {
			rsltClass[0] = o.getClass();
			return true;
		});

		assertThat(rsltClass[0]).isAssignableTo(Number.class);
	}


	protected void testCount() throws Exception {
		System.out.println("count");

		final Class<?>[] rsltClass = new Class<?>[1];
//        final boolean sorted[] = new boolean[] {true};


		instance.executeQuery("select count(a.value, 'true') from java.lang.String a", o -> {
			rsltClass[0] = o.getClass();
			return true;
		});

		assertThat(rsltClass[0]).isInstanceOf(Double.class);
	}


	protected void testMultivalue() throws Exception {
		System.out.println("multi-value");

		final Class<?>[] rsltClass = new Class<?>[1];
//        final boolean sorted[] = new boolean[] {true};


		instance.executeQuery("select { name: t.name? t.name.toString() : \"null\", thread: t }  from instanceof java.lang.Thread t", o -> {
			rsltClass[0] = o.getClass();
			return true;
		});

		assertThat(rsltClass[0]).isAssignableTo(Map.class);
	}


	protected void testComplexStatement1() throws Exception {
		System.out.println("complex statement 1");

		var res = new AtomicReference<String>();

		instance.executeQuery(
			"select map(filter(heap.findClass('java.lang.System').statics.props.table, 'it != null && it.key != null && it.value != null'),  " +
				"function (it) { " +
				"return 'MapEntry{' + it.key.toString() + ' = ' + it.value.toString() + '}' ;" +
				"}" +
				")", o -> {
				System.out.println(o);
				res.set(o.toString());
				return true;
			});

		assertThat(res.get()).isEqualTo("MapEntry{sun.cpu.isalist = }");
	}


	protected void testComplexStatement2() throws Exception {
		System.out.println("complex statement 2");

		var tmp = new AtomicReference<String>();

		instance.executeQuery(
			"select map(filter(heap.findClass('java.lang.System').statics.props.table, 'it != null && it.key != null && it.value != null'), " +
				"'{ key: it.key.toString(), value: it.value.toString() }')", o -> {
				System.out.println(o);
				tmp.set(o.toString());
				return true;
			});
		assertThat(tmp.get()).isEqualTo("{value=, key=sun.cpu.isalist}");
	}


	protected void testMapWrapping() throws Exception {
		System.out.println("map wrapping");

		final String[] result = new String[]{"", "<a href='file://class/1746081976' name='1746081976'>class java.util.HashMap$Entry[]</a>"};

		instance.executeQuery("select unique(map(filter(reachables(a), 'it != null'), 'toHtml(it.clazz)')) from instanceof java.util.HashMap a", o -> {
			result[0] = o.toString();
			return true;
		});

		assertThat(result[1]).isEqualTo(result[0]);
	}


	protected void testUnwrapIterator() throws Exception {
		System.out.println("unwrap iterator");

		instance.executeQuery("select map(filter(a.table, 'it != null'), 'reachables(it)') from instanceof java.util.HashMap a", o -> {
			System.out.println(o);
			return true;
		});
	}


	protected void testUnwrapIteratorComplex() throws Exception {
		System.out.println("unwrap iterator complex");

		instance.executeQuery("select map(map(filter(a.table, 'it != null'), 'reachables(it)'), 'it.clazz.statics') from instanceof java.util.HashMap a", o -> {
			System.out.println(o);
			return true;
		});
	}


	protected void testTop() throws Exception {
		System.out.println("top 5");

		instance.executeQuery("select top(heap.objects('java.lang.String', false, '(2 * it.offset) + (2 * (it.value.length - (1*it.count + 1*it.offset))) > 0'), '((2 * rhs.offset) + (2 * (rhs.value.length - (1*rhs.count + 1*rhs.offset)))) - ((2 * lhs.offset) + (2 * (lhs.value.length - (1*lhs.count + 1*lhs.offset))))')", o -> {
			System.out.println(o);
			return false;
		});
	}
}
