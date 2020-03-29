package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

public class StringJavaFileObject extends SimpleJavaFileObject {

    private String code;


    public StringJavaFileObject(String code) throws Exception {

        super(URI.create(""), Kind.SOURCE);
        this.code = code;

    }


    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return this.code;
    }


}
