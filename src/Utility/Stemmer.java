package Utility;

import snowballstemmer.EnglishStemmer;

/**
 * Created by Jewel Li on 15-4-4. ivanka@udel.edu
 */
public class Stemmer {

    /* Stemmer using SnowballStemmer
     * Stem word if it can, otherwise return original word
      * */
    public static String mystem(String s){
        /* Stemmer */
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(s);
        if (stemmer.stem())
            return stemmer.getCurrent();
        else
            return s;
    }
}
