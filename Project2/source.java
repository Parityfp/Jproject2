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
    public String getID(){ return ID;}
}
class Material extends Item {

    public Material(String id)   { super(id); }
    public synchronized void addToBalance(int amount) {
        balance += amount;
        System.out.printf("%-15s >>  put %10d %-15s balance = %8d %s \n",
                Thread.currentThread().getName(),amount,ID,balance,ID);
    }

    public synchronized int use(int amount) 
    {
        
        if(amount<balance){
        balance -= amount;
        }else{
        amount = balance;
        balance = 0;
        } 
        
        System.out.printf("%-15s >>  get %10d %-15s balance = %8d %s \n",
                Thread.currentThread().getName(),amount,ID,balance,ID);
        
        return amount;
    }

    @Override
    public String toString(){
        return this.ID;
    }


}
class Product extends Item implements Comparable<Product> { 
    
    public  Product(String id)   { super(id); }
    
    public void create() 
    {
        balance++;
        System.out.printf("%-15s >> %-10s Production succeeds, lot %3d \n",Thread.currentThread().getName(),ID,balance);
           
    }
    
     @Override
    public int compareTo(Product other) {
     Product n = (Product) other;
     if(this.balance<other.balance) return 1;
     else if (this.balance>other.balance) return -1;
     else return this.getID().compareToIgnoreCase(other.getID());
    }
}
/* README
please build methods from inside the threads to satisfy the project conditions. 

most conditions completed 
to do list:
- testing with demo 1/2
- formatting 
- code clean up
- prep final ver. 

*/
abstract class MyAbstractThread extends Thread
{
    protected  int		rounds,length;
    protected  CyclicBarrier	cfinish;
    protected Random random;
    protected SharedBuffer share;
    protected ArrayList<Material>            sharedMaterial;
    protected int[] rates;
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
 
 public SupplierThread(String name, ArrayList<Material> ma,int[] r)		
 { 
     super(name); 
     sharedMaterial = ma;
     rates = r;
 
 }
 @Override
 synchronized public void run() {
        
     for(int i=1; i<=rounds;i++){
        try {
            share.access(2);
            /*
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
               */
            for(int k = 0; k < rates.length; k++){
                sharedMaterial.get(k).addToBalance(rates[k]);

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
    private Product product;
    private int[] holding;

    public FactoryThread(String name,Product p, ArrayList<Material> ma,int[] r, int[] h){ 
        super(name);
        lots = 0;
        product = p;
        sharedMaterial = ma;
        rates = r;
        holding = h;
    }
    @Override
    synchronized public void run() {
        
        for(int i=1; i<=rounds;i++){
        try {
            share.access(3);
            System.out.printf("%-15s >>  Holding ",Thread.currentThread().getName());
            for(int k = 0; k < holding.length; k++){
               System.out.printf(" %10d %-15s ",holding[k], sharedMaterial.get(k).getID()); 
            }
            System.out.println();
            //Thread.sleep(random.nextInt(500));
           // System.out.println(Thread.currentThread().getName());
        } catch (Exception e) {
            System.out.println(e);
        }
        try { cfinish.await();  } catch (Exception e) { }
        //critical section
       for(int k = 0; k < rates.length; k++){
             if(holding[k]==0) holding[k]=sharedMaterial.get(k).use(rates[k]);

            }
        try { cfinish.await();  } catch (Exception e) { }
        if(Arrays.equals(holding, rates)) {product.create(); Arrays.fill(holding, 0);}
        else {
            System.out.printf("%-15s >> %-10s Production fails \n",Thread.currentThread().getName(),product.getID());
            for(int k = 0; k < rates.length; k++){
             if(holding[k]!=0&&holding[k]!=rates[k]){
                 sharedMaterial.get(k).addToBalance(holding[k]);
                holding[k]=0;
                }
            }
        }

        try { cfinish.await();  } catch (Exception e) { }
        share.update(1);
        if(i<rounds)share.access(3);
        }// System.out.println(Thread.currentThread().getName()+ " Finishes");
    }
}

 class source {
    ArrayList<Material> AllMaterials = new ArrayList<>();
    ArrayList<Product> AllProducts = new ArrayList<>();
    //ArrayList<ArrayList<String>> AllSuppliers = new ArrayList<>();
    //ArrayList<ArrayList<String>> AllFactories = new ArrayList<>();
    ArrayList<FactoryThread> AllFthreads = new ArrayList <>();
    ArrayList<SupplierThread> AllSthreads = new ArrayList <>();
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
        //String path = "C:\\Users/person/Desktop/Coding/Java/paradigms/src/Project2/", filename = "config.txt"; 
        //String path = "Project2/", filename = "config.txt"; 
        int j;
        boolean fileopened = false;
        while (!fileopened){
        try( Scanner fscanner = new Scanner(new File(path+filename));){
            fileopened = true;
            while(fscanner.hasNext()){
                String line = fscanner.nextLine();
                String [] col = line.split(",");
            switch(col[0]){
                case("D"): days = Integer.parseInt(col[1].trim()); 
                System.out.printf("%-15s >>  simulation days = %d\n", Thread.currentThread().getName(), days);
                break;
                case("M"): for(int i = 1; i < col.length; i++){
                        Material material = new Material(col[i].trim());
                        AllMaterials.add(material);
                    } break;
                case("S")://ArrayList<String> supplier = new ArrayList<>();
                    int[] Srates = new int[col.length-2];
                    for(int i = 1; i < col.length; i++){
                        //supplier.add(col[i].trim()); 
                    }
                    //AllSuppliers.add(supplier);
                    j = 0;
                     System.out.printf("%-15s >>  %-12s  daily supply rates = ",Thread.currentThread().getName(), col[1].trim() ) ;
                    for(int i = 2; i < col.length; i++){
                       
                        Srates[j] = Integer.parseInt(col[i].trim()) ;
                        System.out.printf(" %4d %10s ", Srates[j], AllMaterials.get(j).getID() );
                        j++;
                    }
                   
                    System.out.println();
        
                    SupplierThread S = new SupplierThread(col[1].trim(),AllMaterials,Srates);
                    AllSthreads.add(S);
                    partiesS++;    
                    break;
                case("F")://ArrayList<String> factory = new ArrayList<>();
                    int[] Frates = new int[col.length-4];
                    int[] holdings = new int[col.length-4];
                    for(int i = 1; i < col.length; i++){
                       // factory.add(col[i].trim()); 
                    }
                    System.out.printf("%-15s >>  %-12s  daily use    rates = ",Thread.currentThread().getName(), col[1].trim() ) ;
                    //AllFactories.add(factory); 
                    Product p = new Product(col[2].trim());
                    AllProducts.add(p);
                     j= 0;
                    for(int i = 4; i < col.length; i++){
                        
                        Frates[j] = Integer.parseInt(col[3].trim())*Integer.parseInt(col[i].trim()) ;
                         System.out.printf(" %4d %10s ", Frates[j], AllMaterials.get(j).getID() );
                        holdings[j] = 0;
                        j++;
                    }
                    System.out.printf(" producing %4s %-15s \n", col[3].trim(), col[2].trim() );
                    FactoryThread F = new FactoryThread(col[1].trim(), p,AllMaterials, Frates, holdings);
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
/*
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
*/
        CyclicBarrier Sbarrier = new CyclicBarrier(partiesS);
        for (SupplierThread S : AllSthreads){ S.setCyclicBarrier(Sbarrier); S.setRounds(days); S.setShare(share);S.start();}
         CyclicBarrier Fbarrier = new CyclicBarrier(partiesF);
        for (FactoryThread F : AllFthreads) { F.setCyclicBarrier(Fbarrier); F.setRounds(days); F.setShare(share);F.start();}
        try{Thread.sleep(1000);}catch (Exception e) {}
        for(int i=1; i<=days;i++){
         share.access(1);
         share.update(2);
         System.out.printf("\n%-15s >>  -----------------------------------------------------------------------\n", Thread.currentThread().getName());
         System.out.printf("%-15s >>  Day %d\n", Thread.currentThread().getName(), i);
            
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
        System.out.printf("\n%-15s >>  -----------------------------------------------------------------------\n", Thread.currentThread().getName());
        System.out.printf("%-15s >>  Summary\n", Thread.currentThread().getName());
        
        Collections.sort(AllProducts);
        for(int i=0; i<AllProducts.size();i++)
        System.out.printf("%-15s >>  Total %-10s = %3d lots\n", Thread.currentThread().getName(),AllProducts.get(i).ID,AllProducts.get(i).balance);
        
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

