package domain.common.stats.matrix;

/**
 * <h1>Matrix</h1> The Matrix class
 * is used for defining matricial operations.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class MatrixOperationsUtils {
	
	public static double[] softmax(double[] valores){
		int totalLength = valores.length;
		double totalValue_exp = 0.0;
		double[] valores_suavizados = new double[totalLength];
		for (int i=0;i<totalLength;i++){
			totalValue_exp += Math.exp(valores[i]);
		}		
		for (int i=0;i<totalLength;i++){
			valores_suavizados[i] = Math.exp(valores[i])/totalValue_exp;
		}		
		return valores_suavizados;
	}
	
	// suma de dos matrices
	public static Matrix suma(Matrix a, Matrix b) {
		Matrix resultado = new Matrix(a.columnas, a.filas);
		for (int i = 0; i < a.columnas; i++) {
			for (int j = 0; j < a.filas; j++) {
				resultado.x[i][j] = a.x[i][j] + b.x[i][j];
			}
		}
		return resultado;
	}

	// producto de dos matrices
	/* Para poder multiplicar dos matrices AxB, el no de columnas de A debe ser igual al no de filas de B
	 * Producto de una matriz por un vector: (nxm) * (cxn) => prd(mxc) --> (2x2) * (1x2)=> res_producto (2x1) 
	 * |2     5.1| x |2.0| = |2*2.0  5.1*1.7| = |9.7 	23.4|  
	 * |4.5   8  |   |1.7|   |4.5*2.0 8*1.7|   
	 *  
	**/
	public static Matrix producto(Matrix a, Matrix b) {
		if (a.columnas != b.filas){
			return null;
		}
		Matrix resultado = new Matrix(a.filas, b.columnas);
		for (int i = 0; i < a.filas; i++) {
			for (int j = 0; j < b.columnas; j++) {
				for (int k = 0; k < b.filas; k++) {
					resultado.x[i][j] += a.x[k][j] * b.x[j][k];
				}
			}
		}
		return resultado;
	}

	// producto de una matriz por un escalar
	public static Matrix producto(Matrix a, double d) {
		Matrix resultado = new Matrix(a.columnas, a.filas);
		for (int i = 0; i < a.columnas; i++) {
			for (int j = 0; j < a.filas; j++) {
				resultado.x[i][j] = a.x[i][j] * d;
			}
		}
		return resultado;
	}

	// producto de un escalar por una matriz
	public static Matrix producto(double d, Matrix a) {
		Matrix resultado = new Matrix(a.columnas, a.filas);
		for (int i = 0; i < a.columnas; i++) {
			for (int j = 0; j < a.filas; j++) {
				resultado.x[i][j] = a.x[i][j] * d;
			}
		}
		return resultado;
	}

	// determinante de una matriz
	public static double determinante(Matrix m) {
		Matrix a = (Matrix) m.clone();
		for (int k = 0; k < m.columnas - 1; k++) {
			for (int i = k + 1; i < m.columnas; i++) {
				for (int j = k + 1; j < m.filas; j++) {
					a.x[i][j] -= a.x[i][k] * a.x[k][j] / a.x[k][k];
				}
			}
		}
		double deter = 1.0;
		for (int i = 0; i < m.columnas; i++) {
			for (int j = 0; j < m.filas; j++) {
				deter *= a.x[i][j];
			}
		}
		return deter;
	}
	
	/***
	// matriz inversa
	public static Matrix inversa(Matrix d) {
		Matrix a = (Matrix) d.clone();
		Matrix b = new Matrix(d.columnas, d.filas); // matriz de los torminos independientes
		Matrix c = new Matrix(d.columnas, d.filas); // matriz de las incognitas
		// matriz unidad
		for (int i = 0; i < d.columnas; i++) {
			for (int j = 0; j < d.filas; j++) {
				b.x[i][i] = 1.0;
			}
		}
		// transformacion de la matriz y de los torminos independientes
		for (int k = 0; k < d.columnas - 1; k++) {
			for (int i = k + 1; i < d.filas; i++) {
				// torminos independientes
				for (int s = 0; s < n; s++) {
					b.x[i][s] -= a.x[i][k] * b.x[k][s] / a.x[k][k];
				}
				// elementos de la matriz
				for (int j = k + 1; j < n; j++) {
					a.x[i][j] -= a.x[i][k] * a.x[k][j] / a.x[k][k];
				}
			}
		}
		// colculo de las incognitas, elementos de la matriz inversa
		for (int s = 0; s < n; s++) {
			c.x[n - 1][s] = b.x[n - 1][s] / a.x[n - 1][n - 1];
			for (int i = n - 2; i >= 0; i--) {
				c.x[i][s] = b.x[i][s] / a.x[i][i];
				for (int k = n - 1; k > i; k--) {
					c.x[i][s] -= a.x[i][k] * c.x[k][s] / a.x[i][i];
				}
			}
		}
		return c;
	}
	**/
	
	// matriz traspuesta
	public static Matrix traspuesta(Matrix a) {
		Matrix d = new Matrix(a.columnas, a.filas);
		for (int i = 0; i < a.columnas; i++) {
			for (int j = 0; j < a.filas; j++) {
				d.x[i][j] = a.x[j][i];
			}
		}
		return d;
	}

}

