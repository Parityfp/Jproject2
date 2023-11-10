package Project2;    
import java.io.File;import java.util.*;import java.util.concurrent.*;

abstract class Item extends Semaphore {
    protected String ID;
    protected int    balance;
   
    
    public Item()      { super(1,true);}    
    public Item(String id) {
        super(1,true);
        ID = id;
        balance = 0; 
                
    }

    public int getBalance(){
        return balance;
    }
}
class Material extends Item {

    public Material(String id)   { super(id); }
    public synchronized void addToBalance(int amount) {
        balance += amount;
    }

    public void use() 
    {
        
    }

    @Override
    public String toString(){
        return this.ID;
    }


}
class Product extends Item { 
    public  Product(String id)   { super(id); }
    
    public void create() 
    {
        
    }
}
/* README
threads are running in the correct order now, but the code for it should be fixed for more score. 
to do list:
- add info to threads 
- threads activity
- Make material be updatable by one thread at a time 
- summary on main 

*/
abstract class MyAbstractThread extends Thread
{
    protected  int		rounds,rates,length;
    protected  CyclicBarrier	cfinish;
    protected Random random;
    protected SharedBuffer share;
    
    public MyAbstractThread(String name)		{ super(name);  random = new Random();
    
    
    }

    public void setRounds(int r)			{ rounds = r; }
    public void setCyclicBarrier(CyclicBarrier f)	{ cfinish = f; }
    public void setShare(SharedBuffer sh)               {share = sh;}
    //public void setlength(int l)               {length = l;}
    @Override
    synchronized public  void run()
    {
        
        for(int i=1; i<=rounds;i++){
        try {
            Thread.sleep(random.nextInt(100,200));
            System.out.println(Thread.currentThread().getName());
        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
    }
    }
};
class SupplierThread extends MyAbstractThread {
 public SupplierThread(String name)		{ super(name); }
 @Override
 synchronized public void run() {
        
     for(int i=1; i<=rounds;i++){
        try {
            share.access(2);
            Thread.sleep(random.nextInt(500));
            for(int k = 0; k < source.AllSuppliers.size(); k++){
                if(Thread.currentThread().getName().equals(source.AllSuppliers.get(k).get(0))){
                    for(int j = 0; j < source.AllMaterials.size(); j++) {
                        System.out.printf("%-15s>>  put %10s %s", 
                            Thread.currentThread().getName(), 
                            source.AllSuppliers.get(k).get(1+j), 
                            source.AllMaterials.get(j).toString()
                        );
                        source.AllMaterials.get(j).addToBalance(
                            Integer.parseInt(source.AllSuppliers.get(k).get(1+j))
                        );
                        System.out.printf("        balance = %s\n", source.AllMaterials.get(j).getBalance());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
            share.update(3);
            if(i<rounds)share.access(2);
        } System.out.println(Thread.currentThread().getName()+ " Finishes");
    }

}

class FactoryThread extends MyAbstractThread {

    public int lots;
    public String product;

    public FactoryThread(String name,String p){ 
        super(name);
        lots = 0;
        product = p;
    }
    @Override
    synchronized public void run() {
        
        for(int i=1; i<=rounds;i++){
        try {
            share.access(3);
            Thread.sleep(random.nextInt(500));
            System.out.println(Thread.currentThread().getName());
        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
        share.update(1);
        if(i<rounds)share.access(3);
        } System.out.println(Thread.currentThread().getName()+ " Finishes");
    }
}

 class source {
    static ArrayList<Material> AllMaterials = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllSuppliers = new ArrayList<>();
    static ArrayList<ArrayList<String>> AllFactories = new ArrayList<>();
    static ArrayList<FactoryThread> AllFthreads = new ArrayList <>();
    static ArrayList<SupplierThread> AllSthreads = new ArrayList <>();
     int days;
     int partiesF = 0, partiesS = 0;
     int runmain = 0;
    public static void main(String[] args) {
         source sim = new source();
        
        sim.ReadConfig();
        sim.runsimulation();
        
    }

    void ReadConfig(){
        //for codebeans
        //String path = "src/main/Java/Project2/", filename = "config.txt";
        Scanner keyboardScan = new Scanner(System.in);
        //for vscode
        //String path = "C:\\Users/person/Desktop/Coding/Java/paradigms/src/Project2/", filename = "config.txt"; 
        String path = "Project2/", filename = "config.txt"; 
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
                    SupplierThread S = new SupplierThread(col[1].trim());
                    AllSthreads.add(S);
                    partiesS++;    
                    break;
                case("F"):ArrayList<String> factory = new ArrayList<>();
                    for(int i = 1; i < col.length; i++){
                        factory.add(col[i].trim()); 
                    }
                    AllFactories.add(factory); 
                    FactoryThread F = new FactoryThread(col[1].trim(), col[2].trim());
                    AllFthreads.add(F);
                    partiesF++;
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
    
    synchronized public void runsimulation(){
        SharedBuffer share = new SharedBuffer(1);
        //int update = 10, w= 1, n=1;

        System.out.printf("%-15s>>  simulation days = %d\n", Thread.currentThread().getName(), days);

        for(int i=0; i < AllSuppliers.size(); i++){
            System.out.printf("%-15s>>  %s  daily supply rates =  ", Thread.currentThread().getName(), AllSuppliers.get(i).get(0));
            for(int j=0; j < AllMaterials.size(); j++){
                System.out.printf("%3s %s     ", AllSuppliers.get(i).get(1+j), AllMaterials.get(j).toString());
            }
            System.out.printf("\n");
        }

        for(int i=0; i < AllFactories.size(); i++){
            System.out.printf("%-15s>>  %s  daily use     rates =  ", Thread.currentThread().getName(), AllFactories.get(i).get(0)); 
            for(int j=0; j < AllMaterials.size(); j++){
                System.out.printf("%3d %s     ", Integer.parseInt(AllFactories.get(i).get(3+j)) * Integer.parseInt(AllFactories.get(i).get(2)), AllMaterials.get(j).toString());
            }
            System.out.printf("producing %3s %s", AllFactories.get(i).get(2), AllFactories.get(i).get(1));
            System.out.printf("\n");
        }

        CyclicBarrier Sbarrier = new CyclicBarrier(partiesS);
        for (SupplierThread S : AllSthreads){ S.setCyclicBarrier(Sbarrier); S.setRounds(days); S.setShare(share);S.start();}
         CyclicBarrier Fbarrier = new CyclicBarrier(partiesF);
        for (FactoryThread F : AllFthreads) { F.setCyclicBarrier(Fbarrier); F.setRounds(days); F.setShare(share);F.start();}
        try{Thread.sleep(1000);}catch (Exception e) {}
        for(int i=1; i<=days;i++){
         share.access(1);
         share.update(2);
         System.out.printf("\n%-15s>>  -----------------------------------------------------------------------\n", Thread.currentThread().getName());
         System.out.printf("%-15s>>  Day %d\n", Thread.currentThread().getName(), i);
            
        }
        for (FactoryThread F : AllFthreads) {
            try {
                F.join();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        for (SupplierThread S : AllSthreads) {
            try {
                S.join();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println("summary");
    }
    
}
class SharedBuffer
    {
	private int share;
	public SharedBuffer(int s)         { share = s; }

	synchronized public void access(int w)
	{
                    notifyAll();
		while(share!=w) try { wait(); Thread.sleep(500);} catch(Exception e) { }
                              
                              
                             // System.out.println(Thread.currentThread().getName() +"notifyall");
                

	}
        
        public void update(int up)
        {
            share = up; //System.out.println(Thread.currentThread().getName() +" updates");
        }
    }

