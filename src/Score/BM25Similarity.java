package Score;

/**
 * @author Jewel Li on 15-4-5. ivanka@udel.edu
 */
public class BM25Similarity extends Similarity{

    private final float b;
    private final float k;

    /**
     * BM25 with the supplied parameter values.
     * @param k Controls non-linear term frequency normalization (saturation).
     * @param b Controls to what degree document length normalizes tf values.
     */
    public BM25Similarity(float b, float k){
        this.b = b;
        this.k = k;
    }


    /* default set k = 1.2f, b = 0.75f */
    public BM25Similarity(){
        this.b = 0.75f;
        this.k = 1.2f;
    }


    protected float idf(long docFreq, long docTotalNum){
        return (float) Math.log( (docTotalNum - docFreq + 0.5D)/(docFreq + 0.5D) );
    }

    protected float idf2(long docFreq, long docTotalNum){
        return (float) Math.log( (docTotalNum + 1)/(docFreq + 1) );
    }


    public float computeScore(){
        float score = 0f;
        return score;
    }

}
