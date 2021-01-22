/*
    Instituto Politécnico Nacional 
    Escuela Superior de Cómputo 
    Teoria Computacional. 2CV1
    Profr. Benjamin Luna Benoso 
    Gloria Oliva Olivares Menez 
    21/01/2021

    Funcionamiento: Las configuraciones están en el archivo .rar con los nombres de C1.txt y C2.txt  
                    Para compilar el programa, se hace a través de la terminal (Linux) o del cmd (Windows).
                    Primero se coloca el comando javac seguido del nombre del archivo con la extension
                    .java, por tanto, quedaria de la siguiente forma: javac MaquinaTuring.java 
                    Posteriormente, para ejecutarlo, se coloca el comando java, el nombre del archivo (sin la 
                    extension) y finalmente el nombre del archivo con la configuracion. Es decir: 
                    java MaquinaTuring C1.txt

    Configuraciones: en el archivo C1.txt se acepta un lenguaje. 
                     Por otro lado, C2.txt realiza una suma binaria. 

    Nota: Cabe destacar que esta máquina de Turing es de sólo una cinta.  
*/

import java.util.*;
import java.io.*; 

public class MaquinaTuring{
    private String symbols[]; //alfabeto de la cadena 
    private String lenguaje[]; //lenguaje
    private String alfcinta[]; //alfabeto de cinta 
    private String cadena1; //primera cadena que se inserta 
    private String cadena2;  //segunda cadena que se inserta 
    private String ini, edoActual;
    private String fin[];
    private HashMap<String,HashMap<String,String[]>> table;
    private ArrayList<String> cinta; //cinta infinita 
    private int indexPrin=0;  //indice principal 
    private boolean suma = false;   // Para saber si es suma binaria
    private int sumando = 0; // Suma binaria
    private int acarreo = 0; // Acarreo de las sumas
    private Stack<Integer> res; // Resultado de la suma binaria

    public MaquinaTuring(String name){
        this.table=new HashMap<String,HashMap<String,String[]>>();
        cinta=new ArrayList<String>();
        try{
            // Se crea y se abre un fichero.
            File inputFile = new File(name);

            // Se crea un lector del archivo y un buffer
            // que contendrá el texto del archivo
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            // Se lee linea por linea el archivo
            String linea;
            String[] aux;
            linea = br.readLine();
            while(linea != null){
                // Se divide la cadena y se guarda en un array
                String dato[] = linea.split(":"); //los edos se dividen por 2 puntos
                if(linea.charAt(0) == '@'){
                    symbols = dato[1].split(",");  //se dividen por comas 
                     alfcinta = dato[2].split(","); 
                     lenguaje = dato[3].split(":");
                   
                }else if(dato[0].equals("Inicio")){
                    ini = dato[1];
                    edoActual = dato[1];
                }else if(dato[0].equals("Final")){
                    fin = Arrays.copyOfRange(dato, 1, dato.length);
                }else{
                    HashMap<String,String[]> tabAux = new HashMap<String,String[]>(); //se instancia el hashmap auxiliar 
                    aux = Arrays.copyOfRange(dato, 1, dato.length);  
                    for(String transi : aux){
                        String []daux = transi.split(","); //el contenido de la tabla auxiliar se divide por comas
                        tabAux.put(daux[0],Arrays.copyOfRange(daux, 1, daux.length));  //se guardan los datos en el hash auxiliar 
                    }
                    table.put(dato[0], tabAux); //se guardan los datos en el hash principal 
                }
                // System.out.println("Linea "+ i +": "+linea);
                linea = br.readLine();
            }
        
            table.remove("");   // Elimino espacio en blanco

            // Se cierra el lector del archivo
            fr.close();

        }catch(FileNotFoundException e){
            System.err.println("ArchivoText: " + e);
            System.exit(0);
        }catch(IOException e){
            System.err.println("ArchivoText: " + e);
            System.exit(0);
        }
    }

    public void showData(){
        System.out.println("Alfabeto de cinta: "+Arrays.toString(symbols)+Arrays.toString(alfcinta)); //Se muestra el alfabeto de cadena y de cinta
        System.out.println("Lenguaje: "+Arrays.toString(lenguaje)); //Se muestra el lenguaje
    }

    public void analizarCadena(){ 
        Scanner cad = new Scanner(System.in);
        System.out.print("Ingrese la cadena 1: ");   //ingresa la primer cadena el usuario 
        cadena1 = cad.nextLine();
        System.out.print("Ingrese la cadena 2 (solo aplica para la configuracion de la suma binaria [C2.txt], en el caso de no necesitar una segunda cadena escriba NINGUNA): "); //ingresa la segunda cadena el usuario
        cadena2 = cad.nextLine();
        int tam=cadena1.length(); //se buscan los tamaños de cada cadena 
        int tam1=cadena2.length();
        if(cadena2.equals("NINGUNA")){ //Si no hay segunda cadena 
            for(int i=0;i<tam;i++){    //recorremos la cadena 
                char cc = cadena1.charAt(i);  //metemos caracter por caracter a la cinta
                cinta.add(String.valueOf(cc));
            }
        }else{  //de lo contrario
            res = new Stack<Integer>(); //se instancia la pila donde se guardará el resultado de la suma 
            int a0 = cadena1.length();  //se toma el tamaño de cada cadena 
            int b0 = cadena2.length();
            //Si ambas cadenas son de diferente tamaño, se hacen iguales agregando ceros a la cadena más pequeña 
            if(a0 > b0){
                for(int i=0; i<a0-b0; i++){ 
                    cadena2 = "0" + cadena2;
                }
            }else if(a0 < b0){
                for(int i=0; i<b0-a0; i++){
                    cadena1 = "0" + cadena1;
                }
            }
            //Se hace una cadena total que es la que se analizará en el metodo recorrerCinta
            String cadenaT = "B"+cadena1+"S"+cadena2+"B"; //se concatenan cadena 1 y cadena 2 con una S en medio para diferenciar cadenas y se agregan 2 espacios en blanco (al principio y al final)
            System.out.println(cadenaT);
            int tam2=cadenaT.length(); //se saca el tamaño de la cadena total 
            for(int j=0;j<tam2;j++){    //se recorre la cadena 
                char s = cadenaT.charAt(j);
                cinta.add(String.valueOf(s));  //smetemos caracter por caracter a la cinta
            }
            suma = true; //aquí se esta aplicando la suma, por tanto el booleano cambia a true 
        }
        recorrerCinta(); // se aplica el metodo recorrerCinta 
    }

    public boolean alfabeto(String c){ //metodo usado para saber si la cadena metida pertenece o no al alfabeto establecido
        int tam=symbols.length;
        for(int i=0;i<tam;i++){
            if(c.equals(symbols[i]))
                return true;
        }
        tam=alfcinta.length; 
        for(int i=0;i<tam;i++){
            if(c.equals(alfcinta[i]))
                return true;
        }
       return false;
    }

    public void recorrerCinta(){ // se hace el recorrido de la cinta 
        while(true){ //entramos a un ciclo infinito 
            String aux=cinta.get(indexPrin);   //Se obtienen los elementos o caracteres dentro de la cina 
            if(alfabeto(aux)){  //si pertenecen al alfabeto 
                acciones(aux);  //se aplica el metodo acciones 
            }else{
                System.out.println("Cadena no valida");  //de lo contrario la cadena no es valida 
                System.exit(0); //se acaba el programa 
            }
        }
    }

    public void doSuma(int b){  
        //en este metodo se establecen las reglas de la suma binaria 
        int r;
        if(acarreo == 1 && sumando==1 && b==1){
            acarreo = 1;  
            r = 1;
        }else if(acarreo == 0 && sumando==1 && b==1){
            acarreo = 1;
            r = 0;
        }else if(acarreo == 1 && sumando==0 && b==1){
            acarreo = 1;
            r = 0;
        }else if(acarreo == 1 && sumando==1 && b==0){
            acarreo = 1;
            r = 0;
        }else if(acarreo == 0 && sumando==0 && b==1){
            acarreo = 0;
            r = 1;
        }else if(acarreo == 0 && sumando==1 && b==0){
            acarreo = 0;
            r = 1;
        }else if(acarreo == 1 && sumando==0 && b==0){
            acarreo = 0;
            r = 1;
        }else{
            acarreo = 0;
            r = 0;
        }
        res.push(r);
        // System.out.println("Suma realizada: r="+r+"\ta="+acarreo);
    }

    public void printCinta(){ //imprime lo que hay en la cinta 
        for(String c : cinta){
            System.out.print(c);
        }
        System.out.println();
    }

    public void acciones(String sb){
        //System.out.println(sb);
        String primEdo = this.edoActual;
        try{
            // printCinta();
            HashMap<String,String[]> aux0= table.get(this.edoActual); //obtenemos el edo actual del hashmap 
            String[] aux1 = aux0.get(sb); //guardamos en un auxiliar el caracter 
            
            //se toma el bit y se cambia por una X, aquí guardamos este bit.
            if(edoActual.equals("q2") && suma && (sb.equals("0") || sb.equals("1"))){
                sumando = Integer.parseInt(sb);
            }
            //se toma el siguiente bit y hacemos la suma
            if(edoActual.equals("q4") && suma && (sb.equals("0") || sb.equals("1"))){
                doSuma(Integer.parseInt(sb));
            }

            cinta.set(indexPrin,aux1[1]); //se sustituye el indice principal por el elemento dictado en la config
             if(aux1[2].equals("D")){  //si en la config aparece una D
                indexPrin++;   //hace movimiento a la derecha 
                if(indexPrin>cinta.size()-1){   //si se pasa de longitud de la cadena en la cinta 
                    cinta.add("B"); //agregamos B's (espacios en blanco)
                }
            }
            if(aux1[2].equals("I")){ //si en la config aparece una I
                indexPrin--;   //hace movimiento a la izquierda  
                if(indexPrin<0){   //si el index es menor a 0
                    ArrayList<String> cintaAux = new ArrayList<String>(); //se crea una cinta auxiliar 
                    cintaAux.add("B"); //se agrega un espacio en Blanco 
                    for(String c : this.cinta){ //por cada caracter en la cinta original (atributo)
                        cintaAux.add(c); //se van agregando también a la cina auxiliar  
                    }
                    this.cinta=cintaAux; //se "anidan" las cintas
                }
            }
            if(aux1[2].equals("S")){ //cuando llegue al caracter S
                evalSuma();   //Se aplica el metodo evalSuma 
            }
            this.edoActual=aux1[0]; //nos cambiamos de estado 
            if(eval() && sb.equals("B") && suma==false){   //Si se cumple el metodo eval, el caracter final es el espacio en blanco y no hay sumas por hacer
                System.out.println("Cadena aceptada"); //se acepta la cadena 
                System.exit(0);
            }
        }catch(Exception e){
            System.out.println("Algo salio mal");
            System.out.println("Caracter: "+sb);
            System.out.println("EdoPrim: "+primEdo);
            System.out.println("Edo: "+edoActual);
            System.exit(0);
        }
    }

    public void printRes(){  //se imprime el resultado de la suma 
        if(acarreo == 1)
            System.out.print(acarreo);
        while(!res.empty()){
            System.out.print(res.pop());
        }
        System.out.println();
    }

    public void evalSuma(){   
        if(eval()){  //si se cumple eval()
            System.out.println("Cadena aceptada");  //se acepta la cadena 
            printRes(); //se imprime el resultado de la suma 
            System.exit(0); //se sale del programa 
        }
    }

    public boolean eval(){ //Si el edo Actual se iguala al edo final se cumple este metodo 
        for(int i=0;i<fin.length;i++){
            if(this.edoActual.equals(this.fin[i]))
                return true;
        }
        return false; 
    }

    public static void main(String[] args){
        MaquinaTuring mT = new MaquinaTuring(args[0]);
        mT.showData();
        mT.analizarCadena();
    }
}