
import Ranker.*;
import Ranker.Faceting.*;
import Ranker.Evaluation.DiscountedCumulativeGain;
import Query.*;
import Utility.Options;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Jewel Li on 15-4-3. ivanka@udel.edu
 */

public class Main {


    public static void main(String args[]) {

        try {
            PrintWriter writer = new PrintWriter("./facet_rank_by_score_time_treated.txt", "UTF-8");

            /**
             * Step 1. First of all, create inverse indexes of a given document collection.
             * This prep work must be done to analyze facet statistics.
             */

            String current_dir = System.getProperty("user.dir");
            String collection_dir = "/src/Var/TestDocuments/infographic/XYFacets/";
            Indexer.IndexReader indexreader = new Indexer.IndexReader();
            indexreader.buildIndex(current_dir + collection_dir);


            writer.write("-------- Documents Indexed -------- \n");


            /**
             * Step 2. Get basic document ranking.
             * Assume you have your own retrieval result to play with.
             * Rank.ReadRankedResult class reads in each query and the list of retrieved documents for each query.
             * @see Ranker.ReadRankedResult
             * */
            ReadRankedResult mixtureModel = new ReadRankedResult(current_dir + "/src/Var/mixture_model_6_ranked_result.txt", current_dir + "/src/Var/QueryMap.txt");
            if (Options.DEBUG == true) {
                writer.write(mixtureModel.getQueryMap().toString() + "\n");
                writer.write(mixtureModel.toString() + "\n");
            }



            /**
             * Step 3. For each query, get ranked list of documents.
             */


            int ndcg_topK = 10;

            int low_ndcg_query_number = 0, high_ndcg_query_number = 0;

            for (String Qid : mixtureModel.getQueryMap().keySet()) {

                String query = mixtureModel.getQueryMap().get(Qid);

//                    if (Qid.equals("5.8.3") == false) {
//                        continue;
//                    }

                System.out.println("Query " + Qid + ": " + mixtureModel.getQueryMap().get(Qid) + "\n");
                writer.write("Qid: " + Qid + " " + mixtureModel.getQueryMap().get(Qid) + "\n");


                /**
                 * Process the input natural language query using my own developed Entity Extraction System.
                 *
                 * The system understands a question type English language query such as "How does Nissan's revenue compare to that of BMW's?"
                 *
                 * The Entity Extraction System is available on GitHub:
                 *
                 * https://github.com/MiumiuCodelicious/Ask4Graphic
                 *
                 */

                String processedQ = NaturalLanguageQueryParser.infographicQueryParser(query);
                writer.write("Processed query: " + processedQ + "\n");

                String[] fieldsWithoutStopwordsStem = {"X", "Y"};

                InfographicQuery infographicQ = new InfographicQuery(processedQ, fieldsWithoutStopwordsStem);





                /**
                 * Step 3.1 Evaluate the nDCG of the original ranked list.
                 */
                float[] original_nDCG = DiscountedCumulativeGain.nDCG(mixtureModel.getResult(Qid), ndcg_topK);

                if (original_nDCG[ndcg_topK-1] > 0.93) {
                    high_ndcg_query_number ++;
                    continue;
                }
                low_ndcg_query_number ++;

                writer.write("Original Ranking nDCG@" + (ndcg_topK) + " = " + original_nDCG[ndcg_topK - 1] + "\n");




                /**
                 * Step 4. Analyze each facet field.
                 */
                for (String facetname : indexreader.getFacetedFields()) {

                    writer.write("\n................. Analyzing Facet field " + facetname + "..................\n");
                    System.out.println("\n................. Analyzing Facet field " + facetname + "..................\n");

                    FacetStats fstats = new FacetStats(indexreader.getFacetIndex(facetname));
                    fstats.setNumberFacetsForEval(20);

                    FacetRanker franker = new FacetRanker(indexreader.getFacetIndex(facetname));


                    /**
                     * Get and merge all query field values in the current facet field under examination
                     */
                    StringBuilder queryFieldValue = new StringBuilder();

                    for ( String queryfield : infographicQ.getKeyword_queryfields().keySet()){
                        if (facetname.contains(queryfield)){
                            for (String value : infographicQ.getKeyword_queryfields().get(queryfield) ){
                                queryFieldValue.append(value + " ");
                            }
                        }
                    }


                    writer.write("Field " + facetname + ": " + queryFieldValue.toString() + "\n");


                    /**
                     * Step 4.1 Analyze basic facet statistics for a query's ranked documents.
                     */
                    String[] rankedDocID = Utility.TemplateFunctions.fetchColumn(mixtureModel.getResult(Qid), 0);
                    if (rankedDocID != null) {
                        writer.write("Average number of " + facetname + " values in each document is " + fstats.avgFacetNum(rankedDocID) + "\n");
                        writer.write("Average number of documents tagged with each facet value in the " + facetname + " field is : " + fstats.avgDocNum(rankedDocID) + "\n");
                        writer.write("\n");

    //                    fstats.printTopFacet(rankedDocID);
    //                    fstats.printBottomFacet(rankedDocID);
    //                    System.out.println("");

                    } else {
                        System.out.println("Returned 0 documents for query.");
                    }

                    /**
                     * Step 4.2 Rank facet values based on Expected Rank Promotion, Similarity, or Combined.
                     */
                    int counter = 0;
                    List<Map.Entry<String, Float>> sortedFacets = franker.rankFacets(queryFieldValue.toString(), mixtureModel.getResult(Qid));
                    if (sortedFacets == null) {
                        System.out.println("Error in getting sorted facet values by expected promotion. ");
                        return;
                    }
                    for (Map.Entry<String, Float> facet : sortedFacets) {
                        if (counter > 20) {
                            break;
                        }
                        counter++;
                        writer.write("Facet: " + facet.getKey() + ":\t" + facet.getValue().toString() + "\t");

                        /**
                         * Evaluate each facet value print the nDCG change resulting from that facet value.
                         * */
                        String[][] facetDocs = franker.reRankDocs(mixtureModel.getResult(Qid), facet.getKey());

                        float[] facet_nDCG = DiscountedCumulativeGain.nDCG(facetDocs, ndcg_topK);


                        writer.write(" nDCG@" + (ndcg_topK) + " = " + facet_nDCG[ndcg_topK - 1] + "\tnDCG@" + ndcg_topK + " Change = " + (facet_nDCG[ndcg_topK - 1] - original_nDCG[ndcg_topK - 1]));


                        if (facet_nDCG[ndcg_topK-1] - original_nDCG[ndcg_topK-1] > 0){
                            writer.write(" ** \n");
                        }else{
                            writer.write("\n");
                        }

                    }
                    writer.write("\n");


                }

                writer.write("There are " + high_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " > 0.9\n");
                writer.write("There are " + low_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " <= 0.9\n\n");
                writer.write("--------------------------------\n\n");

            }

        writer.close();


        }catch (IOException ioe){

            ioe.printStackTrace();

        } // end try-catch for result writing




    } // end main()

} // end class Main
