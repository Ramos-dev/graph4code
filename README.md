[toc]
# 图数据库和图计算介绍
​	图作为一种表示和分析大数据的有效方法，已成为社交网络、推荐系统、网络安全、文本检索和生物医疗等领域至关重要的数据分析和挖掘工具。
![DTCC 2019 | 百度安全开源图数据库HugeGraph 引领国产图数据库发展_存储](https://tva1.sinaimg.cn/large/00831rSTly1gdb24iuybgj30o00c9jsm.jpg)
​	图数据库（Graph Database）是一种支持属性图模型，用于处理高度连接数据查询与存储的实时可靠的数据库。传统关系型数据库对于超过3度的查询十分低效难以胜任，但图数据库可轻松应对社交网络的各种复杂存储和查询场景。
​	图计算系统面向的场景主要是全图分析类的任务。图数据库是类似于关系型数据的用意重点以事务为核心，一个查询往往只是涉及到图中的少量数据；而图计算与是解决大规模图数据处理的方法。往往如字节跳动、百度、阿里这样的大公司在数据量逐步庞大的的情况下会将图算法跑在分布式的计算系统上。
## 痛点和机会
​	**所谓”一图胜千言“，通过图模型可以清晰表达代码的调用逻辑。**软件开发中和周围的许多可视化都是图可视化，从类图和其他（UML）图到项目之间和项目内部的依赖关系跟踪到体系结构分析。目前鲜有从代码还原为架构视图的工具，本文提供的技术为此提供了可能性。
​	**计算驱动效率，目前的软件静态分析工具性能瓶颈严重。**sonar工具动辄数亿行的数据库内容极大影响查询效率，各项SAST工具都不支持分布式扫描，又受限于机器引擎性能和license难以扩展。静态分析工具如fortify、cobra、findbugs仅支持全量仓库扫描，不能快速实施增量文件扫描。图数据库平台支持可以仅在代码仓库保存图模型基于”关系”的变化，不保留实际的代码文件，读多写少海量存储，拥有无限的扩展性和安全性。
​	**没有比“图”更适合安全运营的工具。**目前的安全报告、漏洞报告是基于专业安全人员编制，不客气地说对于非安全人士可读性较差。图分析结果可以直观表示函数调用流程，极大地提升软件修复和沟通效率，降低漏洞对外暴露周期。
​	**图数据贴合分析第三方软件引入的流程。**图模型具有强大的表现力对于快速更新的事物有很强的适应性，目前已经在工业领域用来管理快速变化的库存、供应链关系，使用优化的模型完全可以实现对每次研发迭代中的组件漏洞管理。
​	**使用大数据是软件安全行业大势所趋。**各种污点分析工作都在重复做request调用service再调用dao类的检测，说白了就是干了本来图数据最擅长的查询几度关系，相比数据流分析图计算后者不需要考虑IO和内存问题导致的性能爆炸。使用各种新技术为行业引入深度学习，机器学习、大数据、人工智能带来了新血液。
## 基于图数据库的软件漏洞解析方法
1. 根据代码调用过程生成调用关系将代码保存为抽象语法树
2. 对语法树进行数据流打标
3. 使用图数据库进行进一步的解析，从中提炼出带有标记的有向图结构作为软件代码结构模型。
4. 使用图分析进行查找软件漏洞
 ![img](https://tva1.sinaimg.cn/large/00831rSTly1gd9xma7dlvg30sg0ncdhd.gif)
   上述是一项软件数据流的模型。
## 详细介绍
​	图结构是节点和边的集合，每个节点代表一个实体，每条边代表实体之间存在的关联关系，一般是三元组<起始节点，关系类型，终止节点>，节点和边都可以有属性，每个属性是kev-value对，可以对实体或者关系进行描述。
![图数据库Neo4j 入门、基本原理及使用场景_网易订阅](https://tva1.sinaimg.cn/large/00831rSTly1gdaxtqeanlj31b70s2jt2.jpg)
​	就像我们在使用 SQL 数据库时需要设计表结构一样，软件分析中把图的数据抽象为有向属性图更加简单，我们就从代码层面介绍下构图过程和点边的数据类型。
​	java抽象语法树的生成的节点可划分为，class\interface\method\file,节点具有属性和标签，比如class，类的属性包括了name、package、access、implement。边是节点之间的关系，比如return，就是method和class或interface的关系，即代码逻辑中：某个方法“return”了某个类或者接口。
​	使用javac编译java文件，将语法树的对象放入node中，下图示例就是将java的import -导入类这个语法赋值给节点，并把这个import节点关联到所import的具体类实现节点中。
```java
@Override
public Void visitImport(ImportTree importTree, Pair<Tree, RelationTypes> t) {

    Node importNode = createSkeletonNode(importTree);
    importNode.setProperty("qualifiedIdentifier", importTree.getQualifiedIdentifier().toString());
    importNode.setProperty("isStatic", importTree.isStatic());

    connectWithParent(importNode, t.getFirst(), RelationTypes.IMPORTS);

    return null;
}
```
​	以简单的helloworld为例，我们可以列出如下的节点
```java
public class HelloWordTest {
    public static void main(String[] args) {
        System.out.println("你好，安全乐观主义");
    }
}
```
![image-20200329165518264](https://tva1.sinaimg.cn/large/00831rSTly1gdawg88we1j31my0ka11i.jpg)
​	将各个节点导入node4j这个图数据。使用Cypher查询语句，分析出边的关系。
```cypher
start 起始节点=node(*) MATCH (终止节点)-[关系]->(起始节点) RETURN 起始节点,关系,终止节点")
```
![image-20200329173236514](https://tva1.sinaimg.cn/large/00831rSTly1gdaxj0ifc5j30sa0pagpr.jpg)

​	通过两张图结合node节点，我们从下往上看看到id为24的String节点”你好，安全乐观主义“是id为23的节点java.lang.System的参数，关联到id为19的关系，即10节点的main方法call调用了id为20的java.io.PrintStream：println方法。
# show me the code
## 分析命令执行漏洞
​	快餐时代大家都是大忙人，直接上核心代码流程：
```java
@Test
public void testRuntime() throws Exception {

    String src =
            "public class RunTimeTest {\n" +
                    "    private static void safe() {\n" +
                    "    };\n" +
                    "    private static void unsafe(String httpParamCmd) throws Exception {\n" +
                    "        eval(httpParamCmd);\n" +
                    "    } ;\n" +
                    "    private static void eval(String cmd) throws Exception {\n" +
                    "        Runtime.getRuntime().exec(cmd);\n" +
                    "    }\n" +
                    "}";

    JavacTaskImpl task = utils.TestUtils.getTask(src);
    List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
    task.analyze(); // attribute with symbols?
    CompilationUnitTree u = parse.get(0);

    WiggleVisitor v = new WiggleVisitor(task, graphDb, Collections.singletonMap("projectName", "RunTimeTest"));
    v.scan(u, null);
    Result result = graphDb.execute("start 起始节点=node(*) MATCH (终止节点)-[关系]->(起始节点) RETURN  id(终止节点),关系, id(起始节点)");
    System.out.println(result.resultAsString());
    Thread.sleep(10000000);

}
```

​	上述的demo示例代码中的safe方法没有风险，unsafe方法有风险，我们将其进行语法树分析后导入图数据库，这里为了方便演示写了很小的代码片段，但是这种技术是可以跨文件跨项目的，只要标记不同项目的project为kv值到数据库就行。接下来的文章将不再使用传统的表格和console显示，而用可视化的图的方式。示例使用了开源图数据库neo4j，读者可以本地搭建或者使用华为云免费的图引擎平台服务 GES（只有华为这一个羊毛可以撸，其他平台都收费）。
![image-20200329181138671](https://tva1.sinaimg.cn/large/00831rSTly1gdaynns3xsj318a0u048o.jpg)
太远看不清楚，我们逐次放大。
![image-20200329180720463](https://tva1.sinaimg.cn/large/00831rSTly1gdayj6rxomj317h0u0akp.jpg)
从这里可以看出，用图数据库是完全可以准确表示漏洞的触发调用逻辑，甚至可以直接用最短路径算法显示调用链。
![image-20200329181824788](https://tva1.sinaimg.cn/large/00831rSTly1gdayup2kvmj30u00u2wkh.jpg)	污点分析还需要从头到尾一一分析数据的控制调用，下面我们利用图数据库的优势，简单构建一条查询语句，从exec这个高危险的中间语句节点反查全部调用过程。图数据库支持构建索引，从软件高速审查的角度为了效率查询建议对敏感函数构建二级的聚簇索引，这样很多查找就从全部遍历优化成了二分查找，使得查询速度大幅提升。
​	下图的查询语句解释为：
设置路径变量为p，用于引用查询路径。变量n为存在exec的函数，查找所有的m，即初始路径，而且初始路径是一个方法。返回初始节点，函数调用路径和最终节点。
```
match p= (m)-[*]-> (n{typeKind: 'EXECUTABLE',name:'exec'}) where m.nodeType='JCMethodDecl' 
return m.name,n,p
```
![image-20200329212506448](https://tva1.sinaimg.cn/large/00831rSTly1gdb48xu2jsj317i0u043z.jpg)

这样就完成了一次漏洞查询和调用分析，避免了误报和漏洞。
## 优点
### 直观展示漏洞的利用路径
​	上面的demo看到，通过编写查询局域可以直观看到调用过程和利用条件。在安全测试人员进行重点分析时，只需要关注可以导致关键条件的变化即可，无需打开深入每一个调用函数最后昏头转向。而开发人员可直接检查修复每一处漏洞调用点，避免了修复时会有遗漏。
### 通用开源的搭建能力
​	目前的图引擎方案基于各家公司的开源技术均可以搭建，无需购买商业代码分析工具。程序自动化得进行数据分析引入，安全人员的工作将主要是写查询语句，图数据库查询性能卓越，实现灵活，特定查询技术要求明确，上手方便。
### 兼容现有的技术方案
​	迁移和改造目前的流程一贯是花费较多时间，基于现有的图分析技术只要适配导出代码分析结果中间件为json或者cve格式，使用ETL工具直接打入图数据库即可，这样安全团队之前积累的历史漏洞和交互修复过程仍然可以发挥有效价值。
# 展望未来
​	图数据库中建立的代码结构模型区分语言的具体实现，例如本文中java的“类的继承”关系、重写方法如果换一门语言就需要重新设定图模型，需要加以特别标注。
​	将代码的函数引入数据库，未来有望实现为一项**复制粘贴神器**，解决公司不同部门间重复写”轮子“代码的情况，也可以帮助代码维护人员进行解耦，减少重复代码量。
​	现如今各种零信任，beyondcorp方案引来的复杂的权限验证模型，图数据库也可以开发这方面的应用，关注数据模型和资源之间的关系实现认证过程。
​	目前在网络安全中实际应用较多的是金融安全防欺诈，数据资产、情报管理也可以使用图的关系进行表达。
​	图数据对应软件的模型也可以考虑增加时间维度，查看结构，元素和关系如何随时间变化，这样从架构设计维度可以看到软件模型的不断变换，建立新的软件成熟度评估模型。
# 参考资料
1. https://www.baeldung.com/spring-data-neo4j-intro
2. https://zhuanlan.zhihu.com/p/50171330
3. https://www.jianshu.com/p/2fb8c8d103da
4. https://database.51cto.com/art/202002/610898.htm


