package tasklisteners;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import visitors.WiggleVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import java.util.Map;

public class AfterAnalyze implements TaskListener {

    private final JavacTask task;
    private final GraphDatabaseBuilder graphDbBuilder;
    private final Map<String, String> cuProps;

    public AfterAnalyze(JavacTask task, GraphDatabaseBuilder graphDbBuilder, Map<String, String> cuProps) {
        this.graphDbBuilder = graphDbBuilder;
        this.task = task;
        this.cuProps = cuProps;
    }

    @Override
    public void finished(TaskEvent arg0) {

        if (arg0.getKind().toString().equals("ANALYZE")) {
            CompilationUnitTree u = arg0.getCompilationUnit();
            GraphDatabaseService graphDb = graphDbBuilder.newGraphDatabase();
            new WiggleVisitor(task, graphDb, cuProps).scan(u, null);
            graphDb.shutdown();
        }
    }

    @Override
    public void started(TaskEvent arg0) {
        // TODO Auto-generated method stub

    }

}
