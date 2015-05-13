
import Ranker.*;
import Ranker.Evaluation.FacetDiscountedCumulativeGain;
import Ranker.Faceting.*;
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
            PrintWriter writer = new PrintWriter("./facet_ranking_by_relevance_0.7_max_dcfg.txt", "UTF-8");

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

            HashMap<String, Float> avg_dcfg_ndcg = new HashMap<String, Float>();
            HashMap<String, Float> avg_dcfg_gain = new HashMap<String, Float>();
            HashMap<String, Float> avg_dcfg_non_negative = new HashMap<String, Float>();


            for ( String facetfield : indexreader.getFacetedFields() ){
                avg_dcfg_ndcg.put(facetfield, 0.0f);
                avg_dcfg_gain.put(facetfield, 0.0f);
                avg_dcfg_non_negative.put(facetfield, 0.0f);
            }

            float sum_max_ndcg_gain = -10.0f, sum_max_facet_gain = -10.0f;


            for (String Qid : mixtureModel.getQueryMap().keySet()) {

                /**
                 * For each query, calculate the maximum discounted nDCG gain:
                 */
                float max_ndcg_gain = 0.0f;
                /**
                 * maximum discounted facet gain:
                 */
                float max_facet_gain = 0.0f;
                /**
                 * maximum non-negative nDCG gain:
                 */
                float max_non_neg_ndcg_gain = 0.0f;



                String query = mixtureModel.getQueryMap().get(Qid);

//                if (Qid.equals("1.8.3") == false) {
//                    continue;
//                }

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

                if (original_nDCG[ndcg_topK-1] > 0.7) {
                    high_ndcg_query_number ++;
                    continue;
                }
                low_ndcg_query_number ++;

                writer.write("Original Ranking nDCG@" + (ndcg_topK) + " = " + original_nDCG[ndcg_topK - 1] + "\n");


                System.out.println("Ideal Ranking iDCG@" + (ndcg_topK) + " = " + DiscountedCumulativeGain.iDCG(mixtureModel.getResult(Qid), ndcg_topK)[ndcg_topK - 1]);


                /**
                 * Step 4. Analyze each facet field.
                 */
                for (String facetname : indexreader.getFacetedFields()) {


                    float dcfg_ndcg = 0.0f, dcfg_gain = 0.0f, dcfg_non_negative = 0.0f;


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
                     * Evaluate the Discounted Cumulative Facet Gain
                     */
                    int counter = 0;

                    List<Map.Entry<String, Float>> sortedFacets = franker.rankFacets(queryFieldValue.toString(), mixtureModel.getResult(Qid));
                    if (sortedFacets == null) {
                        System.out.println("Error in getting sorted facet values by expected promotion. ");
                        return;
                    }

                    for (Map.Entry<String, Float> facet : sortedFacets) {
                        if (counter > 10) {
                            break;
                        }
                        counter++;
                        writer.write("Facet: " + facet.getKey() + ":\t" + facet.getValue().toString() + "\t");

                        System.out.println("----------> Facet: " + facet.getKey());


                        /**
                         * Evaluate each facet value print the nDCG change resulting from that facet value.
                         * */
                        String[][] facetDocs = franker.reRankDocs(mixtureModel.getResult(Qid), facet.getKey());


                        float[] facet_nDCG = DiscountedCumulativeGain.nDCGFacet(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);


                        writer.write(" nDCG@" + (ndcg_topK) + " = " + facet_nDCG[ndcg_topK - 1] + "\tnDCG@" + ndcg_topK + " Change = " + (facet_nDCG[ndcg_topK - 1] - original_nDCG[ndcg_topK - 1]));


                        if (facet_nDCG[ndcg_topK-1] - original_nDCG[ndcg_topK-1] > 0){
                            writer.write(" ** \t");
                        }else{
                            writer.write("\t");
                        }



                        /**
                         * Evaluate the facet value Gain
                         */
                        float facet_gain = FacetDiscountedCumulativeGain.facetGain(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);

                        writer.write("\nGain @" + ndcg_topK + " = " + facet_gain + ", ");

                        float facet_loss = FacetDiscountedCumulativeGain.facetLoss(facetDocs, mixtureModel.getResult(Qid), ndcg_topK);

                        writer.write("Loss @" + ndcg_topK + " = " + facet_loss + "\n");

                        if (counter <= 5) {
                            dcfg_ndcg += (facet_gain - facet_loss) / (float) (Math.log(counter + 1) / Math.log(2));
                            dcfg_gain += (facet_gain) / (float) (Math.log(counter + 1) / Math.log(2));

                            if(facet_gain - facet_loss > 0) {
                                dcfg_non_negative += (facet_gain - facet_loss) / (float) (Math.log(counter + 1) / Math.log(2));
                            }
                        }


                    }
                    writer.write("\nDiscounted nDCG Gain @5 = " + dcfg_ndcg + ", alpha = 1, this measures difference between nDCG.\n");
                    writer.write("Discounted Cumulative Facet Gain @5 = " + dcfg_gain + ", alpha = 0, only considering relevance documents covered by facets. \n");
                    writer.write("Only considering possitive gain, Discounted Cumulative Facet Gain @5 = " + dcfg_non_negative + ", alpha = 1 ");
                    writer.write("\n\n");

                    float sum_dcfg1 = avg_dcfg_ndcg.get(facetname) + dcfg_ndcg;
                    avg_dcfg_ndcg.put(facetname, sum_dcfg1);

                    float sum_dcfg2 = avg_dcfg_gain.get(facetname) + dcfg_gain;
                    avg_dcfg_gain.put(facetname, sum_dcfg2);

                    float sum_dcfg3 = avg_dcfg_non_negative.get(facetname) + dcfg_non_negative;
                    avg_dcfg_non_negative.put(facetname, sum_dcfg3);

                    if (dcfg_ndcg > max_ndcg_gain) {
                        max_ndcg_gain = dcfg_ndcg;
                    }
                    if (dcfg_gain > max_facet_gain) {
                        max_facet_gain = dcfg_gain;
                    }
                }

                sum_max_ndcg_gain += max_ndcg_gain;
                sum_max_facet_gain += max_facet_gain;

                writer.write("For query " + Qid + ", the maximum nDCG Gain @5 = " + max_ndcg_gain + " on either X or Y field, alpha = 1. \n");
                writer.write("For query " + Qid + ", the maximum Discounted Cumulative Facet Gain @5 = " + max_facet_gain + ", alpha = 0, only consider relevance documents covered by facets. \n\n\n");

            }

            for (String facetfield : avg_dcfg_ndcg.keySet() ) {
                writer.write("Facet field " + facetfield + "average Discounted Cumulative Facet Gain @5 = " + avg_dcfg_ndcg.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 1\n");
            }
            for (String facetfield: avg_dcfg_gain.keySet()) {
                writer.write("Facet field " + facetfield + "average Discounted Cumulative Facet Gain @5 = " + avg_dcfg_gain.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 0.5 \n");
            }
            for (String facetfield: avg_dcfg_non_negative.keySet()) {
                writer.write("Only considering possitive gain, facet field " + facetfield + "average Discounted Cumulative Facet Gain @5 = " + avg_dcfg_non_negative.get(facetfield)/(float)low_ndcg_query_number + ", alpha = 1 \n");
            }

            writer.write("\nAverage over all queries, Maximum nDCG Gain = " + sum_max_ndcg_gain/(float)low_ndcg_query_number + ", alpha = 1\n");

            writer.write("\nAverage over all queries, Maximum Discounted Cumulative Facet Gain = " + sum_max_facet_gain/(float)low_ndcg_query_number + ", alpha = 0.5\n\n");

            writer.write("There are " + high_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " > 0.9\n");
            writer.write("There are " + low_ndcg_query_number + " queries with nDCG@" + ndcg_topK + " <= 0.9\n\n");
            writer.write("--------------------------------\n\n");

        writer.close();


        }catch (IOException ioe){

            ioe.printStackTrace();

        } // end try-catch for result writing




    } // end main()

} // end class Main
