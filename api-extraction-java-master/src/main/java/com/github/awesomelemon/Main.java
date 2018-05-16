package com.github.awesomelemon;

import com.github.arunsoman.ipc.mappedbus.MappedBusWriter;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Stopwatch;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
//        List<Method> methods1 = ProcessFile("/media/jet/HDD/DeepApiJava/treejames/SmartAndroid/Smart_Client/app/libs/guava-master/guava/src/com/google/common/reflect/TypeToken.java");
//        List<Method> methods1 = ProcessFile("/media/jet/HDD/DeepApiJava/zephiK/android_frameworks_base/rs/java/android/renderscript/Allocation.java");
//        List<Method> methods1 = ProcessFile("/media/jet/HDD/DeepApiJava/michael-rapp/AndroidAdapters/library/src/main/java/de/mrapp/android/adapter/expandablelist/selectable/SingleChoiceExpandableListAdapterImplementation.java");
//        List<Method> methods1 = ProcessFile("/media/jet/HDD/DeepApiJava/KNightWeng/TAkeMeHome/app/src/main/java/com/knightweng/android/takemehome/common/ApiUtils.java");
//        List<Method> methods1 = ProcessFile("/media/jet/HDD/DeepApiJava/liuyq/TerminalIDE/src/com/sun/tools/javac/comp/Attr.java");
//        if (true) return;
//        storeEdingburghJavaDataset();
//        String javaParserTestMain = "D:\\Users\\Alexander\\Documents\\JavaparserTest\\src\\Main.java";
//        String javaParserTestMain = "/home/jet/JavaParserTest"; Original!
//        String databasePath = "D:\\YandexDisk\\DeepApiJava.sqlite";
        Stopwatch s = Stopwatch.createStarted();

        for(int i = 0; i < 10; i++){
//            String repoPath = "/home/hddcable/Downloads/cropiwa-master/library"; // For "Cropiwa" repository
            String repoPath = "/media/jet/HDD/guava"; // For "Guava"repository
            ProcessRepoUsingSourceRoot(repoPath);
        }

        s.stop();
        System.out.println("TOTAL TIME: " + s.toString());

/*        String databasePath = "/media/jet/HDD/DeepApiJava (1).sqlite";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        connection.setAutoCommit(false);
        RepoPathProvider repoPathProvider = new RepoPathProvider(connection);
        ResultWriter resultWriter = new ResultWriter(connection);
        int cnt = 0;
        while (true) {
            Pair<String, Integer> repo = repoPathProvider.getNext();

            while (repo != null) {
                final String repoPath = repo.getKey();
                final Integer repoId = repo.getValue();
                //                System.out.println(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
                resultWriter.markSolutionProcessed(repoId);
                Thread thread = new Thread(() -> {
                    try {
                        List<Method> methods = ProcessRepoUsingSourceRoot(repoPath);
                        resultWriter.write(methods, repoId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                thread.join();
                if (cnt % 20 == 0) {
                    System.gc();
                }
                deleteDir(new File(repoPath));
                repo = repoPathProvider.getNext();
            }
            System.out.println("Sleeping...");
            Thread.sleep(100000);
        }*/



    }

    public static void mainCompareProcessFuns(String[] args) throws IOException, SQLException, InterruptedException {
//        testStupidDotJavaFolderFail();
//        if (true) return;
//        testRepoExtract();
//        if (true) return;
        File dir = new File("/media/jet/HDD/DeepApiJava");
        List<File> javaFiles = new ArrayList<>();
        List<String> files = Arrays.stream(dir.listFiles()).map(File::getAbsolutePath).collect(Collectors.toList());
        compareProcessFuns(files);
    }

    private static void compareProcessFuns(List<String> repos) {
        int seen = 0;
        int seenCp = seen;
        int badCnt = 0;
        int diffsSum = 0;
        try {
            for (String repo : repos) {
                if (seenCp-- > 0) continue;
                long startTime = System.currentTimeMillis();
                List<Method> methods = ProcessRepo(repo);
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                int size = methods.size();
                long startTime2 = System.currentTimeMillis();
                methods = ProcessRepoUsingSourceRoot(repo);
                long stopTime2 = System.currentTimeMillis();
                long elapsedTime2 = stopTime2 - startTime2;

                long diff = elapsedTime - elapsedTime2;
//                System.out.println(diff);
                diffsSum += diff;

                int size1 = methods.size();
                seen++;
//                if (size != size1) {
//                    badCnt++;
//                    System.out.println("Diff!");
//                    System.out.println(size);
//                    System.out.println(size1);
//                    System.out.println(seen);
//                    System.out.println();
//                }
                if (seen % 10 == 0) {
//                    System.out.println("!!!");
//                    System.out.println(badCnt);
                    System.out.println(diffsSum);
//                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testRepoExtract() throws IOException {
        String repo = "/media/jet/HDD/DeepApiJava/thetonrifles";
        List<Method> methods = ProcessRepoUsingSourceRoot(repo);
        System.out.println();
        List<Method> methods2 = ProcessRepo(repo);
        System.out.println(methods.size());
    }


    private static List<Method> ProcessRepoUsingSourceRoot(String repoPath) throws IOException {
        List<Method> repoMethods = new ArrayList<>();
        Path rootDir = Paths.get(repoPath);
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        File srcDir = new File(repoPath);
        if (!srcDir.exists()) return repoMethods;
        combinedTypeSolver.add(new JavaParserTypeSolver(srcDir));

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(false);
//        JavaParser parser = new JavaParser(parserConfiguration);
        SourceRoot sourceRoot = new SourceRoot(rootDir, parserConfiguration);
        List<ParseResult<CompilationUnit>> parseResults = null;
        System.out.println(repoPath);
        try {
            parseResults = sourceRoot.tryToParse();
        } catch (NoSuchFileException e) {
            e.printStackTrace();
            System.out.println("Try to pass the job to ProcessRepo");
            return ProcessRepo(repoPath);//temporary solution because of wrong
            //handling of dirs with dots by JavaParser
        }
        if (parseResults == null) return repoMethods;
        boolean stopPoint = false;
        for (ParseResult<CompilationUnit> parseResult : parseResults) {
            if (stopPoint) {
                continue;
            }
            Optional<CompilationUnit> maybeCu = parseResult.getResult();
            if (!maybeCu.isPresent()) {
                continue;
            }
            CompilationUnit cu = maybeCu.get();
            ArrayList<MethodDeclaration> methods = new ArrayList<>();
            MethodCollector methodCollector = new MethodCollector();
            methodCollector.visit(cu, methods);

            //create better type solver, using different dir
            CombinedTypeSolver combinedTypeSolver2 = new CombinedTypeSolver();
            combinedTypeSolver2.add(new ReflectionTypeSolver());
            File javaFile = new File(String.valueOf(cu.getStorage().get().getPath()));
            if ("/media/jet/HDD/guava/guava/src/com/google/common/collect/Range.java".equals(javaFile.getAbsolutePath())) {
                System.out.println("STOP");
                stopPoint = true;
                continue;
            }
//            System.out.println(javaFile.getAbsolutePath());
            combinedTypeSolver2.add(new JavaParserTypeSolver(new File(repoPath)));
            combinedTypeSolver2.add(new JavaParserTypeSolver(findProperRootDir(javaFile)));

            ApiSequenceExtractor apiSequenceExtractor = new ApiSequenceExtractor(
                    combinedTypeSolver2
            );
//            if (methods.size() == 0) continue;
//            System.out.println(1);

            for (MethodDeclaration method : methods) {
                ArrayList<ApiCall> apiCalls = new ArrayList<>();
//                try {
                apiSequenceExtractor.visit(method, apiCalls);
//                } catch (StackOverflowError | OutOfMemoryError e) {// I'm in no mood to debug Javaparser
//                    System.gc();
//                    System.out.println("Stack overflowed. Or memory. Deal with it.");
//                }

//                System.out.println(apiCalls);
                if (apiCalls.size() == 0) continue;
                repoMethods.add(new Method(
                        method.getJavadocComment().get().getContent(),
                        ApiCall.createStringSequence(apiCalls),
                        getFullMethodName(method)));
            }
            JavaParserFacade.clearInstances();
        }
        System.out.println(repoMethods.size());
        return repoMethods;
    }

    private static List<Method> ProcessRepo(String repoPath) throws IOException {
        List<File> javaFiles = findJavaFiles(repoPath);
        List<Method> repoMethods = new ArrayList<>();
        System.out.println(repoPath);
        for (File javaFile : javaFiles) {
            if (javaFile.isDirectory()) continue;//yep, there're dirs ending in '.java' E.g. in Wala
//            if (javaFile.getName().equals(
//                    "Editor.java")
//                    || javaFile.getAbsolutePath().startsWith("/media/jet/HDD/DeepApiJava/SlimRoms/frameworks_base/core/java/android/widget/")
//                    || javaFile.getAbsolutePath().startsWith("/media/jet/HDD/DeepApiJava/SlimRoms/frameworks_base/core/java/com/android/internal/view")
//                    || javaFile.getAbsolutePath().startsWith("/media/jet/HDD/DeepApiJava/SlimRoms/frameworks_base/core/java/com/android/internal/widget")
//                    || javaFile.getName().equals("PackageInstaller.java")
//                    || javaFile.getName().equals("FastScroller.java")
//                    || javaFile.getName().equals("LauncherApps.java")) {
//                continue;//stack overflow in java parser. 1gb stack is not enough
//            }
//            if (javaFile.getAbsolutePath().startsWith("/media/jet/HDD/DeepApiJava/SlimRoms/frameworks_base/media/java/android/media/AudioManager.java")) {
//                break;
//            }
            File rootDir = findProperRootDir(javaFile);
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver());
            combinedTypeSolver.add(new JavaParserTypeSolver(rootDir));

            Optional<CompilationUnit> maybeCu;

            ParserConfiguration parserConfiguration = new ParserConfiguration()
                    .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                    .setLexicalPreservationEnabled(false);

            JavaParser parser = new JavaParser(parserConfiguration);
            try {
                maybeCu = parser.parse(
                        ParseStart.COMPILATION_UNIT,
                        new StreamProvider(new FileInputStream(javaFile))
                ).getResult();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            if (!maybeCu.isPresent()) continue;
            CompilationUnit cu = maybeCu.get();

            ArrayList<MethodDeclaration> methods = new ArrayList<>();
            MethodCollector methodCollector = new MethodCollector();
            methodCollector.visit(cu, methods);

            ApiSequenceExtractor apiSequenceExtractor = new ApiSequenceExtractor(combinedTypeSolver);
//            if (methods.size() == 0) continue;
            System.out.println(javaFile);

            for (MethodDeclaration method : methods) {
                ArrayList<ApiCall> apiCalls = new ArrayList<>();
//                try {
                apiSequenceExtractor.visit(method, apiCalls);
//                } catch (StackOverflowError | OutOfMemoryError e) {// I'm in no mood to debug Javaparser
//                    System.gc();
//                    System.out.println("Stack overflowed. Or memory. Deal with it.");
//                }
//                System.out.println(apiCalls);
                if (apiCalls.size() == 0) continue;
                repoMethods.add(new Method(
                        method.getJavadocComment().get().getContent(),
                        ApiCall.createStringSequence(apiCalls),
                        getFullMethodName(method)));
            }
            JavaParserFacade.clearInstances();
        }
        return repoMethods;
    }

    private static List<Method> ProcessFile(String filePath) throws IOException {
        File javaFile = new File(filePath);
        List<Method> repoMethods = new ArrayList<>();
        System.out.println(filePath);
        File rootDir = findProperRootDir(javaFile);
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(rootDir));

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(false);
        JavaParser parser = new JavaParser(parserConfiguration);
        Optional<CompilationUnit> maybeCu = parser.parse(
                ParseStart.COMPILATION_UNIT,
                new StreamProvider(new FileInputStream(javaFile))
        ).getResult();
        CompilationUnit cu = maybeCu.get();

        ArrayList<MethodDeclaration> methods = new ArrayList<>();
        MethodCollector methodCollector = new MethodCollector();
        methodCollector.visit(cu, methods);

        ApiSequenceExtractor apiSequenceExtractor = new ApiSequenceExtractor(combinedTypeSolver);
//            if (methods.size() == 0) continue;
        System.out.println(javaFile);

        for (MethodDeclaration method : methods) {
            ArrayList<ApiCall> apiCalls = new ArrayList<>();
            System.out.println(method.getName().asString());
//                try {
            apiSequenceExtractor.visit(method, apiCalls);
//                } catch (StackOverflowError | OutOfMemoryError e) {// I'm in no mood to debug Javaparser
//                    System.gc();
//                    System.out.println("Stack overflowed. Or memory. Deal with it.");
//                }
//                System.out.println(apiCalls);
            if (apiCalls.size() == 0) continue;
            repoMethods.add(new Method(
                    method.getJavadocComment().get().getContent(),
                    ApiCall.createStringSequence(apiCalls),
                    getFullMethodName(method)));
        }
        JavaParserFacade.clearInstances();
        return repoMethods;
    }

    private static String getFullMethodName(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        Optional<Node> parentNode = method.getParentNode();
        if (parentNode.isPresent()) {
            String parentName = "";
            if (parentNode.get() instanceof ClassOrInterfaceDeclaration) {
                parentName = ((ClassOrInterfaceDeclaration) parentNode.get()).getNameAsString();
            }
            if (parentNode.get() instanceof EnumDeclaration) {
                parentName = ((EnumDeclaration) parentNode.get()).getNameAsString();
            }
            methodName = parentName + "." + methodName;
        }
        return methodName;
    }

    private static File findProperRootDir(File javaFile) {
        File folderName = javaFile.getParentFile();
        while (!folderName.getName().equals("src") && !folderName.getName().equals("java")) {
            folderName = folderName.getParentFile();
            if (folderName == null) break;
        }
        return folderName == null ? javaFile.getParentFile() : folderName;
    }

    private static List<String> findJavaFilePaths(String path) {
        File dir = new File(path);
        List<String> javaFiles = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                javaFiles.addAll(findJavaFilePaths(file.getAbsolutePath()));
            }
            if (file.getName().endsWith(".java")) {
                javaFiles.add(file.getAbsolutePath());
            }
        }
        return javaFiles;
    }

    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    private static List<File> findJavaFiles(String path) {
        File dir = new File(path);
        List<File> javaFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                javaFiles.addAll(findJavaFiles(file.getAbsolutePath()));
            }
            if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }
        return javaFiles;
    }

    public static void noSolverMain(String[] args) throws Exception {
        String javaParserTestMain = "D:\\Users\\Alexander\\Documents\\JavaparserTest\\src\\Main.java";
        CompilationUnit cu = JavaParser.parse(new FileInputStream(javaParserTestMain));

        ArrayList<MethodDeclaration> methods = new ArrayList<>();
        MethodCollector methodCollector = new MethodCollector();
        methodCollector.visit(cu, methods);

        ApiSequenceExtractor apiSequenceExtractor = new ApiSequenceExtractor(null);
        ArrayList<ApiCall> apiCalls = new ArrayList<>();

        for (MethodDeclaration method : methods) {
            apiSequenceExtractor.visit(method, apiCalls);
            System.out.println(apiCalls);
        }
    }

    public static void oldMain() throws Exception {
        String myFile = "D:\\DeepJavaReps\\retrofit\\samples\\src\\main\\java\\com\\example\\retrofit\\ChunkingConverter.java";
        CompilationUnit cu = JavaParser.parse(new FileInputStream(myFile));
//        TypeSolver typeSolver = new CombinedTypeSolver();
//        ParserConfiguration parserConfiguration =
//                new ParserConfiguration().setSymbolResolver(
//                        new JavaSymbolSolver(typeSolver));
//        JavaParser parser = new JavaParser(parserConfiguration);
//        CompilationUnit cu =
//                parser.parse(ParseStart.COMPILATION_UNIT,
//                        new StreamProvider(new FileInputStream(myFile)))
//                        .getResult().get();

        VoidVisitor<List<String>> methodNameVisitor = new MethodNamePrinter();
        ArrayList<String> methodNames = new ArrayList<>();
//        methodNameVisitor.visit(cu, methodNames);
//        System.out.println(methodNames);
        MethodCollector methodCollector = new MethodCollector();
        ArrayList<MethodDeclaration> methods = new ArrayList<>();
        methodCollector.visit(cu, methods);
        for (MethodDeclaration method : methods) {
            methodNameVisitor.visit(method, methodNames);
        }
        System.out.println(methodNames);

//        TypeSolver typeSolver = new CombinedTypeSolver();
//        ParserConfiguration parserConfiguration =
//                new ParserConfiguration().setSymbolResolver(
//                        new JavaSymbolSolver(typeSolver));
//        JavaParser parser = new JavaParser(parserConfiguration);
//        CompilationUnit compilationUnit =
//                parser.parse(ParseStart.COMPILATION_UNIT,
//                        new StreamProvider(new FileInputStream(myFile)))
//                        .getResult().get();
//        List<Node> childNodes = cu.getChildNodes();
//
//        Type type = JavaParserFacade.get(typeSolver).getType(childNodes.get(21).getChildNodes().get(5).getChildNodes().get(5).getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(0).getChildNodes().get(2));
//        System.out.println(childNodes);

    }


    private static void testStupidDotJavaFolderFail() throws IOException {
        Path rootDir = Paths.get("/media/jet/HDD/DeepApiJava/erahal");
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File("/media/jet/HDD/DeepApiJava/erahal")));

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(false);
//        JavaParser parser = new JavaParser(parserConfiguration);
        SourceRoot sourceRoot = new SourceRoot(rootDir, parserConfiguration);
        Files.walkFileTree(Paths.get("/media/jet/HDD/DeepApiJava/erahal"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isDirectory() && file.toString().endsWith(".java") && file.toString().contains("etch")) {
                    Path root = Paths.get("/media/jet/HDD/DeepApiJava/erahal");
                    Path relative = root.relativize(file.getParent());
                    System.out.println(relative);
                    sourceRoot.tryToParse(relative.toString(), file.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    private static void storeEdingburghJavaDataset() throws SQLException {
        final String datasetPath = "D:\\Users\\Alexander\\Downloads\\java_projects\\java_projects";
        List<String> dirPaths = Arrays.stream(new File(datasetPath).listFiles()).map(dirFile -> dirFile.getAbsolutePath()).collect(Collectors.toList());

        String databasePath = "D:\\YandexDisk\\DeepApiJava.sqlite";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        connection.setAutoCommit(false);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO Solution(Path) VALUES (?)");
            preparedStatement.setQueryTimeout(30);
            for (String dirPath : dirPaths) {
                preparedStatement.setString(1, dirPath);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
