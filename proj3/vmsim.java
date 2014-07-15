import java.io.*;
import java.util.*;

public class vmsim {
	public static int fc,mac,fault,write,cf;
	public static int refresh;
	public static String alg;
	public static String file;
	public static String [][] address;
	public static int []array, rBit;
	public static int [][]counter;
	public static Hashtable<Integer,Page>p;
	public static Hashtable<Integer,LinkedList<Integer>> future;

	public static void main(String []args)throws Exception {
	try{
	fc = Integer.parseInt(args[1]);
	if (fc!=8 && fc!=32 && fc!=64 && fc!=128)
	{
		System.out.println("please input 8, 32, 64, or 128 frames!");
		System.exit(0);
	}
	alg = args[3];
	
	if (alg.equals("nru")|| alg.equals("aging"))
	{
		refresh = Integer.parseInt(args[5]);
		file= args[6];
	}
	else
	{
		file = args[4];
	}
	if (file.equals("-r"))
	{
		System.out.println("Wrong Command!");
		System.out.println("useage: java vmsim 每n <numframes> -a <opt|rand> <tracefile>");
		System.out.println("or java vmsim 每n <numframes> -a nru|aging -r <refresh> <tracefile>");
		System.exit(0);
	}
    //System.out.println(fc+" "+alg+" "+refresh+" "+file);
	}
	catch(Exception e)
	{
		System.out.println("Wrong Command!");
		System.out.println("useage: java vmsim 每n <numframes> -a <opt|rand> <tracefile>");
		System.out.println("or java vmsim 每n <numframes> -a nru|aging -r <refresh> <tracefile>");
		System.exit(0);
	}
	
	array = new int [fc];
    rBit = new int [fc];
    		
	for(int i = 0; i < fc; i++){			
		array[i] = -1;
		rBit[i] = 0;
	}
    if(alg.equals("aging"))
    {
    	counter = new int[fc][8];
        for(int i=0; i< fc; i++)
        {
    		for(int j=0; j<8; j++){
    			counter[i][j] = 0;
    		}
    	}
    }
    
	p = new Hashtable<Integer,Page>();
	future = new Hashtable<Integer,LinkedList<Integer>>();
	for(int x = 0; x < 1048576; x++)
	{
		Page pg = new Page();
		if(alg.equals("opt")){	
			future.put(x, new LinkedList<Integer>());
		}
		p.put(x,  pg);
	}
	address = new String[1000000][2];
	cf = 0;
	
	
	if (alg.equals("opt"))
	{
		BufferedReader f = new BufferedReader( new FileReader( file) );
		int i = 0;
		while (f.ready())
		{
			
			String s = f.readLine();
			address[i] = s.split(" ");
			int pn = Integer.decode("0x"+address[i][0].substring(0, 5));
	
			future.get(pn).add(i);
			i++;
		}
	}

	if (alg.equals("opt"))
	{
		opt();
	}
	else if (alg.equals("rand"))
	{
		rand();
	}
	else if (alg.equals("aging"))
	{
		aging();
	}
	else if (alg.equals("nru"))
	{
		nru();
	}
	else
	{
		System.out.println("Wrong Command!");
		System.out.println("useage: java vmsim 每n <numframes> -a <opt|rand> <tracefile>");
		System.out.println("or java vmsim 每n <numframes> -a nru|aging -r <refresh> <tracefile>");
		System.exit(0);
	}
	System.out.println("Number of frames:\t"+fc);
	System.out.println("Total memory accesses:\t"+mac);
	System.out.println("Total page faults:\t"+fault);
	System.out.println("Total writes to disk:\t"+write);
	//System.out.println(aw);
}
	public static void opt()  throws Exception{
		BufferedReader f = new BufferedReader( new FileReader( file) );
		
		while (f.ready())
		{
			mac++;
			String s = f.readLine();
			String [] a = s.split(" ");
			
		
			int pn = Integer.decode("0x"+a[0].substring(0, 5));
			future.get(pn).removeFirst();
			Page e = p.get(pn);
			e.id=pn;
			e.referenced=1;
			
			if(a[1].equals("W"))
			{
				e.d=1;
			}
			if(e.v == 0)
			{
				fault++;
				
				if (cf>=fc)
				{
					int temp = findMax();
					Page t = p.get(temp);
					if(t.d==1)
					{
						System.out.println(a[0]+"--evict dirty!");
						write++;
					}
					else
						System.out.println(a[0]+"--evict clean!");
					
					array[t.frame] = e.id;
					e.frame= t.frame;
					e.v = 1;
					t.d = 0;
					t.referenced = 0;
					t.v = 0;
					t.frame= -1;
					p.put(temp, t);
					
				}
				else
				{
					System.out.println(a[0]+"--no eviction!");
					array[cf]=pn;
					e.frame=cf;
					e.v=1;
					cf++;
				}
			}
			else
				System.out.println(a[0]+"--Hit!");
			p.put(pn, e);	
			
		}
	}
	
	public static int findMax() {
		int idx = 0, maxValue = 0;
		for (int i = 0; i < array.length; i++){
			if(future.get(array[i]).isEmpty())
			{
				
				return array[i];
			}
			else{
				if(future.get(array[i]).get(0) > maxValue){
					maxValue = future.get(array[i]).get(0);
					idx = array[i];
				}
			}
		}
		return idx;
	}
	public static void nru() throws Exception{
		BufferedReader f = new BufferedReader( new FileReader( file) );
		while (f.ready())
		{
			mac++;
			if( mac % refresh == 0){
				//Set all r bits to 0
				for(int in = 0 ; in < cf; in++)
				{
					Page e = p.get(array[in]);
					e.referenced = 0;
					p.put(e.id,e);
				}
			}
			String s = f.readLine();
			String [] a = s.split(" ");
			
		
			int pn = Integer.decode("0x"+a[0].substring(0, 5));
			Page e = p.get(pn);
			e.id=pn;
			e.referenced=1;
			
			if(a[1].equals("W"))
			{
				e.d=1;
			}
			if(e.v == 0)
			{
				fault++;
				
				if (cf>=fc)
				{
					int done_flag = 0;
					
					Page t = null;
					while(done_flag == 0){
						
						for(int w = 0; w < array.length; w++){

							Page te = p.get(array[w]);
							
							if(te.referenced== 0 && te.d == 0 && te.v == 1){
								
								e.frame = te.frame;
								
								if(te.d == 1){
									System.out.println(a[0]+"--evict dirty!");
								
									write++;
								}
								else
									System.out.println(a[0]+"--evict clean!");
								array[e.frame] = e.id;
								te.v = 0;
								te.d = 0;
								te.referenced = 0;
								te.frame= -1;
								p.put(te.id, te);
								e.v = 1;
								p.put(e.id, e);
								done_flag = 1;
								break;
							}
							else{
								if(te.referenced == 0 && te.d == 1 && te.v == 1){
									
									t = new Page(te);
									continue;
								}
								else{
									if(te.referenced == 1 && te.d == 0 && te.v == 1 && t == null){
										
										t = new Page(te);
										continue;
									}
									else{
										if(te.referenced == 1 && te.d == 1 && te.v == 1 && t == null){
											
											t = new Page(te);
											continue;
										}
									}
								}
							}

						}
						
						if(done_flag == 1){
							continue;
						}
						e.frame = t.frame;
						
						if(t.d == 1){
							write++;
							System.out.println(a[0]+"--evict dirty!");
						}
						else
						System.out.println(a[0]+"--evict clean!");
						array[e.frame] = e.id;
						t.v = 0;
						t.d = 0;
						t.frame= -1;
						t.referenced = 0;
						p.put(t.id, t);
						e.v = 1;
						p.put(e.id, e);
						
						done_flag = 1;
					}
				}
				else
				{
					System.out.println(a[0]+"--no eviction!");
					array[cf]=pn;
					e.frame=cf;
					e.v=1;
					cf++;
					p.put(e.id, e);	
				}
			}
			else
				System.out.println(a[0]+"--Hit!");
			
		}
	}
	public static void aging() throws Exception{
		BufferedReader f = new BufferedReader( new FileReader( file) );
		while (f.ready())
		{
			mac++;
			if( mac % refresh == 0){
				for(int i=0; i<fc; i++)
				{
					for(int j=1; j<8; j++){
						counter[i][j] = counter[i][j-1];
					}
					counter[i][0]= rBit[i];
					rBit[i]=0;
				}
			}
			String s = f.readLine();
			String [] a = s.split(" ");
			//
		
			int pn = Integer.decode("0x"+a[0].substring(0, 5));
			Page e = p.get(pn);
			e.id=pn;
			e.referenced=1;
			if(a[1].equals("W"))
			{
				e.d=1;
			}
			if(e.v == 0)
			{
				fault++;
				if (cf >=fc )
				{
					
					int temp = getLRU();
					Page t = p.get(array[temp]);
					for(int j=0; j<8; j++){
						counter[temp][j] = 0;
					}
					rBit[temp] = 1;
					if(t.d==1)
					{
						System.out.println(a[0]+"--evict dirty!");
						write++;
					}
					else
						System.out.println(a[0]+"--evict clean!");
					e.frame= temp;
					array[temp] = e.id;
					e.v = 1;
					t.d = 0;
					t.referenced = 0;
					t.v = 0;
					t.frame= -1;
					p.put(t.id, t);
				}
				else
				{
					System.out.println(a[0]+"--no eviction!");
					array[cf]=pn;
					e.frame=cf;
					e.v=1;
					rBit[cf] = 1;
					cf++;
				}
			}
			else
			{
				rBit[e.frame]=1;
				System.out.println(a[0]+"--Hit!");
			}
			p.put(e.id, e);
			
		}
	}
	public static int getLRU(){
		int index = 0;
		int min = 9999999;
		String bit = "";
		
		
		for(int i = 0; i<fc; i++){
			int temp = 0;
			for(int j = 0; j<8; j++){
				bit += counter[i][j];			
			}
			temp = Integer.parseInt(bit,2);
			if (rBit[i]!=1)
			{
				if(temp < min){
					min = temp;
					index = i;
				}
			}
			bit="";
		}
			
		return index;
	}

	public static void rand() throws Exception{
		
		Random rdm = new Random();
		int pointer = rdm.nextInt(fc);
		
		
		BufferedReader f = new BufferedReader( new FileReader( file) );
		
		while (f.ready())
		{
			mac++;
			String s = f.readLine();
			String [] a = s.split(" ");
			//
		
			int pn = Integer.decode("0x"+a[0].substring(0, 5));
			Page e = p.get(pn);
			e.id=pn;
			e.referenced=1;
			
			if(a[1].equals("W"))
			{
				e.d=1;
			}
			if(e.v == 0)
			{
				fault++;
				if (cf >=fc )
				{
					int temp = array[pointer];
					Page t = p.get(temp);
					if(t.d==1)
					{
						System.out.println(a[0]+"--evict dirty!  ");
						write++;
						
					}
					else{
						System.out.println(a[0]+"--evict clean!  ");
						
					}
					t.d = 0;
					t.referenced = 0;
					t.v = 0;
					t.frame= -1;
					p.put(temp, t);
					array[pointer] = pn;
					e.frame= pointer;
					pointer = rdm.nextInt(fc);
					e.v = 1;
				}
				else
				{
					System.out.println(a[0]+"--no eviction!  ");
					
					array[cf]=pn;
					e.frame=cf;
					e.v=1;
					cf++;
				}
			}
			else{
				System.out.println(a[0]+"--Hit!  ");
				
			}
			p.put(pn, e);
			
		}
	}
	
}
class Page {

	int id;
	byte referenced;
	int d;
	int v;
	int frame;
	public Page()
	{
		this.id = 0;
		this.referenced = 0;
		this.d = 0;
		this.v = 0;
		this.frame = -1;
	}
	public Page(Page copy){
		this.id = copy.id;
		this.referenced = copy.referenced;
		this.d = copy.d;
		this.v = copy.v;
		this.frame = copy.frame;
	}
}
