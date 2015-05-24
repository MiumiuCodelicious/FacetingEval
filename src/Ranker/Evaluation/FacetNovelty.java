package Ranker.Evaluation;

/**
 * @author Jewel Li on 15-5-17. ivanka@udel.edu
 */
public class FacetNovelty {


    /**
     * Given the ranked documents of a facet, and the original ranked documents, return the number of new relevant documents brought into top K
     * @param facet_rankedlist      the document ranked list by facet
     * @param original_rankedlist   the original document ranked list
     * @param topK  top K documents
     * @return a number (integer) of new un-seen relevant documents that were originally out of the top K.
     */
    public static int facetNovelty (String[][] facet_rankedlist, String[][] original_rankedlist, int topK) {

        int sum = 0;
        int bound = Math.min(topK, facet_rankedlist.length);
        String[] original_docs = Utility.TemplateFunctions.fetchColumn(original_rankedlist, 0, topK);

        for( int i = 0; i < bound; i++){
            if ( Utility.TemplateFunctions.contains( facet_rankedlist[i][0], original_docs) < 0 && Integer.parseInt( facet_rankedlist[i][2] ) > 0 ){
                sum ++;
            }
         }

        return sum;
    }

}
