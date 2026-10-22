package application;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.robot.Robot;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VariableUtile {
	public static double largeurFenetre = 500;
	public static double hauteurFenetre = 500;
	public static double px = 1;
	public static double py = 1;
	public static Color couleur1 = Color.GRAY;
	public static Color couleur2 = Color.WHITESMOKE;
	public static int page = 0;
	public static File dossierDanses;
	public static File dossierImagesDanses;
	public static File dossierImports;
	public static File fichierInfosDanses;
	public static ArrayList<Bouton> boutonsDanse = new ArrayList<>();
	public static ArrayList<Bouton> boutonsAction = new ArrayList<>();
	public static ArrayList<Bouton> boutonsMenu = new ArrayList<>();
	public static ArrayList<Bouton> boutonsFiltres = new ArrayList<>();
	public static ArrayList<Bouton> boutonsFiltresAnnexes = new ArrayList<>();
	public static ArrayList<Bouton> boutonsSelection = new ArrayList<>();
	public static ArrayList<Bouton> boutonsTri = new ArrayList<>();
	public static ArrayList<Bouton> boutonsTriAnnexes = new ArrayList<>();
	public static ArrayList<Text> textesFiltres = new ArrayList<>();
	public static ArrayList<Text> textes = new ArrayList<>();
	public static ArrayList<Node> elementAAfficher = new ArrayList<>();
	public static LecteurVLCJ lecteurVLCJ;
	public static LecteurAudioVLC lecteurAudio;
	public static RadialGradient shadePaint;
	public static Scene scene;
	public static BorderPane root;
	public static Stage primaryStage;
	public static MainDanse main;
	public static HashMap<String, Danse> danses;
	public static ArrayList<Danse> dansesFiltrees;
	public static HashMap<Integer, String> artistes;
	public static Danse danseSelectionnee;
	public static Danse danseNeant;
	public static Text textPage;
	public static DropShadow effetTextPage;
	public static Bouton boutonPrecedent;
	public static Bouton boutonSuivant;
	public static Bouton boutonQuitter;
	public static Bouton boutonRejouer;
	public static Bouton boutonPasser;
	public static Bouton boutonSuggestion;
	public static Bouton boutonFiltrer;
	public static Bouton boutonTrier;
	public static Bouton boutonIllimite;
	public static Bouton boutonMelange;
	public static Bouton boutonSelection;
	public static Bouton boutonLancerSelection;
	public static Bouton boutonAnnulerSelection;
	public static Bouton boutonReinitialiserSelection;
	public static Text textFiltreIntensite;
	public static Text textFiltreTechnique;
	public static Text texteFiltreGenre;
	public static Text texteFiltreNbDanseurs;
	public static Bouton boutonFiltreIntensiteFaible;
	public static Bouton boutonFiltreIntensiteModeree;
	public static Bouton boutonFiltreIntensiteIntense;
	public static Bouton boutonFiltreTechniqueFaible;
	public static Bouton boutonFiltreTechniqueModeree;
	public static Bouton boutonFiltreTechniqueIntense;
	public static Bouton boutonFiltreLatino;
	public static Bouton boutonFiltreElectro;
	public static Bouton boutonFiltreSportif;
	public static Bouton boutonFiltreFolklore;
	public static Bouton boutonFiltreRock;
	public static Bouton boutonFiltreRandB;
	public static Bouton boutonFiltreRetro;
	public static Bouton boutonFiltrePop;
	public static Bouton boutonFiltreJazzy;
	public static Bouton boutonFiltre1Danseur;
	public static Bouton boutonFiltre2Danseurs;
	public static Bouton boutonFiltre3Danseurs;
	public static Bouton boutonFiltre4Danseurs;
	public static Bouton boutonFiltreValider;
	public static Bouton boutonFiltreReinitialiser;
	public static Bouton boutonTriAleatoire;
	public static Bouton boutonTriAlphabetique;
	public static Bouton boutonTriArtiste;
	public static Bouton boutonTriVersion;
	public static Bouton boutonTriOrdre;
	public static Bouton boutonTriValider;
	public static boolean videoEnCours = false;
	public static boolean modeIllimite = false;
	public static boolean modeMelange = false;
	public static boolean modeSelection = false;
	public static boolean modeFiltre = false;
	public static boolean modeTri = false;
	public static int numeroProchaineDanseSelection = 0;
	public static int volumeRecommande = 100;
	public static ImageView imageViewDanseEchangeSelection;
	public static Bouton boutonGlissePourEchangeSelection;
	public static ArrayList<MediaView> mediaViewsMelange = new ArrayList<>();
	public static ArrayList<MediaPlayer> playerVideosMelange = new ArrayList<>();
	public static Timeline timelineMelange;
	private static Timeline veille;
	private static Robot robot;
	private static int sensVeille = 1;
	public static ArrayList<Danse> dansesSelectionnees;
	public static String police = "Segoe UI"; // voir Theme.famille
	public static BarreRecherche barreRecherche;

	public static Text textCompteur;

	public static int nbPages() {
		return Math.max(1, (int) Math.ceil(dansesFiltrees.size() / 6.0));
	}

	public static void genererTextPage() {
		if (textPage != null) {
			int numDanseSelectionne = 0;
			if (danseSelectionnee != null) {
				numDanseSelectionne = danseSelectionnee.ordreActuel;
			}
			textPage.setText("Danse " + numDanseSelectionne + "/" + dansesFiltrees.size() + " - Page " + (page + 1) + "/"
					+ nbPages());
		}
	}

	public static void genererBoutonSuggestion(boolean nettoyerPrecedant) {
		if (nettoyerPrecedant) {
			boutonsMenu.remove(boutonSuggestion);
			Platform.runLater(() -> {
				if (boutonSuggestion != null) {
					boutonSuggestion.setVisible(false);
					root.getChildren().remove(boutonSuggestion);
				}
			});
		}
		Platform.runLater(() -> {
			if (dansesFiltrees.size() > 0) {
				int aleaSuggestion = (int) Math.ceil(Math.random() * dansesFiltrees.size());
				Danse danse = dansesFiltrees.get(aleaSuggestion - 1);
				Bouton nouvelle = new Bouton(px * 79.5, py * 4, px * 18, py * 33, 20, danse, false, 6);
				boutonSuggestion = nouvelle;
				nouvelle.genererSuperTitre("Suggestion");
				boutonsMenu.add(nouvelle);
				// la suggestion n'est visible que si le menu principal l'est
				nouvelle.setVisible(!boutonsDanse.isEmpty() && boutonsDanse.get(0).isVisible());
				if (danse.imageDanse1 == null) {
					danse.importerImagesAsync(() -> {
						nouvelle.rafraichirImage();
						definirIconeFenetre(danse);
					});
				} else {
					definirIconeFenetre(danse);
				}
			}
		});
	}

	private static boolean iconeDefinie = false;

	private static void definirIconeFenetre(Danse danse) {
		if (!iconeDefinie && danse.imageDanse2 != null) {
			iconeDefinie = true;
			primaryStage.getIcons().add(danse.imageDanse2);
		}
	}

	public static void formerNombreCompteur(String nombre) {
		textCompteur.setText(nombre);
	}

	/** Extrait musical au survol d'une danse (asynchrone, jamais bloquant). */
	public static void apercuMusique(Danse danse) {
		if (lecteurAudio == null) {
			return;
		}
		if (danse.equals(danseNeant) || modeMelange) {
			lecteurAudio.arreter();
			return;
		}
		lecteurAudio.apercu(fichierVideo(danse), 20, 20);
	}

	public static File fichierVideo(Danse danse) {
		return new File(dossierDanses, danse.nomVideo);
	}

	public static boolean actionsVisibles() {
		for (Bouton b : boutonsAction) {
			if (b != null && b.isVisible()) {
				return true;
			}
		}
		return false;
	}

	/** Vrai quand l'utilisateur est sur un écran de menu (et pas devant une vidéo). */
	public static boolean ecranMenuActif() {
		return !(videoEnCours || modeMelange || lecteurVLCJ.estEnLecture() || lecteurVLCJ.estEnPause()
				|| lecteurVLCJ.estEnPleinEcran || actionsVisibles());
	}

	public static void lancerVideo(Danse danse) {
		if (!fichierVideo(danse).exists()) {
			MainDanse.afficherErreur("Vidéo introuvable : " + fichierVideo(danse));
			return;
		}
		MainDanse.annulerDecompte();
		lecteurVLCJ.arreter();

		File videoDanse = fichierVideo(danse);

		lecteurVLCJ.estEnPleinEcran = true;
		primaryStage.setFullScreen(true);
		lecteurVLCJ.imageView.fitWidthProperty().bind(scene.widthProperty());
		lecteurVLCJ.imageView.fitHeightProperty().bind(scene.heightProperty());
		if (!root.getChildren().contains(lecteurVLCJ.imageView)) {
			root.getChildren().add(lecteurVLCJ.imageView);
		}
		lecteurVLCJ.imageView.toFront();

		lecteurVLCJ.setOnVideoFinished(() -> actionVideoTerminee());

		lecteurVLCJ.imageView.setVisible(true);
		lecteurVLCJ.demarrer(videoDanse);

		cacherMenuPrincipal();
		if (modeSelection) {
			cacherEcranSelection();
		}
		Theme.afficherFond(false);

		videoEnCours = true;
		scene.setCursor(Cursor.NONE);
	}

	private static void actionVideoTerminee() {
		mouvSourisVsVeuille();
		if (modeSelection) {
			numeroProchaineDanseSelection++;
			if (dansesSelectionnees.size() > numeroProchaineDanseSelection) {
				if (MainDanse.sauterDansesVides()) {
					lancerVideo(dansesSelectionnees.get(numeroProchaineDanseSelection));
				} else {
					finSelection();
				}
			} else {
				finSelection();
			}
		}
	}

	/** Empêche la mise en veille pendant une vidéo (une seule minuterie pour toute l'appli). */
	public static void demarrerAntiVeille() {
		if (veille == null) {
			veille = new Timeline(new KeyFrame(Duration.seconds(100), e -> {
				if (videoEnCours || modeMelange) {
					mouvSourisVsVeuille();
				}
			}));
			veille.setCycleCount(Timeline.INDEFINITE);
			veille.play();
		}
	}

	public static void mouvSourisVsVeuille() {
		// Micro-mouvement de souris, alternativement +1 / -1 pour ne pas dériver
		try {
			if (robot == null) {
				robot = new Robot();
			}
			sensVeille = -sensVeille;
			robot.mouseMove(robot.getMouseX() + sensVeille, robot.getMouseY());
		} catch (Exception e) {
			Diagnostic.log("Anti-veille impossible", e);
		}
	}

	public static void quitterPleinEcran() {
		lecteurVLCJ.estEnPleinEcran = false;
		scene.setCursor(Cursor.DEFAULT);
		videoEnCours = false;
		afficherBarreAction();
	}

	public static void lancerVideoAuHasard() {
		if (dansesFiltrees.isEmpty()) {
			MainDanse.afficherErreur("Aucune danse ne correspond aux filtres.");
			return;
		}
		int aleaVideo = (int) Math.ceil(Math.random() * dansesFiltrees.size());
		lancerVideo(dansesFiltrees.get(aleaVideo - 1));
	}

	// ------------------------------------------------------------------ mélange

	public static void lancerMelange() {
		if (dansesFiltrees.isEmpty()) {
			MainDanse.afficherErreur("Aucune danse ne correspond aux filtres.");
			return;
		}
		MainDanse.annulerDecompte();
		stoperMelange();
		// Arrêt de la musique
		if (lecteurAudio != null) {
			lecteurAudio.arreter();
		}
		modeMelange = true;

		cacherMenuPrincipal();
		afficherBarreAction();
		Theme.afficherFond(false);

		videoEnCours = true;
		scene.setCursor(Cursor.NONE);
		lecteurVLCJ.estEnPleinEcran = true;
		primaryStage.setFullScreen(true);

		ArrayList<Integer> musiquesDejaChoisiees = new ArrayList<Integer>();
		timelineMelange = new Timeline();
		for (int i = 1; i < 7; i++) {
			int aleaVideo;
			do {
				aleaVideo = (int) Math.ceil(Math.random() * dansesFiltrees.size());
			} while (musiquesDejaChoisiees.contains(aleaVideo) && dansesFiltrees.size() >= i);
			musiquesDejaChoisiees.add(aleaVideo);

			Danse danse = dansesFiltrees.get(aleaVideo - 1);
			MediaPlayer playerVideoMelange;
			try {
				playerVideoMelange = new MediaPlayer(new Media(fichierVideo(danse).toURI().toURL().toExternalForm()));
			} catch (Exception e) {
				MainDanse.afficherErreur("Impossible de charger la vidéo " + danse.nomVideo);
				continue;
			}
			MediaView mediaViewMelange = new MediaView(playerVideoMelange);
			mediaViewMelange.setVisible(false);
			mediaViewMelange.fitWidthProperty().bind(scene.widthProperty());
			mediaViewMelange.fitHeightProperty().bind(scene.heightProperty());
			root.getChildren().add(mediaViewMelange);
			mediaViewsMelange.add(mediaViewMelange);
			playerVideosMelange.add(playerVideoMelange);

			final int morceauJoue = i;
			timelineMelange.getKeyFrames().add(new KeyFrame(Duration.seconds(35.0 * (i - 1)),
					e -> lancerMorceauDeMelange(playerVideoMelange, mediaViewMelange, morceauJoue)));
		}
		timelineMelange.play();
	}

	/** Exécuté sur le thread JavaFX à l'heure prévue de chaque morceau. */
	private static void lancerMorceauDeMelange(MediaPlayer player, MediaView vue, int morceauJoue) {
		if (!modeMelange) {
			return;
		}
		player.setVolume(1);
		int debut = 35 * (morceauJoue - 1);
		try {
			player.setStartTime(Duration.seconds(debut));
		} catch (Exception e) {
			player.setStartTime(Duration.seconds(35));
		}
		if (morceauJoue < 6) {
			try {
				player.setStopTime(Duration.seconds(debut + 35));
			} catch (Exception e) {
				// aller jusqu'à la fin
			}
		}
		vue.setVisible(true);
		vue.toFront();
		player.play();

		player.setOnEndOfMedia(() -> {
			vue.setVisible(false);
			root.getChildren().remove(vue);
			if (morceauJoue == 6 || playerVideosMelange.indexOf(player) == playerVideosMelange.size() - 1) {
				finMelange();
			}
		});
	}

	private static void finMelange() {
		modeMelange = false;
		stoperMelange();
		genererBoutonSuggestion(true);
		scene.setCursor(Cursor.DEFAULT);
		cacherBarreAction();
		afficherMenuPrincipal();
		videoEnCours = false;
		lecteurVLCJ.estEnPleinEcran = false;
		primaryStage.setFullScreen(false);
	}

	public static void rembobinerVideo(int nbSecondes) {
		long tempsActuel = lecteurVLCJ.tempsActuelMillis();
		long nouveauTemps = tempsActuel - (nbSecondes * 1000L);
		if (nouveauTemps < 0) {
			nouveauTemps = 0;
		}
		lecteurVLCJ.lancerAuTemps(nouveauTemps);
	}

	/** Arrête le mélange et LIBÈRE les lecteurs (sans dispose(), chaque lecteur JavaFX garde ses ressources natives). */
	public static void stoperMelange() {
		if (timelineMelange != null) {
			timelineMelange.stop();
			timelineMelange = null;
		}
		for (MediaPlayer player : new ArrayList<>(playerVideosMelange)) {
			try {
				player.setOnEndOfMedia(null);
				player.stop();
				player.dispose();
			} catch (Exception e) {
				Diagnostic.log("Arrêt d'un lecteur du mélange", e);
			}
		}
		playerVideosMelange.clear();
		for (MediaView vue : mediaViewsMelange) {
			vue.setVisible(false);
			vue.setMediaPlayer(null);
			root.getChildren().remove(vue);
		}
		mediaViewsMelange.clear();
	}

	// ------------------------------------------------------------------ sélection

	public static void finSelection() {
		lecteurVLCJ.arreter();
		lecteurVLCJ.imageView.setVisible(false);
		root.getChildren().remove(lecteurVLCJ.imageView);
		quitterPleinEcran();
		modeSelection = false;
		genererBoutonSuggestion(true);
		MainDanse.genererBoutonsDanse(true);
		cacherEcranSelection();
		cacherBarreAction();
		afficherMenuPrincipal();

		videoEnCours = false;

		lecteurVLCJ.estEnPleinEcran = false;
		primaryStage.setFullScreen(false);
	}

	public static void ouvrirEcranSelection() {
		modeSelection = true;
		cacherMenuPrincipal();
		afficherEcranSelection();

		// Sélection de six danses
		dansesSelectionnees.clear();
		ArrayList<Integer> musiquesDejaChoisiees = new ArrayList<Integer>();
		for (int i = 0; i < 6; i++) {
			if (musiquesDejaChoisiees.size() < dansesFiltrees.size()) {
				int aleaVideo;
				do {
					aleaVideo = (int) Math.ceil(Math.random() * (dansesFiltrees.size())) - 1;
				} while (musiquesDejaChoisiees.contains(aleaVideo));
				musiquesDejaChoisiees.add(aleaVideo);
				dansesSelectionnees.add(dansesFiltrees.get(aleaVideo));
			} else {
				dansesSelectionnees.add(danseNeant);
			}
		}

		MainDanse.genererBoutonsDanse(true, true, true);
	}

	/** Remplace la danse d'un emplacement de la sélection par une autre, au hasard. */
	public static void remplacerDanseSelection(int emplacement) {
		int nombreDansesSelectionnables = 0;
		for (Danse d : dansesSelectionnees) {
			if (!d.equals(danseNeant) && dansesFiltrees.contains(d)) {
				nombreDansesSelectionnables++;
			}
		}
		if (nombreDansesSelectionnables < dansesFiltrees.size()) {
			int aleaVideo;
			do {
				aleaVideo = (int) Math.ceil(Math.random() * (dansesFiltrees.size())) - 1;
			} while (dansesSelectionnees.contains(dansesFiltrees.get(aleaVideo)));
			dansesSelectionnees.set(emplacement, dansesFiltrees.get(aleaVideo));
		} else {
			dansesSelectionnees.set(emplacement, danseNeant);
		}
		MainDanse.genererBoutonsDanse(true, true, true);
	}

	// ------------------------------------------------------------------ affichage

	public static void cacherMenuPrincipal() {
		for (Bouton bouton : boutonsDanse) {
			bouton.setVisible(false);
			if (bouton.changerDanseBouton != null) {
				bouton.changerDanseBouton.setVisible(false);
			}
		}
		for (Bouton boutonMenu : boutonsMenu) {
			if (boutonMenu != null) {
				boutonMenu.setVisible(false);
			}
		}
		for (Text unText : textes) {
			unText.setVisible(false);
		}
		barreRecherche.setVisible(false);
	}

	public static void cacherBarreAction() {
		for (Bouton boutonAction : boutonsAction) {
			boutonAction.setVisible(false);
		}
	}

	public static void cacherMenuFiltres() {
		for (Bouton boutonFiltre : boutonsFiltres) {
			boutonFiltre.setVisible(false);
		}
		for (Bouton boutonFiltreAnnexe : boutonsFiltresAnnexes) {
			boutonFiltreAnnexe.setVisible(false);
		}
		for (Text texteFiltre : textesFiltres) {
			texteFiltre.setVisible(false);
		}
	}

	public static void cacherMenuTri() {
		for (Bouton boutonTri : boutonsTri) {
			boutonTri.setVisible(false);
		}
		for (Bouton boutonTriAnnexe : boutonsTriAnnexes) {
			boutonTriAnnexe.setVisible(false);
		}
	}

	public static void cacherEcranSelection() {
		for (Bouton boutonSelection : boutonsSelection) {
			boutonSelection.setVisible(false);
		}
		barreRecherche.setVisible(false);
	}

	public static void afficherMenuPrincipal() {
		Theme.afficherFond(true);
		for (Bouton bouton : boutonsDanse) {
			bouton.setVisible(true);
		}
		for (Bouton boutonMenu : boutonsMenu) {
			if (boutonMenu != null) {
				boutonMenu.setVisible(true);
			}
		}
		for (Text unText : textes) {
			unText.setVisible(true);
		}
		barreRecherche.setVisible(true);
	}

	public static void afficherBarreAction() {
		for (Bouton boutonAction : boutonsAction) {
			if (modeIllimite || !boutonAction.text.getText().equals("Passer")) {
				boutonAction.setVisible(true);
				boutonAction.toFront();
			}
		}
	}

	public static void afficherMenuFiltres() {
		Theme.afficherFond(true);
		modeFiltre = true;
		for (Bouton boutonFiltre : boutonsFiltres) {
			boutonFiltre.setVisible(true);
		}
		for (Bouton boutonFiltreAnnexe : boutonsFiltresAnnexes) {
			boutonFiltreAnnexe.setVisible(true);
		}
		for (Text texteFiltre : textesFiltres) {
			texteFiltre.setVisible(true);
		}
		barreRecherche.setVisible(true);
	}

	public static void afficherMenuTri() {
		Theme.afficherFond(true);
		modeTri = true;
		for (Bouton boutonTri : boutonsTri) {
			boutonTri.setVisible(true);
		}
		for (Bouton boutonTriAnnexe : boutonsTriAnnexes) {
			boutonTriAnnexe.setVisible(true);
		}
		barreRecherche.setVisible(true);
	}

	public static void afficherEcranSelection() {
		Theme.afficherFond(true);
		for (Bouton boutonSelection : boutonsSelection) {
			boutonSelection.setVisible(true);
		}
		boutonFiltrer.setVisible(true);
		barreRecherche.setVisible(true);
	}

	public static void reafficherEcranSelection() {
		afficherEcranSelection();
		for (Bouton boutonDanse : boutonsDanse) {
			boutonDanse.setVisible(true);
			if (boutonDanse.changerDanseBouton != null) {
				boutonDanse.changerDanseBouton.setVisible(true);
			}
		}
	}

	public static void quitterDanse() {
		MainDanse.annulerDecompte();
		if (!modeMelange) {
			lecteurVLCJ.arreter();
			lecteurVLCJ.imageView.setVisible(false);
		} else {
			modeMelange = false;
			stoperMelange();
		}
		videoEnCours = false;
		scene.setCursor(Cursor.DEFAULT);
		if (!modeSelection) {
			genererBoutonSuggestion(true);
			modeIllimite = false;
			modeSelection = false;
			cacherBarreAction();
			afficherMenuPrincipal();
		} else {
			finSelection();
		}
	}

	public static Color couleurInverse(Color base) {
		Color inverseAvecLuminosite;

		double luminosite = 0.2126 * base.getRed() + 0.7152 * base.getGreen() + 0.0722 * base.getBlue();

		Color inverse = base.invert();

		if (luminosite > 0.5) {
			inverseAvecLuminosite = inverse.darker().deriveColor(0, 1, 1, 1);
		} else {
			inverseAvecLuminosite = inverse.brighter().deriveColor(0, 1, 1, 1);
		}
		return inverseAvecLuminosite;
	}
}
