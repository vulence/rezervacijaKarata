package karte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Karte implements Serializable {
	private static final long serialVersionUID = -7651597756294375649L;
	int id;
	boolean vip;
	String ime, prezime, jmbg, email;
	
	public Karte(int id, String ime, String prezime, String jmbg, String email, boolean vip) {
		this.id = id;
		this.ime = ime;
		this.prezime = prezime;
		this.jmbg = jmbg;
		this.email = email;
		this.vip = vip;
	}
	
	@Override
	public String toString() {
		return "ID - " + id + ", Ime - " + ime + ", Prezime - " + prezime + ", Jmbg - " + jmbg + ", Email - " + email + ", Vip: " + vip;
	}
	
	public static ArrayList<Karte> kreirajKarte() {
		ArrayList<Karte> pom = new ArrayList<>();
		for (int i = 0; i < 20; i++) pom.add(new Karte(i+1, null, null, null, null, false));
		for (int i = 0; i < 5; i++) pom.add(new Karte(21+i, null, null, null, null, true));
		
		return pom;
	}
	
	public static void sacuvajKarte(ArrayList<Karte> karte) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = new FileOutputStream("dostupneKarte");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(karte);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			try {
				if (fos != null) fos.close();
				if (oos != null) oos.close();
			}
			catch (Exception e) {}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Karte> ucitajKarte() {
		ArrayList<Karte> karte = new ArrayList<>();
		File f = new File("dostupneKarte");
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		if (f.exists()) {
			try {
				fis = new FileInputStream(f);
				ois = new ObjectInputStream(fis);
				
				karte = (ArrayList<Karte>) ois.readObject();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (ois != null) ois.close();
					if (fis != null) fis.close();
				}
				catch (Exception e) {}
			}
		}
		
		else {
			karte = kreirajKarte();
			Karte.sacuvajKarte(karte);
		}
		
		return karte;
	}
	
	public static int[] proveriDostupnost(ArrayList<Karte> karte) {
		int br[] = {0, 0};
		for (int i = 0; i < karte.size(); i++) {
			if (karte.get(i).ime == null) {
				if (karte.get(i).vip) br[1]++;
				else br[0]++;
			}
		}
		
		return br;
	}
	
	public static boolean rezervisiKartu(ArrayList<Karte> karte, String ime, String prezime, String jmbg, String email, boolean vip) {
		if (!proveriKorisnika(karte, jmbg)) return false;
		
		for (int i = 0; i < karte.size(); i++) {
			if (karte.get(i).ime == null && karte.get(i).vip == vip) {
				karte.get(i).ime = ime;
				karte.get(i).prezime = prezime;
				karte.get(i).jmbg = jmbg;
				karte.get(i).email = email;
				karte.get(i).vip = vip;
				Karte.sacuvajKarte(karte);
				Karte.kartaUTxt(karte.get(i).id, ime, prezime, jmbg, email, karte.get(i).vip);
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean proveriKorisnika(ArrayList<Karte> karte, String jmbg) {
		int br = 0;
		for (int i = 0; i < karte.size(); i++) {
			if (karte.get(i).jmbg == null) break;
			else if (karte.get(i).jmbg.equals(jmbg)) br++;
		}
		
		if (br == 4) return false;
		return true;
	}
	
	public static void kartaUTxt(int id, String ime, String prezime, String jmbg, String email, boolean vip) {
		FileWriter fw = null;
		String FileName = "kartaKorisnik" + jmbg + "1" + ".txt";
		File f = new File(FileName);
		int i = 2;
		while (f.exists()) {
			FileName = "kartaKorisnik" + jmbg + i + ".txt";
			f = new File(FileName);
			i++;
		}
		
		try {
			fw = new FileWriter(FileName);
			String text = "ID Karte: " + id + "\n Ime: " + ime + "\n Prezime: " + prezime + "\n JMBG: " + jmbg + "\n Email: " + email
					+ "\n VIP: " + vip;
			fw.write(text);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (fw != null) fw.close();
			}
			catch (IOException e) {}
		}
	}
	
	public static boolean ponistiKartu(ArrayList<Karte> karte, String jmbg, boolean vip) throws Exception {	
		int i = 10;
		boolean uspesno = false;
		BufferedReader br = null;
		String FileName = "kartaKorisnik" + jmbg + i + ".txt";
		File f = new File(FileName);
		while (!f.exists() && i >= 0) {
			i--;
			FileName = "kartaKorisnik" + jmbg + i + ".txt";
			f = new File(FileName);
		}
		
		while (i > 0 && f.exists()) {
			br = new BufferedReader(new FileReader(FileName));
			String line = br.readLine();
			while (line != null) {
				if (line.contains("VIP")) {
					if ((line.trim().equals("VIP: false") && !vip) || (line.trim().equals("VIP: true") && vip)) uspesno = true;
					break;
				}
				
				line = br.readLine();
			}
			
			if (uspesno) break;
			
			i--;
			FileName = "kartaKorisnik" + jmbg + i + ".txt";
			f = new File(FileName);
		}
		
		if (br != null) br.close();
		
		if (!uspesno) return uspesno;
		
		System.gc();
		if (f.delete()) {
			if (vip) {
				for (i = 24; i > 19; i--) {
					if (karte.get(i).jmbg != null && karte.get(i).jmbg.equals(jmbg)) {
						karte.get(i).ime = null;
						karte.get(i).prezime = null;
						karte.get(i).jmbg = null;
						karte.get(i).email = null;
						break;
					}
				}
			}
			else {
				for (i = 19; i >= 0; i--) {
					if (karte.get(i).jmbg != null && karte.get(i).jmbg.equals(jmbg)) {
						karte.get(i).ime = null;
						karte.get(i).prezime = null;
						karte.get(i).jmbg = null;
						karte.get(i).email = null;
						break;
				}
			}
		}
			Karte.sacuvajKarte(karte);
			return uspesno;
		}
		
		return false;
	}
}
