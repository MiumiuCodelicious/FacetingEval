package Utility;
import java.util.HashSet;

/**
 * Created by Jewel Li on 15-4-3. ivanka@udel.edu
 */
public class StopWordHandler {

    private static HashSet<String> stopWords;
    private static HashSet<String> units;

    private static HashSet<String> getSingletonStopWords(){
        if(stopWords == null){
            generateStopWordHash();
        }
        return stopWords;
    }

    private static HashSet<String> getSingletonUnits(){
        if(units == null){
            generateUnitsHash();
        }
        return units;
    }

    private static void generateStopWordHash(){
        stopWords = new HashSet<String>();
        addStringArrayToHash(STOP_WORDS);

        addStringArrayToHash(PREPOSITIONS);
        addStringArrayToHash(PRONOUNS);
        addStringArrayToHash(DETERMINERS);
        addStringArrayToHash(CONJUNCTIONS);

        addStringArrayToHash(MODAL_VERBS);
        addStringArrayToHash(ADVERBS);

        addStringArrayToHash(MISC_USELESS_WORDS);
        addStringArrayToHash(GOOD_LIST);

    }

    private static void generateUnitsHash(){
        units = new HashSet<String>();
        addStringArrayToHash(UNIT_LIST);
    }

    private static void addStringArrayToHash(String [] toAdd){
        for(int i = 0; i < toAdd.length; i++){
            if(!isStopWord(toAdd[i])){
                stopWords.add(toAdd[i]);
            }
        }
    }

    public static boolean isStopWord(String word) {//Faster
        return getSingletonStopWords().contains(word.trim());
    }

    public static String removeStopWord(String str){
        String result = "";
        for (String word : str.split("\\s")){
            if (!isStopWord(word)) result += word + " ";
        }
        return result.trim();
    }


    public static String removeGraphWord(String str){
        String result = "";
        for (String word : str.split("\\s")){
            if (!isGraphWord(word)) result += word + " ";
        }
        return result.trim();
    }

    public static boolean isGraphWord(String word){
        return getSingletonUnits().contains(word.trim());
    }

    private static final String [] PREPOSITIONS = {
            "about", "across", "against", "along", "around", "at", "behind", "beside", "besides", "by",
            "despite", "down", "during", "for", "from", "in", "inside", "into", "near", "of", "off",
            "on", "onto", "over", "through", "to", "toward", "with", "within", "without", "also", "in"
    };
    private static final String [] PRONOUNS = {
            "i", "you", "he", "she", "me", "her", "him", "my", "mine", "her", "hers", "his", "myself", "himself", "herself",
            "anything", "everything", "anyone", "everyone", "ones", "such", "it", "we", "they", "us", "them", "our",
            "ours", "their", "theirs", "itself", "ourselves", "themselves", "something", "nothing", "someone"
    };
    private static final String [] DETERMINERS = {
            "the", "some", "this", "that", "every", "all", "both", "one", "first", "other", "next", "many", "much", "more",
            "most", "several", "no", "a", "an", "any", "each", "no", "half", "twice", "two", "second", "another", "last",
            "few", "little", "less", "least", "own"
    };
    private static final String [] CONJUNCTIONS = {
            "and", "but", "after", "when", "as", "because", "if", "what", "where", "which", "how", "than", "or", "so",
            "before", "since", "while", "although", "though", "who", "whose", "not", "except", "were", "nah"
    };
    private static final String [] MODAL_VERBS = {
            "can", "may", "will", "shall", "could", "might", "would", "should", "must", "has", "had", "is", "was"
    };

    private static final String [] ADVERBS = {
            "begin", "end", "here", "there", "today", "tomorrow", "now", "then", "always", "never", "sometimes", "usually",
            "often", "s", "d", "t", "re", "nt",
            "therefore", "however", "besides", "moreover", "though", "otherwise", "else", "instead", "anyway", "incidentally", "meanwhile"
    };
//	private static final String [] MISC_USELESS_WORDS = {
//		"been", "like", "s", "have", "those", "these", "am", "are", "http", "html", "html", "com", "org", "ref", "www", "url", "edu", "isbn",  "be", "doing"
//	};

    private static final String [] STOP_WORDS = {//TODO: consider having a stop word file instead of a hardcoded list//TODO: get a better stop word list
            "a", "about", "above", "across", "after", "again", "against", "all", "almost", "alone", "along", "already", "also",
            "although", "always", "among", "an", "and", "another", "any", "anybody", "anyone", "anything", "anywhere", "are", "area",
            "areas", "around", "as", "ask", "asked", "asking", "asks", "at", "away", "b", "back", "backed", "backing", "backs", "be",
            "became", "because", "become", "becomes", "been", "before", "began", "behind", "being", "beings", "best", "better",
            "between", "big", "both", "but", "by", "c", "came", "can", "cannot", "case", "cases", "certain", "certainly", "clear",
            "clearly", "come", "could", "d", "did", "differ", "different", "differently", "do", "does", "done", "down", "down", "downed",
            "downing", "downs", "during", "e", "each", "early", "either", "end", "ended", "ending", "ends", "enough", "even", "evenly",
            "ever", "every", "everybody", "everyone", "everything", "everywhere", "f", "face", "faces", "fact", "facts", "far", "felt",
            "few", "find", "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", "furthering", "furthers",
            "g", "gave", "general", "generally", "get", "gets", "give", "given", "gives", "go", "going", "good", "goods", "got", "great",
            "greater", "greatest", "group", "grouped", "grouping", "groups", "h", "had", "has", "have", "having", "he", "her", "here",
            "herself", "high", "high", "high", "higher", "highest", "him", "himself", "his", "how", "however", "i", "if", "important",
            "in", "interest", "interested", "interesting", "interests", "into", "is", "it", "its", "itself", "j", "just", "k", "keep",
            "keeps", "kind", "knew", "know", "known", "knows", "l", "large", "largely", "last", "later", "latest", "least", "less", "let",
            "lets", "like", "likely", "long", "longer", "longest", "m", "made", "make", "making", "man", "many", "may", "me", "member",
            "members", "men", "might", "more", "most", "mostly", "mr", "mrs", "much", "must", "my", "myself", "n", "necessary", "need",
            "needed", "needing", "needs", "never", "new", "new", "newer", "newest", "next", "no", "nobody", "non", "noone", "not",
            "nothing", "now", "nowhere", "number", "numbers", "o", "of", "off", "often", "old", "older", "oldest", "on", "once", "one",
            "only", "open", "opened", "opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", "others", "our", "out",
            "over", "p", "part", "parted", "parting", "parts", "per", "perhaps", "place", "places", "point", "pointed", "pointing", "points",
            "possible", "present", "presented", "presenting", "presents", "problem", "problems", "put", "puts", "q", "quite", "r", "rather",
            "really", "right", "right", "room", "rooms", "s", "said", "same", "saw", "say", "says", "second", "seconds", "see", "seem",
            "seemed", "seeming", "seems", "sees", "several", "shall", "she", "should", "show", "showed", "showing", "shows", "side", "sides",
            "since", "small", "smaller", "smallest", "so", "some", "somebody", "someone", "something", "somewhere", "state", "states",
            "still", "still", "such", "sure", "t", "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore",
            "these", "they", "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts", "three", "through",
            "thus", "to", "today", "together", "too", "took", "toward", "turn", "turned", "turning", "turns", "two", "u", "under", "until",
            "up", "upon", "us", "use", "used", "uses", "v", "very", "w", "want", "wanted", "wanting", "wants", "was", "way", "ways", "we",
            "well", "wells", "went", "were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose", "why", "will",
            "with", "within", "without", "work", "worked", "working", "works", "would", "x", "y", "yet", "you", "young",
            "younger", "youngest", "your", "yours", "z"
    };

    private static final String [] MISC_USELESS_WORDS = {
            "http", "html", "htm", "com", "org", "ref", "www", "url", "edu", "isbn", "wiki", "jpg", "png", "ps", "px",
            "1px", "2px", "240px", "250px", "infobox", "postscript", "height", "width", "px", "date","cite", "nas", "thumb",
            "accessdate","jpg", "nbsp", "image", "php", "name", "category", "title", "web", "been", "like", "s",
            "have", "those", "these", "am", "are", "http", "html", "html", "com", "org", "ref", "www", "url", "edu",
            "isbn",  "be", "doing", "former", "latter", "previous", "next", "other", "logo", "author", "pdf", "content",
            "format", "page", "text", "latex", "align", "style", "center", "flagicon", "clearleft", "shtml", "colspan",
            "background", "ndash", "publisher", "font", "document", "indef", "alt", "link", "links", "position", "homepage",
            "article", "news", "autoblog", "gainsboro"
    };

    private static final String [] GOOD_LIST = {
            "a", "able", "accordingly", "after", "albeit", "all", "although", "including", "amount", "an", "and", "another", "any",
            "anybody", "anyone", "anything", "as", "be", "because", "before", "better", "both", "but", "can", "certain", "consequently",
            "could", "dare", "each", "either", "enough", "every", "everybody", "everyone", "everything", "few", "few", "fewer", "for",
            "had", "have", "he", "heaps", "hence", "her", "her", "hers", "herself", "him", "himself", "his", "", "his", "however", "I",
            "if", "it", "its", "its", "itself", "lack", "less", "little", "loads", "lots", "major", "total", "past", "top", "majority",
            "many", "many", "masses", "may", "me", "might", "mine", "minority", "more", "most", "much", "must", "my", "myself", "",
            "", "need", "neither", "neither", "neither", "neither", "nevertheless", "no", "no", "nobody", "none", "none", "nor",
            "nothing", "no_one", "number", "numbers", "of", "once", "one", "one", "or", "other", "other", "other", "ought", "ought",
            "our", "ours", "ourselves", "part", "per", "plenty", "plethora", "quantities", "remainder", "rest", "several", "several",
            "shall", "she", "should", "since", "so", "some", "some", "some", "somebody", "someone", "something", "such", "than",
            "that", "that", "that", "the", "the", "the", "the", "", "the", "the", "the", "the", "the", "their", "theirs", "them",
            "themselves", "then", "thence", "therefore", "these", "these", "they", "this", "this", "tho", "those", "those", "though",
            "thus", "till", "to", "to", "to", "to", "to", "tons", "unless", "until", "us", "used", "various", "we", "what", "whatever",
            "whatever", "when", "whenever", "where", "whereas", "wherever", "whether", "which", "whichever", "whichever", "while",
            "whilst", "who", "whoever", "whole", "whom", "whomever", "whose", "will", "would", "yet", "you", "your", "yours",
            "yourself", "yourselves"
    };

    private static final String [] UNIT_LIST = {
            "hundred", "hundreds", "thousand", "thousands", "million", "millions", "mil", "billion", "bil", "billions",
            "number", "amount", "average", "total", "current", "minimum", "min", "maximum", "max", "most", "least", "all",
            "percent", "percentage", "percentages", "portion", "ratio", "rate", "ranking", "rankings", "daily",
            "proportion", "unit", "units", "km", "kg", "gram", "kilo", "meter", "g/km", "g/mil",
            "kilometer", "kilometers", "mile", "miles", "dollar", "dollars", "pound", "pounds", "yen", "yens", "yuan", "yuans", "euro",
            "euros", "increase", "increases", "decrease", "decreases", "change", "growth", "trend", "compare", "difference", "diff", "comparison",
            "increasing", "decreasing", "changing"
    };
}
