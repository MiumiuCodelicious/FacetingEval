package Document;

import java.io.*;

/**
 * Created by Jewel Li on 15-4-3. ivanka@udel.edu
 *
 * Generic document class.
 */

public abstract class Document{
    protected String docID;
    protected String filelocation;
    protected String plainContent;
    protected String processedContent;

    public Document(String filelocation){
        this.filelocation = filelocation;
        setPlainContent();
        setProcessedContent();
    }

    public Document(String filelocation, String docID){
        this(filelocation);
        this.docID = docID;
    }

    private void setPlainContent(){
        this.plainContent = readdoc(this.filelocation);
    }

    private void setProcessedContent(){
        this.processedContent = basicProcess(this.plainContent);
    }


    public String getDocID(){
        return this.docID;
    }
    public String getFileLocation(){
        return this.filelocation;
    }
    public String getPlainContent(){
        return this.plainContent;
    }
    public String getProcessedContent(){
        return this.processedContent;
    }

    /*
    * Static method readdoc() can read any pure text document given it's file location
    * */
    public static String readdoc(String filelocation) {
        String readstr = "";
        File f = new File(filelocation);
        if (f.exists() && f.isFile()){
            try{
                StringBuilder sbuild = new StringBuilder();
                BufferedReader br = new BufferedReader( new FileReader(f) );
                String line;
                while( (line = br.readLine()) != null ){
                    sbuild.append(line + "\n");
                }
                readstr = sbuild.toString();
            }catch (IOException  e){
                System.out.println("Crashed in Document.Document.readdoc() ");
                e.printStackTrace();
            }
            return readstr;
        }else
            return null;
    }

    /*
    * A basic document processing function
    * To lowercase
    * Remove anything that is not made of alphabets or numbers: punctuations, 's, stop words
    * */

    public static String basicProcess(String filecontent){
        filecontent = filecontent.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ");
        filecontent = Utility.StopWordHandler.removeStopWord(filecontent.toLowerCase());
        return filecontent;
    }


}
