package Query;

/**
 * @author Jewel Li on 15-5-3. ivanka@udel.edu
 */

public class KeywordQuery extends Object{

    protected String original;
    protected String keywords;
    protected String keywords_stemmed;

    private String Qid;

    /**
     * Defalt constructor
     */
    public KeywordQuery(){
        original = "";
        keywords = "";
        keywords_stemmed = "";
    }

    /**
     * Constructor
     * @param query     Original user input query.
     */
    public KeywordQuery(String query){
        original = query;
        keywords = removeStopWords();
        keywords_stemmed = stem();
    }


    /**
     * @param query     Original user input query.
     * @param qid       Query ID.
     */
    public KeywordQuery(String query, String qid){
        this(query);
        Qid = qid;
    }


    protected String removeStopWords() {
        if (original != null) {
            return Document.Document.process(original);
        }else{
            if (Utility.Options.DEBUG == true){
                System.out.println("Error: No given user input.");
            }
            return null;
        }
    }

    protected String stem(){
        if (keywords != null) {
            return Utility.Stemmer.mystem(keywords);
        }else{
            if (Utility.Options.DEBUG == true){
                System.out.println("Error: No keywords from user query.");
            }
            return null;
        }
    }

    public String getOriginal(){
        return original;
    }

    public String getKeywords(){
        return keywords;
    }

    public String getKeywords_stemmed(){
        return keywords_stemmed;
    }


    /**
     * Need an input choice: original, keywords, stemmed.
     * @param choice    Give at least 3 beginning characters of one of the following choices: original, keywords, stemmed.
     * @return      A string of either original query, keywords in the query, or stemmed keywords in the query.
     */
    public String toString(String choice) {
        if (choice.toLowerCase().contains("ori")) {
            return original;
        } else if (choice.toLowerCase().contains("key")){
            return keywords;
        }else if (choice.toLowerCase().contains("ste")){
            return keywords_stemmed;
        }else{
            System.out.println("Error. Valid choices are: original, keywords, stemmed.");
            return null;
        }
    }


    public static void main (String args[] ){
        String query = "How much revenue have credit card companies gained over the past five years ?";
        KeywordQuery keywordQ = new KeywordQuery(query);
        System.out.println( keywordQ.toString("original") );
        System.out.println( keywordQ.toString("keywords") );
        System.out.println( keywordQ.toString("stemmed") );

    }
}
