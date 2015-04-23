package Faceting;

import Index.InverseIndex;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Jewel Li on 15-4-22. ivanka@udel.edu
 *
 * This class get statistics about a single facet field.
 * For multiple facet fields, create a FacetStats instance for each facet index.
 */

public class FacetStats implements FacetStatsInterface {

    private InverseIndex facetIndex;
    private HashMap<String, Integer> docMap;
    private HashMap<String, Integer> facetMap;


    /**
     * how many documents are covered in each facet value
     * Key: facet value
     * Value: the number of documents covered
     * */
    private HashMap<String, Integer> facetCover = new HashMap<String, Integer>();
    /**
     * how many facet values are tagged in each document
     * Key: doc ID
     * Value: the number of facet values in this doc
     * */
    private HashMap<String, Integer> docCover = new HashMap<String, Integer>();

    /**
     * @param index     To instantiate a FacetStats class, an inverse index of a facet must be given.
     */
    public FacetStats(InverseIndex index){
        if (index != null) {
            facetIndex = index;
            docMap = index.getDocMap();
            facetMap = index.getWordMap();
        }
    }

    /**
     * @TODO I want to do this: let the user give a list of docIDs and get stats about only these documents
     * @param index
     * @param docIDs
     */
    public FacetStats(InverseIndex index, String[] docIDs){

    }


    /**
     * Given a document by its ID, get an ArrayList of facet values in String format.
     * @param docID  the String ID of a document
     * @return <code>ArrayList<String></code> of facet values
     * */
    public ArrayList<String> getDocFacets(String docID){
        if (facetIndex == null){
            System.out.println("Error: no facet index is given. ");
            return null;
        }
        ArrayList<String> facetlist = new ArrayList<String>();
        int doc = facetIndex.getIndexOfDoc(docID);
        if (doc < 0 ) { return null; }

        // for each facet value in the facet index, check if tagged in this document
        for (int fvindex = 0; fvindex < facetIndex.getWORD_SIZE(); fvindex ++ ){
            if (facetIndex.getPostinglist(fvindex).get(doc) > 0){
                facetlist.add(facetIndex.getWordByIndex(fvindex));
            }
        }
        return facetlist;
    }



    /**
     * @return  the average number of facet values in each document
     * */
    public float avgFacetNum(){
        // for each document in docMap, count the number of facet values.
        if (docCover.isEmpty()) {   setDocCover();  }
        int total = 0;
        for (Integer v : docCover.values()) {
            total += v.intValue() ;
        }
        return ( docCover.size() > 0 ) ? (float)total/docCover.size() : 0.0f ;
    }

    /**
     * Initialize key-value pairs in private class member HashMap docCover.
     * Keys in docCover is each document ID, values are the number of facet values in each document.
     */
    private void setDocCover() {
        if (docMap == null){ return; }
        for (String docID : docMap.keySet()) {
            docCover.put(docID, getDocFacets(docID).size());
        }
    }



    /**
     * Initialize key-value pairs in private class member facetCover.
     * Keys in facetCover is each facet value, values are the number of documents tagged with each facet value.
     */
    private void setFacetCover(){
        if (facetMap == null) { return; }
        for (String facetname : facetMap.keySet()){
            int total = 0 ;
            for ( int doc : facetIndex.getPostinglist(facetname) ){
                if ( doc > 0 ){
                    total ++;
                }
            }
            facetCover.put(facetname, total);
        }
    }






    /**
     * Print the facet values that cover the highest number of documents.
     */
    public void printTopFacet(){
        System.out.println("Top facet values with the most number of documents are: ");
        printSorted(facetCover, true);
    }

    /**
     * Print the facet values that cover the minimum number of documents.
     */
    public void printBottomFacet(){
        System.out.println("Bottom facet values with the least number of documents are: ");
        printSorted(facetCover, false);
    }

    /**
     * print the documents that are tagged with the highest number of facet values.
     */
    public void printTopDoc(){
        System.out.println("Top documents that contain the most number of facet values are: ");
        printSorted(docCover, true);
    }

    /**
     * Print the documents that are tagged with the least number of facet values.
     */
    public void printBottomDoc(){
        System.out.println("Bottom documents that contain the least number of facet values are: ");
        printSorted(docCover, false);
    }


    /**
     * Given a <code>HashMap<String, Integer></code> map, print the top sorted Entries.
     * @param map   must be of <code>HashMap<String, Integer></code> type
     * @param highest   if true, sorted from highest values to lowest; if false, sorted from lowest values to highest.
     */
    private void printSorted( HashMap<String, Integer> map, boolean highest) {
        boolean asc = !highest;
        List<Entry<String, Integer>> sortedFacets = sortMapComparator(map, asc);
        int counter = 0;
        for (Map.Entry<String, Integer> entry : sortedFacets) {
            if (counter > 10) {
                break;
            }
            counter ++;
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();
    }


    /**
     * Need to write a comparator to sort Map by Key.
     * @param map   HashMap to sort
     * @param asc     if true, ascending; if false, decreasing.
     */
    private List<Entry<String, Integer>> sortMapComparator (HashMap<String, Integer> map, boolean asc) {

        final boolean ascend = asc;
        if (map == null || map.isEmpty()){  return null;  }

        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());

        // sort list by value
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                if (ascend == true) {
                    return (e1.getValue()).compareTo(e2.getValue());
                } else{
                    return (e2.getValue()).compareTo(e1.getValue());
                }
            }
        });

        return list;
    }



    /**
     * @return  the average number of documents for each facet value
     * */
    public float avgDocNum(){
        if (facetCover.isEmpty()){
            setFacetCover();
        }
        int total = 0;
        for (Integer v : facetCover.values()){
            total += v.intValue();
        }
        return ( facetCover.size() > 0 )  ?  (float)total/facetCover.size()  :  0.0f;
    }




    public static void main (String args[]){
        Index.IndexReader indexreader = new Index.IndexReader();
        String current = System.getProperty("user.dir");
        indexreader.buildIndex(current + "/src/Var/TestDocuments/infographic/XYFacets/");
        indexreader.printAllFacets();

        System.out.println(" -------- Facet Index Prepared -------- ");
        for ( String facetname : indexreader.getFacetedFields() ){
            System.out.println("Analyzing Facet field " + facetname + "..................");
            FacetStats fstats = new FacetStats( indexreader.getFacetIndex(facetname) );
            String expdoc = "set5_9-2_exp.xml";
            System.out.println(expdoc + " is tagged with" + facetname + " values: " + fstats.getDocFacets(expdoc).toString() );

            System.out.println("Average" + facetname + " value number in each document is " + fstats.avgFacetNum() );
            System.out.println("Average document number tagged with each facet value " + facetname + " is : " + fstats.avgDocNum() );

            fstats.printTopFacet();
            fstats.printBottomFacet();
            fstats.printTopDoc();
            fstats.printBottomDoc();

            System.out.println("");

        }


    }
}