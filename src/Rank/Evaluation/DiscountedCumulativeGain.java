package Rank.Evaluation;

/**
 * @author Jewel Li on 15-4-27. ivanka@udel.edu
 * Given a list of ranked documents with their ID, score, and relevance judgement.
 * Evaluate the Discounted Cunulative Gain of this list.
 *
 * Only static classes are provided in this class. Create an instance once and for all evaluations.
 */
public class DiscountedCumulativeGain {

    /**
     * Given a ranked list of documents following this format:
     * --------------------------------+
     * Qid: 2.3.3                      |
     * DocID1   5.81916274628  1       |
     * DocID2   5.36491699619  2       |
     * ...                             |
     * --------------------------------+
     *
     * @param rankedlist   an array of String array. Outer rows are each documents, inner rows are the doc ID, score, and relevance judgement.
     * @param topK      an integer indicating the top K documents to evaluate
     * @return  a float[] array containing CG@K from 0 to topK
     */
    public static float[] CG(String[][] rankedlist, int topK){
        if (rankedlist == null || rankedlist.length < 1 || topK < 0 ){
            return null;
        }
        
        return null;
    }


}
