import org.apache.commons.io.FileUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.ml.data.MLData
import org.encog.ml.data.MLDataPair
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.ml.train.MLTrain
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.back.Backpropagation
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.Task
import ru.ipccenter.plagiarism.detectors.impl.JCCDDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository

final TASKS = [
        new Task("array1", "Array3dImpl.java"),
        //new Task("collections2", "WordCounterImpl.java"),
        //new Task("reflection0", "ReflectionsImpl.java")
]

final DETECTORS = [
        "plaggie" : new PlaggieDetector(11),
        "jccd" : new JCCDDetector()
]

final int NUMBER_INTERVALS = 5
final int MAXIMUM_SIMILARITY_DEGREE = NUMBER_INTERVALS - 1

def dataDirectoryPath = args[0]
def work_directory = new File(dataDirectoryPath)
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

final ManualChecksSolutionsPairRepository repository = new ManualChecksSolutionsPairRepository(solutionRepository, manual_checks_directory, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)
def task_solution_pairs = repository.loadSolutionsPairs(TASKS)

task_solution_pairs.each { task, solution_pairs ->
    println "Processing ${task}"

    def task_results_directory = new File(comparison_results_directory, task.name)
    task_results_directory.mkdirs()

    solution_pairs.each{ pair ->
        DETECTORS.each { detectorName, detector ->
            def detectionResult = detector.performDetection(pair)
            pair.addDetectionResult(detectorName, detectionResult)

            new File(task_results_directory,
                    "${pair.solution1.author.name}_${pair.solution2.author.name}_${detectorName}.txt")
            .withOutputStream { out ->
                out << detectionResult.report
            }
            println task.name + " " + pair.solution1.author.name + "_" + pair.solution2.author.name +
                    " " + detectionResult.similarity
        }

    }

    BasicNetwork network = new BasicNetwork();
    final INPUT_NEURONS = 2
    final INPUT_LAYER = 0
    network.addLayer(new BasicLayer(null,false, INPUT_NEURONS));
    final HIDDEN_NEURONS = 3
    final HIDDEN_LAYER = 1
    network.addLayer(new BasicLayer(new ActivationSigmoid(),true, HIDDEN_NEURONS));
    final OUTPUT_NEURONS = 1
    final OUTPUT_LAYER = 2
    network.addLayer(new BasicLayer(new ActivationSigmoid(),true, OUTPUT_NEURONS));

    network.getStructure().finalizeStructure();

    (0..INPUT_NEURONS-1).each { inputNeuron ->
        (0..HIDDEN_NEURONS-1).each { hiddenNeuron ->
            network.setWeight(INPUT_LAYER, inputNeuron, hiddenNeuron, 0.5)
        }
    }

    (0..HIDDEN_NEURONS-1).each { hiddenNeuron ->
        (0..OUTPUT_NEURONS-1).each { outputNeuron ->
            network.setWeight(HIDDEN_LAYER, hiddenNeuron, outputNeuron, 1.0 / 3)
        }
    }

    println network.dumpWeights()

    MLDataSet trainingSet = new BasicMLDataSet(
            solution_pairs.collect { solutionsPair ->
                [solutionsPair.detectionResults["plaggie"].similarity, solutionsPair.detectionResults["jccd"].similarity]
            } as double[][],
            solution_pairs.collect { solutionsPair ->
                [solutionsPair.estimatedSimilarity]
            } as double[][],
    );

    final MLTrain train = new Backpropagation(network, trainingSet);

    int epoch = 1;

    while(true) {
        train.iteration();
        println("Epoch #" + epoch + " Error:" + train.getError());
        epoch++;
        if (train.getError() < 0.05) break;
    };

    // test the neural network
    def deltas = []
    System.out.println("Neural Network Results:");
    for(MLDataPair pair: trainingSet ) {
        final MLData output = network.compute(pair.getInput());
        System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
        deltas.add(Math.abs(output.getData(0) - pair.getIdeal().getData(0)))
    }

    def statistics = new DescriptiveStatistics(deltas as double[])
    println "${statistics.mean}±${statistics.standardDeviation} (${deltas.size()})"

    println network.dumpWeights()
}
