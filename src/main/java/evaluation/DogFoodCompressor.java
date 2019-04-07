package evaluation;

import Util.RDFTurtleConverter;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.base.Sys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DogFoodCompressor {

    public static void main(String[] args) {


        List<CompressionResult> results = new ArrayList<>();
        CompressionStarter hdtStarter = new GraphRePairStarter();

        List<File> files = Util.Util.getAllFileRecursively("", null);//TODO passt nicht
        for (File file : files) {
            if (file != null)
                results.add(hdtStarter.compress(file.getAbsolutePath(), file.getName() + ".hdt", true));
        }

//        Collections.sort(results);

        for (CompressionResult result : results) {
            if (result != null)
                System.out.print(result.getCompressionRatio() + ", ");
        }
    }
}
