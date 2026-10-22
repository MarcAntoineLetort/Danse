package application;

import java.io.File;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * Lecteur vidéo plein écran.
 * Règles de robustesse :
 *  - toutes les commandes VLC (et l'analyse ffmpeg du volume) s'exécutent sur un
 *    thread dédié, jamais sur le thread JavaFX ;
 *  - l'état (lecture / pause / temps) est mémorisé via les événements VLC, donc
 *    les méthodes d'interrogation ne bloquent jamais ;
 *  - les écouteurs d'événements n'appellent jamais VLC (risque d'interblocage).
 */
public class LecteurVLCJ {

	private final MediaPlayerFactory factory;
	private final EmbeddedMediaPlayer mediaPlayer;
	private final ScheduledThreadPoolExecutor vlc;
	public final ImageView imageView;

	private volatile Runnable onVideoFinished;
	private volatile boolean enLecture = false;
	private volatile boolean enPause = false;
	private volatile long tempsMillis = 0;

	public boolean estEnPleinEcran = false;

	public LecteurVLCJ() {
		this.factory = new MediaPlayerFactory();
		this.mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
		this.imageView = new ImageView();
		imageView.setSmooth(true);

		vlc = new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "vlc-video");
			t.setDaemon(true);
			return t;
		});

		mediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));

		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void finished(MediaPlayer mp) {
				enLecture = false;
				enPause = false;
				Platform.runLater(() -> {
					Runnable r = onVideoFinished;
					if (r != null) {
						r.run();
					}
				});
			}

			@Override
			public void playing(MediaPlayer mp) {
				enLecture = true;
				enPause = false;
				// Réglage du volume 0,5 s plus tard, sur le thread VLC (pas dans l'écouteur)
				executer(500, () -> {
					mediaPlayer.audio().setMute(false);
					mediaPlayer.audio().setVolume(VariableUtile.volumeRecommande);
				});
			}

			@Override
			public void paused(MediaPlayer mp) {
				enLecture = false;
				enPause = true;
			}

			@Override
			public void stopped(MediaPlayer mp) {
				enLecture = false;
				enPause = false;
			}

			@Override
			public void timeChanged(MediaPlayer mp, long newTime) {
				tempsMillis = newTime;
			}

			@Override
			public void error(MediaPlayer mp) {
				enLecture = false;
				MainDanse.afficherErreur("Erreur de lecture de la vidéo");
			}
		});
	}

	private void executer(long delaiMs, Runnable action) {
		try {
			vlc.schedule(() -> {
				try {
					action.run();
				} catch (Exception e) {
					Diagnostic.log("Échec d'une commande VLC (vidéo)", e);
				}
			}, delaiMs, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			// lecteur déjà libéré
		}
	}

	private void executer(Runnable action) {
		executer(0, action);
	}

	/** Lance la vidéo ; avec null, reprend la lecture en cours. Ne bloque pas. */
	public void demarrer(File fichierVideo) {
		if (VariableUtile.lecteurAudio != null) {
			VariableUtile.lecteurAudio.arreter();
		}
		if (fichierVideo != null) {
			executer(() -> {
				// ffmpeg peut prendre quelques secondes la 1re fois : on est sur le thread VLC
				VariableUtile.volumeRecommande = AnalyseurVolume.ajusterVolume(fichierVideo);
				mediaPlayer.media().play(fichierVideo.getAbsolutePath());
			});
		} else {
			executer(() -> mediaPlayer.controls().play());
		}
	}

	public void arreter() {
		enLecture = false;
		enPause = false;
		executer(() -> mediaPlayer.controls().stop());
	}

	public void liberer() {
		try {
			vlc.execute(() -> {
				try {
					mediaPlayer.controls().stop();
					mediaPlayer.release();
					factory.release();
				} catch (Exception e) {
					Diagnostic.log("Échec de la libération du lecteur vidéo", e);
				}
			});
			vlc.shutdown();
			vlc.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (RejectedExecutionException e) {
			// déjà libéré
		}
	}

	public void setOnVideoFinished(Runnable action) {
		this.onVideoFinished = action;
	}

	public boolean estEnLecture() {
		return enLecture;
	}

	public boolean estEnPause() {
		return enPause;
	}

	public boolean estPret() {
		return mediaPlayer != null;
	}

	public long tempsActuelMillis() {
		return tempsMillis;
	}

	public void lancerAuTemps(long millis) {
		tempsMillis = millis;
		executer(() -> mediaPlayer.controls().setTime(millis));
	}

	public void volume(int volume) {
		executer(() -> mediaPlayer.audio().setVolume(volume));
	}

	public void pause() {
		executer(() -> mediaPlayer.controls().pause());
	}
}
