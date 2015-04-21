package Rank;
import java.util.HashMap;

/**
 * Created by Jewel Li on 15-4-17. ivanka@udel.edu
 *
 * Your ranking algorithm might be independently developed,
 * and integrating your algorithm in this project might be a pain.
 *
 * This is an interface to read in different types of ranked results,
 * in pure text format.
 *
 * Document ID are the unique document names.
 *
 */

public interface ReadRankedResultInterface{

    HashMap<Integer, String> queryMap = new HashMap<Integer, String>();

    /* Entire ranked result to string */
    String toString();

    /* Ranked result for a particular query to string */
    String toString(String Qid);

    /* Print all queries and their Qid */
    HashMap<String, String> getQueryMap();
}
