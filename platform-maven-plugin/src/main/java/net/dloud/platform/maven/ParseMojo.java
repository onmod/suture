package net.dloud.platform.maven;


import com.alibaba.fastjson.JSON;
import net.dloud.platform.common.gateway.info.ClassInfo;
import net.dloud.platform.common.gateway.info.TypeInfo;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.alibaba.fastjson.serializer.SerializerFeature.PrettyFormat;
import static net.dloud.platform.maven.ParseUtil.classSuffix;


@Mojo(name = "parse", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ParseMojo extends AbstractMojo {
    private Log log = getLog();
    private String save = "/resources/PARSE-INF/";
    private Map<String, String> map = new LinkedHashMap<>();
    private Set<String> filter = new HashSet<>();
    private Set<String> suffix = new HashSet<>();
    private Set<String> flieds = new HashSet<>();
    private List<ClassInfo> infos = new ArrayList<>();

    @Parameter(property = "sourceDir", required = true, defaultValue = "${project.build.sourceDirectory}")
    private String sourceDirectory;

    @Parameter(property = "targetDir", defaultValue = "${project.build.sourceDirectory}")
    private String targetDirectory;

    @Parameter(property = "systemId", required = true)
    private String systemId;

    @Parameter(property = "systemName", required = true)
    private String systemName;

    @Parameter(property = "packageName", required = true)
    private String packageName;

    @Parameter(property = "indexName", defaultValue = "index")
    private String indexName;

    @Parameter(property = "versionName", defaultValue = "version")
    private String versionName;

    @Parameter(property = "isCompress", defaultValue = "true")
    private Boolean isCompress;

    @Parameter(property = "isAnnotation", defaultValue = "false")
    private Boolean isAnnotation;

    @Parameter(property = "injectionType", defaultValue = "net.dloud.platform.common.annotation.Injection")
    private String injectionType;

    @Parameter(property = "injectionEnum", defaultValue = "net.dloud.platform.common.gateway.InjectEnum")
    private String injectionEnum;

    @Parameter(property = "permissionType", defaultValue = "net.dloud.platform.common.annotation.Permission")
    private String permissionType;

    @Parameter(property = "whitelistType", defaultValue = "net.dloud.platform.common.annotation.Whitelist")
    private String whitelistType;

    @Parameter(property = "backgroundType", defaultValue = "net.dloud.platform.common.annotation.Background")
    private String backgroundType;

    @Parameter(property = "enquireType", defaultValue = "net.dloud.platform.common.annotation.Enquire")
    private String enquireType;

    @Parameter(property = "makeJson", defaultValue = "false")
    private Boolean makeJson;

    @Parameter(property = "filterFile", defaultValue = "")
    private String filterFile;

    @Parameter(property = "filterSuffix", defaultValue = "")
    private String filterSuffix;

    @Parameter(property = "fliedFilter", defaultValue = "")
    private String fliedFilter;

    @Parameter(property = "kryoSuffix", required = true, defaultValue = "Entry,Result,Param,Pojo,POJO,DTO,VO")
    private String kryoSuffix;


    @Override
    public void execute() throws MojoExecutionException {
        if (!sourceDirectory.endsWith("/")) {
            sourceDirectory += "/";
        }
        packageName = packageName.replaceAll("\\.", "/");
        final String saveBasePath = sourceDirectory.substring(0, sourceDirectory.lastIndexOf("/java")) + save;
        final String savePath = saveBasePath + systemId + "/";

        filter.add(".DS_Store");
        if (null != filterFile && !filterFile.isEmpty()) {
            filter.addAll(Arrays.asList(filterFile.split(",")));
        }
        suffix.add("impl");
        suffix.add("Impl");
        suffix.add("Util");
        suffix.add("Utils");
        suffix.add("Enum");
        suffix.add("Enums");
        suffix.add("Constant");
        suffix.add("Constants");
        suffix.add("Filter");
        suffix.add("Listener");
        if (null != filterSuffix && !filterSuffix.isEmpty()) {
            suffix.addAll(Arrays.asList(filterSuffix.split(",")));
        }

        flieds.add("serialVersionUID");
        if (null != fliedFilter && !fliedFilter.isEmpty()) {
            flieds.addAll(Arrays.asList(fliedFilter.split(",")));
        }

        Set<String> kryos = new HashSet<>(Arrays.asList(kryoSuffix.split(",")));
        Set<String> optimizer = new HashSet<>();
        try {
            deleteDir(Paths.get(savePath).toFile());
            parseDir(Paths.get(sourceDirectory + packageName).toFile());

            Map<String, Set<TypeInfo>> superMap = new HashMap<>();
            Map<String, Set<String>> importMap = new HashMap<>();
            for (ClassInfo sourceInfo : infos) {
                if (null != sourceInfo.getSuperclass() && !sourceInfo.getSuperclass().isEmpty()) {
                    superMap.put(sourceInfo.getQualifiedName(), sourceInfo.getSuperclass());
                }
                if (null != sourceInfo.getImportInfo() && !sourceInfo.getImportInfo().isEmpty()) {
                    importMap.put(sourceInfo.getQualifiedName(), sourceInfo.getImportInfo());
                }
            }
            for (ClassInfo sourceInfo : infos) {
                //合并父类及导入
                Set<TypeInfo> superInfo = new HashSet<>();
                Set<String> importInfo = new HashSet<>();
                if (null != sourceInfo.getSuperclass()) {
                    for (TypeInfo superClass : sourceInfo.getSuperclass()) {
                        final Set<TypeInfo> superSet = superMap.get(superClass.getQualifiedName());
                        if (null != superSet) {
                            superInfo.addAll(superSet);
                        }
                        superInfo.add(superClass);
                    }
                    sourceInfo.setSuperclass(superInfo);
                }
                if (null != sourceInfo.getImportInfo()) {
                    for (String importClass : sourceInfo.getImportInfo()) {
                        final Set<String> importSet = importMap.get(importClass);
                        if (null != importSet) {
                            importInfo.addAll(importSet);
                        }
                        importInfo.add(importClass);
                    }
                    sourceInfo.setImportInfo(importInfo);
                }
                saveParse(sourceInfo, savePath);


                if (!sourceInfo.getIfInterface() && !sourceInfo.getIfMember()) {
                    final String qualifiedName = sourceInfo.getQualifiedName();
                    if (kryos.contains(classSuffix(qualifiedName))) {
                        optimizer.add(qualifiedName);
                    }
                }

                for (String importClass : importInfo) {
                    if (kryos.contains(classSuffix(importClass))) {
                        optimizer.add(importClass);
                    }
                }
            }

            if (!map.isEmpty()) {
                saveIndex(savePath);
            }
            if (!optimizer.isEmpty()) {
                saveOptimizer(optimizer, saveBasePath + "optimizer");
            }
        } catch (Exception e) {
            log.error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void deleteDir(File dir) throws IOException {
        if (null != dir && dir.isDirectory()) {
            log.info("Delete Directory: " + dir);
            for (File one : Objects.requireNonNull(dir.listFiles())) {
                if (one.isDirectory()) {
                    log.info("Delete Directory: " + one);
                    deleteDir(one);
                } else {
                    one.delete();
                }
            }
        }
    }

    private void parseDir(File dir) throws IOException {
        if (null != dir && dir.isDirectory()) {
            log.info("Process Directory: " + dir);
            for (File one : Objects.requireNonNull(dir.listFiles())) {
                if (!filter.contains(one.getName())) {
                    if (one.isDirectory()) {
                        parseDir(one);
                    } else {
                        infos.addAll(parseVisitor(one).getSourceInfo());
                    }
                }
            }
        }
    }

    private ParseVisitor parseVisitor(File one) throws IOException {
        log.info("Process File: " + one);
        ParseVisitor visitor = new ParseVisitor(systemName, injectionType, injectionEnum,
                permissionType, whitelistType, backgroundType, enquireType, isAnnotation, suffix, flieds);
        final int length = (int) one.length();
        char[] chars = new char[length];
        try (final FileReader reader = new FileReader(one)) {
            final ASTParser parser = ASTParser.newParser(8);
            reader.read(chars);
            parser.setSource(chars);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            CompilationUnit comp = (CompilationUnit) (parser.createAST(null));
            comp.accept(visitor);
        }
        return visitor;
    }

    private void saveParse(ClassInfo sourceInfo, String savePath) throws IOException {
        if (null != sourceInfo && null != sourceInfo.getQualifiedName()) {
            final String qualifiedName = sourceInfo.getQualifiedName();
            log.info("Process File Success: " + qualifiedName);

            final File saveDir = Paths.get(savePath).toFile();
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            final byte[] data = saveFile(sourceInfo, savePath + qualifiedName);
            map.put(qualifiedName, ParseUtil.getVersion(data));
        }
    }

    private void saveIndex(String savePath) throws IOException {
        log.info("Process Index");
        final String version = ParseUtil.getVersion(saveFile(map, savePath + indexName));
        try (final FileWriter writer = new FileWriter(savePath + versionName)) {
            writer.write(version);
            writer.flush();
        }
    }

    private void saveOptimizer(Set<String> optimizer, String savePath) throws IOException {
        log.info("Process Optimizer");
        saveFile(optimizer, savePath);
    }

    private byte[] saveFile(Object sourceInfo, String fullPath) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(fullPath)) {
            byte[] data = saveByKryo(sourceInfo);

            if (makeJson) {
                JSON.writeJSONString(fos, sourceInfo, PrettyFormat);
            } else {
                if (isCompress) {
                    final byte[] compress = Snappy.compress(data);
                    log.info("Compression Ratio: " + (int) ((compress.length / (double) data.length) * 10000) / 100.0);
                    fos.write(compress);
                    data = compress;
                } else {
                    fos.write(data);
                }
            }
            fos.flush();

            return data;
        }
    }


    private byte[] saveByJava(Object sourceInfo) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(sourceInfo);
            return bos.toByteArray();
        }
    }

    private <T> byte[] saveByKryo(T sourceInfo) throws IOException {
        return KryoBaseUtil.writeToByteArray(sourceInfo, false, true);
    }
}
