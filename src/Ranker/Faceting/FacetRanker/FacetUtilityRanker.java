package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Ranker.Faceting.FacetStats;
import Utility.Options;


import java.util.*;

/**
 * @author Jewel Li on 15-4-29. ivanka@udel.edu
 *
 * Algorithms to rank facet values in a single facet field, given a ranked list of documents for a query.
 */

public class FacetUtilityRanker extends BasicFacetRanker implements FacetRankerInterface {


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
     * Private instances of 2 facet rankers: FacetSimilarityRanker, and FacetExpectedPromoRanker
     */
    private FacetSimilarityRanker similarityRanker;

    private FacetExpectedPromoRanker expectedPromoRanker;

    /**
     * Given a query, there is only one zipfian distribution based on the query's original document ranking.
     */
    private float[] zipf;

    public FacetUtilityRanker(InverseIndex index){
        super(index);

        this.facetIndex = super.facetIndex;
        this.facetMap = super.facetMap;

        similarityRanker  = new FacetSimilarityRanker(index);
        expectedPromoRanker = new FacetExpectedPromoRanker(index);
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
        if ( pullFacets(docs) < 1 )    { return null;}
        if (similarityRanker.pullFacets(docs) < 1) {return null;}
        if (expectedPromoRanker.pullFacets(docs) < 1) {return null;}

        // set values in class variable: float[] zipf
        if ( expectedPromoRanker.zipfianDist(docs) < 1 )   return null;
        else    this.zipf = expectedPromoRanker.zipf;

        HashMap<String, Float> facetranked = new HashMap<String, Float>();

        HashMap<String, Float> word2vec = similarityRanker.facetRelevance(queryFieldValue);

        for ( String facetvalue : subFacetCover.keySet() ) {

            float score = 0.0f, exp_promo = 0.0f, relevance = 0.0f;

            exp_promo = expectedPromoRanker.expectedPromo(originalDocIDs, facetvalue);

            // There is no query field in this case:
            if (word2vec == null){

                score = exp_promo;

            }else if(word2vec.containsKey(facetvalue)){

                relevance = word2vec.get(facetvalue);
                score = relevance * exp_promo;

            }

            if (Options.DEBUG == true) {
                System.out.println("face value: " + facetvalue + ", score = " + exp_promo * relevance + "\n");
            }

            facetranked.put(facetvalue, score);

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
