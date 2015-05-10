package Ranker.Evaluation;

import Ranker.Faceting.FacetRanker;

import java.util.List;
import java.util.Map;

/**
 * @author Jewel Li on 15-5-8. ivanka@udel.edu
 *
 * For each query and a given list of ranked facets, evaluate the Facet Discounted Cumulative Gain from the facet value ranking.
 *
 * For each facet value, the facet ranked list is to compare with the original ranked list.
 * A parameter alpha controls how much "lost" relevance documents matter.
 *
 * From what I can see now, "lost" documents should be given less consideration. New pulled up documents should be weighted higher.
 *
 * When alpha = 1, the Normalized Gain is simply the nDCG difference.
 *
 */
public class FacetDiscountedCumulativeGain {


    /**
     * Given a list of ranked facets
     * @param rankedFacets
     * @param original_rankedlist
     * @param topK
     * @return
     */
    public static float[] facetDCG(List<Map.Entry<String, Float>> rankedFacets, String[][] original_rankedlist, int topK){


        return null;
    }


    /**
     * For a single facet value, given the original ranked list, calculate the Gain from choosing this facet value at topK.
     * @param facet_rankedlist      a 2D String array of array for facet document ranking.
     * @param original_rankedlist   a 2D String array of array for original document ranking.
     *                              Outer elements are each document record, inner elements are: docID, score, relevance.
     * Example ranked list of documents follow this format:
     * --------------------------------+
     * DocID1   5.81916274628  1       |
     * DocID2   5.36491699619  2       |
     * ...                             |
     * --------------------------------+
     *
     * @param topK      the top K facet Gain to cut-off
     * @return       a float array of size topK, containing the facet Gain @1-topK.
     */
    public static float[] facetGain (String[][] facet_rankedlist, String[][] original_rankedlist, int topK){
        float[] facet_gain = new float[topK];

        String[] facet_docIDs = Utility.TemplateFunctions.fetchColumn(facet_rankedlist, 0);
        String[] original_docIDs = Utility.TemplateFunctions.fetchColumn(original_rankedlist, 0);

        int facet_rank = -1, original_rank = -1;
        for (String doc : facet_docIDs){
            facet_rank ++;
            original_rank = -1;

            // Gain
            if ( (original_rank = Utility.TemplateFunctions.contains(doc, original_docIDs) ) >= 0){
                facet_rank - original_rank
            }

        }

        return facet_gain;
    }

}
