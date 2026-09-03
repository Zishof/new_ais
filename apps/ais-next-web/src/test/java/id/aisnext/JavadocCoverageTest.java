package id.aisnext;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

/**
 * Enforces complete JavaDoc coverage for every Java type, constructor, and method.
 *
 * <p>The test parses source syntax with the JDK compiler API, so annotations, records, nested
 * declarations, and multiline signatures are handled without fragile regular expressions.
 * Anonymous classes are excluded because Java does not provide a declaration site to which a
 * JavaDoc comment can be attached; their explicitly declared methods are still checked. A complete
 * method contract has a summary, a description for every parameter, a return description when it
 * returns a value, and a description for every declared exception.</p>
 */
class JavadocCoverageTest {

    /**
     * Creates the repository-wide documentation coverage test.
     */
    JavadocCoverageTest() {
    }

    /**
     * Scans production and test sources in the entire Maven reactor and reports incomplete
     * declarations with their repository-relative path and line number.
     *
     * @throws IOException when source discovery or compiler file access fails
     */
    @Test
    void everyJavaTypeConstructorAndMethodHasJavadoc() throws IOException {
        Path repositoryRoot = findRepositoryRoot(Path.of("").toAbsolutePath());
        List<Path> sources = findJavaSources(repositoryRoot);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        assertThat(compiler)
                .as("A full JDK is required to audit JavaDoc coverage")
                .isNotNull();
        assertThat(sources)
                .as("Java source files discovered under %s", repositoryRoot)
                .isNotEmpty();

        List<Violation> violations = new ArrayList<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT, null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromPaths(sources);
            JavacTask task = (JavacTask) compiler.getTask(
                    null, fileManager, null, List.of("-proc:none"), null, units);
            Iterable<? extends CompilationUnitTree> parsedUnits = task.parse();
            DocTrees docTrees = DocTrees.instance(task);
            Trees trees = Trees.instance(task);

            for (CompilationUnitTree unit : parsedUnits) {
                new DocumentationScanner(repositoryRoot, unit, docTrees, trees, violations).scan(unit, null);
            }
        }

        violations.sort(Comparator.comparing(Violation::path).thenComparingLong(Violation::line));
        assertThat(violations)
                .as("Undocumented Java declarations (add a /** ... */ comment that explains the contract)")
                .isEmpty();
    }

    /**
     * Walks upward from a module working directory until the root reactor is found.
     *
     * @param start absolute or relative path from which to begin the search
     * @return root directory containing the reactor POM and application modules
     * @throws IllegalStateException when no AIS Next reactor root exists in the ancestor chain
     */
    private static Path findRepositoryRoot(Path start) {
        Path candidate = start.toAbsolutePath().normalize();
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("apps"))
                    && Files.isDirectory(candidate.resolve("platform"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalStateException("Cannot locate the AIS Next Maven reactor from " + start);
    }

    /**
     * Finds all production and test Java source files while excluding generated build output.
     *
     * @param repositoryRoot root of the multi-module repository
     * @return sorted list of Java source paths
     * @throws IOException when the repository tree cannot be read
     */
    private static List<Path> findJavaSources(Path repositoryRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(repositoryRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(JavadocCoverageTest::belongsToSourceSet)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Determines whether a path is inside a conventional Maven main or test Java source set.
     *
     * @param path candidate Java file path
     * @return {@code true} for files below {@code src/main/java} or {@code src/test/java}
     */
    private static boolean belongsToSourceSet(Path path) {
        for (int index = 0; index + 2 < path.getNameCount(); index++) {
            if (path.getName(index).toString().equals("src")
                    && (path.getName(index + 1).toString().equals("main")
                    || path.getName(index + 1).toString().equals("test"))
                    && path.getName(index + 2).toString().equals("java")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visits declaration nodes and records those without an attached JavaDoc tree.
     */
    private static final class DocumentationScanner extends TreePathScanner<Void, Void> {
        private final Path repositoryRoot;
        private final CompilationUnitTree unit;
        private final DocTrees docTrees;
        private final Trees trees;
        private final List<Violation> violations;

        /**
         * Creates a scanner for one parsed compilation unit.
         *
         * @param repositoryRoot repository root used to produce readable relative paths
         * @param unit parsed Java compilation unit
         * @param docTrees compiler service for retrieving attached JavaDoc comments
         * @param trees compiler service for retrieving source positions
         * @param violations shared collection that receives missing-documentation findings
         */
        private DocumentationScanner(Path repositoryRoot, CompilationUnitTree unit, DocTrees docTrees,
                                     Trees trees, List<Violation> violations) {
            this.repositoryRoot = repositoryRoot;
            this.unit = unit;
            this.docTrees = docTrees;
            this.trees = trees;
            this.violations = violations;
        }

        /**
         * Checks named classes, records, enums, interfaces, and annotation interfaces.
         *
         * @param node type declaration being visited
         * @param unused scanner state, intentionally unused
         * @return scanner result from nested declarations
         */
        @Override
        public Void visitClass(ClassTree node, Void unused) {
            if (!node.getSimpleName().isEmpty()) {
                DocCommentTree documentation = docTrees.getDocCommentTree(getCurrentPath());
                String kind = node.getKind().name().toLowerCase(Locale.ROOT);
                String name = node.getSimpleName().toString();
                if (documentation == null) {
                    addViolation(node, kind, name + " has no JavaDoc");
                } else if (!hasText(documentation.getFullBody())) {
                    addViolation(node, kind, name + " has no summary");
                }
                if (node.getKind() == Tree.Kind.CLASS && node.getMembers().stream()
                        .filter(MethodTree.class::isInstance)
                        .map(MethodTree.class::cast)
                        .noneMatch(method -> method.getReturnType() == null)) {
                    addViolation(node, "constructor", name + " uses an undocumented implicit constructor");
                }
            }
            return super.visitClass(node, unused);
        }

        /**
         * Checks every explicitly declared constructor and method, regardless of visibility.
         *
         * @param node method or constructor declaration being visited
         * @param unused scanner state, intentionally unused
         * @return scanner result from nested declarations
         */
        @Override
        public Void visitMethod(MethodTree node, Void unused) {
            DocCommentTree documentation = docTrees.getDocCommentTree(getCurrentPath());
            if (documentation == null) {
                String kind = node.getReturnType() == null ? "constructor" : "method";
                addViolation(node, kind, node.getName() + " has no JavaDoc");
            } else {
                validateMethodDocumentation(node, documentation);
            }
            return super.visitMethod(node, unused);
        }

        /**
         * Validates the descriptive text and contract tags attached to a method or constructor.
         *
         * @param node method or constructor declaration being validated
         * @param documentation parsed JavaDoc attached to the declaration
         */
        private void validateMethodDocumentation(MethodTree node, DocCommentTree documentation) {
            if (!hasText(documentation.getFullBody())) {
                addViolation(node, "documentation", node.getName() + " has no summary");
            }
            List<? extends DocTree> tags = documentation.getBlockTags();
            if (!isRecordConstructor(node)) {
                for (VariableTree parameter : node.getParameters()) {
                    boolean present = tags.stream()
                            .filter(ParamTree.class::isInstance)
                            .map(ParamTree.class::cast)
                            .anyMatch(tag -> !tag.isTypeParameter()
                                    && tag.getName().toString().equals(parameter.getName().toString())
                                    && hasText(tag.getDescription()));
                    if (!present) {
                        addViolation(node, "documentation",
                                node.getName() + " is missing @param " + parameter.getName());
                    }
                }
            }
            if (node.getReturnType() != null && !node.getReturnType().toString().equals("void")) {
                boolean present = tags.stream()
                        .filter(ReturnTree.class::isInstance)
                        .map(ReturnTree.class::cast)
                        .anyMatch(tag -> hasText(tag.getDescription()));
                if (!present) {
                    addViolation(node, "documentation", node.getName() + " is missing @return");
                }
            }
            for (Tree exception : node.getThrows()) {
                String declaredName = exception.toString();
                boolean present = tags.stream()
                        .filter(ThrowsTree.class::isInstance)
                        .map(ThrowsTree.class::cast)
                        .anyMatch(tag -> exceptionNamesMatch(declaredName, tag.getExceptionName().toString())
                                && hasText(tag.getDescription()));
                if (!present) {
                    addViolation(node, "documentation", node.getName() + " is missing @throws " + declaredName);
                }
            }
        }

        /**
         * Detects a record constructor whose component parameters are documented on the record.
         *
         * <p>The parser exposes compact-constructor components as method parameters even though the
         * source declaration has no parameter list. Their {@code @param} tags therefore belong to
         * the record JavaDoc rather than the compact constructor JavaDoc.</p>
         *
         * @param node method declaration being inspected
         * @return {@code true} when the method is a constructor nested directly in a record
         */
        private boolean isRecordConstructor(MethodTree node) {
            return node.getReturnType() == null
                    && getCurrentPath().getParentPath().getLeaf() instanceof ClassTree parent
                    && parent.getKind() == Tree.Kind.RECORD;
        }

        /**
         * Reports whether a JavaDoc fragment contains visible descriptive text.
         *
         * @param content parsed JavaDoc nodes to inspect
         * @return {@code true} when at least one node contains non-whitespace text
         */
        private static boolean hasText(List<? extends DocTree> content) {
            return content.stream().anyMatch(node -> !node.toString().isBlank());
        }

        /**
         * Compares declared and documented exception names while allowing one side to be qualified.
         *
         * @param declaredName exception name used in the Java method signature
         * @param documentedName exception name used by the JavaDoc tag
         * @return {@code true} when the names identify the same simple or qualified type
         */
        private static boolean exceptionNamesMatch(String declaredName, String documentedName) {
            return declaredName.equals(documentedName)
                    || declaredName.endsWith("." + documentedName)
                    || documentedName.endsWith("." + declaredName);
        }

        /**
         * Adds a source-positioned finding for an undocumented declaration.
         *
         * @param node declaration missing JavaDoc
         * @param kind human-readable declaration or documentation finding kind
         * @param name concise description of the incomplete declaration
         */
        private void addViolation(com.sun.source.tree.Tree node, String kind, String name) {
            long position = trees.getSourcePositions().getStartPosition(unit, node);
            long line = position < 0 ? -1 : unit.getLineMap().getLineNumber(position);
            Path source = Path.of(unit.getSourceFile().toUri());
            violations.add(new Violation(repositoryRoot.relativize(source).toString(), line, kind, name));
        }
    }

    /**
     * Identifies one undocumented source declaration in an assertion-friendly form.
     *
     * @param path repository-relative Java source path
     * @param line one-based source line, or {@code -1} when unavailable
     * @param kind declaration kind reported by the scanner
     * @param name source-level declaration name
     */
    private record Violation(String path, long line, String kind, String name) {
        /**
         * Renders the finding as a concise source location and declaration description.
         *
         * @return human-readable violation text used by AssertJ
         */
        @Override
        public String toString() {
            return path + ":" + line + " " + kind + " " + name;
        }
    }
}
