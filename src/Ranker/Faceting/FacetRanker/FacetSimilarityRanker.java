package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Ranker.Faceting.FacetStats;
import Utility.Options;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jewel Li on 15-5-15. ivanka@udel.edu
 *
 * Facet ranker by the similarity between each facet value and the original query component.
 */
public class FacetSimilarityRanker extends BasicFacetRanker implements FacetRankerInterface{

    /**
     * The entire index of facet values.
     * You still need the facetMap class variable, because in facetIndex, facet values are indexed by integer.
     * Facet map actually hold a mapping from the String facet values to its integer index in the facet index.
     */
    protected InverseIndex facetIndex;

    protected HashMap<String, Integer> facetMap;

    /**
     * A container to hold a subset of facet values in a list of documents.
     * The collection of covered facet values are the keys in the hash map, the number of documents associated with each facet value is the value.
     */
    public HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();


    public FacetSimilarityRanker() {
        super();

        this.facetIndex = super.facetIndex;
        this.facetMap = super.facetMap;
    }


    public FacetSimilarityRanker(InverseIndex index){
        super(index);

        this.facetIndex = super.facetIndex;
        this.facetMap = super.facetMap;
    }



    /**
     * Special processing for query time entities. Detect if the query field entity is about years, months, or generally time.
     * @param queryfield
     * @return
     */
    protected String processQueryNumericTime(String queryfield){
        Pattern year = Pattern.compile("(19[0-9][0-9])|(20[0-9][0-9])");
        Pattern decade = Pattern.compile("([0-9]0s)");

        Matcher yearm = year.matcher(queryfield);
        Matcher decadem = decade.matcher(queryfield);

        if( yearm.find() ){
            return "year";
        }else if (decadem.find()){
            return "decade";
        }else {
            return queryfield;
        }

    }


    /**
     * Given a query field value, given a facet value in the corresponding document field, estimate their relevance.
     * This relevance will be used to rank-estimate the probability that a facet value satisfies the information need in a query.
     *
     * Example command lines to call my slightly modified version of word2vec:
     *
     * ./distance_facet vectors.bin "china|japan"
     * ./distance_facet vectors.bin "china_gdp|japan"
     * ./distance_facet vectors.bin "china_population_growth|japan_population_shrink"
     *
     * @param queryfield    a field in query along with the field value. For example, this String could be: "Nissan"
     * @param facetvalue    a facet value in the corresponding field. For example, a document facet value could be: "car"
     * @return
     */
    public float facetRelevance(String queryfield, String facetvalue){

        try{
            String[] command={"./word2vec/distance_facet", "./word2vec/vectors.bin",  queryfield.trim().replaceAll("\\s+", "_") + "|" + facetvalue.trim().replaceAll("\\s+", "_")};

            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            BufferedReader processed_output = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String output = "";
            StringBuilder result = new StringBuilder();

            while ( (output = processed_output.readLine()) != null) {
                result.append(output + "\n");
            }

            return parseWord2VecDistance(result.toString());

        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return -1.0f;
        }


    }



    /**
     * @TODO I am still not sure when query field is empty, whether I should give facet values a default relevance value, or simply return null
     * This is a batch process version to get word2vec scores at once for all facet values associated with a query. A lot faster.
     * @param queryfield   the query field value to rank facet values according to.
     *                     When the query field value is empty, set all facet value similarity to a fixed value such as 0.01f.
     */
    public HashMap<String, Float> facetRelevance(String queryfield){

        /* If query field value is empty, set all facet value similarity to 0.01f. */
        if (queryfield.length() < 1){
//            HashMap<String, Float> facetSim = new HashMap<String, Float>();
//            for (String facetvalue : subFacetCover.keySet()) {
//                facetSim.put(facetvalue, 0.001f);
//            }
//            return facetSim;
            return null;
        }

        String processed_queryfield = processQueryNumericTime(queryfield);

        writeFacetValue(processed_queryfield);
        File facetfile = new File("./word2vec/facetvalues.txt");
        if (!facetfile.exists() || !facetfile.isFile()){
            System.out.println("ERROR: A file containing all facet values for the given query has not been properly created.");
            return null;
        }

        try {

            String[] command = {"./word2vec/distance_facet2", "./word2vec/vectors.bin", "./word2vec/facetvalues.txt"};

            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            BufferedReader processed_output = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String output = "";
            StringBuilder result = new StringBuilder();

            while ((output = processed_output.readLine()) != null) {
                result.append(output + "\n");
            }

            return getWord2VecResults(result.toString());


        }catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }


    }



    /**
     * Write all facet values for the current query. Simply for use of word2vec language model.
     * @param queryfield
     */
    protected void writeFacetValue(String queryfield) {
        try {
            PrintWriter writer = new PrintWriter("./word2vec/facetvalues.txt", "UTF-8");

            for (String facetvalue : subFacetCover.keySet()) {

                writer.write(queryfield.trim().replaceAll("\\s+", "_").toLowerCase() + "|" + facetvalue.trim().replaceAll("\\s+", "_").toLowerCase() + "\n");

            }
            writer.close();


        }catch(IOException ioe){
            ioe.printStackTrace();
        }

    }



    /**
     * Take word2vec similarity between 2 phrases, and parse out the relevance score.
     * @param word2vec_result       word2vec similarity of the distance between 2 phrases: phrase1, phrase2.
     * @return      a relevance score.
     */
    private float parseWord2VecDistance(String word2vec_result){

        for (String line : word2vec_result.split("\n")){
            if (line.startsWith("distance(")){
                /* Sometimes, word2vec do not contain the query word or numbers, similarity score will be produced as nan, aka not a number. */
                if (line.substring(line.indexOf("=") + 1, line.length()).trim().equals("nan")){
                    return 0.001f;
                }
                float relevance = Float.parseFloat( line.substring(line.indexOf("=") + 1, line.length()).trim() );
                return relevance > 0 ? relevance : 0.0f ;
            }
        }
        return 0.0f;
    }


    /**
     * @TODO Normalize all word2vec similarity scores so that the range is 0.0 -1.0 .
     * @param word2vecScore
     */
    protected void word2vecNorm( HashMap<String, Float> word2vecScore ){

    }


    /**
     *
     * @param word2vec_result   parse and get the word2vec similarities for a batch of phrase pairs. This is used for batch process.
     * @return   A HashMap of each facet value and their word2vec similarity score to the original query field value.
     */
    protected HashMap<String, Float>  getWord2VecResults(String word2vec_result){

        HashMap<String, Float> word2vec = new HashMap<String, Float>();

        for (String line : word2vec_result.split("\n")){

            if (line.startsWith("distance(")){
                String phrases = line.substring( line.indexOf("(") + 1, line.indexOf(")")-1 );
                String facetvalue = phrases.split(",")[1].trim().replaceAll("\\s+", " ");

                /* Sometimes, word2vec do not contain the query word or numbers */
                float relevance;
                String score = line.substring(line.indexOf("=") + 1, line.length()).trim();
                if (score.equals("nan")){
                    relevance = 0.01f;
                }else {
                    relevance = Float.parseFloat(score);
                }

                if (subFacetCover.containsKey(facetvalue)){
                    word2vec.put(facetvalue, relevance);
                }else{
                    System.out.println(facetvalue + " is not found in the facet value HashMap! ");
                }

            }

        }
        return word2vec;

    }





    /**
     * Given the original list of documents, return a ranked list of facet values
     * @param originalDocIDs    a 2D String array of ranked documents.
     *                          Outer layer is each document. Inner layer consists of: docID, score, relevance.
     * @return      List<Map.Entry<String, Float>> sorted List of Map.Entry: facet value and with its score.
     */
    public List<Map.Entry<String, Float>> rankFacets(String queryFieldValue, String[][] originalDocIDs){
        String[] docs = Utility.TemplateFunctions.fetchColumn(originalDocIDs, 0);

        // pull facets into class variable: HashMap<String, Integer> subFacetCover
        if ( pullFacets(docs) < 1 )    return null;

        HashMap<String, Float> facetranked = new HashMap<String, Float>();

        HashMap<String, Float> word2vec = facetRelevance(queryFieldValue);

        for ( String facetvalue : subFacetCover.keySet() ) {

            float relevance = 0.0f;

            // There is no query field in this case: back off to basic facet ranker
            if (word2vec == null){
                return super.rankFacets(queryFieldValue, originalDocIDs);
//                facetranked.put(facetvalue, 0.0f);

            }else if(word2vec.containsKey(facetvalue)){
                relevance = word2vec.get(facetvalue);
                facetranked.put(facetvalue, relevance);
            }

            if (Options.DEBUG == true){
                System.out.println("face value: " + facetvalue + ", relevance = " + relevance);
            }
        }

        return sortMapComparator(facetranked, false);
    }






    /**
     * Given a specific facet value, return the documents covered by this facet value.
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    public String[][] reRankDocs(String[][] docIDs, String facetvalue){
        return super.reRankDocs(docIDs, facetvalue);
    }

    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    public int pullFacets(String[] docIDs) {
        int result = super.pullFacets(docIDs);
        this.subFacetCover = super.subFacetCover;
        return result;
    }


    /**
     * Need to write a comparator to sort Map by Float Value.
     * @see FacetStats  I already have a comparator to sort HashMap in FacetStats, for Integer Values
     * The reason I cannot have a generic type T for Integer, Float, and Double
     * Because these 3 types are FINAL. Therefore, they can't be exteneded.
     * @param map   HashMap to sort
     * @param asc     if true, ascending; if false, decreasing.
     */
    protected List<Map.Entry<String, Float>> sortMapComparator (HashMap<String, Float> map, boolean asc) {
        return super.sortMapComparator(map, asc);
    }




}
