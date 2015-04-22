package Index;

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

    /**
     * This function is to give you a choice to index only a specific list of docIDs.
     * @param dir the directory to all files to be indexed
     * @param docIDs
     */
    void buildIndex(String dir, String[] docIDs);

    /**
     * iterate through given directory and given document type set in schema.xml
     * */
    void readdocs(String dir, String doctype);

    /**
     *  save index to disk
     *  */
    void writeIndex();

}
