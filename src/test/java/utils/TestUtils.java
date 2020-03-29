package utils;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import com.sun.tools.javac.api.JavacTaskImpl;

public class TestUtils {


    private static JavaCompiler tool = ToolProvider.getSystemJavaCompiler();

    public static JavacTaskImpl getTask(String... sourceCode) throws Exception {

        List<JavaFileObject> l = new ArrayList<JavaFileObject>();

        for (String src : sourceCode) {
            l.add(new StringJavaFileObject(src));
        }

        JavacTaskImpl task = (JavacTaskImpl)
                tool.getTask(null, null, null, null, null, l);

        return task;
    }

}
