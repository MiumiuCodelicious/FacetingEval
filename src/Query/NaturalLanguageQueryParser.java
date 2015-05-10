package Query;

import Utility.Options;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jewel Li on 15-5-4. ivanka@udel.edu
 *
 * Query parser for Natural Language queries.
 */
public class NaturalLanguageQueryParser {


    /**
     * Given a natural language query:
     *
     * 1. parse the query using the C&C parser developed by
     *    Stephen Clark et al. in Cambridge University, UK;
     *
     * 2. extract noun phrase entities from the parse tree, along with attributes associated with each entity;
     *
     * 3. used a pre-trained decision tree model to classify each entity into these categories:
     *    X-axis entity, Y-axis entity, None of the axes;
     *
     * 4. use another decision tree model to classify an Intended Message hypothesized from this query to be one of the following:
     *    1: Get-Rank
     *    2: Maximum-Minimum-Multiple
     *    3: Maximum-Minimum-Single
     *    4: General-Multiple
     *    5: General-Single
     *    6: Relative-Difference
     *    7: Rank-All
     *    8: Trend
     *
     * 5. use another decision tree model to classify the X-axis entities into:
     *    focused entities, non-focused entities;

     * @param nlq   a user input natural language question type query. This question must be of the following question types:
     *              What/Which, What is, How do, How much/many, How have
     * @return  processed output. An example of the output format:
     *
     *
     */
    public static String infographicQueryParser(String nlq){
        try{

            String[] command={"python", "./src/Query/EntityExtractionSystem_03_2015/src/AllQueries.py", nlq.replaceAll("\\s+", " ") };

            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            BufferedReader processed_output = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String output = "";
            StringBuilder result = new StringBuilder();

            while ( (output = processed_output.readLine()) != null) {

                if(Options.DEBUG == true) {
                    System.out.println("Entity Extraction System is printing: " + output);
                }

                result.append(output + "\n");
            }

            String infographicQ = toSolrQuery(result.toString());

            return infographicQ;

        }catch(Exception e){

            return null;
        }
    }


    /**
     * Transform processed natural language query to Lucene/Solr multiple field query.
     * @param processedNLQ
     * @return
     */
    private static String toSolrQuery(String processedNLQ){

        Pattern infographic_fields = Pattern.compile("(X\\s?:)|(Y\\s?:)|(IMCategory\\s?:)|(focusX\\s?:)");

        StringBuilder solrQ = new StringBuilder();

        for (String line : processedNLQ.split("\n") ){

            Matcher fields_match = infographic_fields.matcher(line);
            if (fields_match.find()){
                if ( !line.substring(fields_match.end(), line.length()).trim().equals("") ) {
                    solrQ.append(line.substring(0, fields_match.end() - 1).trim() + ":("
                            + line.substring(fields_match.end(), line.length()).trim()
                            + ") AND ");
                }
            }

        }

        solrQ.delete(solrQ.length() - 5, solrQ.length());
        return solrQ.toString();
    }


    public static void main (String[] args){

        String processedQ = NaturalLanguageQueryParser.infographicQueryParser("What company has the highest revenue  : Ford , BMW , Toyota , or Honda ?");
        System.out.println("processed natural language query: " + processedQ);
        String[] fieldsWithoutStopwordsStem = {"X", "Y"};

        InfographicQuery infographicQ = new InfographicQuery(processedQ, fieldsWithoutStopwordsStem);

        System.out.println(infographicQ.getKeywords().toString());


    }


}
