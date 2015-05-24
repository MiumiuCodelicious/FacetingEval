package Ranker.Evaluation;

import java.util.Collections;
import java.util.ArrayList;
/**
 * @author Jewel Li on 15-4-27. ivanka@udel.edu
 * Given a list of ranked documents with their ID, score, and relevance judgement.
 * Evaluate the Discounted Cunulative Gain of this list.
 *
 * Only static classes are provided in this class. Create an instance once and for all evaluations.
 *
 * CG = 2^rel - 1
 * DCG = sum_(i)[ CG / (log_2(i+1)) ]
 * nDCG = sum_(q)[ DCG / iDCG ]
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
     * @param rankedlist   an array of String array. Outer rows are each documents, inner rows are the docID, score, and relevance judgement.
     * @param topK      an integer indicating the top K documents to evaluate
     * @return  a float[] array containing DCG@K from 0 to topK
     */
    public static float[] DCG(String[][] rankedlist, int topK){
        if (rankedlist == null || rankedlist.length < 1 || topK < 0 ){
            return null;
        }

        int[] rellist = new int[topK];

        for (int r = 0; r < topK; r ++ ){
            if (r+1 >= rankedlist.length){
                rellist[r] = 0;
                continue;
            }
            rellist[r] = Integer.parseInt( rankedlist[r][2] );
        }
        return dcg(rellist, topK);
    }



    private static float[] dcg(int[] rellist, int topK){

        float[] dcglist = new float[topK];

        for ( int r = 0; r < topK; r ++ ){
            if (r+1 > rellist.length){
                dcglist[r] = dcglist[r-1];
                continue;
            }
            float delta = (float)(Math.pow(2, rellist[r]) -1) / ((float)Math.log(r+2) / (float)Math.log(2));
            if ( r > 0 ) {
                dcglist[r] = dcglist[r - 1] + delta;
            }else{
                dcglist[r] = delta;
            }
        }
        return dcglist;
    }


    /**
     * Ideal DCG of the given ranked list of documents. Outer rows are each documents, inner rows are the docID, score, and relevance judgement.
     * @param rankedlist    an integer indicating the top K documents to evaluate
     * @param topK  a float[] array containing ideal DCG@K from 0 to topK
     * @return
     */
    public static float[] iDCG(String[][] rankedlist, int topK){

        ArrayList<Integer> ideallist = new ArrayList<Integer>();

        int rellist[] = new int[topK];

        for ( int r = 0; r < rankedlist.length; r ++){
            ideallist.add(  Integer.parseInt(rankedlist[r][2]) );
        }
        Collections.sort(ideallist, Collections.reverseOrder());

        int r = 0;
        for (int rel : ideallist ){
            if( r < topK ) {
                rellist[r] = rel;
                r++;
            }else{
                break;
            }
        }
        return dcg(rellist, topK);
    }



    public static float[] nDCG(String[][] rankedlist, int topK){
        float[] nDCG = new float[topK];
        float[] DCG = DCG(rankedlist, topK);
        float[] iDCG = iDCG(rankedlist, topK);

        for ( int r = 0; r < topK; r ++ ){
            if (iDCG[r] > 0) {
                nDCG[r] = DCG[r] / iDCG[r];
            }else{
                nDCG[r] = 0;
            }
        }
        return nDCG;
    }


    /**
     * For facet document ranking evaluation, we still want to normalize to the ideal DCG of the original document ranking.
     * @param facet_rankedlist
     * @param original_rankedlist
     * @param topK
     * @return
     */
    public static float[] nDCGFacet(String[][] facet_rankedlist, String[][] original_rankedlist, int topK){

        float[] nDCG = new float[topK];
        float[] DCG = DCG(facet_rankedlist, topK);
        float[] iDCG = iDCG(original_rankedlist, topK);

        for ( int r = 0; r < topK; r ++ ){
            if (iDCG[r] > 0) {
                nDCG[r] = DCG[r] / iDCG[r];
            }else{
                nDCG[r] = 0;
            }
        }
        return nDCG;
    }



    /**
     * Measure the recall at top K for a given ranked list of documents
     * @param rankedlist   a ranked list of documents
     * @param topK  top K documents to consider
     * @return  recall of the ranked list
     */
    public static float recall(String[][] rankedlist, int topK){
        int sum = 0, top_sum = 0;
        for (int i = 0; i < rankedlist.length; i++){
            if( Integer.parseInt( rankedlist[i][2] ) > 0 ){
                sum += 1;
            }
        }
        if( sum <= 0 ){
            System.out.println("No relevant document for this query. Recall should be 0.");
            return 0.0f;
        }

        int bound = Math.min(topK, rankedlist.length);
        for(int i = 0; i < bound; i ++){
            if ( Integer.parseInt( rankedlist[i][2] ) > 0){
                top_sum += 1;
            }
        }
        return (float)top_sum/(float)sum;
    }





    /**
     * If no top K is specified, get the Cumulative Gain of the entire ranked list.
     * @param rankedlist     an array of String array. Outer rows are each documents, inner rows are the doc ID, score, and relevance judgement.
     * @return  a floating number DCG of the entire ranked list.
     */
    public float DCG(String[][] rankedlist){
        return DCG(rankedlist, rankedlist.length)[rankedlist.length-1];
    }


    public static void main(String[] args){
        String current = System.getProperty("user.dir");
        Ranker.ReadRankedResult mixtureModel = new Ranker.ReadRankedResult( current + "/src/Var/mixture_model_6_ranked_result.txt", current + "/src/Var/QueryMap.txt") ;

        DiscountedCumulativeGain dcg = new DiscountedCumulativeGain();

        for ( String Qid : mixtureModel.getQueryMap().keySet() ){
            System.out.println("Qid " + Qid + ": " + mixtureModel.getQueryMap().get(Qid));
            int topK = 10;
            for (int i = 0; i < topK; i ++){
                System.out.print( mixtureModel.getResult(Qid)[i][2] + ", ") ;
            }
            System.out.println("\n");
            for ( float v :  dcg.nDCG( mixtureModel.getResult(Qid), topK ) ) {
                System.out.print(v + ", ");
            }
            System.out.println("\nDCG:");
            for ( float v :  dcg.DCG(mixtureModel.getResult(Qid), topK) ) {
                System.out.print(v + ", ");
            }
            System.out.println("\niDCG:");
            for ( float v :  dcg.iDCG(mixtureModel.getResult(Qid), topK) ) {
                System.out.print(v + ", ");
            }
            System.out.println("\n");
        }


    }


}
