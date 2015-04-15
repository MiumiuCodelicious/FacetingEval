package Index;

/**
 * Created by Jewel Li on 15-4-5. ivanka@udel.edu
 */
public interface IndexReaderInterface {

    /*
    * @TODO
    * Maybe create config.xml to specify the location of schema file
    * and location to write save index ?
    * */

    /* main function */
    void buildIndex(String dir);

    /* iterate through given directory and given document type set in schema.xml */
    void readdocs(String dir, String doctype);

    /* save index to disk */
    void writeIndex();

}
