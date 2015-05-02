package Ranker.Faceting;

import Indexer.InverseIndex;
import Utility.Options;


import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Jewel Li on 15-4-29. ivanka@udel.edu
 *
 * Algorithms to rank facet values in a single facet field, given a ranked list of documents for a query.
 */

public class FacetRanker {


    private InverseIndex facetIndex;

    private HashMap<String, Integer> facetMap;

    private HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();

    private float[] zipf;

    private static int topK = 10;


    public FacetRanker(InverseIndex index){
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
        String[] ids = fetchColumn(original_docIDs, 0);

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
     * Given a 2D String array, fetch column using column_no.
     *
     * @param input_matrix  the 2D matrix
     * @param column_no     column number to retrieve, starting from 0
     * @param <T>           generic type
     * @return              the wanted row as an anrray
     */
    public static <T> T[] fetchColumn(T[][] input_matrix, int column_no){
        if ( input_matrix.length < 1 ){
            return null;
        }
        if ( column_no > input_matrix[0].length - 1 ){
            return null;
        }
        T[] col = (T[]) Array.newInstance(input_matrix.getClass().getComponentType().getComponentType(), input_matrix.length);
        int rowno = 0;
        for (T[] row : input_matrix){
            col[rowno] = row[column_no];
            rowno ++;
        }
        return col;
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
        String[] docs = fetchColumn(docIDs, 0);
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

        String[] original_docs = fetchColumn(original_DocIDs, 0);
        String[] reRanked_docs = fetchColumn(reRankedDocs, 0);
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
     * Given the original list of documents, return a ranked list of facet values
     * @param originalDocIDs    a 2D String array of ranked documents.
     *                          Outer layer is each document. Inner layer consists of: docID, score, relevance.
     * @return      List<Map.Entry<String, Float>> sorted List of Map.Entry: facet value and with its score.
     */
    public List<Map.Entry<String, Float>> rankFacets(String[][] originalDocIDs){
        String[] docs = fetchColumn(originalDocIDs, 0);

        // pull facets into class variable: HashMap<String, Integer> subFacetCover
        if ( pullFacets(docs) < 1 )    return null;

        // set values in class variable: float[] zipf
        if ( zipfianDist(docs) < 1 )   return null;

        HashMap<String, Float> facetExpPromo = new HashMap<String, Float>();

        for ( String facetvalue : subFacetCover.keySet() ) {
            facetExpPromo.put( facetvalue, expectedPromo(originalDocIDs, facetvalue) );
        }
        return sortMapComparator(facetExpPromo, false);

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
