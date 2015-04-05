package Indexer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import Document.*;
import Utility.Stemmer;

/**
 * Created by Jewel Li on 15-4-4. ivanka@udel.edu
 */
public class InversedIndex{

    /* InveredIndex data is implemented as a 2-dimensional ArrayList<Integer> */
    private int DOC_SIZE = 250;
    private int WORD_SIZE = 1000;
    private ArrayList<ArrayList<Integer>> wordByDocIndex ;

    /* Document frequency list */
    private HashMap<String, Integer> docFreq = new HashMap<String, Integer>();

    /* A Map to map word string to its index location */
    private HashMap<String, Integer> wordMap = new HashMap<String, Integer>();

    /* A map to map doc ID (string) to its index location */
    private HashMap<String, Integer> docMap = new HashMap<String, Integer>();


    /* Simple Initialization of wordByDocIndex into a 1000 * 3000 matrix
     * @TODO
     * The initialization is too dirty...
      * */
    public InversedIndex(){
        ArrayList<Integer> postinglist = new ArrayList<Integer>(Collections.nCopies(DOC_SIZE, 0));
        this.wordByDocIndex = new ArrayList<ArrayList<Integer>>(Collections.nCopies(WORD_SIZE, postinglist));
    }



    /*  Add Generic Document
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(Document d){
        try {
            /* add d's docID to docMap */
            addToMap(d.getDocID(), this.docMap.size(), this.docMap);

            /*
             * 1) add words in d to wordMap,
             * 2) increment document frequency
             * 3) add words in d to wordByDocIndex
             * */
            HashMap<String, Integer> dmap = singleDocMap(d.getProcessedContent());
            for (String word : dmap.keySet()) {
                if (this.wordMap != null && !this.wordMap.containsKey(word)) {

                    /* add word to word map containing each word and its index in the inversed index */
                    addToMap(word, this.wordMap.size(), this.wordMap);

                    /* increase word count in document frequency HashMap */
                    incrementMap(word, this.docFreq);
                    int dindex = this.docMap.get(d.getDocID());

                    /* update index */
                    ArrayList<Integer> postinglist = this.wordByDocIndex.get(this.wordMap.get(word));
                    postinglist.set(dindex, dmap.get(word));
                    this.wordByDocIndex.set(this.wordMap.get(word), postinglist);
                }
            }
        }catch (NullPointerException e){
            return 0;
        }
        return 1;
    }

    /* add a plain text document
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(PlainText d){
        adddoc((Document)d);
        return 1;
    }

    /* add a specific field @param field in Lucene or Solr standard XML document
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(LuceneSolrXML d, String fieldname){
        /* Get field in d */
        ArrayList<String> fieldlist = d.getFieldMap().get(fieldname);
        String fieldcontent = "";
        for (String f : fieldlist){
            fieldcontent += f + " ";
        }
        PlainText pd = new PlainText(d.getFileLocation(), fieldcontent, d.getDocID());
        adddoc(pd);
        return 1;
    }

    /* add a specific field @param field in an Infographics XML document, which is also in the Lucene or Solr standard XML format
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(InfographicXML d, String fieldname){
        adddoc((LuceneSolrXML)d, fieldname);
        return 1;
    }


    /* Helper function to turn the content string of a document into a HashMap of word by wordcount */
    private HashMap<String, Integer> singleDocMap(String filecontent){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String word : filecontent.split(" ")){
            map = incrementMap(Stemmer.mystem(word), map);
        }
        return map;
    }




    /* Template Functions
     * -----------------------------------------------------------------------------------------
     */

    /* add a T t to a Hashmap, with t being the key, and index as value */
    static <T> HashMap<T, Integer> addToMap(T t, int i, HashMap<T, Integer> map) {
        if (map != null && t != null) {
            if (!map.containsKey(t)){
                map.put(t, i);
            }
        }
        return map;
    }

    /* Increase the Integer value by 1 given key T */
    static <T> HashMap<T, Integer> incrementMap(T t, HashMap<T, Integer> map){
        if (map != null && t != null){
            if (map.containsKey(t)){
                map.put(t, map.get(t) + 1 );
            }else{
                map.put(t, 1);
            }
        }
        return map;
    }





    /* Regular Get and Set Functions
     * -----------------------------------------------------------------------------------------
     */

    /* get function for the wordMap, containing each word and its index in the inversed index */
    public HashMap<String, Integer> getWordMap(){
        return this.wordMap;
    }

    /* get function for the docMap, containing eaach document's docID and its index in the inversed index */
    public HashMap<String, Integer> getDocMap(){
        return this.docMap;
    }

    public void setDocNum(int totalDocNumber){
        this.DOC_SIZE = totalDocNumber;
    }

    public void setWordNum(int totalWordNumber){
        this.WORD_SIZE = totalWordNumber;
    }




    public static void main(String args[]){
        System.out.println(" ------------ Testing Class InversedIndex ------------ ");

        String testdocpath1 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/ArthurRimbaud.txt";
        String testdocpath2 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/OscarWilde.txt";
        String testdocpath3 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/Schopenhauer.txt";
        Document doc1 = new LuceneSolrXML(testdocpath1);
        Document doc2 = new LuceneSolrXML(testdocpath2);
        Document doc3 = new LuceneSolrXML(testdocpath3);

        InversedIndex index = new InversedIndex();
        index.setDocNum(5); index.setWordNum(50);
        index.adddoc(doc1);
        index.adddoc(doc2);
        index.adddoc(doc3);

        System.out.println(index.getDocMap().toString());
        System.out.println(index.getWordMap().toString());
        System.out.println("Word-index Map = " + index.getWordMap().toString());
        System.out.println("Doc-index Map = " + index.getDocMap().toString());
    }


}
