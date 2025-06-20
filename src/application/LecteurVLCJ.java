package application;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class LecteurVLCJ {

	private final MediaPlayerFactory factory;
	private final EmbeddedMediaPlayer mediaPlayer;
	public final ImageView imageView;

	private Runnable onVideoFinished;
	
	public boolean estEnPleinEcran = false;

	public LecteurVLCJ() {
		this.factory = new MediaPlayerFactory();
		this.mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
		this.imageView = new ImageView();

		imageView.setSmooth(true);

		// Configurer la surface vidéo avec l'ImageView
		mediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));

		// Gérer la fin de la vidéo
		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void finished(MediaPlayer mediaPlayer) {
				Platform.runLater(() -> {
					if (onVideoFinished != null)
						onVideoFinished.run();
				});
			}
			public void playing(MediaPlayer mediaPlayer) {
				 new Thread(() -> {
				        try {
				            Thread.sleep(500); // Laisser le temps à VLC de charger les pistes
				        } catch (InterruptedException ignored) {}
				        Platform.runLater(() -> {
				            mediaPlayer.audio().setMute(false);
				            mediaPlayer.audio().setVolume(VariableUtile.volumeRecommande);
				        });
				    }).start();
		    }

		    @Override
		    public void error(MediaPlayer mediaPlayer) {
		        MainDanse.afficherErreur("Erreur de lecture !");
		    }
		});
	}

	public void demarrer(File fichierVideo) {
		if (VariableUtile.lecteurAudio != null) {
			VariableUtile.lecteurAudio.arreter();
		}
		if (fichierVideo != null) {
			VariableUtile.volumeRecommande = AnalyseurVolume.ajusterVolume(fichierVideo);
			mediaPlayer.media().play(fichierVideo.getAbsolutePath());
		} else {
			mediaPlayer.controls().play();
		}
		
		if(VariableUtile.lecteurAudio.estEnLecture()) {
			MainDanse.afficherErreur("Lecteur vidéo démarré pendant que le lecteur audio est en cours");
		}
	}

	public void arreter() {
		mediaPlayer.controls().stop();
	}

	public void liberer() {
		mediaPlayer.release();
		factory.release();
	}

	public void setOnVideoFinished(Runnable action) {
		this.onVideoFinished = action;
	}

	public boolean estEnLecture() {
		return mediaPlayer.status().isPlaying();
	}

	public boolean estEnPause() {
		return mediaPlayer.status().state() == State.PAUSED;
	}

	public boolean estPret() {
		return mediaPlayer != null && mediaPlayer.status().isPlayable();
	}

	public long tempsActuelMillis() {
		return mediaPlayer.status().time();
	}

	public void lancerAuTemps(long millis) {
		mediaPlayer.controls().setTime(millis);
	}

	public void volume(int volume) {
		mediaPlayer.audio().setVolume(volume);
	}

	public void pause() {
		mediaPlayer.controls().pause();
	}

}
