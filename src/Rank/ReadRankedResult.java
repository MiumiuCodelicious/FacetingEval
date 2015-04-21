package Rank;

import Document.Document;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by Jewel Li on 15-4-17. ivanka@udel.edu
 *
 * You can define your own query ID, and store the query ID to actual query mapping in a separate file, named for example "QueryMapping.txt".
 * The Ranked Result file must follow this format:
 * --------------------------------+
 * Qid: 2.3.3                      |
 * DocID1   5.81916274628  1       |
 * DocID2   5.36491699619  2       |
 * ...                             |
 * --------------------------------+
 *
 * Second field is the score for each document. Third field is the relevance judgement for each field.
 * The third field is optional. Query Qid, DocID, and score are mandatory.
 *
 * IMPORTANT: No empty is allowed between the retrieved documents for each query.
 *
 */
public class ReadRankedResult implements ReadRankedResultInterface {

    private HashMap<String, String> queryMap;
    private HashMap<String, String[][]> resultMap;


    public ReadRankedResult(String result_path, String query_map_path){
        queryMap = new HashMap<String, String>();
        resultMap = new HashMap<String, String[][]>();
        readQueries(query_map_path);
        readRankedResult(result_path);
    }


    /* read ranked result of 1 or more queries */
    private void readRankedResult(String resultPath){
        String resultContent = Document.readdoc(resultPath);
        /* chunk up the entire file into retrieval results for each query */
        StringBuilder perQueryBuilder = new StringBuilder();
        String prevQid = "";
        prevQid = getQid( resultContent.split("\n")[0] );
        for ( String line : resultContent.split("\n") ){
            if ( getQid(line) != null ){
                if (perQueryBuilder.length() > 1) {
                    perQueryBuilder.deleteCharAt(perQueryBuilder.lastIndexOf("\n"));
                    resultPerQuery(prevQid, perQueryBuilder);
                    prevQid = getQid(line);
                }
                perQueryBuilder.setLength(0);
                continue;
            }
            perQueryBuilder.append( line + "\n" );
        }
    }


    /* read ranked result for a single query */
    private void resultPerQuery(String Qid, StringBuilder perQueryBuilder){
        String[] lines =  perQueryBuilder.toString().split("\n");
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



    private String getQid(String line){
        Pattern p_id = Pattern.compile("qid:");
        Matcher m_id = p_id.matcher(line.toLowerCase());
        if (m_id.find()){
            return line.substring(m_id.end(), line.length()).trim() ;
        }
        return null;

    }


    /* read in query Map */
    private void readQueries(String queryPath){
        String queries = Document.readdoc(queryPath);
        String qid = "";
        // read queries line by line.
        for (String line : queries.split("\n")){
            if ( getQid(line) != null){
                qid = getQid(line);
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


    /* Entire ranked result to string */
    public String toString(){
        StringBuilder query = new StringBuilder();
        for ( String Qid :  resultMap.keySet() ){
            query.append("Qid: " + Qid + " " + queryMap.get(Qid) + "\n" + toString(Qid) );
        }
        return query.toString();
    }


    /* Ranked result for a particular query to string */
    public String toString(String Qid){
        StringBuilder result = new StringBuilder();
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
