package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Ranker.Faceting.FacetStats;
import Utility.Options;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jewel Li on 15-4-29. ivanka@udel.edu
 *
 * Algorithms to rank facet values in a single facet field, given a ranked list of documents for a query.
 */

public class FacetUtilityRanker implements FacetRankerInterface {


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
    protected HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();

    /**
     * Given a query, there is only one zipfian distribution based on the query's original document ranking.
     */
    private float[] zipf;

    private static int topK = 10;


    public FacetUtilityRanker(InverseIndex index){
        if (index != null) {

            facetIndex = index;
            facetMap = index.getWordMap();
        }

    }


    /**
     * For the ranked list of documents given 1 query, estimate an Zipfian distribution.
     * This function will set values in the class variable zipf, which is a float[] array
     * containing a probability for each document in the original_docs.
     *
     * @param original_docs    the original ranked list of documents.
     * @return if successfully runs, return 1; otherwise, 0.
     */


    private int zipfianDist(String[] original_docs){
        if ( original_docs == null ) return 0;

        zipf = new float[original_docs.length];
        float norm = 0.0f;

        for (int r = 0; r < original_docs.length; r ++) {
            zipf[r] = (float)(Math.pow(r+1, -2));
            norm += zipf[r];
        }
        for (int r = 0; r < original_docs.length; r ++) {
            zipf[r] /= norm;
        }
        return 1;
    }


    /**
     * Given a list of re-ranked documents by a facet value, get the zipfian distribution of
     * documents in reRanked_docs.
     * @param original_docs     the original ranked list of documents.
     * @param reRanked_docs     the re-ranked list of documents of a facet value.
     * @return
     */
    public float[] getZipf(String[] original_docs, String[] reRanked_docs){

        if ( reRanked_docs == null ) return null;

        float[] return_dist = new float[reRanked_docs.length];

        for (int pos = 0; pos < reRanked_docs.length; pos ++){
            int original_pos = Utility.TemplateFunctions.contains(reRanked_docs[pos], original_docs);
            if ( original_pos >= 0 ){
                return_dist[pos] = zipf[original_pos];
            }
        }
        return return_dist;
    }


    /**
     * Given the original ranked lists of documents along with each document's score and relevance
     * Calculate the rank promotion for each document in the ranked list after a particular facet.
     * @see FacetStats use static template function: public static <T> int contains(T ele, T[] list)
     * @param original_docIDs   the original ranked list of documents
     * @param facet_docIDs      the new ranked list of documents suppose a facet value is chosen
     * --------------------------------+
     * DocID1   5.81916274628  1       |
     * DocID2   5.36491699619  2       |
     * ...                             |
     * --------------------------------+
     * @return an integer array with the same length as the original ranked documents. If new ranked list
     *         does not contain a document, the promotion is negative original rank.
     */
    public int[] rankPromotion(String[][] original_docIDs, String[][] facet_docIDs){

        int[] promo = new int[facet_docIDs.length];

        /* Get a list of doc IDs from the new ranked list */
        String[] ids = Utility.TemplateFunctions.fetchColumn(original_docIDs, 0);

        for (int newr = 0; newr < facet_docIDs.length; newr ++){

            /* if original ids comtain the new id */
            int oldr = Utility.TemplateFunctions.contains(facet_docIDs[newr][0], ids);

            /* If old rank < -1, document lost in new ranked list */
            if ( oldr > -1 ){
                promo[newr] = oldr - newr;
            }
        }
        return promo;
    }




    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    private int pullFacets(String[] docIDs) {
        subFacetCover.clear();
        if (facetMap == null) {
            System.out.println("Error: No facets indexed.");
            return 0;
        }
        for (String facetname : facetMap.keySet()) {
            int total = 0;
            boolean hasdoc = false;
            ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetname);
            for (int doc = 0; doc < postinglist.size(); doc ++) {
                if ( Utility.TemplateFunctions.contains(facetIndex.getDocByIndex(doc), docIDs) >=0 && postinglist.get(doc) > 0) {
                    total++;
                    hasdoc = true;
                }
            }
            if (hasdoc == true) {
                subFacetCover.put(facetname, total);
            }
        }
        return 1;
    }


    /**
     * Given a specific facet value, re-rank
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    public String[][] reRankDocs(String[][] docIDs, String facetvalue){
        String[] docs = Utility.TemplateFunctions.fetchColumn(docIDs, 0);
        if (docIDs == null || facetIndex.getPostinglist(facetvalue) == null){
            return null;
        }
        /* Get the number of documents covered in the given facet value */
        String[][] reRankedDocs;
        if ( subFacetCover != null && subFacetCover.containsKey(facetvalue) ){
            reRankedDocs = new String[ subFacetCover.get(facetvalue).intValue() ] [ docIDs[0].length ] ;
        }else{
            return null;
        }

        /* If a document belonging to a facet value is found in the given ranked list
         *  */
        int j = 0;
        ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetvalue);
        for (int i = 0; i < docIDs.length; i ++) {
            int doc = facetIndex.getIndexOfDoc(docIDs[i][0]);
            if ( postinglist.get(doc) > 0 ) {
                reRankedDocs[j] = docIDs[i];
                j ++;
            }
        }
        return reRankedDocs;

    }


    /**
     * Given an original ranked list of documents and a chosen facet value, create the new ranked list of documents covered by this facet value.
     * @param original_DocIDs    a 2D String array. Outer elements are each document, inner elements are: docID, score, relevance.
     * @param facetvalue    the chosen facet value
     * @return      a float number of Expected Promotion for the chosen facet value.
     */
    public float expectedPromo(String[][] original_DocIDs, String facetvalue){
        float EP = 0.0f;

        String[][] reRankedDocs = reRankDocs(original_DocIDs, facetvalue);

        String[] original_docs = Utility.TemplateFunctions.fetchColumn(original_DocIDs, 0);
        String[] reRanked_docs = Utility.TemplateFunctions.fetchColumn(reRankedDocs, 0);
        float[] prob = getZipf(original_docs, reRanked_docs);

        int[] promo = rankPromotion(original_DocIDs, reRankedDocs);

        for (int r = 0; r < reRankedDocs.length; r ++){
            EP += prob[r] * promo[r];

            if (Options.DEBUG == true) {
                System.out.println("Original ranked list length = " + original_DocIDs.length + ". Facet = " + facetvalue + ". Zipf = " + prob[r] + ". Promo = " + promo[r] + ". Expected promo = " + (prob[r] * promo[r]));
            }

        }
        return EP;
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
     * Write all facet values for the current query. Simply for use of word2vec language model.
     * @param queryfield
     */
    private void writeFacetValue(String queryfield) {
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
    private HashMap<String, Float>  getWord2VecResults(String word2vec_result){

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

        // set values in class variable: float[] zipf
        if ( zipfianDist(docs) < 1 )   return null;

        HashMap<String, Float> facetranked = new HashMap<String, Float>();

        HashMap<String, Float> word2vec = facetRelevance(queryFieldValue);

        for ( String facetvalue : subFacetCover.keySet() ) {

            float score = 0.0f, exp_promo = 0.0f, relevance = 0.0f;

            exp_promo = expectedPromo(originalDocIDs, facetvalue);

            // There is no query field in this case:
            if (word2vec == null){

                score = exp_promo;

            }else if(word2vec.containsKey(facetvalue)){

                relevance = word2vec.get(facetvalue);
                score = relevance * exp_promo;

            }

//            System.out.println("face value: " + facetvalue + ", expected promo = " + exp_promo);
//            System.out.println("face value: " + facetvalue + ", relevance = " + relevance);
//            System.out.println("face value: " + facetvalue + ", score = " + exp_promo * relevance + "\n");

            facetranked.put(facetvalue, relevance);

        }
        return sortMapComparator(facetranked, false);

    }

    /**
     * Need to write a comparator to sort Map by Float Value.
     * @see FacetStats  I already have a comparator to sort HashMap in FacetStats, for Integer Values
     * The reason I cannot have a generic type T for Integer, Float, and Double
     * Because these 3 types are FINAL. Therefore, they can't be exteneded.
     * @param map   HashMap to sort
     * @param asc     if true, ascending; if false, decreasing.
     */
    private List<Map.Entry<String, Float>> sortMapComparator (HashMap<String, Float> map, boolean asc) {

        final boolean ascend = asc;
        if (map == null || map.isEmpty()){  return null;  }

        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(map.entrySet());

        // sort list by value
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> e1, Map.Entry<String, Float> e2) {
                if (ascend == true) {
                    return (e1.getValue()).compareTo(e2.getValue());
                } else {
                    return (e2.getValue()).compareTo(e1.getValue());
                }
            }
        });

        return list;
    }





}
