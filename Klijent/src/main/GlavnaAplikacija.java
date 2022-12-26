package main;

import java.net.Socket;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.PrintStream;
import java.awt.Desktop;

public class GlavnaAplikacija {
	
	static boolean prijavljen = false;
	static String username = "";
	static String buf;
	static Socket s = null;
	static InputStream in = null;
	static BufferedReader br = null;
	static Scanner kb = null;
	static OutputStream out = null;
	static PrintStream ps = null;

	public static void main(String[] args) {
		init();
		System.out.println("Trenutno se nalazite u Guest rezimu, niste prijavljeni.");
		System.out.println("Unesite:\n1 da biste videli preostali broj karata,\n2 da biste izvrsili rezervaciju"
				+ ",\n3 da se prijavite sa postojecim nalogom,\n4 da se registrujete,\nexit da biste izasli iz aplikacije: ");
		try {
			String str;
			
			while (!(str = kb.nextLine()).equals("exit")) {
				ps.println(str);
				
				switch (str) {
				case "1":
					buf = br.readLine();
					System.out.println(buf);
					break;
				case "2":
					String tmp = rezervisiKartu(ps, kb, br);
					if (tmp.equals("Karta uspesno rezervisana!")) {
						primiKartu(br);
					}
					break;
				case "3":
					prijaviKorisnika(ps, kb, br);
					break;
				case "4":
					registrujKorisnika(ps, kb, br);
					prijaviKorisnika(ps, kb, br);
					break;
				case "5":
					if (!prijavljen) break;
					ponistiRezervaciju(ps, kb, br);
					break;
				case "6":
					if (!prijavljen) break;
					izlogujKorisnika();
					break;
				}
				
				if (prijavljen) {
					System.out.println("Trenutno ste prijavljeni kao korisnik " + username + ".");
					System.out.println("Unesite:\n1 da biste videli preostali broj karata,\n2 da biste izvrsili rezervaciju"
						+ ",\n3 da se prijavite sa postojecim nalogom,\n4 da se registrujete,\n5 da ponistite rezervaciju,"
						+ "\n6 da se izlogujete,\nexit da biste izasli iz aplikacije: ");
				}
					
				else {
					System.out.println("Trenutno se nalazite u Guest rezimu, niste prijavljeni.");
					System.out.println("Unesite:\n1 da biste videli preostali broj karata,\n2 da biste izvrsili rezervaciju"
						+ ",\n3 da se prijavite sa postojecim nalogom,\n4 da se registrujete,\nexit da biste izasli iz aplikacije: ");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			exit();
		}
	}
	
	private static void init() {
		try {
			s = new Socket("localhost", 4444);
	
			in = s.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			kb = new Scanner(System.in);
			out = s.getOutputStream();
			ps = new PrintStream(out, true);
			
			buf = br.readLine();
			System.out.println(buf);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void exit() {
		try {
			if (kb != null) kb.close();
			if (br != null) br.close();
			if (s != null) s.close();
			if (ps != null) ps.close();
		}
		catch (IOException e) {}
	}
	
	private static boolean JMBGValidan(String jmbg) {
		if (!jmbg.chars().allMatch(Character::isDigit)) {
			System.out.println("JMBG se sastoji samo iz cifara!");
			return false;
		}
		
		if (jmbg.length() != 13) {
			System.out.println("JMBG mora da ima tacno 13 cifara!");
			return false;
		}
		
		int dani = Integer.parseInt(jmbg.substring(0, 2));
		int meseci = Integer.parseInt(jmbg.substring(2, 4));
		
		if (dani < 1 || dani > 31) {
			System.out.println("Prve dve cifre JMBG-a moraju da budu izmedju 1 i 31!");
			return false;
		}
		else if (meseci < 1 || meseci > 12) {
			System.out.println("Druge dve cifre JMBG-a moraju da budu izmedju 1 i 12!");
			return false;
		}
		
		return true;
	}
	
	private static String rezervisiKartu(PrintStream ps, Scanner kb, BufferedReader br) throws IOException {
		String buf;
		String status = br.readLine();

		if (status.equals("1")) {
			buf = br.readLine();
			System.out.println(buf);
			String opcija = kb.nextLine();
			while (!opcija.equals("1") && !opcija.equals("2")) {
				System.out.println("Unos nije validan. Ukucajte 1 da biste rezervisali VIP kartu, 2 da rezervisete obicnu: ");
				opcija = kb.nextLine();
			}
			
			ps.println(opcija);
		}
		
		else if (status.equals("-1")) {
			buf = imePrezime(kb, "Unesite vase ime: ");
			ps.println(buf);
			buf = imePrezime(kb, "Unesite vase prezime: ");
			ps.println(buf);
			do {
				System.out.println("Unesite vas jmbg: ");
				buf = kb.nextLine();
			}
			while (!JMBGValidan(buf));
			ps.println(buf);
			if (br.readLine().equals("-1")) {
				do {
					System.out.println("Vec postoji registrovan korisnik sa ovim JMBG-om. Da li zelite da se prijavite pre "
										+ "nego sto nastavite sa registracijom? (Y/N)");
					status = kb.nextLine().toUpperCase();
				}
				while (!status.equals("Y") && !status.equals("N"));
				ps.println(status);
				if (status.equals("Y")) {
					prijaviKorisnika(ps, kb, br);
					return "-1";
				}
			}
			System.out.println("Unesite vas email: ");
			buf = kb.nextLine();
			ps.println(buf);
		}
		
		buf = br.readLine();
		System.out.println(buf);
		return buf;
	}
	
	private static void primiKartu(BufferedReader br) throws Exception {
		char[] rezultat = new char[1024];
		br.read(rezultat);
	
		FileWriter fw = null;
		String FileName = "novaKarta1.txt";
		File f = new File(FileName);
		int i = 2;
		while (f.exists()) {
			FileName = "novaKarta" + i + ".txt";
			f = new File(FileName);
			i++;
		}
		
		fw = new FileWriter(FileName);
		fw.write(rezultat);
		
		fw.close();
		
		f = new File(FileName);
		Desktop.getDesktop().open(f);
	}
	
	private static void prijaviKorisnika(PrintStream ps, Scanner kb, BufferedReader br) throws IOException {
		while (true) {
			System.out.println("Unesite vase korisnicko ime: ");
			String ime = kb.nextLine();
			ps.println(ime);
			System.out.println("Unesite vasu sifru: ");
			String sifra = kb.nextLine();
			ps.println(sifra);
			String response = br.readLine();
			System.out.println(response);
			
			if (response.equals("Uspesno ste se ulogovali!")) {
				username = ime;
				prijavljen = true;
				break;
			}
			
			String status;
			do {
				System.out.println("Da li zelite da pokusate ponovo? (Y/N): ");
				status = kb.nextLine().toUpperCase();
			}
			while (!status.equals("Y") && !status.equals("N"));
				
			ps.println(status);
			if (status.equals("N")) break;
		}
	}
	
	private static void registrujKorisnika(PrintStream ps, Scanner kb, BufferedReader br) throws Exception {
		while (true) {
			System.out.println("Unesite zeljeno korisnicko ime: ");
			String username = kb.nextLine();
			ps.println(username);
			if (br.readLine().equals("-1")) {
				System.out.println("Korisnik sa ovim korisnickim imenom vec postoji!");
				continue;
			}
			System.out.println("Unesite zeljenu sifru: ");
			String sifra = kb.nextLine();
			ps.println(sifra);
			String ime = imePrezime(kb, "Unesite ime: ");
			ps.println(ime);
			String prezime = imePrezime(kb, "Unesite prezime: ");
			ps.println(prezime);
			String jmbg;
			do {
				System.out.println("Unesite jmbg: ");
				jmbg = kb.nextLine();
			}
			while (!JMBGValidan(jmbg));
			ps.println(jmbg);
			if (br.readLine().equals("-1")) {
				System.out.println("Korisnik sa ovim jmbgom vec postoji!");
				continue;
			}
			System.out.println("Unesite email: ");
			String email = kb.nextLine();
			ps.println(email);
			break;
		}

		System.out.println(br.readLine());
	}
	
	private static void ponistiRezervaciju(PrintStream ps, Scanner kb, BufferedReader br) throws Exception {
		String status;
		do {
			System.out.println("Unesite 1 ako zelite da ponistite vasu VIP rezervaciju, 2 ako zelite da ponistite obicnu rezervaciju: ");
			status = kb.nextLine();
		}
		while (!status.equals("1") && !status.equals("2"));
		
		ps.println(status);
		if (br.readLine().equals("1")) System.out.println("Uspesno ste obrisali kartu!");
		else System.out.println("Takva karta ne postoji");
	}
	
	private static void izlogujKorisnika() {
		prijavljen = false;
		username = "";
		System.out.println("Uspesno ste se izlogovali!");
	}
	
	private static String imePrezime(Scanner kb, String prompt) {
		String rez;
		boolean brojevi;
		do {
			brojevi = false;
			System.out.println(prompt);
			rez = kb.nextLine();
			for (int i = 0; i < rez.length(); i++) {
				if (Character.isDigit(rez.charAt(i))) brojevi = true;
			}
		}
		while (rez.isBlank() || brojevi);
		
		return rez;
	}
}