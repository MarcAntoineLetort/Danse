package application;

import java.io.File;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class LecteurAudioVLC {

//	private MediaPlayer audioPlayer;
	private final DoubleProperty volumeProperty = new SimpleDoubleProperty(0.0);
	private final MediaPlayerFactory factory;
	private final EmbeddedMediaPlayer audioPlayer;

	public LecteurAudioVLC() {
		String[] vlcArgs = {
                "--intf", "dummy",
                "--no-video",
                "--quiet",
                "--no-xlib"
        };
		factory = new MediaPlayerFactory(vlcArgs);
		this.audioPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
		
//		player = factory.mediaPlayers().newEmbeddedMediaPlayer();
		
		// Synchroniser la propriété observable avec le volume réel du lecteur VLC
		volumeProperty.addListener((obs, oldVal, newVal) -> {
			int volumeVLC = (int) Math.round(newVal.doubleValue() * 100);
			audioPlayer.audio().setVolume(volumeVLC);
		});
	}

	public void arreter() {
		audioPlayer.controls().stop();
	}

	public void liberer() {
		audioPlayer.release();
		factory.release();
	}

	public void volume(double volume) {
		volumeProperty.set(volume);
	}

	public void definirTempsDepart(int secondes) {
		audioPlayer.controls().setTime(secondes * 1000L);
	}

	public void demarrer() {
		audioPlayer.controls().play();
		
		if(VariableUtile.lecteurVLCJ.estEnLecture()) {
			MainDanse.afficherErreur("Lecteur audio démarré pendant que le lecteur vidéo est en cours");
		}
	}

	public void chargerMedia(String cheminAbsolu) {
		if (cheminAbsolu == null || cheminAbsolu.isEmpty())
			return;

		File file = new File(cheminAbsolu);
		if (!file.exists()) {
			MainDanse.afficherErreur("Le fichier n'existe pas : " + cheminAbsolu);
			return;
		}

		// Pas de surface vidéo = pas d'affichage
		audioPlayer.videoSurface().set(null);

		boolean ok = audioPlayer.media().prepare(file.getAbsolutePath());
		if (!ok) {
			MainDanse.afficherErreur("Erreur lors de la préparation du fichier audio");
			return;
		}

		demarrer();
		audioPlayer.controls().setPause(true);
	}

	public DoubleProperty volumeProperty() {
		return volumeProperty;
	}

	public void setVolume(double value) {
		volumeProperty.set(value);
	}
	
	public boolean estEnLecture() {
		return audioPlayer.status().isPlaying();
	}
	
//	public void arreterAvecFondu(double dureeSecondes) {
//        Timeline fadeOut = new Timeline();
//        KeyValue kv = new KeyValue(volumeProperty, 0.0);
//        KeyFrame kf = new KeyFrame(Duration.seconds(dureeSecondes), kv);
//        fadeOut.getKeyFrames().add(kf);
//
//        fadeOut.setOnFinished(e -> arreter());
//        fadeOut.play();
//    }

}
