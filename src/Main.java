
import Ranker.*;
import Ranker.Faceting.*;
import Ranker.Evaluation.DiscountedCumulativeGain;
import Utility.Options;

import java.lang.reflect.Array;

/**
 * @author Jewel Li on 15-4-3. ivanka@udel.edu
 */

public class Main {



    public static void main(String args[]){

        /**
         * Step 1. First of all, create inverse indexes of a given document collection.
         * This prep work must be done to analyze facet statistics.
         */

        String current_dir = System.getProperty("user.dir");
        String collection_dir = "/src/Var/TestDocuments/infographic/XYFacets/";
        Indexer.IndexReader indexreader = new Indexer.IndexReader();
        indexreader.buildIndex(current_dir + collection_dir);


        System.out.println("-------- Documents Indexed -------- ");


        /**
         * Step 2. Get basic document ranking.
         * Assume you have your own retrieval result to play with.
         * Rank.ReadRankedResult class reads in each query and the list of retrieved documents for each query.
         * @see Ranker.ReadRankedResult
         * */
        ReadRankedResult mixtureModel = new ReadRankedResult( current_dir + "/src/Var/mixture_model_6_ranked_result.txt", current_dir + "/src/Var/QueryMap.txt") ;
        if (Options.DEBUG == true) {
            System.out.println(mixtureModel.getQueryMap().toString());
            System.out.println(mixtureModel.toString());
        }


        /**
         * Step 3. Analyze each facet field.
         */
        for ( String facetname : indexreader.getFacetedFields() ) {

            System.out.println("Analyzing Facet field " + facetname + "..................\n");

            FacetStats fstats = new FacetStats(indexreader.getFacetIndex(facetname));
            fstats.setNumberFacetsForEval(10);
            int ndcg_topK = 10;
            FacetRanker franker = new FacetRanker(indexreader.getFacetIndex(facetname));


            /**
             * Step 4 For each query, get ranked list of documents.
             */
            for (String Qid : mixtureModel.getQueryMap().keySet()) {

                System.out.println("Qid: " + Qid + " " + mixtureModel.getQueryMap().get(Qid));

                /**
                 * Step 4.1 Evaluate the nDCG of the original ranked list.
                 */
                int rank = 0;
                for ( float v :  DiscountedCumulativeGain.nDCG(mixtureModel.getResult(Qid), ndcg_topK)  ) {
                    System.out.print("nDCG@" + (rank + 1) + "= " + v + "\t");
                    rank ++;
                }
                System.out.print("\n");

                /**
                 * Step 4.2 Analyze basic facet statistics for a query's ranked documents.
                 */
                String[] rankedDocID = FacetRanker.fetchColumn(mixtureModel.getResult(Qid), 0) ;
                if (rankedDocID != null) {
                    System.out.println("Average number of " + facetname + " values in each document is " + fstats.avgFacetNum(rankedDocID));
                    System.out.println("Average number of documents tagged with each facet value in the " + facetname + " field is : " + fstats.avgDocNum(rankedDocID));
                    System.out.println("");

                    fstats.printTopFacet(rankedDocID);
                    fstats.printBottomFacet(rankedDocID);

                    System.out.println("");
                }else{
                    System.out.println("Returned 0 documents for query.");
                }

                /**
                 * Step 4.3 Rank facet values for a query's ranked documents.
                 */
                franker.expectedPromo(mixtureModel.getResult(Qid), facetname);




            }
        }

        }
}
