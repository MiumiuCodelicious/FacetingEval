package Faceting;

import java.util.ArrayList;

/**
 * @author Jewel Li on 15-4-20. ivanka@udel.edu
 *
 * Statistics about facets in anu given document collection.
 * This collection should be specified by a given list of document IDs.
 *
 * @see Index.InverseIndex
 * An inverse index must have been built already to analyze facet statistics.
 * Otherwise, build an inverse index.
 *
 * Actual facetvalue-by-doc index is of data structure ArrayList<ArrayList<Integer>>
 * where a mapping of facetvalue to integer index is of data structure HashMap<String, Integer>
 */

public interface FacetStatsInterface {


    /**
     * @param docID a single document ID
     * @return <code>ArrayList<String></code> of facet values in a single document
     * */
    ArrayList<String> getFacetsInDoc(String docID);


    /**
     * Given a facet value name, return an ArrayList of all documents tagged with it.
     * @param facetname     String facet value name
     * @return  an <code>ArrayList<String></code> of document IDs.
     */
    ArrayList<String> getDocsInFacet( String facetname );


    /**
     * @return  the average number of facet values in the collection
     * */
    float avgFacetNum();

    /**
     * Overload function avgFacetNum(): allow user to specify a list of documents to analyze
     * @param docIDs    a list of document IDs
     * @return  average number of facet values in the given list of documents
     */
    float avgFacetNum(String[] docIDs);

    /**
     * @return  the average number of documents for each facet value
     * */
    float avgDocNum();

    /**
     * Print the facet values that cover the highest number of documents.
     */
    void printTopFacet();

    /**
     * Overload function printTopFacet(): allow user to specify a list of documents to analyze
     * @param docIDs a list of document IDs
     */
    void printTopFacet(String[] docIDs);


    void printBottomFacet();
    /**
     * Overload function printBottomFacet(): allow user to specify a list of documents to analyze
     */
    void printBottomFacet(String[] docDIs);

    /**
     * print the documents that are tagged with the highest number of facet values.
     */
    void printTopDoc();
    /**
     * Overload function printTopDoc(): allow user to specify a list of documents to analyze
     */
    void printTopDoc(String[] docIDs);

    void printBottomDoc();
    /**
     * Overload function printBottomDoc(): allow user to specify a list of documents to analyze
     */
    void printBottomDoc(String[] docIDs);

}
