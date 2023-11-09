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
        System.out.printf("%-15s>> Enter config file for simulation =\n",Thread.currentThread().getName());
        ReadConfig();
        System.out.println(days + "\n" + AllMaterials.toString() + "\n" + AllSuppliers + "\n" + AllFactories);
    }

    static void ReadConfig(){
        //for codebeans
        String path = "src/main/Java/Project2/", filename = "config";
        Scanner keyboardScan = new Scanner(System.in);
        //for vscode
        //String inputFile = "C:\\Users/person/Desktop/Coding/Java/paradigms/src/Project2/config.txt";
         boolean fileopened = false;
        while (!fileopened){
        try( Scanner fscanner = new Scanner(new File(path+filename));){
            fileopened = true;
            while(fscanner.hasNext()){
                String line = fscanner.nextLine();
                String [] col = line.split(",");
            switch(col[0]){
                case("D"): days = Integer.parseInt(col[1].trim()); break;
                case("M"): for(int i = 1; i < col.length; i++){
                        Material material = new Material(col[i].trim());
                        AllMaterials.add(material);
                    } break;
                case("S"):ArrayList<String> supplier = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        supplier.add(col[i].trim()); 
                    }
                    AllSuppliers.add(supplier);    
                    break;
                case("F"):ArrayList<String> factory = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        factory.add(col[i].trim()); 
                    }
                    AllFactories.add(factory); 
                    break;
            }
            }
        fscanner.close();
        
        }catch (Exception e) {
            System.out.println();
            System.out.println(e);
            System.out.printf("Thread %-15s>> Enter config file for simulation =\n",Thread.currentThread().getName());
            filename = keyboardScan.next();
        }
    }
    }
}
