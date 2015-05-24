
import Ranker.*;
import Ranker.Evaluation.FacetDiscountedCumulativeGain;
import Ranker.Evaluation.FacetNovelty;
import Ranker.Evaluation.FacetRecall;
import Ranker.Faceting.*;
import Ranker.Faceting.FacetRanker.*;
import Ranker.Evaluation.DiscountedCumulativeGain;
import Query.*;
import Utility.Options;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jewel Li on 15-4-3. ivanka@udel.edu
 */

public class Main {


    public static void main(String args[]) {

        try {
            int top_facet_number = 3;
            PrintWriter writer = new PrintWriter("./facet_rank_by_basic_0.25-0.35_Mingzhi_relevance_top" + top_facet_number + ".txt", "UTF-8");

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
            ReadRankedResult mixtureModel = new ReadRankedResult(current_dir + "/src/Var/mixture_model_6_ranked_result_Mingzhi_relevance.txt", current_dir + "/src/Var/QueryMap.txt");
            if (Options.DEBUG == true) {
                writer.write(mixtureModel.getQueryMap().toString() + "\n");
                writer.write(mixtureModel.toString() + "\n");
            }



            /**
             * Step 3. For each query, get ranked list of documents.
             */
            int ndcg_topK = 5;

            int low_ndcg_query_number = 0, high_ndcg_query_number = 0;


            HashMap<String, Float> avg_dcfg_ndcg = new HashMap<String, Float>();
            HashMap<String, Float> avg_dcfg_gain = new HashMap<String, Float>();
            HashMap<String, Float> avg_dcfg_binary = new HashMap<String, Float>();
            HashMap<String, Float> avg_novel_docs = new HashMap<String, Float>();
            HashMap<String, Float> avg_facet_recall = new HashMap<String, Float>();


            for ( String facetfield : indexreader.getFacetedFields() ){
                avg_dcfg_ndcg.put(facetfield, 0.0f);
                avg_dcfg_gain.put(facetfield, 0.0f);
                avg_dcfg_binary.put(facetfield, 0.0f);
                avg_novel_docs.put(facetfield, 0.0f);
                avg_facet_recall.put(facetfield, 0.0f);
            }

            /**
             * sum of the maximum nDCG gain on either X or Y field, across all queries.
             */
            float sum_max_ndcg_gain = -0.0f;
            /**
             * sum of the maximum facet gain on either X or Y field, across all queries.
             */
            float sum_max_facet_gain = -0.0f;
            /**
             * sum of the maximum non-negative nDCG gain on either X or Y field, across all queries.
             */
            float sum_max_binary_ndcg_gain = -0.0f;


            for (String Qid : mixtureModel.getQueryMap().keySet()) {

                /**
                 * For each query, calculate the maximum discounted nDCG gain:
                 */
                float max_ndcg_gain = -10.0f;
                /**
                 * maximum discounted facet gain:
                 */
                float max_facet_gain = 0.0f;
                /**
                 * maximum non-negative nDCG gain:
                 */
                float max_binary_ndcg_gain = 0.0f;



                String query = mixtureModel.getQueryMap().get(Qid);

//                if (Qid.equals("1.2.4") == false && Qid.equals("5.7.4") == false ) {
//                    continue;
//                }

                System.out.println("Query " + Qid + ": " + mixtureModel.getQueryMap().get(Qid) + "\n");
                writer.write("Qid: " + Qid + " " + mixtureModel.getQueryMap().get(Qid) + "\n");


                /**
                 * Step 3.1 Evaluate the nDCG of the original ranked list.
                 */
                float[] original_nDCG = DiscountedCumulativeGain.nDCG(mixtureModel.getResult(Qid), ndcg_topK);

                if (original_nDCG[ndcg_topK-1] >= 0.35 || original_nDCG[ndcg_topK-1] < 0.25 ) {
                    high_ndcg_query_number ++;
                    continue;
                }
                low_ndcg_query_number ++;

                writer.write("Original Ranking nDCG@" + (ndcg_topK) + " = " + original_nDCG[ndcg_topK - 1] + "\n");

                float original_recall = DiscountedCumulativeGain.recall(mixtureModel.getResult(Qid), ndcg_topK);
                writer.write("Original Recall@" + (ndcg_topK) + " = " + original_recall + "\n");

//                System.out.println("Ideal Ranking iDCG@" + (ndcg_topK) + " = " + DiscountedCumulativeGain.iDCG(mixtureModel.getResult(Qid), ndcg_topK)[ndcg_topK - 1]);




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
                 * Step 4. Analyze each facet field.
                 */
                for (String facetname : indexreader.getFacetedFields()) {

                    /**
                     * discounted nDCG gain for this facet field:
                     */
                    float dcfg_ndcg = 0.0f;
                    /**
                     * discounted cumulative facet gain for this facet field, considering only relevance documents covered in facets:
                     */
                    float dcfg_gain = 0.0f;
                    /**
                     * discounted non-negative nDCG gain for this facet field, simulate to document ranking, do not consider negative penalty:
                     */
                    float dcfg_binary = 0.0f;
                    /**
                     * average novel relevant document brought in top ndcg_topK per facet value:
                     */
                    int avg_novel = 0;
                    /**
                     * To get top 5 facet recall
                     */
                    String[][][] top_facet_rankedlists = new String[top_facet_number][][];



                    writer.write("\n................. Analyzing Facet field " + facetname + "..................\n");
                    System.out.println("\n................. Analyzing Facet field " + facetname + "..................\n");

                    FacetStats fstats = new FacetStats(indexreader.getFacetIndex(facetname));
//                    fstats.setNumberFacetsForEval(20);

                    BasicFacetRanker facet_ranker = new BasicFacetRanker(indexreader.getFacetIndex(facetname));


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
                    System.out.println("Field " + facetname + ": " + queryFieldValue.toString() + "\n");


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
                     * Evaluate the Discounted Cumulative Facet Gain
                     */
                    int counter = 0;

                    List<Map.Entry<String, Float>> sortedFacets = facet_ranker.rankFacets(queryFieldValue.toString(), mixtureModel.getResult(Qid));
                    if (sortedFacets == null) {
                        System.out.println("In Main.class: Error in getting sorted facet values. ");
                        return;
                    }

                    for (Map.Entry<String, Float> facet : sortedFacets) {
                        if (counter > 10) {
                            break;
                        }
                        counter++;
                        writer.write("Facet: " + facet.getKey() + ":\t" + facet.getValue().toString() + "\t");


                        /**
                         * Evaluate each facet value print the nDCG change resulting from that facet value.
                         * */
                        String[][] facetDocs = facet_ranker.reRankDocs(mixtureModel.getResult(Qid), facet.getKey());



                        float[] facet_nDCG = DiscountedCumulativeGain.nDCGFacet(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);


                        writer.write(" nDCG@" + (ndcg_topK) + " = " + facet_nDCG[ndcg_topK - 1] + "\tnDCG@" + ndcg_topK + "\t nDCG gain = " + (facet_nDCG[ndcg_topK - 1] - original_nDCG[ndcg_topK - 1]));


                        if (facet_nDCG[ndcg_topK-1] - original_nDCG[ndcg_topK-1] > 0){
                            writer.write(" ** \n");
                        }else{
                            writer.write("\n");
                        }



                        /**
                         * Evaluate the facet value Gain
                         */
                        float facet_gain = FacetDiscountedCumulativeGain.facetGain(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);

                        writer.write("\t\tfacet gain @" + ndcg_topK + " = " + facet_gain + ", ");

                        float facet_loss = FacetDiscountedCumulativeGain.facetLoss(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);

                        writer.write("facet loss @" + ndcg_topK + " = " + facet_loss + "\t");


                        /**
                         * Count the number of novel documents brought by this facet
                         */
                        int facet_novel = FacetNovelty.facetNovelty(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);

                        writer.write("novel docs @" + ndcg_topK + " = " + facet_novel + "\t");


                        /**
                         * Discounting facet nDCG gain, facet gain, non-negative nDCG gain by facet rank:
                         */
                        if (counter <= top_facet_number) {
                            dcfg_ndcg += (facet_gain - facet_loss) / (float) (Math.log(counter + 1) / Math.log(2));
                            dcfg_gain += (facet_gain) / (float) (Math.log(counter + 1) / Math.log(2));

                            if(facet_gain - facet_loss > 0) {
                                dcfg_binary += 1.0 / (float) (Math.log(counter + 1) / Math.log(2));
                            }else if (facet_gain - facet_loss < 0){
                                dcfg_binary += -1.0 / (float) (Math.log(counter + 1) / Math.log(2));
                            }
                            avg_novel += facet_novel;


                            /**
                             * Get the facet recall
                             */
                            float facet_recall = FacetRecall.facetRecall(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);
                            top_facet_rankedlists[counter-1] = facetDocs;

                            writer.write("recall @" + ndcg_topK + " = " + facet_recall + "\t" + "facet gains recall " + (facet_recall-original_recall) + "\n");

                        }



                        /**
                         * Comment out when not want to show top ranked documents by this facet.
                         */
//                        int bound = Math.min(ndcg_topK, facetDocs.length);
//                        for (int i = 0; i < bound; i ++){
//                            writer.write("\n\t" + facetDocs[i][0] + "\t" + facetDocs[i][2] + "\n");
//                        }


                    }

                    float facet_field_recall = FacetRecall.facetRecall( top_facet_rankedlists, mixtureModel.getResult(Qid), ndcg_topK);

                    writer.write("\n" +facetname + ", discounted nDCG Gain @5 = " + dcfg_ndcg + ", alpha = 1 (this measures difference between nDCG).\n");
                    writer.write(facetname + ", discounted cumulative facet Gain @5 = " + dcfg_gain + ", alpha = 0 (only considering documents covered by each facet value). \n");
                    writer.write(facetname + ", discounted non-negative nDCG Gain @5 = " + dcfg_binary + ", alpha = 1 (assume users know the correct facet values to choose.) \n");
                    writer.write(facetname + ", average novel documents per facet value @5 = " + (float) avg_novel / (float)top_facet_number + "\n");
                    writer.write(facetname + ", total recall @5 = " + facet_field_recall + "\t" + "facet gains recall " + (facet_field_recall-original_recall) + "\n");
                    writer.write("\n");

                    float sum_dcfg1 = avg_dcfg_ndcg.get(facetname) + dcfg_ndcg;
                    avg_dcfg_ndcg.put(facetname, sum_dcfg1);

                    float sum_dcfg2 = avg_dcfg_gain.get(facetname) + dcfg_gain;
                    avg_dcfg_gain.put(facetname, sum_dcfg2);

                    float sum_dcfg3 = avg_dcfg_binary.get(facetname) + dcfg_binary;
                    avg_dcfg_binary.put(facetname, sum_dcfg3);

                    float sum_novel = avg_novel_docs.get(facetname) + (float)avg_novel/(float)top_facet_number;
                    avg_novel_docs.put(facetname, sum_novel);

                    float sum_facet_recall = avg_facet_recall.get(facetname) + facet_field_recall;
                    avg_facet_recall.put(facetname, sum_facet_recall);


                    if (dcfg_ndcg > max_ndcg_gain) {
                        max_ndcg_gain = dcfg_ndcg;
                    }
                    if (dcfg_gain > max_facet_gain) {
                        max_facet_gain = dcfg_gain;
                    }
                    if (dcfg_binary > max_binary_ndcg_gain){
                        max_binary_ndcg_gain = dcfg_binary;
                    }
                }

                sum_max_ndcg_gain += max_ndcg_gain;
                sum_max_facet_gain += max_facet_gain;
                sum_max_binary_ndcg_gain += max_binary_ndcg_gain;

                writer.write("For query " + Qid + ", on either X or Y field, the maximum nDCG Gain @5 = " + max_ndcg_gain + ", alpha = 1 (assume user know which axis to choose).\n");
                writer.write("For query " + Qid + ", on either X or Y field, the maximum Discounted Cumulative Facet Gain @5 = " + max_facet_gain + ", alpha = 0 (only consider documents covered by facets). \n");
                writer.write("For query " + Qid + ", on either X or Y field, the maximum binary nDCG Gain @5 = " + max_binary_ndcg_gain + ", alpha = 1 (assume user know which axis to choose, only consider non-negative nDCG.). \n\n");

            }

            for (String facetfield : avg_dcfg_ndcg.keySet() ) {
                writer.write("Facet field " + facetfield + " across all queries, average discounted nDCG Gain @5 = " + avg_dcfg_ndcg.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 1.\n");
            }
            for (String facetfield: avg_dcfg_gain.keySet()) {
                writer.write("Facet field " + facetfield + " across all queries, average discounted cumulative facet gain @5 = " + avg_dcfg_gain.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 0.\n");
            }
            for (String facetfield: avg_dcfg_binary.keySet()) {
                writer.write("Facet field " + facetfield + " across all queries, discounted binary nDCG Gain @5 = " + avg_dcfg_binary.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 1. Using binary facet value judgement.\n");
            }
            for (String facetfield : avg_novel_docs.keySet()){
                writer.write("Facet field " + facetfield + " across all queries, novel relevant docs @5 = " + avg_novel_docs.get(facetfield)/(float)low_ndcg_query_number + ".\n");
            }

            for (String facetfield : avg_facet_recall.keySet()){
                writer.write("Facet field " + facetfield + " across all queries, facet recall @5 = " + avg_facet_recall.get(facetfield)/(float)low_ndcg_query_number + ".\n");
            }

            writer.write("\n");
            writer.write("Across all queries, on either X or Y axis, the maximum nDCG Gain = " + sum_max_ndcg_gain/(float)low_ndcg_query_number + ", alpha = 1\n");

            writer.write("Across all queries, on either X or Y axis, the maximum discounted cumulative facet Gain = " + sum_max_facet_gain/(float)low_ndcg_query_number + ", alpha = 0\n");

            writer.write("Across all queries, on either X or Y axis, the maximum discounted binary nDCG Gain = " + sum_max_binary_ndcg_gain/(float)low_ndcg_query_number + ", alpha = 1\n");


            writer.write("There are " + high_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " not in [0.25, 0.35) that are not faceted.\n");
            writer.write("There are " + low_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " in [0.25, 0.35) that are faceted. \n\n");
            writer.write("------------------------------------------------------------------\n\n");

        writer.close();


        }catch (IOException ioe){

            ioe.printStackTrace();

        } // end try-catch for result writing




    } // end main()

} // end class Main
