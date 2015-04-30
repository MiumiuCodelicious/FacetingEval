package Indexer;

import java.util.Set;

/**
 * @author Jewel Li on 15-4-5. ivanka@udel.edu
 */
public interface IndexReaderInterface {

    /**
    * @TODO allow a config file -- config.xml -- to specify the location of schema file and location to write save index.
    * */

    /**
     * @param dir the directory to all files to be indexed
     * main function to build index
     * */
    void buildIndex(String dir);
//
//    /**
//     * This function is to give you a choice to index only a specific list of docIDs.
//     * @param dir the directory to all files to be indexed
//     * @param docIDs
//     */
//    void buildIndex(String dir, String[] docIDs);

    /**
     * iterate through given directory and given document type set in schema.xml
     * */
    void readdocs(String dir, String doctype);

    /**
     *  save index to disk
     *  */
    void writeIndex();

    /**
     * Get document type defined in schema.xml
     * @return  String document type
     */
    String getDocType();

    /**
     * Public method to access an inverse index given a String field name.
     * @param field String field name
     * @return  the inverse index of the given field.
     */
    InverseIndex getIndex(String field);

    /**
     * Public method to access a facet inverse index given the facet field name.
     * @param facet String facet field name.
     * @return  the inverse index of that facet field.
     */
    InverseIndex getFacetIndex(String facet);

    /**
     * Public method to get all the indexed field names defined in schema.xml
     * @return  String set of indexed field names.
     */
    Set<String> getIndexedFields();

    /**
     * Public method to get all the faceted field names defined in schema.xml
     * @return  String set of facet field names.
     */
    Set<String> getFacetedFields();


    /**
     * All classes implementing this interface should be able to print a symbolic portion of the inverse index given a field name.
     * @param field which field index to print
     */
    void printIndex(String field);

    /**
     * All classes implementing this interface should be able to print a symbolic portion the entire index of all fields.
     * @see Utility.Options defines how many words and documents to print
     */
    void printAllIndexes();

    /**
     * All classes implementing this interface should be able to print the inverse index given a single facet name.
     * @param facet     which facet to print
     *  */
    void printFacet(String facet);

    /**
     * All classes implementing this interface should be able to print the entire inverse index of all faceted fields.
     */
    void printAllFacets();

    /**
     * All classes implementing this interface should be able to print the indexed fields and faceted fields defined
     * in schema.xml
     */
    void printSchema();


}
