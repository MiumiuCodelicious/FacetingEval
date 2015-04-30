package Ranker.Faceting;

import Indexer.InverseIndex;

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
     * p(d) = 1/r(d)
     * p(d) = 1/log(r(d))
     * p(d) = 1/d^2
     * @param length    length of the list of ranked documents to estimate Zipfian distribution about.
     * @return  a float[] array containing a probability for each document in the given docIDs.
     */
    public float[] zipfianDist(int length){
        if (length < 0) return null;
        float[] dist = new float[length];
        for (int docind = 0; docind < length; docind ++) {
            dist[docind] = 1.0f / (float)(Math.log(docind)/Math.log(2));
        }
        return dist;
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
                if (FacetStats.contains(facetIndex.getDocByIndex(doc), docIDs) && postinglist.get(doc) > 0) {
                    total++;
                    hasdoc = true;
                }
            }
            if (hasdoc == true) {
                subFacetCover.put(facetname, total);
            }
        }
    }


    public static void main (String[] args){

    }

}
