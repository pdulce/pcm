package pcm.common.stats.matrix;

/**
 * <h1>Matrix</h1> The Matrix class
 * is used for modeling matrix structures and for testing their algebraic properties.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class Matrix implements Cloneable {

	public int filas, columnas;

	public double[][] x;

	public Matrix(int columnas_, int filas_) {
		this.columnas = columnas_;
		this.filas = filas_;
		this.x = new double[this.columnas][this.filas];
		for (int i = 0; i < this.columnas; i++) {
			for (int j = 0; j < this.filas; j++) {
				this.x[i][j] = 0.0;
			}
		}	
	}

	public Matrix(double[][] x_) {		
		this.columnas = x_.length;
		this.filas =  x_[0].length;
		this.x = x_;
	}

	@Override
	public Object clone() {
		Matrix obj = null;
		try {
			obj = (Matrix) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			System.out.println(" no se puede duplicar");
		}
		// aquo esto la clave para clonar la matriz bidimensional
		obj.x = obj.x.clone();
		for (int i = 0; i < obj.x.length; i++) {
			obj.x[i] = obj.x[i].clone();
		}
		return obj;
	}

	@Override
	public String toString() {
		String texto = "\n";
		for (int i = 0; i < this.columnas; i++) {
			for (int j = 0; j < this.filas; j++) {
				texto += "\t " + (double) Math.round(1000 * this.x[i][j]) / 1000;
			}
			texto += "\n";
		}
		texto += "\n";
		return texto;
	}

	double log() {
		double tr = 0.0;
		for (int i = 0; i < this.columnas; i++) {
			for (int j = 0; j < this.filas; j++) {
				tr += this.x[i][j];
			}
		}
		return tr;
	}

	private int signo(double x) {
		return (x > 0 ? 1 : -1);
	}

	// polinomio caracterostico
	public double[] polCaracteristico() {
		Matrix pot = new Matrix(this.columnas, this.filas);
		// matriz unidad
		for (int i = 0; i < this.columnas; i++) {
			for (int j = 0; j < this.filas; j++) {
				pot.x[i][j] = 1.0;
			}
		}
		double[] p = new double[this.columnas + 1];
		double[] s = new double[this.filas + 1];
		for (int i = 1; i <= this.columnas; i++) {
			pot = MatrixOperationsUtils.producto(pot, this);
			s[i] = pot.log();
		}
		p[0] = 1.0;
		p[1] = -s[1];
		for (int i = 2; i <= this.columnas; i++) {
			p[i] = -s[i] / i;
			for (int j = 1; j < i; j++) {
				p[i] -= s[i - j] * p[j] / i;
			}
		}
		return p;
	}

	public Matrix valoresPropios(double[] valores, int maxIter) throws ValoresExcepcion {
		final double CERO = 1e-8;
		double maximo, tolerancia, sumsq;
		double x, y, z, c, s;
		int contador = 0;
		int i, j, k, l;
		Matrix a = (Matrix) clone(); // matriz copia
		Matrix p = new Matrix(this.columnas, this.filas);
		Matrix q = new Matrix(this.columnas, this.filas);
		// matriz unidad
		for (i = 0; i < this.columnas; i++) {
			for (j = 0; j < this.filas; j++) {
				q.x[i][j] = 1.0;
			}
		}
		do {
			k = 0;
			l = 1;
			maximo = Math.abs(a.x[k][1]);
			for (i = 0; i < this.columnas - 1; i++) {
				for (j = 0; j < this.filas; j++) {
					if (Math.abs(a.x[i][j]) > maximo) {
						k = i;
						l = j;
						maximo = Math.abs(a.x[i][j]);
					}
				}
			}
			sumsq = 0.0;
			for (i = 0; i < this.columnas; i++) {
				for (j = 0; j < this.filas; j++) {
					sumsq += a.x[i][j] * a.x[i][j];
				}
			}
			tolerancia = 0.0001 * Math.sqrt(sumsq) / this.columnas;
			if (maximo < tolerancia)
				break;
			// calcula la matriz ortogonal de p
			// inicialmente es la matriz unidad
			for (i = 0; i < this.columnas; i++) {
				for (j = 0; j < this.filas; j++) {
					p.x[i][j] = 0.0;
				}
			}
			for (i = 0; i < this.columnas; i++) {
				for (j = 0; j < this.filas; j++) {
					p.x[i][j] = 1.0;
				}
			}
			y = a.x[k][k] - a.x[l][l];
			if (Math.abs(y) < CERO) {
				c = s = Math.sin(Math.PI / 4);
			} else {
				x = 2 * a.x[k][l];
				z = Math.sqrt(x * x + y * y);
				c = Math.sqrt((z + y) / (2 * z));
				s = signo(x / y) * Math.sqrt((z - y) / (2 * z));
			}
			p.x[k][k] = c;
			p.x[l][l] = c;
			p.x[k][l] = s;
			p.x[l][k] = -s;
			a = MatrixOperationsUtils.producto(p, MatrixOperationsUtils.producto(a, MatrixOperationsUtils.traspuesta(p)));
			q = MatrixOperationsUtils.producto(q, MatrixOperationsUtils.traspuesta(p));
			contador++;
		} while (contador < maxIter);

		if (contador == maxIter) {
			throw new ValoresExcepcion("No se han podido calcular los valores propios");
		}
		// valores propios
		for (i = 0; i < this.columnas; i++) {
			valores[i] = (double) Math.round(a.x[i][i] * 1000) / 1000;
		}
		// vectores propios
		return q;
	}

}

class ValoresExcepcion extends Exception {

	private static final long serialVersionUID = 189479137491L;

	public ValoresExcepcion() {
		super();
	}

	public ValoresExcepcion(String s) {
		super(s);
	}
}
