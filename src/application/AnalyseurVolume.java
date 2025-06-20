package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AnalyseurVolume {

    public static void analyserVolume(File video, File logFile) throws IOException, InterruptedException {
    	String cheminScript = "C:\\Users\\marca\\Documents\\Java\\Danse\\Ressources\\imports_danses";
    	File script = new File(cheminScript + "\\analyser_volume.bat");

    	
        String commande = script + " \"" + video.getAbsolutePath() + "\" \"" + logFile.getAbsolutePath() + "\"";
        Process process = Runtime.getRuntime().exec(commande);
        process.waitFor();
    }

    public static double lireVolumeMax(File logFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("max_volume")) {
                    String[] parts = line.trim().split(":");
                    return Double.parseDouble(parts[1].replace(" dB", "").trim());
                }
            }
        } catch (IOException e) {
            MainDanse.afficherErreur("Erreur lecture log : " + e.getMessage());
        }
        return Double.NaN;
    }

    public static int calculerVolumePourVLCJ(double maxVolumeDb) {
        if (Double.isNaN(maxVolumeDb)) return 100; // volume par défaut
        double gain = -maxVolumeDb;
        int volume = (int) (100 * Math.pow(10, gain / 20));
        return Math.min(volume, 200); // limite de VLCJ
    }

    public static int ajusterVolume(File video) {
        File log = new File("volume_log.txt");
        File cache = new File("volumes_cache.txt");
        String nomVideo = video.getName();
        try {
        	//Lecture du cache des volumes max
        	if (cache.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(cache))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length == 2 && parts[0].equals(nomVideo)) {
                            double maxDb = Double.parseDouble(parts[1]);
                            return calculerVolumePourVLCJ(maxDb);
                        }
                    }
                }
            }
        	 // Sinon, analyser et ajouter au cache
            analyserVolume(video, log);
            double maxDb = lireVolumeMax(log);
            
            try (FileWriter writer = new FileWriter(cache, true)) {
                writer.write(nomVideo + ";" + maxDb + System.lineSeparator());
            }
            
            return calculerVolumePourVLCJ(maxDb);
        } catch (Exception e) {
            MainDanse.afficherErreur("Erreur ajustement volume : " + e.getMessage());
            return 100;
        }
    }
}

