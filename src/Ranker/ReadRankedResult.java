package Ranker;

import Document.Document;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Jewel Li on 15-4-17. ivanka@udel.edu
 *
 * The Ranked Result file (result_path) must follow this format:
 * --------------------------------+
 * Qid: 2.3.3                      |
 * DocID1   5.81916274628  1       |
 * DocID2   5.36491699619  2       |
 * ...                             |
 * --------------------------------+
 *
 * Second field is the score for each document. Third field is the relevance judgement for each field.
 * The third field is optional, however, when missing the document is by default of relevance grade 0.
 * Query Qid, DocID, and score are mandatory.
 *
 * IMPORTANT: No empty is allowed between the retrieved documents for each query.
 *
 *
 * In the query mapping file (query_map_path), you can define your own query ID,
 * and store the query ID to actual query mapping in a separate file,
 * example in the unit test main function: "/src/Var/QueryMap.txt".
 *
 */
public class ReadRankedResult implements ReadRankedResultInterface {

    private HashMap<String, String> queryMap;
    private HashMap<String, String[][]> resultMap;

    /**
     * @param result_path  a string path to the text file storing ranked list of documents for each query (details below)
     * @param query_map_path   a string path to the text file storing the mapping of each query to a unique String type Qid (query id)
     */
    public ReadRankedResult(String result_path, String query_map_path){
        queryMap = new HashMap<String, String>();
        resultMap = new HashMap<String, String[][]>();
        readQueries(query_map_path);
        readRankedResult(result_path);
    }


    /**
     * @param resultPath    a text file storing the ranked list of documents for each query. Format defined in class comment.
     *  read ranked result of 1 or more queries
     *  */
    private void readRankedResult(String resultPath){
        String resultContent = Document.readdoc(resultPath);
        /* chunk up the entire file into retrieval results for each query */
        StringBuilder perQueryBuilder = new StringBuilder();
        String prevQid = "";
        prevQid = extractQid(resultContent.split("\n")[0]);
        for ( String line : resultContent.split("\n") ){
            if ( extractQid(line) != null ){
                if (perQueryBuilder.length() > 1) {
                    perQueryBuilder.deleteCharAt(perQueryBuilder.lastIndexOf("\n"));
                    resultPerQuery(prevQid, perQueryBuilder.toString());
                    prevQid = extractQid(line);
                }
                perQueryBuilder.setLength(0);
                continue;
            }
            perQueryBuilder.append( line + "\n" );
        }
        resultPerQuery(prevQid, perQueryBuilder.toString());
    }


    /**
     * @param Qid                   a String unique query ID
     * @param perQueryResult        a StringBuilder containing the ranked list of documents for the query Qid.
     * read ranked result for a single query
     * */
    private void resultPerQuery(String Qid, String perQueryResult){
        String[] lines =  perQueryResult.split("\n");
        String[][] results = new String[lines.length][3];
        int index = 0;
        for ( String line : lines ) {
            if (line.length() > 3) {
                results[index] = line.split("\\s+");
                index ++;
            }else{
                System.out.println("Error! No empty line allowed !");
                break;
            }
        }
        resultMap.put(Qid, results);
    }



    private String extractQid(String line){
        Pattern p_id = Pattern.compile("qid:");
        Matcher m_id = p_id.matcher(line.toLowerCase());
        if (m_id.find()){
            return line.substring(m_id.end(), line.length()).trim() ;
        }
        return null;

    }


    /**
     *  @param queryPath    the String file path to a file mapping each query to a query ID. See format in class comment.
     *  read in query Map
     *  */
    private void readQueries(String queryPath){
        String queries = Document.readdoc(queryPath);
        String qid = "";
        // read queries line by line.
        for (String line : queries.split("\n")){
            if ( extractQid(line) != null){
                qid = extractQid(line);
            }else if( line.length() > 3 && !qid.equals("") ){
                queryMap.put( qid, line );
            }
        }
    }

    public HashMap<String, String> getQueryMap(){
        return this.queryMap;
    }

    public HashMap<String, String[][]> getResultMap(){
        return this.resultMap;
    }

    /**
     * @param Qid   String query ID
     * @return  2-D String array [each document][docID, score, relevance]
     */
    public String[][] getResult(String Qid){
        if (resultMap.containsKey(Qid)){
            return resultMap.get(Qid);
        }
        return null;
    }

    /**
     * Entire ranked result to string
     * */
    public String toString(){
        StringBuilder query = new StringBuilder();
        for ( String Qid :  resultMap.keySet() ){
            query.append("Qid: " + Qid + " " + queryMap.get(Qid) + "\n" + toString(Qid) );
        }
        return query.toString();
    }


    /**
     *  @param Qid      result for a particular query (with ID = Qid) to string
     *  */
    public String toString(String Qid){
        StringBuilder result = new StringBuilder();
        if (!resultMap.containsKey(Qid)) {
            return null;
        }
        for ( String[] doc : resultMap.get(Qid) ){
            result.append( doc[0] + "\t" + doc[1] + "\t" + doc[2] + "\n");
        }
        result.append("\n");
        return result.toString();
    }



    public static void main(String args[]){
        String current = System.getProperty("user.dir");
        ReadRankedResult mixtureModel = new ReadRankedResult( current + "/src/Var/mixture_model_6_ranked_result.txt", current + "/src/Var/QueryMap.txt") ;
        System.out.println( mixtureModel.getQueryMap().toString() );
        System.out.println( mixtureModel.toString() );

    }



}
