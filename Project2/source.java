package Project2;    
import java.io.File;import java.util.*;import java.util.concurrent.*;

class Material {
    public String ID;
    protected int balance = 0;
    protected Random random;

    public Material(int Balance) {
        this.balance = this.balance + Balance;
    }    
    public Material(String id) {
        this.ID = id;
    }

    @Override
    public String toString(){
        return this.ID;
    }
}

class SupplierThread extends Thread {

}

class FactoryThread extends Thread {

    public int lots;
    public String product;

    public FactoryThread(String product, int lots){ 
        
    }
}

public class source {
    static ArrayList<Material> AllMaterials = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllSuppliers = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllFactories = new ArrayList<>();
    static int days;

    public static void main(String[] args) {
        ReadConfig();
        System.out.println(days + "\n" + AllMaterials.toString() + "\n" + AllSuppliers + "\n" + AllFactories);
    }

    static public void ReadConfig(){
        //for codebeans
        //String inputFile = "src/main/Java/Project2/config.txt";
        //for vscode
        String inputFile = "C:\\Users/person/Desktop/Coding/Java/paradigms/src/Project2/config.txt";
        try{
            Scanner fscanner = new Scanner(new File(inputFile));
            
            while(fscanner.hasNext()){
                String line = fscanner.nextLine();
                String [] col = line.split(",");

                if("D".equals(col[0])){
                    days = Integer.parseInt(col[1].trim());
                }

                if("M".equals(col[0])){
                    for(int i = 1; i < col.length; i++){
                        Material material = new Material(col[i].trim());
                        AllMaterials.add(material);
                    }
                }

                if("S".equals(col[0])){
                    ArrayList<String> supplier = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        supplier.add(col[i].trim()); 
                    }
                    AllSuppliers.add(supplier);
                }

                if("F".equals(col[0])){
                    ArrayList<String> factory = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        factory.add(col[i].trim()); 
                    }
                    AllFactories.add(factory);
                }
            }
        fscanner.close();
        
        }catch (Exception e) {
            System.err.println("An error occurred while processing the file.");
            e.printStackTrace();
        }
    }
}
