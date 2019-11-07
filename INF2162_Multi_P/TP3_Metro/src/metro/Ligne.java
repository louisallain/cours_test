package metro;

public class Ligne {

	private int numero;

	public Ligne(int numero_) {

		this.numero = numero_;
	}

	public Integer getNumero() {

		return this.numero;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this) 
			return true; 

		if (obj == null || obj.getClass() != this.getClass()) 
			return false; 

		Ligne uneLigne = (Ligne) obj;
		return this.numero == uneLigne.numero;
	}

	@Override
	public int hashCode() {
		return numero;
	}

	@Override
	public String toString() {
		return new Integer(this.numero).toString();
	}
}