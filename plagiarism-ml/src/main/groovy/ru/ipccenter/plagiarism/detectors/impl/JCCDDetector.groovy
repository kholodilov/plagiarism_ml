package ru.ipccenter.plagiarism.detectors.impl

import org.eposoft.jccd.comparators.ast.AcceptFileNames
import org.eposoft.jccd.comparators.ast.java.AcceptLogicalOperators
import org.eposoft.jccd.comparators.ast.java.AcceptStringLiterals
import org.eposoft.jccd.comparators.ast.java.NumberLiteralToDouble
import org.eposoft.jccd.data.*
import org.eposoft.jccd.data.ast.ANode
import org.eposoft.jccd.data.ast.NodeTypes
import org.eposoft.jccd.detectors.APipeline
import org.eposoft.jccd.detectors.ASTDetector
import org.eposoft.jccd.detectors.ASTParseUnit
import org.eposoft.jccd.parser.StandardParserSelector
import org.eposoft.jccd.preprocessors.java.*
import ru.ipccenter.plagiarism.detectors.DetectionResult
import ru.ipccenter.plagiarism.detectors.Detector
import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author kholodilov
 */
class JCCDDetector implements Detector
{

    private ASTDetector detector

    JCCDDetector()
    {
        detector = new ASTDetector()

        detector.addOperator(new GeneralizeMethodDeclarationNames());
        detector.addOperator(new GeneralizeVariableNames());
        detector.addOperator(new CompleteToBlock());
        detector.addOperator(new GeneralizeMethodArgumentTypes());
        detector.addOperator(new GeneralizeMethodReturnTypes());
        detector.addOperator(new GeneralizeVariableDeclarationTypes());
        detector.addOperator(new GeneralizeClassDeclarationNames());
        detector.addOperator(new NumberLiteralToDouble());
        detector.addOperator(new AcceptFileNames());
        detector.addOperator(new GeneralizeMethodCallNames());
        detector.addOperator(new AcceptStringLiterals());
        detector.addOperator(new RemoveAnnotations());
        detector.addOperator(new AcceptLogicalOperators());
        detector.addOperator(new RemoveImports());
        detector.addOperator(new TokenLengthAnnotator(0));
    }

    @Override
    DetectionResult performDetection(SolutionsPair pair)
    {
        def file1 = new JCCDFile(pair.solution1.file)
        def file2 = new JCCDFile(pair.solution2.file)
        detector.sourceFiles = [file1, file2];

        SimilarityGroupManager similarityGroupManager = detector.process()
        def filteredSimilarityGroups = similarityGroupManager.similarityGroups.findAll { similarityGroup ->
            def filenames = similarityGroup.nodes.collect { getFileNameForNode(it) }
            return filenames.findAll { filenames[0].equals(it) }.size() < filenames.size()
        }

        def file1Tree = getTree(file1)
        def file2Tree = getTree(file2)

        int totalTokens = file1Tree.annotations.get(TokenLengthAnnotator.class).get("tokens")
        int cloneTokens = filteredSimilarityGroups.collect { similarityGroup ->
                    similarityGroup.nodes
                            .findAll { file1.nameWithoutPrefix.equals(getFileNameForNode(it)) }
                            .collect { it.annotations.get(TokenLengthAnnotator.class).get("tokens") }
                            .sum()
                }
                .sum(0)

        def similarity = cloneTokens / totalTokens
        def report = generateReport(filteredSimilarityGroups, file1, file1Tree, file2, file2Tree)
        return new DetectionResult(pair, similarity, report)
    }

    private String generateReport(
            final List<SimilarityGroup> simGroups, JCCDFile file1, ANode file1Tree, JCCDFile file2, ANode file2Tree) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);

        //def file1Tree = getOriginalTreeWithTokenCounts(file1)
        //def file2Tree = getOriginalTreeWithTokenCounts(file2)

        if (simGroups != null && simGroups.size() > 0) {
            for (int i = 0; i < simGroups.size(); i++) {
                final ASourceUnit[] nodes = simGroups[i].getNodes();
                out.println("");
                out.println("Similarity Group "
                        + simGroups[i].getGroupId());
                out.print("========================================");
                out.println("========================================");
                for (int j = 0; j < nodes.length; j++) {
                    final SourceUnitPosition minPos = APipeline.getFirstNodePosition((ANode) nodes[j]);
                    final SourceUnitPosition maxPos = APipeline.getLastNodePosition((ANode) nodes[j]);

                    def filename = getFileNameForNode(nodes[j])

                    out.print(filename);
                    out.print(" (");

                    out.print(minPos.getLine());
                    out.print(".");
                    out.print(minPos.getCharacter());
                    out.print(" - ");

                    out.print(maxPos.getLine());
                    out.print(".");
                    out.print(maxPos.getCharacter());
                    out.println(")");

/*                    def nodeIdentifier = nodes[j].getNodeIdentifier()
                    out.println(nodeIdentifier)
                    out.println("Processed subtree subtree:")
                    printTree(out, nodes[j], 0)
                    out.println("Original subtree:")
                    if (file1.nameWithoutPrefix.equals(filename))
                    {
                        printTree(out, findNodeByIdentifier(file1Tree, nodeIdentifier), 0)
                    }
                    else if (file2.nameWithoutPrefix.equals(filename))
                    {
                        printTree(out, findNodeByIdentifier(file2Tree, nodeIdentifier), 0)
                    }
                    else
                    {
                        out.println("failed to match file ${filename} with one of source files ${file1.nameWithoutPrefix}, ${file2.nameWithoutPrefix}")
                    }*/
                }
                out.print("========================================");
                out.println("========================================");
            }
        } else {
            out.println("No similar nodes found.");
        }

        out.println()
        printFileWithLineNumbers(out, file1)
        out.println("###Tree:")
        printTree(out, file1Tree, 0)

        out.println()
        printFileWithLineNumbers(out, file2)
        out.println("###Tree:")
        printTree(out, file2Tree, 0)

        return outputStream.toString()
    }

    private ANode findNodeByIdentifier(ANode file1Tree, String nodeIdentifier)
    {
        def path = nodeIdentifier.split("\\|") as List<String>
        path.remove(0)
        ANode currenNode = file1Tree
        path.each { index ->
            currenNode = currenNode.getChild(Integer.parseInt(index))
        }
        return currenNode
    }

    private getFileNameForNode(ANode node)
    {
        while (node.getType() != NodeTypes.FILE.getType()) {
            node = node.getParent();
        }
        return node.text
    }

    private printFileWithLineNumbers(PrintStream out, JCCDFile jccdFile)
    {
        def origFile = jccdFile.file
        out.println "${origFile.absolutePath}:"
        origFile.eachLine { line, lineNumber ->
            out.println "${lineNumber}: ${line}"
        }
    }

    private ANode getOriginalTreeWithTokenCounts(JCCDFile jccdFile)
    {
        def parser = new ASTParseUnit(new StandardParserSelector());
        def sourceUnits = parser.parse(jccdFile)
        sourceUnits.preprocess(new TokenLengthAnnotator(0))
        return sourceUnits.root
    }

    private ANode getTree(JCCDFile jccdFile)
    {
        def sourceUnits = detector.parse(jccdFile)
        sourceUnits.preprocess(detector.getPreprocessors())
        return sourceUnits.root
    }

    private static void printTree(PrintStream out, ANode root, int level)
    {
        out.println("  " * level + root.text +
                " (directChilds: " + root.childCount + ", " +
                root.annotations.get(TokenLengthAnnotator.class) +
                ")")
        for (int i = 0; i < root.childCount; i++)
        {
            printTree(out, root.getChild(i), level + 1)
        }
    }
}
