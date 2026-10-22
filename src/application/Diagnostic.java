package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javafx.application.Platform;

/**
 * Journal + détection de gel de l'interface.
 * Tout ce qui se passe (erreurs, exceptions, gels) est écrit dans danse.log,
 * ce qui permet de comprendre un blocage de la version compilée (sans console).
 */
public final class Diagnostic {

	private static File fichierLog;
	private static volatile boolean pingEnCours = false;
	private static volatile long dateDernierPing = 0;
	private static boolean gelDejaSignale = false;
	private static File dossierApp;

	private Diagnostic() {
	}

	/** Dossier de l'application (celui du jar, ou du projet sous Eclipse). */
	public static synchronized File dossierApp() {
		if (dossierApp == null) {
			try {
				String path = MainDanse.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				if (path == null || path.isEmpty() || path.startsWith("rsrc")) {
					path = new File(".").getAbsolutePath();
				}
				dossierApp = new File(path).getParentFile();
			} catch (Exception e) {
				dossierApp = new File(System.getProperty("user.dir"));
			}
			if (dossierApp == null) {
				dossierApp = new File(System.getProperty("user.dir"));
			}
		}
		return dossierApp;
	}

	/** Fichier à écrire : à côté de l'appli si possible, sinon dans le dossier utilisateur. */
	public static File fichierEcriture(String nom) {
		File dossier = dossierApp();
		if (!dossier.canWrite()) {
			dossier = new File(System.getProperty("user.home"), "DanseLogs");
			dossier.mkdirs();
		}
		return new File(dossier, nom);
	}

	public static void init() {
		fichierLog = fichierEcriture("danse.log");
		if (fichierLog.length() > 2_000_000) {
			File ancien = new File(fichierLog.getPath() + ".old");
			ancien.delete();
			fichierLog.renameTo(ancien);
		}
		log("==== Démarrage — Java " + System.getProperty("java.version") + ", JavaFX "
				+ System.getProperty("javafx.runtime.version") + ", " + System.getProperty("os.name") + " ====");

		// Tout ce qui sort sur la console (dont les erreurs du moteur de rendu JavaFX,
		// invisibles en version compilée) est aussi copié dans le journal.
		try {
			OutputStream flux = new FileOutputStream(fichierLog, true);
			System.setErr(new PrintStream(new Tee(System.err, flux), true, "UTF-8"));
			System.setOut(new PrintStream(new Tee(System.out, flux), true, "UTF-8"));
		} catch (IOException e) {
			// pas grave
		}

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> log("Exception non gérée dans " + t.getName(), e));
		demarrerSurveillance();
	}

	public static synchronized void log(String message) {
		String ligne = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "  " + message
				+ System.lineSeparator();
		if (fichierLog == null) {
			System.out.print(ligne);
			return;
		}
		try (FileOutputStream out = new FileOutputStream(fichierLog, true)) {
			out.write(ligne.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// impossible d'écrire le journal : on ne fait rien de plus
		}
	}

	public static void log(String message, Throwable e) {
		StringBuilder sb = new StringBuilder(message).append(" : ").append(e);
		for (StackTraceElement el : e.getStackTrace()) {
			sb.append(System.lineSeparator()).append("    at ").append(el);
		}
		log(sb.toString());
	}

	/**
	 * Toutes les secondes, on demande à l'interface de répondre. Si elle ne répond
	 * plus depuis 4 s, on écrit dans le journal ce que fait chaque thread (donc
	 * l'endroit exact où le thread JavaFX est bloqué).
	 */
	private static void demarrerSurveillance() {
		Thread t = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				long maintenant = System.currentTimeMillis();
				if (!pingEnCours) {
					pingEnCours = true;
					dateDernierPing = maintenant;
					gelDejaSignale = false;
					try {
						Platform.runLater(() -> pingEnCours = false);
					} catch (IllegalStateException e) {
						return; // JavaFX arrêté
					}
				} else if (maintenant - dateDernierPing > 4000 && !gelDejaSignale) {
					gelDejaSignale = true;
					ecrireEtatThreads(maintenant - dateDernierPing);
				}
			}
		}, "surveillance-interface");
		t.setDaemon(true);
		t.start();
	}

	private static void ecrireEtatThreads(long dureeMs) {
		StringBuilder sb = new StringBuilder();
		sb.append("!!! INTERFACE FIGÉE depuis ").append(dureeMs / 1000).append(" s — état des threads :");
		for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
			Thread th = e.getKey();
			sb.append(System.lineSeparator()).append("--- ").append(th.getName()).append(" [").append(th.getState())
					.append("]");
			for (StackTraceElement el : e.getValue()) {
				sb.append(System.lineSeparator()).append("      ").append(el);
			}
		}
		long[] bloques = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
		if (bloques != null) {
			sb.append(System.lineSeparator()).append("!!! INTERBLOCAGE (deadlock) détecté entre :");
			for (ThreadInfo ti : ManagementFactory.getThreadMXBean().getThreadInfo(bloques)) {
				sb.append(System.lineSeparator()).append("    ").append(ti.getThreadName()).append(" attend ")
						.append(ti.getLockName()).append(" détenu par ").append(ti.getLockOwnerName());
			}
		}
		log(sb.toString());
	}

	private static class Tee extends OutputStream {
		private final OutputStream a;
		private final OutputStream b;

		Tee(OutputStream a, OutputStream b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public synchronized void write(int c) throws IOException {
			a.write(c);
			b.write(c);
		}

		@Override
		public synchronized void write(byte[] buf, int off, int len) throws IOException {
			a.write(buf, off, len);
			b.write(buf, off, len);
		}

		@Override
		public void flush() throws IOException {
			a.flush();
			b.flush();
		}
	}
}
