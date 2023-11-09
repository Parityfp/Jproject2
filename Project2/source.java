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
/*
to do list:
- create wait/notify for waiting conditions 
- cyclic barrier for only the factorythreads 
- main thread report before day 1 
- threads activity
- summary on main 

*/
abstract class MyAbstractThread extends Thread
{
    protected  int		rounds,rates;
    protected  CyclicBarrier	cfinish;
    protected Random random;
    protected SharedBuffer share;
    
    public MyAbstractThread(String name)		{ super(name);  random = new Random();
    
    
    }

    public void setRounds(int r)			{ rounds = r; }
    public void setCyclicBarrier(CyclicBarrier f)	{ cfinish = f; }
    public void setShare(SharedBuffer sh)               {share = sh;}

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
        share.access(2,2,3);
     for(int i=1; i<=rounds;i++){
        try {
            
            
            Thread.sleep(random.nextInt(500));
            System.out.println(Thread.currentThread().getName());
        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
            share.access(2,2,3);
        }
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
        share.access(3,3,1);
        for(int i=1; i<=rounds;i++){
        try {
            
            Thread.sleep(random.nextInt(500));
            System.out.println(Thread.currentThread().getName());
        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
        share.access(3,3,1);
        }
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
        System.out.println(days + "\n" + AllMaterials.toString() + "\n" + AllSuppliers + "\n" + AllFactories);
        CyclicBarrier Sbarrier = new CyclicBarrier(partiesS);
        for (SupplierThread S : AllSthreads){ S.setCyclicBarrier(Sbarrier); S.setRounds(days); S.setShare(share);S.start();}
         CyclicBarrier Fbarrier = new CyclicBarrier(partiesF);
        for (FactoryThread F : AllFthreads) { F.setCyclicBarrier(Fbarrier); F.setRounds(days); F.setShare(share);F.start();}
        try{Thread.sleep(1000);}catch (Exception e) {}
        for(int i=1; i<=days;i++){
         share.access(1,1,2);
         System.out.println(Thread.currentThread().getName() + i);
            
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

	synchronized public void access(int w, int n, int up)
	{
		while(share!=w) try { wait(); Thread.sleep(1000);} catch(Exception e) { }
                if(share==w){ share = up;
                              notifyAll();
                              System.out.println(Thread.currentThread().getName() +"notifyall");
                }
                try{
                Thread.sleep(200);
                }catch(Exception e){}
	}
    }
