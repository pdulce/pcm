package facturacionUte.utils.bolsa;

public class SeparadorApellidos {

	//Sub Separar()
	
	public String[] separar(String apellidos){
		String apellidos_ = "";
		apellidos_= apellidos.replaceAll("DE LOS ", "#DE#LOS#");
		apellidos_ = apellidos_.replaceAll("DE LAS ", "#DE#LAS#");
		apellidos_ = apellidos_.replaceAll("DE LA ", "#DE#LA#");
		apellidos_ = apellidos_.replaceAll("DEL ", "#DEL#");
		apellidos_ = apellidos_.replaceAll("DE ", "#DE#");
		apellidos_ = apellidos_.replaceAll("LA ", "#LA#");
		
		String[] splitter = apellidos_.split(" ");
		String[] newSplitter = new String[splitter.length];
		for (int i=0;i<splitter.length;i++){
			newSplitter[i] = splitter[i].replaceAll("#DE#LOS#", "DE LOS ");
			newSplitter[i] = newSplitter[i].replaceAll("#DE#LAS#", "DE LAS ");
			newSplitter[i] = newSplitter[i].replaceAll("#DE#LA#", "DE LA ");
			newSplitter[i] = newSplitter[i].replaceAll("#DEL#", "DEL ");
			newSplitter[i] = newSplitter[i].replaceAll("#DE#", "DE ");
			newSplitter[i] = newSplitter[i].replaceAll("#LA#", "LA ");
		}
		
		return newSplitter;
	}
	
	
	public static void main(String[] args){
		String[] c1 = new SeparadorApellidos().separar("DEL HORNO GARCIA");
		String[] c2 = new SeparadorApellidos().separar("GARCIA DEL HORNO");
		String[] c3 = new SeparadorApellidos().separar("RODRIGUEZ DE LA OCA");
		String[] c4 = new SeparadorApellidos().separar("DE LOS CABALLEROS ALONSO");
		System.out.println("DEL HORNO GARCIA--> 1er apellido: " + c1[0] + ", 2º apellido: " + c1[1]);
		System.out.println("GARCIA DEL HORNO--> 1er apellido: " + c2[0] + ", 2º apellido: " + c2[1]);
		System.out.println("RODRIGUEZ DE LA OCA--> 1er apellido: " + c3[0] + ", 2º apellido: " + c3[1]);
		System.out.println("DE LOS CABALLEROS ALONSO--> 1er apellido: " + c4[0] + ", 2º apellido: " + c4[1]);
	}
	
}
