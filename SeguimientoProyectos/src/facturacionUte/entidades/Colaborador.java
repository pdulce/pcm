package facturacionUte.entidades;

public class Colaborador {

	public static final String TABLE_COLABORADORES = "colaborador";

	private String nombre, apellidos;

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String n) {
		this.nombre = n.trim();
	}

	public String getApellidos() {
		return this.apellidos;
	}

	public void setApellidos(String a) {
		this.apellidos = a.trim();
	}

	@Override
	public String toString() {
		return "Nombre Completo: " + getNombre() + " " + getApellidos();
	}

}
