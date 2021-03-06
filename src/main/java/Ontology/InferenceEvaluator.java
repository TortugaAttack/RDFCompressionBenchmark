package Ontology;

import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class InferenceEvaluator {


    public static EvalResult evaluateDatasetsWithOntology(File[] datasets, File ontology) throws FileNotFoundException {
        List<CompressionResult> compressionResultsOriginal = new ArrayList<>();
        List<CompressionResult> compressionResultsInference = new ArrayList<>();

        Model modelSchema = Util.Util.getModelFromFile(ontology.getAbsolutePath());

        //TODO: soll dict mitgezählt werden? (eig. geht es ja eher um die Struktur)
        final boolean addDicToComprSize = true;

        CompressionStarter grpStarter = new GraphRePairStarter(2);
        for (File file : datasets) {
            // first, compress original file

            final double percentageOfFile = 0.001;

            Model model = Util.Util.getModelFromFile(file.getAbsolutePath(), percentageOfFile);
            File reducedFile = new File("reduced.ttl");
            Util.Util.writeModelToFile(reducedFile, model);

            CompressionResult resultOriginal = grpStarter.compress(reducedFile.getName(), null, addDicToComprSize);
            compressionResultsOriginal.add(resultOriginal);

            // second, build inference model and compress it
            File fileInfModel = new File(reducedFile.getAbsolutePath() + ".inf");
            Model infModel = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), modelSchema, model);

            System.out.println("inference size ratio: " + 1.0 * infModel.getGraph().size() / model.getGraph().size());
            Util.Util.writeModelToFile(fileInfModel, infModel);


            CompressionResult resultInf = grpStarter.compress(fileInfModel.getAbsolutePath(), null, addDicToComprSize);
            compressionResultsInference.add(resultInf);
        }

        // finally, also compress ontology itself
        CompressionResult resultOnt = grpStarter.compress(ontology.getAbsolutePath(), null, addDicToComprSize);
        return new EvalResult(compressionResultsOriginal, compressionResultsInference, resultOnt);
    }


    public static void main(String[] args) {

//        String NS = "urn:x-hp-jena:eg/";
//
//        // Build a trivial example data set
//        Model rdfsExample = ModelFactory.createDefaultModel();
//        Property p = rdfsExample.createProperty(NS, "p");
//        Property q = rdfsExample.createProperty(NS, "q");
//        rdfsExample.add(p, RDFS.subPropertyOf, q);
//        rdfsExample.createResource(NS + "a").addProperty(p, "foo");
//
//        InfModel inf = ModelFactory.createRDFSModel(rdfsExample);
//
//        Model modelSchema = getModelFromFile("/Users/philipfrerk/Documents/RDF_data/DBPedia_2015/dbpedia_2015-04.owl");
//        Model modelModel = getModelFromFile("/Users/philipfrerk/Documents/RDF_data/DBPedia_2015/geo-coordinates_en.nt");
//
//        Model infModel =  ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), modelSchema, modelModel);
//
//
//
////        List<Triple> triples = getTriples(infModel);
//        System.out.println("schema size: "+modelSchema.size());
//        System.out.println("model size: "+modelModel.size());
//        System.out.println("infmodel size: "+infModel.size());


        try {
            EvalResult evalResult = evaluateDatasetsWithOntology(new File[]{new File("geo-coordinates_en.nt")},
                    new File("dbpedia_2015-04.owl"));
            for (CompressionResult compressionResultOrig : evalResult.compressionResultsOriginal) {
                System.out.print(compressionResultOrig.getCompressionRatio() + ",");
            }

            System.out.println();
            for (CompressionResult compressionResultInf : evalResult.compressionResultsInference) {
                System.out.print(compressionResultInf.getCompressionRatio() + ",");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class EvalResult {
        private List<CompressionResult> compressionResultsOriginal;
        private List<CompressionResult> compressionResultsInference;
        private CompressionResult ontologyResult;

        public EvalResult(List<CompressionResult> compressionResultsOriginal, List<CompressionResult> compressionResultsInference, CompressionResult ontologyResult) {
            this.compressionResultsOriginal = compressionResultsOriginal;
            this.compressionResultsInference = compressionResultsInference;
            this.ontologyResult = ontologyResult;
        }
    }


}
