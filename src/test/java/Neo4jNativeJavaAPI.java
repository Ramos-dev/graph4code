import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;

public class Neo4jNativeJavaAPI {
    private static void registerShutdownHook(final GraphDatabaseService graphDB) {
        // Registers a shutdown hook for the Neo4j instance so that it shuts down nicely
        // when the VM exits (even if you "Ctrl-C" the running example before it's completed)
        /*为了确保neo4j数据库的正确关闭，我们可以添加一个关闭钩子方法 registerShutdownHook。
         *这个方法的意思就是在jvm中增加一个关闭的钩子，
         *当jvm关闭的时候，会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，
         *当系统执行完这些钩子后，jvm才会关闭。
         *所以这些钩子可以在jvm关闭的时候进行内存清理、对象销毁等操作。*/
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        //Shutdown the Database
                        System.out.println("Server is shutting down");
                        try {
                            Thread.sleep(10000000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // graphDB.shutdown();
                    }
                }
        );
    }

    public static void main(String[] args) {
        //指定 Neo4j 存储路径
        File file = new File("/tmp/data/databases/graph4.db");
        //Create a new Object of Graph Database
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );

        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(file).setConfig( bolt.type, "BOLT" )
                .setConfig( bolt.enabled, "true" )
                .setConfig( bolt.address, "localhost:7688" )
                .newGraphDatabase();;
        System.out.println("Server is up and Running");
        try(Transaction tx = graphDB.beginTx()){
            /**
             * 新增User节点
             * 添加Lable以区分节点类型
             * 每个节点设置name属性
             */
            Node user1 = graphDB.createNode(MyLabels.USERS);
            user1.setProperty("name", "John Johnson");

            Node user2 = graphDB.createNode(MyLabels.USERS);
            user2.setProperty("name", "Kate Smith");

            Node user3 = graphDB.createNode(MyLabels.USERS);
            user3.setProperty("name", "Jack Jeffries");
            /**
             * 为user1添加Friend关系
             * 注：Neo4j的关系是有向的箭头，正常来讲Friend关系应该是双向的，
             * 此处为了简单起见，将关系定义成单向的，不会影响后期的查询
             */
            user1.createRelationshipTo(user2,MyRelationshipTypes.IS_FRIEND_OF);
            user1.createRelationshipTo(user3,MyRelationshipTypes.IS_FRIEND_OF);
            /**
             * 新增Movie节点
             * 添加Lable以区分节点类型
             * 每个节点设置name属性
             */
            Node movie1 = graphDB.createNode(MyLabels.MOVIES);
            movie1.setProperty("name", "Fargo");

            Node movie2 = graphDB.createNode(MyLabels.MOVIES);
            movie2.setProperty("name", "Alien");

            Node movie3 = graphDB.createNode(MyLabels.MOVIES);
            movie3.setProperty("name", "Heat");
            /**
             * 为User节点和Movie节点之间添加HAS_SEEN关系, HAS_SEEN关系设置stars属性
             */
            Relationship relationship1 = user1.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            relationship1.setProperty("stars", 5);
            Relationship relationship2 = user2.createRelationshipTo(movie3, MyRelationshipTypes.HAS_SEEN);
            relationship2.setProperty("stars", 3);
            Relationship relationship6 = user2.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);
            relationship6.setProperty("stars", 6);
            Relationship relationship3 = user3.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            relationship3.setProperty("stars", 4);
            Relationship relationship4 = user3.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);
            relationship4.setProperty("stars", 5);

            tx.success();
            System.out.println("Done successfully");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //graphDB.shutdown();    //关闭数据库
        }

        //Register a Shutdown Hook
        registerShutdownHook(graphDB);
    }
}

/**
 * Label类型枚举类
 */
enum MyLabels implements Label {
    MOVIES, USERS
}

/**
 * 关系类型枚举类
 */
enum MyRelationshipTypes implements RelationshipType {
    IS_FRIEND_OF, HAS_SEEN
}