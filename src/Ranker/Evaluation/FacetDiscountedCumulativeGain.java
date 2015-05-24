package Ranker.Evaluation;

import Utility.Options;

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
     * @TODO this is currently done in Main.class
     * Given a list of ranked facets
     * @param rankedFacets
     * @param original_rankedlist
     * @param topK
     * @param alpha     a floating point parameter controling how much Loss weight. Loss, in contrast with Gain, measures the nDCG of the lost relevant documents.
     * @return
     */
    public static float[] facetDCG(List<Map.Entry<String, Float>> rankedFacets, String[][] original_rankedlist, int topK, float alpha){


        return null;
    }


    /**
     * Given the facet ranked documents, and the original ranked documents, calculate the Gain in document ranking at top K in the facet ranking.
     *
     * For each relevant document d_r in facet_rankedlist:
     *
     *      Gain = sum( ndcg_facet(d_r) - ndcg_original(d_r) )
     *
     * IMPORTANT: ndcg_facet(d_r) is only the normalized DCG value for d_r only.
     *
     * IMPORTANT: ndcg_facet and ndcg_original are both normalized to iDCG_original.
     *
     * @param facet_rankedlist      a 2D String array of array for facet document ranking.
     * @param original_rankedlist   a 2D String array of array for original document ranking.
     *                              Outer elements are each document record, inner elements are: docID, score, relevance.
     *
     * Example document ranking:
     * --------------------------------+
     * DocID1   5.81916274628  1       |
     * DocID2   5.36491699619  2       |
     * ...                             |
     * --------------------------------+
     *
     * @param topK      the top K documents to cut-off
     *
     *
     * @return       a float array of size topK, containing the facet Gain @1-topK.
     */
    public static float facetGain (String[][] facet_rankedlist, String[][] original_rankedlist, int topK){
        float facet_gain = 0.0f;

        /**
         * Get topK in original document ranking.
         */
        String[] original_topKDocIDs = new String[topK];
        System.arraycopy(Utility.TemplateFunctions.fetchColumn(original_rankedlist, 0), 0, original_topKDocIDs, 0, topK);

        float original_iDCG = DiscountedCumulativeGain.iDCG(original_rankedlist, topK)[topK-1];

//        System.out.println("Original iDCG = " + original_iDCG);
//        System.out.println("Gain------");

        if(original_iDCG == 0.0f){
            System.out.println("Original Ideal DCG = 0. Nothing to gain. ");
            return 0.0f;
        }

        int bound = Math.min(facet_rankedlist.length, topK);
        /**
         * Loop on facet document ranking:
         */
        for (int i = 0; i < bound; i++){

            int original_rank = -1;

            String docID = facet_rankedlist[i][0] ;
            int rel = Integer.parseInt( facet_rankedlist[i][2] );

            float doc_facet_dcg =  (float)( Math.pow( 2, rel) -1) / ((float)Math.log(i+2) / (float)Math.log(2)) ;

            if (Options.DEBUG == true) {
                System.out.println(docID + ", relevance = " + rel + "  in facet ranking = " + (i + 1) + ", facet DCG = " + doc_facet_dcg);
            }

            /**
             * if each document covered by this facet is found in the topK original ranking: increment facet gain by dcg gain
             */
            if ( ( original_rank = Utility.TemplateFunctions.contains( docID, original_topKDocIDs ) ) >= 0 ){

                float doc_original_dcg = (float)( Math.pow(2, rel) -1) / ((float)Math.log(original_rank+2) / (float)Math.log(2));

                facet_gain +=  (doc_facet_dcg - doc_original_dcg)  ;

                if (Options.DEBUG == true) {
                    System.out.println(", in original ranking = " + (original_rank + 1) + ", original DCG = " + doc_original_dcg);
                    System.out.println(" gain = " + (doc_facet_dcg - doc_original_dcg) + "\n");
                }

                /**
                 * if a document covered by this facet is not within the topK original ranking: increment facet gain by new dcg
                 */
            }else{

                facet_gain += doc_facet_dcg;

            }


        }


        if (Options.DEBUG == true) {
            System.out.println(" Gain " + (facet_gain) + "\n");
            System.out.println(" Normalized Gain " + (facet_gain / original_iDCG) + "\n");
        }


        return (facet_gain/original_iDCG);
    }



    public static float facetLoss (String[][] facet_rankedlist, String[][] original_rankedlist, int topK){
        float facet_loss = 0.0f;

        /**
         * Get topK in facet document ranking.
         */
        int length = Math.min(topK, facet_rankedlist.length);
        String[] facet_topKDocIDs = new String[topK];
        System.arraycopy(Utility.TemplateFunctions.fetchColumn(facet_rankedlist, 0), 0, facet_topKDocIDs, 0, length);

        float original_iDCG = DiscountedCumulativeGain.iDCG(original_rankedlist, topK)[topK-1];

//        System.out.println("Original iDCG = " + original_iDCG);
//        System.out.println("Loss ----");


        if(original_iDCG == 0.0f){
            System.out.println("Original Ideal DCG = 0. Nothing to lose. ");
            return 0.0f;
        }

        int bound = Math.min(original_rankedlist.length, topK);
        /**
         * Loop on original document ranking
         */
        for (int i = 0; i < bound; i++){

            int facet_rank = -1;

            String docID = original_rankedlist[i][0] ;
            int rel = Integer.parseInt( original_rankedlist[i][2] );

            float doc_original_dcg =  (float)( Math.pow( 2, rel) -1) / ((float)Math.log(i+2) / (float)Math.log(2)) ;


            /**
             * if a document covered in the original ranking is found in the topK facet ranking: no loss is possible
             */
            if ( ( facet_rank = Utility.TemplateFunctions.contains( docID, facet_topKDocIDs ) ) >= 0 ){

            /**
             * if a document covered in the original ranking is not found in the topK facet ranking: loss the original DCG
             */
            }else{

                facet_loss += doc_original_dcg;
                if (Options.DEBUG == true) {
                    System.out.println(docID + ", relevance = " + rel + ", original rank = " + (i + 1) + ", original DCG = " + doc_original_dcg + " is lost");
                }
            }

        }

        if (Options.DEBUG == true) {
            System.out.println(" Loss " + (facet_loss) + "\n");
            System.out.println(" Normalized Loss " + (facet_loss / original_iDCG) + "\n");
        }

        return (facet_loss/original_iDCG);
    }





}
