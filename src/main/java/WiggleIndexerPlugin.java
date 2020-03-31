import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//import org.neo4j.kernel.impl.util.FileUtils;

import tasklisteners.AfterAnalyze;

import com.sun.source.util.JavacTask;


public class WiggleIndexerPlugin implements com.sun.source.util.Plugin {

    private static final String PLUGIN_NAME = "WiggleIndexerPlugin";
    private GraphDatabaseBuilder graphDbBuilder;

    private String wiggleDbPath;
    private String wiggleClearDb;

    @Override
    public void init(JavacTask task, String[] args) {

        createDb();
        Map<String, String> env = System.getenv();
        Map<String, String> cuProps = new HashMap<>();
        for (Map.Entry<String, String> e : env.entrySet()) {
            if (e.getKey().startsWith("WIGGLE_CUPROP_")) {
                cuProps.put(underToCamel(e.getKey().substring("WIGGLE_CUPROP_".length())), e.getValue());
            }
        }
        String projectName = getProjectName();
        System.out.println("Running " + PLUGIN_NAME);
        System.out.println("WIGGLE_PROJECT_NAME:" + projectName);
        System.out.println("WIGGLE_DB_PATH:" + wiggleDbPath);
        System.out.println("WIGGLE_CLEAR_DB:" + wiggleClearDb);

        cuProps.put("projectName", projectName);
        task.setTaskListener(new AfterAnalyze(task, graphDbBuilder, cuProps));
        System.out.println("finished");

    }

    // Turn SOME_CAMEL_IDENT into someCamelIdent.  A trailing underscore is left intact
    private static String underToCamel(String s) {
        String r = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '_' && i + 1 < s.length()) {
                r += Character.toUpperCase(s.charAt(i + 1)); // Upper case following char
                i += 1; // Skip next char now we've dealt with it
            } else {
                r += Character.toLowerCase(s.charAt(i));
            }
        }
        return r;
    }


    private String getProjectName() {
        String projectName = System.getenv("WIGGLE_PROJECT_NAME");
        if (projectName == null)
            projectName = "NO_NAME";
        return projectName;
    }


    private String getDBPath() {
        String dbPath = System.getenv("WIGGLE_DB_PATH");
        if (dbPath == null)
            dbPath = "./neo4j/data/wiggle.db";
        return dbPath;
    }

    private File getDBFile() {
       return new File("./neo4j/data/graph4code.db");
    }


    public void createDb() {

        this.wiggleDbPath = getDBPath();

        this.wiggleClearDb = System.getenv("WIGGLE_CLEAR_DB");
        if (wiggleClearDb != null && wiggleClearDb.equals("y")) {
            clearDb(wiggleDbPath);
        }


        graphDbBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(getDBFile()).
                setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType").
                setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind").
                setConfig(GraphDatabaseSettings.node_auto_indexing, "true").
                setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true");

        //registerShutdownHook( graphDb );

    }

    private void clearDb(String dbPath) {
//        try {
//            FileUtils.deleteRecursively(new File(dbPath));
            System.out.println("delete recursively");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

}
