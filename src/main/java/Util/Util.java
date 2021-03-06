package Util;

import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Util {

    private static final String HTTP_PREFIX_SUBJECT = "http://uni-paderborn.computerscience.de/subject/";
    private static final String HTTP_PREFIX_PREDICATE = "http://uni-paderborn.computerscience.de/predicate/";
    private static final String HTTP_PREFIX_OBJECT = "http://uni-paderborn.computerscience.de/object/";

    private static final int TRIPLE_COMPONENT_LENGTH = 10;

    public static final int TRIPLE_AMOUNT = Integer.MAX_VALUE;

    private static Random r = new Random();

    public static int roundToNearestInteger(double dec) {
        int floor = (int) Math.floor(dec);

        double distanceDown = dec - floor;
        double distanceUp = floor + 1 - dec;

        if (distanceDown < distanceUp) {
            return floor;
        } else {
            return floor + 1;
        }
    }

    public static String fillWithLeadingZeros(String tripleComponent) {
        StringBuilder leadingZeros = new StringBuilder();
        for (int i = 0; i < TRIPLE_COMPONENT_LENGTH - tripleComponent.length(); i++) {
            leadingZeros.append("0");
        }
        return leadingZeros + tripleComponent;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return r.nextInt((max - min) + 1) + min;
    }


    public static void removeIntFromList(List<Integer> list, int intToRemove) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == intToRemove) {
                list.remove(i);
                break;
            }
        }
    }

    public static String getFileNameWithoutSuffix(String file){
        String[] split = file.split("\\.");
        String nameWithoutSuffix = "";
        for (int i = 0; i < split.length-1; i++) {
            nameWithoutSuffix+=split[i];
            if(i<split.length-2){
                nameWithoutSuffix+=".";
            }
        }
        return nameWithoutSuffix;
    }

    public static String getParentDirectory(String file){
        String[] split = file.split("/");
        String parentDir = "";
        for (int i = 0; i < split.length-1; i++) {
            parentDir+=split[i];
            if(i<split.length-2){
                parentDir+="/";
            }
        }
        return parentDir;
    }

    public static List<File> listFilesSorted(String dir) {
        List<File> files = Arrays.asList(new File(dir).listFiles());
        List<File> filesNew = new ArrayList<>();
        for(int i = 0;i<files.size();i++){
            if(!files.get(i).getName().toLowerCase().contains("ds_store")){
                filesNew.add(files.get(i));
            }
        }
        Collections.sort(filesNew);
        return filesNew;
    }

    public static Model getModelFromFile(String filePath) {
        return getModelFromFile(filePath, 1.0);
    }

    public static Model getModelFromFile(String filePath, int numTriples) {
        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }


        StreamRDFBase destination = new StreamRDFBase();
        RDFDataMgr.parse(destination, filePath);

        Graph g = GraphFactory.createDefaultGraph();
        int count = 0;
        ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
        while (count < numTriples && tripleExtendedIterator.hasNext()) {
            g.add(tripleExtendedIterator.next());
            count++;
        }
        return ModelFactory.createModelForGraph(g);

    }

    public static Model getModelFromFile(String filePath, double percentage) {

        if (percentage <= 0 || percentage > 1) {
            throw new IllegalArgumentException();
        }

        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (percentage == 1) {
            return model;
        } else {
            Graph g = GraphFactory.createDefaultGraph();
            int count = 0;
            int size = model.getGraph().size();
            ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();

            while (1.0 * count / size <= percentage) {
                g.add(tripleExtendedIterator.next());
                count++;
            }
            return ModelFactory.createModelForGraph(g);
        }
    }

    private static class Entity implements Comparable{
        private String uri;
        private int numOccurrences;

        public Entity(String uri, int numOccurrences) {
            this.uri = uri;
            this.numOccurrences = numOccurrences;
        }

        @Override
        public int compareTo(Object o) {
            Entity entity = (Entity)o;
            if(numOccurrences<entity.numOccurrences){
                return -1;
            }
            else if(numOccurrences>entity.numOccurrences){
                return 1;
            }else{
                return 0;
            }
        }

        @Override
        public boolean equals(Object obj) {
            return uri.equals(((Entity)obj).uri);
        }
    }

    public static String getMostFrequentEntityFromGraph(String filePath) {
        FileReader fi=null;
        BufferedReader bufferedReader=null;
        try {
             fi = new FileReader(filePath);
             bufferedReader = new BufferedReader(fi);

            String line = bufferedReader.readLine();
            List<Entity> entities = new ArrayList<>();
            while (line != null) {
                    String[] parts = line.split(" ");
                    if (parts.length > 0 && parts[0].contains("<") && parts[0].contains(":")) {
                        String strEntity = parts[0];
                        strEntity = strEntity.replaceAll("<", "");
                        strEntity = strEntity.replaceAll(">", "");

                        Entity entity = new Entity(strEntity,1);

                        boolean found = false;
                        for (int i = 0; i < entities.size(); i++) {
                            if(entities.get(i).equals(entity)){
                                found = true;
                                entities.get(i).numOccurrences++;
                            }
                        }
                        if(!found){
                            entities.add(entity);
                        }

                    }
                line = bufferedReader.readLine();
            }

            Collections.sort(entities);

            int index = (int) Math.floor(entities.size()*0.8);
            return entities.get(index).uri;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("no entity found in "+filePath);
    }

    public static void createEntityBasedSubGraph(String entity, String fileIn, String fileOut, int transitiveSteps, int triplesLimitPerStep) {
        Set<String> desiredStrings = new LinkedHashSet<>();
        List<String> linesGathered = new ArrayList<>();

        desiredStrings.add(entity);

        for (int i = 0; i < transitiveSteps; i++) {
            List<String> newLines = getLinesContainingStrings(fileIn, desiredStrings, triplesLimitPerStep, linesGathered);
            linesGathered.addAll(newLines);
            desiredStrings.addAll(getSubjectsAndObjectsFromTriples(newLines));
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String line : linesGathered) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        try {
            java.nio.file.Files.write(Paths.get(fileOut), stringBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<String> getLinesContainingStrings(String file, Set<String> desiredStrings, int numTriples, List<String> existingLines) {
        FileReader fi = null;
        BufferedReader bufferedReader = null;
        File tempfile = new File(file + "tempFile.ttl");
        tempfile.delete();

        try {
            fi = new FileReader(file);
            bufferedReader = new BufferedReader(fi);

            String line = bufferedReader.readLine();
            List<String> lines = new ArrayList<>();

            Set<String> existingLinesSet = null;
            if (existingLines != null) {
                existingLinesSet = new HashSet<>(existingLines);
            }


            while (line != null && (lines.size() < numTriples || numTriples == -1)) {

                if (existingLinesSet != null && existingLinesSet.contains(line)) {
                    line = bufferedReader.readLine();
                    continue;
                }

                boolean lineContainsDesiredString = false;
                for (String desiredString : desiredStrings) {
                    if (line.contains(desiredString)) {
                        lineContainsDesiredString = true;
                        break;
                    }
                }

                if (lineContainsDesiredString) {
                    lines.add(line);
                    if (lines.size() % 10 == 0) {
                        System.out.println("Got " + lines.size() + " lines");
                    }
                }

                line = bufferedReader.readLine();
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Set<String> getSubjectsAndObjectsFromTriples(List<String> triples) {
        Set<String> subjectsAndObjects = new LinkedHashSet<>();
        for (String line : triples) {
            String[] components = line.split(" ");
            if (components.length > 4) {
                continue;
            }
            subjectsAndObjects.add(components[0]);
            subjectsAndObjects.add(components[2]);

        }
        return subjectsAndObjects;
    }

    public static Model streamRealSubModelFromFile(String file, int numTriplesWithDesiredStrings, int numTriplesResidual, List<String> desiredStrings) {
        List<String> lines = getLinesContainingStrings(file, new LinkedHashSet<>(desiredStrings), numTriplesWithDesiredStrings, null);
        Set<String> subjectsAndObjects = getSubjectsAndObjectsFromTriples(lines);


        List<String> linesWithSubjectsAndObjects = getLinesContainingStrings(file, subjectsAndObjects, numTriplesResidual, lines);

        Set<String> linesSet = new LinkedHashSet<>(lines);
        linesSet.addAll(linesWithSubjectsAndObjects);

        lines = new ArrayList<>(linesSet);

        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < lines.size() - 1; k++) {
            sb.append(lines.get(k));
            if (k < lines.size() - 2) {
                sb.append("\n");
            }
        }

        File tempfile = new File(file + "_tempSub.ttl");
        try {
            Files.write(sb.toString().getBytes(), tempfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getModelFromFile(tempfile.getAbsolutePath());
    }

    public static boolean isFileInNTriplesFormat(String file){
        FileReader fi = null;
        BufferedReader bufferedReader = null;
        try {

            fi = new FileReader(file);
            bufferedReader= new BufferedReader(fi);

            bufferedReader.readLine();
            String line = bufferedReader.readLine();
            return !line.startsWith("@")&&line.contains("<") && line.contains(">") && line.endsWith(".");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Model streamModelFromFile(String file, int numTriples) {
        FileReader fi = null;
        BufferedReader bufferedReader = null;
        File tempfile = new File(file + "_temp.ttl");
        tempfile.delete();

        try {

            List<String> lines = new ArrayList<>();
            fi = new FileReader(file);
            bufferedReader = new BufferedReader(fi);
            String line = bufferedReader.readLine();
            while (line != null && lines.size() < numTriples) {
                if (!lines.contains(line)) {
                    lines.add(line);
                }
                line = bufferedReader.readLine();
            }

            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < lines.size() - 1; k++) {
                sb.append(lines.get(k));
                if (k < lines.size() - 2) {
                    sb.append("\n");
                }
            }

            Files.write(sb.toString().getBytes(), tempfile);
            Model model = getModelFromFile(tempfile.getAbsolutePath());
            tempfile.delete();
            return model;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bufferedReader.close();
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printModel(Model model) {
        ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
        System.out.println("Model:");
        while (tripleExtendedIterator.hasNext()) {
            System.out.println(tripleExtendedIterator.next());
        }
        System.out.println();

    }

    public static void writeModelToFile(File file, Model model) {
        if (file.exists()) {
            file.delete();
        }
        try {
            model.write(new FileOutputStream(file), "N-TRIPLE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeTriplesToFile(List<Triple> triples, String filePath) {
        StringBuilder sb = new StringBuilder();
        for (Triple triple : triples) {
            sb.append("<" + HTTP_PREFIX_SUBJECT + triple.getSubject() + "> <" + HTTP_PREFIX_PREDICATE + triple.getPredicate() + "> <"
                    + HTTP_PREFIX_OBJECT + triple.getObject() + "> .\n");
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            Files.write(sb.toString().getBytes(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendStringToFile(File file, String string) {
        String s = string + "\n";
        try {
            java.nio.file.Files.write(Paths.get(file.getAbsolutePath()), s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            // will not happen
            e.printStackTrace();
        }
    }

    public static List<File> getAllFileRecursively(String dirPath, String[] allowedSuffices) {
        File dir = new File(dirPath);
        List<File> files = new ArrayList<>();

        List<File> queueDirectories = new ArrayList<>();
        queueDirectories.add(dir);


        while (!queueDirectories.isEmpty()) {
            File currentDir = queueDirectories.remove(0);
            try {
                currentDir.listFiles();
            } catch (NullPointerException e) {
                continue;
            }

            for (File file : currentDir.listFiles()) {
                if (isSuffixAllowed(file.getAbsolutePath(), allowedSuffices)) {
                    try {
                        files.add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    queueDirectories.add(file);
                }
            }
        }

        return files;
    }

    public static boolean isSuffixAllowed(String file, String[] allowedSuffices) {
        for (String allowedSuffix : allowedSuffices) {
            if (file.endsWith(allowedSuffix)) {
                return true;
            }
        }
        return false;
    }

    public static String appendStringToFileName(String fileName, String string) {
        String[] split = fileName.split("\\.");
        String newName = "";
        for (int i = 0; i < split.length - 1; i++) {
            newName += split[i];
            if (i < split.length - 2) {
                newName += ".";
            }
        }
        newName += string + ".";
        newName += split[split.length - 1];
        return newName;
    }

    public static void sendMail(double time) {
        try {

            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", "mail.gmx.net");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.ssl.trust", "mail.gmx.net");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("p.gymheepen@gmx.de", "logitech55");
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("p.gymheepen@gmx.de"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse("philip.frerk@gmail.com"));
            message.setSubject("Computation done");

            String msg = "It took " + time + " hours and finished at " + new Date();

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] agrs) {
//      createEntityBasedSubGraph("http://dbpedia.org/resource/Abraham_Lincoln","mappingbased-properties_en.ttl",
//                "mappingbased-properties_en_lincoln.ttl", 3, 1000);

//        createEntityBasedSubGraph("http://wordnet-rdf.princeton.edu/rdf/lemma/knave#knave-n","wordnet.nt",
//                "wordnet_entity_subgraph.ttl", 3, 1000);

//        createEntityBasedSubGraph("http://wordnet-rdf.princeton.edu/rdf/lemma/know#know-n","wordnet.nt",
//                "wordnet_entity_subgraph2.ttl", 3, 1000);

        createEntityBasedSubGraph("http://dbpedia.org/resource/Idu_Mishmi_language", "/Users/philipfrerk/Documents/RDF_data/DBPedia_2015/instance-types_en.nt",
                "instance-types_en_entitiybased.ttl", 3, 1000);
    }
}
