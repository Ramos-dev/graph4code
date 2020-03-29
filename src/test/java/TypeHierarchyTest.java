import java.util.Collections;
import java.util.List;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.kernel.logging.BufferingLogger;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.graphdb.Result;

import visitors.WiggleVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;


public class TypeHierarchyTest {

    private GraphDatabaseService graphDb;

    @Before
    public void prepareTestDatabase() {
        // create temporary database for each unit test.
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().newGraphDatabase();
    }

    @After
    public void destroyTestDatabase() {
        graphDb.shutdown();
    }

    @Test
    public void testExtends() throws Exception {

        String src = "class A{\n"
                + "static class B extends A{}\n"
                + "static class C extends B{}\n"
                + "}";

        JavacTaskImpl task = utils.TestUtils.getTask(src);

        List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
        task.analyze(); // attribute with symbols?

        CompilationUnitTree u = parse.get(0);

        WiggleVisitor v = new WiggleVisitor(task, graphDb, Collections.singletonMap("projectName", "Test_Extends"));
        v.scan(u, null);

//		ExecutionEngine engine = new ExecutionEngine(graphDb, new BufferingLogger());


        Result result = graphDb.execute("start n=node(*) MATCH m-[r]->n RETURN m,r,n");
//		ExecutionResult result = engine.execute("start n=node(*) MATCH m-[r]->n RETURN m,r,n");
        System.out.println(result.resultAsString());


    }

    @Test
    public void testRuntime() throws Exception {

        String src =
                "public class RunTimeTest {\n" +
                        "    public static void  eval(String cmd) throws Exception{\n" +
                        "            Runtime.getRuntime().exec(cmd);\n" +
                        "\n" +
                        "    }\n" +
                        "}";

        JavacTaskImpl task = utils.TestUtils.getTask(src);

        List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
        task.analyze(); // attribute with symbols?

        CompilationUnitTree u = parse.get(0);

        WiggleVisitor v = new WiggleVisitor(task, graphDb, Collections.singletonMap("projectName", "RunTimeTest"));
        v.scan(u, null);

        //ExecutionEngine engine = new ExecutionEngine(graphDb, new BufferingLogger());


        Result result = graphDb.execute("start n=node(*) MATCH m-[r]->n RETURN m,r,n");
        //ExecutionResult result = engine.execute("start n=node(*) MATCH m-[r]->n RETURN m,r,n");
        System.out.println(result.resultAsString());


    }

    @Test
    public void testMultipleExtendsAndImplements() throws Exception {

        String src = "class A implements Cloneable{\n"
                + "static class B extends A{}\n"
                + "static class C extends B{}\n"
                + "static class D extends Z{}\n"
                + "}";

        String src2 = "import java.io.*;\n"
                + "class Z implements Cloneable, Serializable{\n"
                + "static class Y extends Z{}\n"
                + "}";

        JavacTaskImpl task = utils.TestUtils.getTask(src, src2);

        List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
        task.analyze(); // attribute with symbols?

        CompilationUnitTree u1 = parse.get(0);
        CompilationUnitTree u2 = parse.get(1);

        WiggleVisitor v = new WiggleVisitor(task, graphDb, Collections.singletonMap("projectName", "Test_Extends"));
        v.scan(u1, null);
        v.scan(u2, null);

//		ExecutionEngine engine = new ExecutionEngine(graphDb, new BufferingLogger());
//		ExecutionResult result = engine.execute("start n=node(*) MATCH m-[r]->n RETURN m,r,n");
//		System.out.println(result.dumpToString());


    }

}
