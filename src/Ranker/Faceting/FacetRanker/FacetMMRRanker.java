package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Utility.Options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jewel Li on 15-5-16. ivanka@udel.edu
 */
public class FacetMMRRanker extends FacetSimilarityRanker implements FacetRankerInterface {

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


    public FacetMMRRanker(InverseIndex index){
        super(index);
        this.facetIndex = super.facetIndex;
        this.facetMap = super.facetMap;
    }


    /**
     * Inheritance functions:
     */
    protected String processQueryNumericTime(String queryfield){
        return super.processQueryNumericTime(queryfield);
    }

    public HashMap<String, Float> facetRelevance(String queryfield){
        return super.facetRelevance(queryfield);
    }


    protected void writeFacetValue(String queryfield) {
        super.writeFacetValue(queryfield);
    }



    protected HashMap<String, Float>  getWord2VecResults(String word2vec_result){
        return super.getWord2VecResults(word2vec_result);
    }


    public int pullFacets(String[] docIDs) {
        int result = super.pullFacets(docIDs);
        this.subFacetCover = super.subFacetCover;
        return result;
    }
    public String[][] reRankDocs(String[][] docIDs, String facetvalue){
        return super.reRankDocs(docIDs, facetvalue);
    }


    protected List<Map.Entry<String, Float>> sortMapComparator (HashMap<String, Float> map, boolean asc) {
        return super.sortMapComparator(map, asc);
    }


    public float facetRelevance(String queryfield, String facetvalue) {
        return super.facetRelevance(queryfield, facetvalue);
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
                /**
                 * Find a "Previous" facet value
                 */
                float subtract = facetRelevance(queryFieldValue, facetvalue);
                facetranked.put(facetvalue, relevance - subtract);
            }

            if (Options.DEBUG == true){
                System.out.println("face value: " + facetvalue + ", relevance = " + relevance);
            }
        }

        return sortMapComparator(facetranked, false);


    }



}
