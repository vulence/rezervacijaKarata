package korisnici;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Korisnik implements Serializable {
	private static final long serialVersionUID = -2289533121016153958L;
	String username, password, ime, prezime, JMBG, email;
	
	public Korisnik(String username, String password, String ime, String prezime, String JMBG, String email) {
		this.username = username;
		this.password = password;
		this.ime = ime;
		this.prezime = prezime;
		this.JMBG = JMBG;
		this.email = email;
	}
	
	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getIme() {
		return ime;
	}



	public void setIme(String ime) {
		this.ime = ime;
	}



	public String getPrezime() {
		return prezime;
	}



	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}



	public String getJMBG() {
		return JMBG;
	}



	public void setJMBG(String jMBG) {
		JMBG = jMBG;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}



	@Override
	public String toString() {
		return "Username: " + username + ", Ime - " + ime + ", Prezime - " + prezime + ", Jmbg - " + JMBG + ", Email - " + email;
	}
	
	public static ArrayList<Korisnik> kreirajKorisnike() {
		ArrayList<Korisnik> pom = new ArrayList<>();
		pom.add(new Korisnik(null, null, null, null, null, null));
		
		return pom;
	}
	
	public static void sacuvajKorisnike(ArrayList<Korisnik> korisnici) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = new FileOutputStream("bazaKorisnika");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(korisnici);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (oos != null) oos.close();
				if (fos != null) fos.close();
			}
			catch (Exception e) {}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Korisnik> ucitajKorisnike() {
		ArrayList<Korisnik> korisnici = new ArrayList<>();
		File f = new File("bazaKorisnika");
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		if (f.exists()) {
			try {
				fis = new FileInputStream(f);
				ois = new ObjectInputStream(fis);
				
				korisnici = (ArrayList<Korisnik>) ois.readObject();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (fis != null) fis.close();
					if (ois != null) ois.close();
				}
				catch (Exception e) {}
			}
		}
		
		else {
			korisnici = kreirajKorisnike();
			Korisnik.sacuvajKorisnike(korisnici);
		}
		
		return korisnici;
	}
	
	public static Korisnik korisnikPostoji(String ime, String sifra, ArrayList<Korisnik> korisnici) {
		for (int i = 0; i < korisnici.size(); i++) {
			if (korisnici.get(i).getUsername() == null) return null;
			if (korisnici.get(i).getUsername().equals(ime) && korisnici.get(i).password.equals(sifra)) return korisnici.get(i);
		}
		
		return null;
	}
	
	public static boolean validnoKorisnickoIme(String username, ArrayList<Korisnik> korisnici) {
		for (int i = 0; i < korisnici.size(); i++) {
			if (korisnici.get(i).getUsername() == null) return true;
			if (korisnici.get(i).getUsername().equals(username)) return false;
		}
		
		return true;
	}
	
	public static boolean validanJMBG(String JMBG, ArrayList<Korisnik> korisnici) {
		for (int i = 0; i < korisnici.size(); i++) {
			if (korisnici.get(i).getJMBG() == null) return true;
			if (korisnici.get(i).getJMBG().equals(JMBG)) return false;
		}
		
		return true;
	}
	
	public static void napraviKorisnika(String username, String sifra, String ime, String prezime, String JMBG, String email, ArrayList<Korisnik> korisnici) {
		Korisnik novi = new Korisnik(username, sifra, ime, prezime, JMBG, email);
		
		for (int i = 0; i < korisnici.size(); i++) {
			if (korisnici.get(i).getUsername() == null) {
				korisnici.set(i, novi);
				Korisnik.sacuvajKorisnike(korisnici);
				return;
			}
		}
		
		korisnici.add(novi);
		Korisnik.sacuvajKorisnike(korisnici);
 	}
	
	public static boolean imaNalog(String jmbg, ArrayList<Korisnik> korisnici) {
		for (int i = 0; i < korisnici.size(); i++) {
			if (korisnici.get(i).getJMBG() == null) return false;
			if (korisnici.get(i).getJMBG().equals(jmbg)) return true;
		}
		
		return false;
	}
}
