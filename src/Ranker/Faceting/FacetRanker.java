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
     * @param length    length of the list of ranked documents to estimate Zipfian distribution about.
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

        int[] promo = new int[original_docIDs.length];

        /* Get a list of doc IDs from the new ranked list */
        String[] ids = fetchColumn(facet_docIDs, 0);

        for (int oldr = 0; oldr < original_docIDs.length; oldr ++){

            int newr = FacetStats.contains( original_docIDs[oldr][0], ids);
            /* If old rank < -1, document lost in new ranked list */
            if ( oldr > -1 ){
                promo[oldr] = newr - oldr;
            }else{
                promo[oldr] = 0 - oldr;
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




}
