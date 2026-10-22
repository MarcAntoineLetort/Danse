package application;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * Extrait musical joué au survol d'une danse.
 * Tous les appels à VLC passent par UN seul thread dédié : le thread JavaFX
 * n'appelle jamais VLC directement (un appel natif bloquant ferait figer toute
 * l'interface), et un survol rapide est « débouncé » pour ne pas enchaîner
 * des dizaines de stop/play.
 */
public class LecteurAudioVLC {

	private static final long DELAI_SURVOL_MS = 180;

	private final MediaPlayerFactory factory;
	private final EmbeddedMediaPlayer audioPlayer;
	private final ScheduledExecutorService vlc;
	private ScheduledFuture<?> enAttente;
	private long generation = 0;
	private volatile boolean enLecture = false;

	public LecteurAudioVLC() {
		String[] vlcArgs = { "--intf", "dummy", "--no-video", "--quiet", "--no-xlib" };
		factory = new MediaPlayerFactory(vlcArgs);
		audioPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();

		ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "vlc-audio");
			t.setDaemon(true);
			return t;
		});
		ex.setRemoveOnCancelPolicy(true);
		vlc = ex;

		// Les écouteurs ne font QUE mettre à jour un indicateur (jamais d'appel à VLC ici)
		audioPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mp) {
				enLecture = true;
			}

			@Override
			public void paused(MediaPlayer mp) {
				enLecture = false;
			}

			@Override
			public void stopped(MediaPlayer mp) {
				enLecture = false;
			}

			@Override
			public void finished(MediaPlayer mp) {
				enLecture = false;
			}

			@Override
			public void error(MediaPlayer mp) {
				enLecture = false;
				Diagnostic.log("Erreur VLC (lecteur audio)");
			}
		});
	}

	/** Joue un extrait de la vidéo/musique, après un court délai anti-rafale. */
	public synchronized void apercu(File fichier, int departSecondes, int volumePourcent) {
		final long id = ++generation;
		if (enAttente != null) {
			enAttente.cancel(false);
		}
		enAttente = vlc.schedule(() -> {
			if (id != generation_() || fichier == null) {
				return;
			}
			try {
				if (!fichier.exists()) {
					Diagnostic.log("Fichier audio introuvable : " + fichier);
					return;
				}
				audioPlayer.videoSurface().set(null);
				audioPlayer.media().play(fichier.getAbsolutePath(), ":start-time=" + departSecondes);
				audioPlayer.audio().setVolume(volumePourcent);
			} catch (Exception e) {
				Diagnostic.log("Échec de l'aperçu audio", e);
			}
		}, DELAI_SURVOL_MS, TimeUnit.MILLISECONDS);
	}

	private synchronized long generation_() {
		return generation;
	}

	/** Arrêt asynchrone : l'appelant n'est jamais bloqué. */
	public synchronized void arreter() {
		generation++;
		enLecture = false;
		if (enAttente != null) {
			enAttente.cancel(false);
			enAttente = null;
		}
		try {
			vlc.execute(() -> {
				try {
					audioPlayer.controls().stop();
				} catch (Exception e) {
					Diagnostic.log("Échec de l'arrêt audio", e);
				}
			});
		} catch (java.util.concurrent.RejectedExecutionException e) {
			// déjà libéré
		}
	}

	public boolean estEnLecture() {
		return enLecture;
	}

	public void liberer() {
		synchronized (this) {
			generation++;
			if (enAttente != null) {
				enAttente.cancel(false);
			}
		}
		try {
			vlc.execute(() -> {
				try {
					audioPlayer.controls().stop();
					audioPlayer.release();
					factory.release();
				} catch (Exception e) {
					Diagnostic.log("Échec de la libération du lecteur audio", e);
				}
			});
			vlc.shutdown();
			vlc.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (java.util.concurrent.RejectedExecutionException e) {
			// déjà libéré
		}
	}
}
