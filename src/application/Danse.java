package application;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Danse {
	int ordreActuel = 0;
	int ordreAjout = 0;
	String nomVideo;
	String titreMusique;
	ArrayList<String> artistes = new ArrayList<String>();
	Image imageDanse1;
	Image imageDanse2;
	ArrayList<Genre> genres = new ArrayList<Genre>();
	int intensite = 1;
	int technique = 1;
	Version version;
	boolean unlimited = false;
	Color couleur1;
	Color couleur2;

	private boolean imagesEssayees = false;

	/** Nombre maximal de danses dont les images restent en mémoire. */
	private static final int IMAGES_EN_MEMOIRE = 48;
	private static final LinkedList<Danse> recentes = new LinkedList<>();
	private static final ExecutorService CHARGEUR = Executors.newFixedThreadPool(2, r -> {
		Thread t = new Thread(r, "chargement-images");
		t.setDaemon(true);
		return t;
	});

	public Danse(String titreMusique, Version version, Color couleur1, Color couleur2) {
		super();
		this.titreMusique = titreMusique;
		if (version != null) {
			this.nomVideo = titreMusique + " - " + version.toString() + ".mp4";
			this.version = version;
		} else {
			// Nom non conforme
			this.nomVideo = titreMusique;
		}
		this.couleur1 = couleur1;
		this.couleur2 = couleur2;
		VariableUtile.danses.put(titreMusique, this);
	}

	// Constructeur utilisé pour le néant
	public Danse(String titreMusique, String titreImage, Color couleur1, Color couleur2) {
		super();
		this.nomVideo = "aucun";
		this.titreMusique = titreMusique;

		this.imageDanse1 = VariableUtile.main.importerImage("imageBouton/" + titreImage + "1.png");
		this.imageDanse2 = VariableUtile.main.importerImage("imageBouton/" + titreImage + "2.png");
		this.imagesEssayees = true;
		this.couleur1 = couleur1;
		this.couleur2 = couleur2;
	}

	public boolean imagesChargees() {
		return imageDanse1 != null;
	}

	/**
	 * Charge les deux images (à appeler hors du thread JavaFX). Les images sont
	 * réduites à la taille d'affichage : charger les 845 images en pleine
	 * résolution saturait la mémoire au fil de la navigation.
	 */
	public synchronized void importerImages() {
		if (imageDanse1 != null || imagesEssayees) {
			return;
		}
		imagesEssayees = true;
		double cible = Math.max(256, VariableUtile.px * 17 * 0.86 * 1.5);
		imageDanse1 = charger(titreMusique + "1.png", cible);
		imageDanse2 = charger(titreMusique + "2.png", cible);
	}

	private Image charger(String nomFichier, double cible) {
		try {
			File f = new File(VariableUtile.dossierImagesDanses, nomFichier);
			if (!f.exists()) {
				return null;
			}
			Image img = new Image(f.toURI().toString(), cible, cible, true, true);
			if (img.isError()) {
				Diagnostic.log("Image illisible : " + f, img.getException());
				return null;
			}
			return img;
		} catch (Exception e) {
			Diagnostic.log("Échec de l'import de l'image : " + nomFichier, e);
			return null;
		}
	}

	/** Charge les images en arrière-plan puis exécute apres sur le thread JavaFX. */
	public void importerImagesAsync(Runnable apres) {
		if (imageDanse1 != null) {
			Theme.fx(apres);
			return;
		}
		CHARGEUR.execute(() -> {
			importerImages();
			synchronized (recentes) {
				recentes.remove(this);
				recentes.addLast(this);
			}
			Theme.fx(apres);
		});
	}

	/** Libère les images des danses vues il y a longtemps (sauf celles affichées). */
	public static void limiterMemoire(java.util.Collection<Danse> affichees) {
		synchronized (recentes) {
			java.util.Iterator<Danse> it = recentes.iterator();
			while (recentes.size() > IMAGES_EN_MEMOIRE && it.hasNext()) {
				Danse d = it.next();
				if (!affichees.contains(d)) {
					d.liberer();
					it.remove();
				}
			}
		}
	}

	private synchronized void liberer() {
		imageDanse1 = null;
		imageDanse2 = null;
		imagesEssayees = false;
	}
}
