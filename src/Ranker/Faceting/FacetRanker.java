package Ranker.Faceting;

import Indexer.InverseIndex;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Jewel Li on 15-4-29. ivanka@udel.edu
 *
 * Algorithms to rank facet values in a single facet field, given a ranked list of documents for a query.
 */

public class FacetRanker {


    private InverseIndex facetIndex;

    private HashMap<String, Integer> docMap;
    private HashMap<String, Integer> facetMap;

    private HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();


    public FacetRanker(InverseIndex index){
        if (index != null) {
            facetIndex = index;
            docMap = index.getDocMap();
            facetMap = index.getWordMap();
        }

    }


    /**
     * Estimate an Zipfian distribution from a given ranked list of documents.
     * @param length    the length of original document list, varies by query.
     * @return  a float[] array containing a probability for each document in the given docIDs.
     */


    public float[] zipfianDist(int length){

        if (length < 0) return null;

        float[] dist = new float[length];
        float norm = 0.0f;

        for (int r = 0; r < length; r ++) {
            dist[r] = 1.0f/(float)(Math.pow(r+1, 0.9));
                norm += dist[r];
        }

        for (int r = 0; r < length; r ++) {
            dist[r] /= norm;
        }
        return dist;
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
            int oldr = FacetStats.contains( facet_docIDs[newr][0], ids);

            /* If old rank < -1, document lost in new ranked list */
            if ( oldr > -1 ){
                promo[newr] = newr - oldr;
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
    private void pullFacets(String[] docIDs) {
        subFacetCover.clear();
        if (facetMap == null) {
            System.out.println("Erorr: No facets indexed.");
            return;
        }
        for (String facetname : facetMap.keySet()) {
            int total = 0;
            boolean hasdoc = false;
            ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetname);
            for (int doc = 0; doc < postinglist.size(); doc ++) {
                if ( FacetStats.contains(facetIndex.getDocByIndex(doc), docIDs) >=0 && postinglist.get(doc) > 0) {
                    total++;
                    hasdoc = true;
                }
            }
            if (hasdoc == true) {
                subFacetCover.put(facetname, total);
            }
        }
    }


    /**
     * Given a specific facet value, re-rank
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    private String[][] reRankDocs(String[][] docIDs, String facetvalue){
        if (docIDs == null || facetIndex.getPostinglist(facetvalue) == null){
            return null;
        }
        /* Pull facets */
        String[][] reRankedDocs;
        String[] docs = fetchColumn(docIDs, 0);
        pullFacets(docs);
        if ( subFacetCover != null && subFacetCover.containsKey(facetvalue) ){
            reRankedDocs = new String[ subFacetCover.get(facetvalue) ] [ docIDs[0].length ] ;
        }else{
            return null;
        }

        for (int i = 0; i < docIDs.length; i ++){
            for (int covered : facetIndex.getPostinglist(facetvalue)){
                if ( docIDs[i][0].equals(  facetIndex.getDocByIndex(covered) )  ) {
                    reRankedDocs[i] = docIDs[i];
                }
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
        float[] prob = zipfianDist(reRankedDocs.length);
        int[] promo = rankPromotion(original_DocIDs, reRankedDocs);

        for (int r = 0; r < reRankedDocs.length; r ++){
            EP += prob[r] * promo[r];
            System.out.println("Expected promo = " + (prob[r] * promo[r]) ) ;
        }
        return EP;
    }



    public void rankFacets(String[][] original_DocIDs){
        
    }




}
