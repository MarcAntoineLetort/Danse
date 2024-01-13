package application;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
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
	public static File dossierImports;
	public static File fichierInfosDanses;
	public static ArrayList<Bouton> boutonsDanse = new ArrayList<>();
	public static ArrayList<Bouton> boutonsAction = new ArrayList<>();
	public static ArrayList<Bouton> boutonsMenu = new ArrayList<>();
	public static ArrayList<Bouton> boutonsFiltres = new ArrayList<>();
	public static ArrayList<Bouton> boutonsFiltresAnnexes = new ArrayList<>();
	public static ArrayList<Bouton> boutonsSelection = new ArrayList<>();
	public static ArrayList<Text> textesFiltres = new ArrayList<>();
	public static ArrayList<Text> textes = new ArrayList<>();
	public static MediaPlayer playerVideo;
	public static MediaView mediaView;
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
	public static Bouton boutonQuitter;
	public static Bouton boutonRejouer;
	public static Bouton boutonPasser;
	public static Bouton boutonSuggestion;
	public static Bouton boutonFiltrer;
	public static Bouton boutonIllimite;
	public static Bouton boutonMelange;
	public static Bouton boutonSelection;
	public static Bouton boutonLancerSelection;
	public static Bouton boutonAnnulerSelection;
	public static Bouton boutonReinitialiserSelection;
	public static Text textFiltreIntensite;
	public static Text textFiltreTechnique;
	public static Text textFiltreGenre;
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
	public static Bouton boutonFiltreValider;
	public static Bouton boutonFiltreReinitialiser;
	public static boolean videoEnCours = false;
	public static boolean modeIllimite = false;
	public static boolean modeMelange = false;
	public static boolean modeSelection = false;
	public static boolean modeFiltre = false;
	public static int numeroProchaineDanseSelection = 0;
	public static ImageView imageViewDanseEchangeSelection;
	public static Bouton boutonGlissePourEchangeSelection;
	public static ArrayList<MediaView> mediaViewsMelange;
	public static ArrayList<MediaPlayer> playerVideosMelange;
	public static ArrayList<Danse> dansesSelectionnees;
	public static String police = "Arial Rounded MT Bold"; // Constantia
	// public static TextField barreRecherche;
	public static BarreRecherche barreRecherche;

	public static Text textCompteur;

	public static void genererTextPage() {
		if (textPage != null && danseSelectionnee != null) {
			textPage.setText("Danse " + danseSelectionnee.ordre + "/" + dansesFiltrees.size() + " - Page " + (page + 1)
					+ "/" + (int) Math.ceil(dansesFiltrees.size() / 6 + 1));
			textPage.setFill(couleur1.brighter().brighter().brighter().brighter());
			effetTextPage.setColor(VariableUtile.couleur1.invert());
		}
	}

	public static void genererBoutonSuggestion(boolean nettoyerPrecedant) {
		if (nettoyerPrecedant) {
			VariableUtile.boutonsMenu.remove(boutonSuggestion);
			VariableUtile.root.getChildren().remove(VariableUtile.boutonSuggestion);
		}
		if (dansesFiltrees.size() > 0) {
			int aleaSuggestion = (int) Math.ceil(Math.random() * dansesFiltrees.size());
			boutonSuggestion = new Bouton(px * 80, py * 4, px * 17, py * 33, 20, dansesFiltrees.get(aleaSuggestion - 1),
					false, 6);
			boutonSuggestion.genererSuperTitre("Suggestion");
			VariableUtile.boutonsMenu.add(boutonSuggestion);
		}
	}

	public static void formerNombreCompteur(String nombre) {
		textCompteur.setText(nombre);
		textCompteur.setFill(couleur1.brighter().brighter().brighter().brighter());
	}

	public static void lancerSon(Danse danse) {
		try {
			VariableUtile.playerVideo = new MediaPlayer(new Media(
					new File(VariableUtile.dossierDanses + "\\" + danse.nomVideo).toURI().toURL().toExternalForm()));
		} catch (Exception e) {
			MainDanse.afficherErreur(
					"Impossible d'ouvrir la vidéo " + VariableUtile.dossierDanses + danse.nomVideo + " " + e);
		}
	}

	public static void lancerVideo(Danse danse) {
		if (VariableUtile.playerVideo != null) {
			VariableUtile.playerVideo.stop();
			VariableUtile.playerVideo.dispose();
		}
		// Démarrage de la vidéo
		try {
			VariableUtile.playerVideo = new MediaPlayer(new Media(
					new File(VariableUtile.dossierDanses + "\\" + danse.nomVideo).toURI().toURL().toExternalForm()));
		} catch (Exception e) {
			MainDanse.afficherErreur(
					"Impossible d'ouvrir la vidéo " + VariableUtile.dossierDanses + danse.nomVideo + " " + e);
		}
		VariableUtile.mediaView = new MediaView(VariableUtile.playerVideo);
		VariableUtile.root.getChildren().add(VariableUtile.mediaView);

		VariableUtile.playerVideo.setVolume(1);

		final DoubleProperty width = VariableUtile.mediaView.fitWidthProperty();
		final DoubleProperty height = VariableUtile.mediaView.fitHeightProperty();

		width.bind(Bindings.selectDouble(VariableUtile.mediaView.sceneProperty(), "width"));
		height.bind(Bindings.selectDouble(VariableUtile.mediaView.sceneProperty(), "height"));

		VariableUtile.primaryStage.setFullScreen(true);

		Platform.runLater(() -> {
			VariableUtile.playerVideo.play();
		});

		cacherMenuPrincipal();
		afficherBarreAction();
		if (VariableUtile.modeSelection) {
			cacherEcranSelection();
		}

		VariableUtile.videoEnCours = true;
		VariableUtile.scene.setCursor(Cursor.NONE);

		VariableUtile.playerVideo.setOnEndOfMedia(() -> {

			// Mouvement de souris pour empêcher la veille
			Robot hal = null;
			try {
				hal = new Robot();
			} catch (AWTException e) {
				Alert a = new Alert(AlertType.ERROR);
				a.setContentText("Echec de la génération du robot de mouvement de souris : " + e);
				a.show();
			}
			hal.mouseMove((int) (MouseInfo.getPointerInfo().getLocation().getX()) + 1,
					(int) (MouseInfo.getPointerInfo().getLocation().getY()) + 1);

			if (VariableUtile.modeIllimite) {
				VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
				VariableUtile.lancerVideoAuHasard();
			}
			if (VariableUtile.modeSelection) {
				VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
				numeroProchaineDanseSelection++;
				if (dansesSelectionnees.size() > numeroProchaineDanseSelection) {
					if (MainDanse.sauterDansesVides()) {
						VariableUtile.lancerVideo(dansesSelectionnees.get(numeroProchaineDanseSelection));
					}
				} else {
					finSelection();
				}
			}
		});
	}

	public static void lancerVideoAuHasard() {
		int aleaVideo = (int) Math.ceil(Math.random() * dansesFiltrees.size());
		lancerVideo(dansesFiltrees.get(aleaVideo - 1));
	}

	public static void lancerMelange() {
		// Arrêt de la musique
		if (VariableUtile.playerVideo != null) {
			VariableUtile.playerVideo.stop();
		}
		VariableUtile.modeMelange = true;

		cacherMenuPrincipal();
		afficherBarreAction();

		VariableUtile.videoEnCours = true;
		VariableUtile.scene.setCursor(Cursor.NONE);

		ArrayList<Integer> musiquesDejaChoisiees = new ArrayList<Integer>();
		VariableUtile.mediaViewsMelange = new ArrayList<MediaView>();
		VariableUtile.playerVideosMelange = new ArrayList<MediaPlayer>();
		for (int i = 1; i < 7; i++) {

			int aleaVideo;
			do {
				aleaVideo = (int) Math.ceil(Math.random() * dansesFiltrees.size());
			} while (musiquesDejaChoisiees.contains(aleaVideo) && dansesFiltrees.size() >= i);
			musiquesDejaChoisiees.add(aleaVideo);

			MediaPlayer playerVideoMelange = new MediaPlayer(new Media(VariableUtile.class
					.getResource("video/" + dansesFiltrees.get(aleaVideo - 1).nomVideo).toExternalForm()));
			System.out.println(dansesFiltrees.get(aleaVideo - 1).nomVideo + " n°" + i);
			MediaView mediaViewMelange = new MediaView(playerVideoMelange);
			VariableUtile.root.getChildren().add(mediaViewMelange);
			mediaViewMelange.setVisible(false);
			VariableUtile.primaryStage.setFullScreen(true);
			VariableUtile.mediaViewsMelange.add(mediaViewMelange);
			VariableUtile.playerVideosMelange.add(playerVideoMelange);

			lancerMusiqueDeMelange(playerVideoMelange, mediaViewMelange, i);
		}
	}

	public static void lancerMusiqueDeMelange(MediaPlayer playerVideoMelange, MediaView mediaViewMelange,
			int morceauJoue) {

		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				// TODO réparer dernière musique parfois non lancée
				System.out.println("numéro = " + morceauJoue);

				if (VariableUtile.modeMelange) {
					playerVideoMelange.setVolume(1);

					final DoubleProperty width = mediaViewMelange.fitWidthProperty();
					final DoubleProperty height = mediaViewMelange.fitHeightProperty();

					width.bind(Bindings.selectDouble(mediaViewMelange.sceneProperty(), "width"));
					height.bind(Bindings.selectDouble(mediaViewMelange.sceneProperty(), "height"));

					int debut = 35 * (morceauJoue - 1);
					System.out.println("début : " + debut);
					try {
						playerVideoMelange.setStartTime(Duration.seconds(debut));
					} catch (Exception e) {
						debut = 35;
						playerVideoMelange.setStartTime(Duration.seconds(debut));
					}
					if (morceauJoue < 6) {
						int fin = debut + 35;
						System.out.println("fin : " + fin);
						try {
							playerVideoMelange.setStopTime(Duration.seconds(fin));
							System.out.println("STOP");
						} catch (Exception e) {
							// Aller jusqu'à la fin
							System.out.println("jusqu'à la fin");
						}
					}

					mediaViewMelange.setVisible(true);
					Platform.runLater(() -> {
						playerVideoMelange.play();
					});

					playerVideoMelange.setOnEndOfMedia(() -> {
						VariableUtile.playerVideosMelange.remove(playerVideoMelange);
						mediaViewMelange.setVisible(false);
						VariableUtile.root.getChildren().remove(mediaViewMelange);
						VariableUtile.mediaViewsMelange.remove(mediaViewMelange);
						if (morceauJoue == 6) {
							System.out.println("fin morceau 6");
							VariableUtile.modeMelange = false;
							VariableUtile.stoperMelange();
							VariableUtile.genererBoutonSuggestion(true);
							VariableUtile.scene.setCursor(Cursor.DEFAULT);
							VariableUtile.cacherBarreAction();
							VariableUtile.afficherMenuPrincipal();
							VariableUtile.primaryStage.setFullScreen(false);
						}
					});
				}
			}
		}, 35000 * (morceauJoue - 1));
	}

	public static void stoperMelange() {
		for (MediaPlayer playerVideoMelange : VariableUtile.playerVideosMelange) {
			playerVideoMelange.stop();
		}
		VariableUtile.playerVideosMelange.clear();
		for (MediaView mediaViewMelange : VariableUtile.mediaViewsMelange) {
			mediaViewMelange.setVisible(false);
			VariableUtile.root.getChildren().remove(mediaViewMelange);
		}
		VariableUtile.mediaViewsMelange.clear();
	}

	public static void finSelection() {
		modeSelection = false;
		genererBoutonSuggestion(true);
		MainDanse.genererBoutonsDanse(true);
		cacherEcranSelection();
		cacherBarreAction();
		afficherMenuPrincipal();

		primaryStage.setFullScreen(false);
	}

	public static void ouvrirEcranSelection() {
		VariableUtile.modeSelection = true;
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
				dansesSelectionnees.add(VariableUtile.danseNeant);
			}
		}

		MainDanse.genererBoutonsDanse(true, true);
	}

	public static void cacherMenuPrincipal() {
		for (Bouton bouton : VariableUtile.boutonsDanse) {
			bouton.setVisible(false);
			if (bouton.changerDanseBouton != null) {
				bouton.changerDanseBouton.setVisible(false);
			}
		}
		for (Bouton boutonMenu : VariableUtile.boutonsMenu) {
			boutonMenu.setVisible(false);
		}
		for (Text unText : VariableUtile.textes) {
			unText.setVisible(false);
		}
		VariableUtile.barreRecherche.setVisible(false);
	}

	public static void cacherBarreAction() {
		for (Bouton boutonAction : VariableUtile.boutonsAction) {
			boutonAction.setVisible(false);
		}
	}

	public static void cacherMenuFiltres() {
		for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
			boutonFiltre.setVisible(false);
		}
		for (Bouton boutonFiltreAnnexe : VariableUtile.boutonsFiltresAnnexes) {
			boutonFiltreAnnexe.setVisible(false);
		}
		for (Text texteFiltre : VariableUtile.textesFiltres) {
			texteFiltre.setVisible(false);
		}
	}

	public static void cacherEcranSelection() {
		for (Bouton boutonSelection : VariableUtile.boutonsSelection) {
			boutonSelection.setVisible(false);
		}
		VariableUtile.barreRecherche.setVisible(false);
	}

	public static void afficherMenuPrincipal() {
		for (Bouton bouton : VariableUtile.boutonsDanse) {
			bouton.setVisible(true);
		}
		for (Bouton boutonMenu : VariableUtile.boutonsMenu) {
			boutonMenu.setVisible(true);
		}
		for (Text unText : VariableUtile.textes) {
			unText.setVisible(true);
		}
		VariableUtile.barreRecherche.setVisible(true);
	}

	public static void afficherBarreAction() {
		for (Bouton boutonAction : VariableUtile.boutonsAction) {
			if (VariableUtile.modeIllimite || !boutonAction.text.getText().equals("Passer")) {
				boutonAction.setVisible(true);
			}
		}
	}

	public static void afficherMenuFiltres() {
		VariableUtile.modeFiltre = true;
		for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
			boutonFiltre.setVisible(true);
		}
		for (Bouton boutonFiltreAnnexe : VariableUtile.boutonsFiltresAnnexes) {
			boutonFiltreAnnexe.setVisible(true);
		}
		for (Text texteFiltre : VariableUtile.textesFiltres) {
			texteFiltre.setVisible(true);
		}
		VariableUtile.barreRecherche.setVisible(true);
	}

	public static void afficherEcranSelection() {
		for (Bouton boutonSelection : VariableUtile.boutonsSelection) {
			boutonSelection.setVisible(true);
		}
		boutonFiltrer.setVisible(true);
		VariableUtile.barreRecherche.setVisible(true);
	}

	public static void reafficherEcranSelection() {
		VariableUtile.afficherEcranSelection();
		for (Bouton boutonDanse : VariableUtile.boutonsDanse) {
			boutonDanse.setVisible(true);
			boutonDanse.changerDanseBouton.setVisible(true);
		}
	}

	public static void quitterDanse() {
		if (!VariableUtile.modeMelange) {
			VariableUtile.playerVideo.stop();
			VariableUtile.mediaView.setVisible(false);
			VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
		} else {
			VariableUtile.modeMelange = false;
			VariableUtile.stoperMelange();
		}
		if (!VariableUtile.modeSelection) {
			VariableUtile.genererBoutonSuggestion(true);
			VariableUtile.modeIllimite = false;
			VariableUtile.modeSelection = false;
			VariableUtile.cacherBarreAction();
			VariableUtile.afficherMenuPrincipal();
		} else {
			finSelection();
		}
	}
}
