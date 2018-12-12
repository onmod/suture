package net.dloud.platform.maven;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * @author QuDasheng
 * @create 2018-09-29 20:29
 **/
public class ParseTest {
    public static void main(String[] args) {
        ParseVisitor visitor = new ParseVisitor("test",
                "net.dloud.platform.common.annotation.Injection",
                "net.dloud.platform.common.gateway.InjectEnum",
                "net.dloud.platform.common.annotation.Permission",
                "net.dloud.platform.common.annotation.Whitelist",
                "net.dloud.platform.common.annotation.Background",
                "net.dloud.platform.common.annotation.Enquire",
                false, new HashSet<>(), new HashSet<>());


        String path = "/Users/dor/Projects/now/platform-all/common-client/src/main/java/net/dloud/platform/common/domain/result/PairResult.java";
        File file = Paths.get(path).toFile();
        final int length = (int) file.length();
        char[] chars = new char[length];
        try (final FileReader reader = new FileReader(file)) {
            final ASTParser parser = ASTParser.newParser(8);
            reader.read(chars);
            parser.setSource(chars);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            CompilationUnit comp = (CompilationUnit) (parser.createAST(null));
            comp.accept(visitor);
            System.out.println(visitor.getSourceInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
