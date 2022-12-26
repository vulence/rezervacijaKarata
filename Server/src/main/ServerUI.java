package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import karte.Karte;
import korisnici.Korisnik;

public class ServerUI {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		try {
			serverSocket = new ServerSocket(4444);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Serverska greska!");
		}
		
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				ServerThread st = new ServerThread(clientSocket);
				st.start();
			}
			catch (Exception e) {}
		}
	}

}

class ServerThread extends Thread {
	Socket s = null;
	
	OutputStream out = null;
	PrintStream ps = null;
	InputStream in = null;
	BufferedReader kb = null;
	ArrayList<Korisnik> korisnici = new ArrayList<>();
	ArrayList<Karte> karte = new ArrayList<>();
	Korisnik korisnik = null;

	public ServerThread(Socket s) {
		this.s = s;
	}
	
	public void run() {
		init();
		String str;
		try {
			while (!(str = kb.readLine()).equals("exit")) {
				karte = Karte.ucitajKarte();
				switch (str) {
				case "1":
					int brkarata[] = Karte.proveriDostupnost(karte);
					ps.println("Preostalo je jos " + brkarata[0] + " obicnih karata i " + brkarata[1] + " VIP karata.");
					break;
				case "2":
					rezervisiKartu(kb, ps);
					break;
				case "3":
					prijaviKorisnika(kb, ps);
					break;
				case "4":
					registrujKorisnika(kb, ps);
					prijaviKorisnika(kb, ps);
					break;
				case "5":
					if (korisnik == null) break;
					ponistiRezervaciju(kb, ps);
					break;
				case "6":
					if (korisnik == null) break;
					korisnik = null;
					break;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Korisnik je zavrsio sa radom! Gasenje tokova..");
		}
		finally {
			exit();
		}
	}
	
	private void init() {
		try {
			korisnici = Korisnik.ucitajKorisnike();
			karte = Karte.ucitajKarte();
			
			for (int i = 0; i < korisnici.size(); i++) korisnici.get(i).toString();
			
			in = s.getInputStream();
			kb = new BufferedReader(new InputStreamReader(in));
	
			out = s.getOutputStream();
			ps = new PrintStream(out, true);
	
			ps.println("Uspesno uspostavljena konekcija sa serverom!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void exit() {
		try {
			if (ps != null) {
				ps.close();
				System.out.println("PrintStream zatvoren!");
			}
			if (kb != null) {
				kb.close();
				System.out.println("BufferedReader zatvoren!");
			}
			if (in != null) {
				in.close();
				System.out.println("InputStream zatvoren!");
			}
			if (s != null) {
				s.close();
				System.out.println("Klijentski socket zatvoren!");
			}
		}
		catch (IOException e) {}
	}
	
	private void posaljiKartu(String jmbg) throws Exception {
		String FilePath = "kartaKorisnik" + jmbg + "1" + ".txt";
		int i = 2;
		File f = new File(FilePath);
		while (f.exists()) {
			FilePath = "kartaKorisnik" + jmbg + i + ".txt";
			f = new File(FilePath);
			i++;
		}
		
		FilePath = "kartaKorisnik" + jmbg + (i-2) + ".txt";
		
		BufferedReader br = new BufferedReader(new FileReader(FilePath));
		String line = br.readLine();
		String rezultat = "";
		
		while (line != null) {
			rezultat += line + "\n";
			line = br.readLine();
		}
		
		br.close();
		
		ps.println(rezultat);
	}
	
	private void prijaviKorisnika(BufferedReader kb, PrintStream ps) throws IOException {
		String ime, sifra;
		
		while (true) {
			ime = kb.readLine();
			sifra = kb.readLine();
			korisnik = Korisnik.korisnikPostoji(ime, sifra, korisnici);
			if (korisnik == null) ps.println("Korisnik sa ovim podacima ne postoji!");
			else {
				ps.println("Uspesno ste se ulogovali!");
				break;
			}
			
			if (kb.readLine().equals("N")) break;
		}
	}
	
	private void registrujKorisnika(BufferedReader kb, PrintStream ps) throws IOException {
		String username, sifra, ime, prezime, JMBG, email;
		
		while (true) {
			username = kb.readLine();
			if (Korisnik.validnoKorisnickoIme(username, korisnici)) ps.println("1");
			else {
				ps.println("-1");
				continue;
			}
			sifra = kb.readLine();
			ime = kb.readLine();
			prezime = kb.readLine();
			JMBG = kb.readLine();
			if (Korisnik.validanJMBG(JMBG, korisnici)) ps.println("1");
			else {
				ps.println("-1");
				continue;
			}
			email = kb.readLine();
			break;
		}
		
		Korisnik.napraviKorisnika(username, sifra, ime, prezime, JMBG, email, korisnici);
		ps.println("Korisnik uspesno kreiran!");
	}
	
	private void ponistiRezervaciju(BufferedReader kb, PrintStream ps) throws Exception {
		String buf = kb.readLine();
		boolean vip = false;
		if (buf.equals("1")) vip = true;
		
		if (Karte.ponistiKartu(karte, korisnik.getJMBG(), vip)) ps.println("1");
		else ps.println("-1");
	}
	
	private void rezervisiKartu(BufferedReader kb, PrintStream ps) throws Exception {
		boolean vip = false;
		if (korisnik == null) {
			ps.println("-1");
			String ime, prezime, jmbg, email;
			ime = kb.readLine();
			prezime = kb.readLine();
			jmbg = kb.readLine();
			if (Korisnik.imaNalog(jmbg, korisnici)) {
				ps.println("-1");
				if (kb.readLine().equals("Y")) {
					prijaviKorisnika(kb, ps);
					return;
				}
			}
			ps.println("1");
			email = kb.readLine();
			if (Karte.rezervisiKartu(karte, ime, prezime, jmbg, email, vip)) {
				ps.println("Karta uspesno rezervisana!");
				posaljiKartu(jmbg);
			}
			else ps.println("Ne mozete rezervisati vise od 4 karata!");
		}
		else {
			ps.println("1");
			ps.println("Da li zelite da rezervisete VIP kartu ili obicnu? Ukucajte 1 za VIP, 2 za obicnu: ");
			String opcija = kb.readLine();
			
			if (opcija.equals("1")) vip = true;
			
			if (Karte.rezervisiKartu(karte, korisnik.getIme(), korisnik.getPrezime(), korisnik.getJMBG(), korisnik.getEmail(), vip)) {
				ps.println("Karta uspesno rezervisana!");
				posaljiKartu(korisnik.getJMBG());
			}
			else ps.println("Ne mozete rezervisati vise od 4 karata!");
		}
	}
}