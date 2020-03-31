import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class Neo4jCypherJavaAPI {
    public static void main(String[] args) {
        //指定 Neo4j 存储路径
        File file = new File("/tmp/data/databases/graph2.db");
        //Create a new Object of Graph Database
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        System.out.println("Server is up and Running");

        try(Transaction tx = graphDB.beginTx()){
            //通过Cypher查询获得结果
            StringBuilder sb = new StringBuilder();
            sb.append("MATCH (john)-[:IS_FRIEND_OF]->(USER)-[:HAS_SEEN]->(movie) ");
            sb.append("RETURN movie");
            Result result = graphDB.execute(sb.toString());
            //遍历结果
            while(result.hasNext()){
                //get("movie")和查询语句的return movie相匹配
                Node movie = (Node) result.next().get("movie");
                System.out.println(movie.getId() + " : " + movie.getProperty("name"));
            }

            tx.success();
            System.out.println("Done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDB.shutdown();    //关闭数据库
        }
    }
}
