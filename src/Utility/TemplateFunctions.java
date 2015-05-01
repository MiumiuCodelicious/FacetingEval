package Utility;

import java.util.HashMap;
import java.util.Set;

/**
 * A zoo of static template functions.
 *
 * @author Jewel Li on 15-5-1. ivanka@udel.edu
 */
public class TemplateFunctions {


    /**
     * Template function for getting the entire key set from a inverse index bucket (HashMap).
     * @param bucket    inverse index bucket <code>HashMap<String, InverseIndex></></code>
     * @param <K>       key, string type field/facet name.
     * @param <T>       inverse index.
     * @return          key set
     */
    public static <K, T> Set<K> getKeyset(HashMap<K, T> bucket){
        if (bucket != null){
            return bucket.keySet();
        }else{
            System.out.println("The given HashMap is empty.");
            return null;
        }
    }


    /**
     *  add a T t to a Hashmap, with t being the key, and i as value
     *  */
    public static <T> HashMap<T, Integer> addToMap(T t, int i, HashMap<T, Integer> map) {
        if (map != null && t != null) {
            if (!map.containsKey(t)){
                map.put(t, i);
            }
        }
        return map;
    }



    /**
     *  @param t key data type
     *  @param map a hashmap HashMap<T, Integer>
     *  Increament the Integer value of key t by 1
     *  */
    public static <T> HashMap<T, Integer> incrementMap(T t, HashMap<T, Integer> map){
        if (map != null && t != null){
            if (map.containsKey(t)){
                map.put(t, map.get(t) + 1 );
            }else{
                map.put(t, 1);
            }
        }
        return map;
    }




    /**
     * Use this template function for Primitive Types to test whether a list contains the value of an element ele
     * @param ele   the element to look for
     * @param list  the list to look in
     * @param <T>   Primitive types only
     * @return      true if list contains an element of the same value, false if not.
     */
    public static <T> int contains(T ele, T[] list){
        int index = 0;
        for ( T e : list){
            if ( e.equals(ele) ) {
                return index;
            }
            index ++;
        }
        return -1;
    }




}
