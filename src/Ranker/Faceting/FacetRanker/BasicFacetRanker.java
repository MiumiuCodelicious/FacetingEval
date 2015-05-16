package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jewel Li on 15-5-15. ivanka@udel.edu
 */
public class BasicFacetRanker implements FacetRankerInterface{

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


    /**
     * Given the original list of documents, return a ranked list of facet values
     * @param originalDocIDs    a 2D String array of ranked documents.
     *                          Outer layer is each document. Inner layer consists of: docID, score, relevance.
     * @return      List<Map.Entry<String, Float>> sorted List of Map.Entry: facet value and with its score.
     */
    public List<Map.Entry<String, Float>> rankFacets(String queryFieldValue, String[][] originalDocIDs){

    }


    /**
     * Given a specific facet value, return the documents covered by this facet value.
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    public String[][] reRankDocs(String[][] docIDs, String facetvalue){

    }


    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    public int pullFacets(String[] docIDs) {
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





}
