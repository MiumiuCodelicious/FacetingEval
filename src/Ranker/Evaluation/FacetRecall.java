package Ranker.Evaluation;

import java.util.HashMap;

/**
 * @author Jewel Li on 15-5-17. ivanka@udel.edu
 */
public class FacetRecall {


    /**
     * Measure the facet recall at top K for 1 facet value
     *
     * @param facet_rankedlist    the ranked documents of a facet value
     * @param original_rankedlist the original ranked documents
     * @param topK                top K documents to consider
     * @return recall of the facet ranked list
     */
    public static float facetRecall(String[][] facet_rankedlist, String[][] original_rankedlist, int topK) {
        int sum = 0, facet_sum = 0;
        for (int i = 0; i < original_rankedlist.length; i++) {
            if (Integer.parseInt(original_rankedlist[i][2]) > 0) {
                sum += 1;
            }
        }
        if (sum <= 0) {
            System.out.println("No relevant document for this query. Recall should be 0.");
            return 0.0f;
        }

        int bound = Math.min(topK, facet_rankedlist.length);
        for (int i = 0; i < bound; i++) {
            if (Integer.parseInt(facet_rankedlist[i][2]) > 0) {
                facet_sum += 1;
            }
        }
        return (float) facet_sum / (float) sum;
    }


    /**
     * Measure the total facet recall at top K for a set of facet values
     *
     * @param facet_rankedlist    the ranked documents of a facet value
     * @param original_rankedlist the original ranked documents
     * @param topK                top K documents to consider
     * @return recall of the facet ranked list
     */
    public static float facetRecall(String[][][] facet_rankedlist, String[][] original_rankedlist, int topK) {

        int sum = 0;
        for (int i = 0; i < original_rankedlist.length; i++) {
            if (Integer.parseInt(original_rankedlist[i][2]) > 0) {
                sum += 1;
            }
        }
        if (sum <= 0) {
            System.out.println("No relevant document for this query. Recall should be 0.");
            return 0.0f;
        }

        HashMap<String, Integer> facet_docs = new HashMap<String, Integer>();
        for (int facet = 0; facet < facet_rankedlist.length; facet ++ ) {
            for (int doc = 0; doc < facet_rankedlist[facet].length; doc++) {
                if ( Integer.parseInt( facet_rankedlist[facet][doc][2] ) > 0  && !facet_docs.containsKey(facet_rankedlist[facet][doc][0]) ) {
                    facet_docs.put(facet_rankedlist[facet][doc][0], 1);
                }
            }
        }

        return (float)facet_docs.size()/(float)sum;

    }


}