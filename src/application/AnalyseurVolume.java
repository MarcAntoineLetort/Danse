package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calcule le volume de lecture conseillé d'une vidéo avec ffmpeg (volumedetect).
 * ATTENTION : ajusterVolume() peut durer plusieurs secondes (première lecture
 * d'une vidéo) : ne JAMAIS l'appeler depuis le thread JavaFX.
 */
public class AnalyseurVolume {

	private static final Pattern MAX_VOLUME = Pattern.compile("max_volume:\\s*(-?[0-9.]+)\\s*dB");
	private static final Map<String, Double> cache = new HashMap<>();
	private static final Set<String> echecs = new HashSet<>();
	private static boolean cacheCharge = false;

	private static File fichierCache() {
		return Diagnostic.fichierEcriture("volumes_cache.txt");
	}

	private static synchronized void chargerCache() {
		if (cacheCharge) {
			return;
		}
		cacheCharge = true;
		File f = fichierCache();
		if (!f.exists()) {
			return;
		}
		try (BufferedReader reader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				int i = line.lastIndexOf(';');
				if (i > 0) {
					try {
						cache.put(line.substring(0, i), Double.parseDouble(line.substring(i + 1).trim()));
					} catch (NumberFormatException e) {
						// ligne ignorée
					}
				}
			}
		} catch (IOException e) {
			Diagnostic.log("Lecture du cache de volumes impossible", e);
		}
	}

	/** Lance ffmpeg sur les 30 premières secondes ; renvoie NaN en cas d'échec. */
	public static double analyserVolume(File video) {
		File ffmpegExe = new File(Diagnostic.dossierApp(), "ffmpeg/ffmpeg.exe");
		if (!ffmpegExe.exists()) {
			ffmpegExe = new File(System.getProperty("user.dir"), "ffmpeg/ffmpeg.exe");
		}
		if (!ffmpegExe.exists()) {
			Diagnostic.log("ffmpeg introuvable : " + ffmpegExe.getAbsolutePath());
			return Double.NaN;
		}
		File log = null;
		Process p = null;
		try {
			log = File.createTempFile("volume_", ".log");
			// -nostdin : sans cela ffmpeg peut rester en attente sur son entrée standard
			ProcessBuilder pb = new ProcessBuilder(ffmpegExe.getAbsolutePath(), "-nostdin", "-hide_banner", "-t", "30",
					"-i", video.getAbsolutePath(), "-vn", "-af", "volumedetect", "-f", "null", "-");
			pb.redirectErrorStream(false);
			pb.redirectError(ProcessBuilder.Redirect.to(log));
			pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
			p = pb.start();
			if (!p.waitFor(25, TimeUnit.SECONDS)) {
				p.destroyForcibly();
				Diagnostic.log("ffmpeg trop long (>25 s) sur " + video.getName());
				return Double.NaN;
			}
			return lireVolumeMax(log);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (p != null) {
				p.destroyForcibly();
			}
			return Double.NaN;
		} catch (IOException e) {
			Diagnostic.log("Échec de l'analyse du volume de " + video.getName(), e);
			return Double.NaN;
		} finally {
			if (log != null) {
				log.delete();
			}
		}
	}

	public static double lireVolumeMax(File logFile) {
		try (BufferedReader reader = Files.newBufferedReader(logFile.toPath(), StandardCharsets.ISO_8859_1)) {
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = MAX_VOLUME.matcher(line);
				if (m.find()) {
					return Double.parseDouble(m.group(1));
				}
			}
		} catch (IOException | NumberFormatException e) {
			Diagnostic.log("Erreur de lecture du résultat ffmpeg", e);
		}
		return Double.NaN;
	}

	public static int calculerVolumePourVLCJ(double maxVolumeDb) {
		if (Double.isNaN(maxVolumeDb)) {
			return 100; // volume par défaut
		}
		double gain = -maxVolumeDb;
		int volume = (int) (100 * Math.pow(10, gain / 20));
		return Math.max(20, Math.min(volume, 200)); // 200 = limite de VLC
	}

	/** À appeler hors du thread JavaFX. */
	public static int ajusterVolume(File video) {
		String nomVideo = video.getName();
		synchronized (AnalyseurVolume.class) {
			chargerCache();
			Double connu = cache.get(nomVideo);
			if (connu != null) {
				return calculerVolumePourVLCJ(connu);
			}
			if (echecs.contains(nomVideo)) {
				return 100;
			}
		}
		double maxDb = analyserVolume(video);
		synchronized (AnalyseurVolume.class) {
			if (Double.isNaN(maxDb)) {
				echecs.add(nomVideo); // on ne réessaie pas à chaque lecture
				return 100;
			}
			cache.put(nomVideo, maxDb);
			try (FileWriter writer = new FileWriter(fichierCache(), StandardCharsets.UTF_8, true)) {
				writer.write(nomVideo + ";" + String.format(Locale.ROOT, "%s", maxDb) + System.lineSeparator());
			} catch (IOException e) {
				Diagnostic.log("Écriture du cache de volumes impossible", e);
			}
		}
		return calculerVolumePourVLCJ(maxDb);
	}
}
