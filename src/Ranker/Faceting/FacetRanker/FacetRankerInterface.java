package Ranker.Faceting.FacetRanker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jewel Li on 15-5-15. ivanka@udel.edu
 *
 * An interfacet for all kinds of facet value rankers.
 */
public class FacetRanker {

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
    public List<Map.Entry<String, Float>> rankFacets(String queryFieldValue, String[][] originalDocIDs);


    /**
     * Given a specific facet value, return the documents covered by this facet value.
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    public String[][] reRankDocs(String[][] docIDs, String facetvalue);



    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    public int pullFacets(String[] docIDs) ;

}
