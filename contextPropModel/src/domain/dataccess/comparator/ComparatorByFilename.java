package domain.dataccess.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class ComparatorByFilename implements Comparator<File>, Serializable {
	
	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final File entry1, final File entry2) {

		String prjName1 = entry1.getName();
		String prjName2 = entry2.getName();
		
		int resultado = 0;
		if (prjName1.compareTo(prjName2)  < 0) {
			resultado = -1;
		} else if (prjName1.compareTo(prjName2)  > 0) {
			resultado = 1;
		}
		return resultado;
	}
}
