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

abstract class MyAbstractThread extends Thread
{
    protected  int		rounds;
    protected  CyclicBarrier	cfinish;

    public MyAbstractThread(String name)		{ super(name); }

    public void setRounds(int r)			{ rounds = r; }
    public void setCyclicBarrier(CyclicBarrier f)	{ cfinish = f; }
    

    @Override
    public void run()
    {
        System.out.println(Thread.currentThread().getName());
    }
};
class SupplierThread extends MyAbstractThread {
 public SupplierThread(String name)		{ super(name); }
}

class FactoryThread extends MyAbstractThread {

    public int lots;
    public String product;

    public FactoryThread(String name,String p, int l){ 
        super(name);
        lots = 0;
        product = p;
    }
}

 class source {
    static ArrayList<Material> AllMaterials = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllSuppliers = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllFactories = new ArrayList<>();
    static int days;
    static int parties = 0;
    public static void main(String[] args) {
        ReadConfig();
        System.out.println(days + "\n" + AllMaterials.toString() + "\n" + AllSuppliers + "\n" + AllFactories);
        CyclicBarrier barrier = new CyclicBarrier(parties);
    }

    static void ReadConfig(){
        //for codebeans
        
        String path = "src/main/Java/Project2/", filename = "config.txt";
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
                    AllSuppliers.add(supplier); parties++;    
                    break;
                case("F"):ArrayList<String> factory = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        factory.add(col[i].trim()); 
                    }
                    AllFactories.add(factory); parties++;
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
