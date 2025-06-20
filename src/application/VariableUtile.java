package application;

import java.awt.MouseInfo;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
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
	public static ArrayList<MediaView> mediaViewsMelange;
	public static ArrayList<MediaPlayer> playerVideosMelange;
	public static ArrayList<Danse> dansesSelectionnees;
	public static String police = "Arial Rounded MT Bold"; // Constantia
	public static BarreRecherche barreRecherche;

	public static Text textCompteur;

	public static void genererTextPage() {
		if (textPage != null && danseSelectionnee != null) {
			textPage.setText("Danse " + danseSelectionnee.ordreActuel + "/" + dansesFiltrees.size() + " - Page "
					+ (page + 1) + "/" + (int) Math.ceil(dansesFiltrees.size() / 6 + 1));
			textPage.setFill(couleur1.brighter().brighter().brighter().brighter());
			effetTextPage.setColor(VariableUtile.couleur1.invert());
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
		if (dansesFiltrees.size() > 0) {
			Platform.runLater(() -> {
				if (dansesFiltrees.size() > 0) {
					int aleaSuggestion = (int) Math.ceil(Math.random() * dansesFiltrees.size());
					Danse danse = dansesFiltrees.get(aleaSuggestion - 1);
					if (danse.imageDanse1 == null) {
						danse.importerImages();
					}
					boutonSuggestion = new Bouton(px * 80, py * 4, px * 17, py * 33, 20, danse, false, 6);
					boutonSuggestion.genererSuperTitre("Suggestion");
					boutonsMenu.add(boutonSuggestion);
				}
			});
		}
	}

	public static void formerNombreCompteur(String nombre) {
		textCompteur.setText(nombre);
		textCompteur.setFill(couleur1.brighter().brighter().brighter().brighter());
	}

	public static void lancerSon(Danse danse) {
		try {
			String cheminComplet = new File(dossierDanses + "\\" + danse.nomVideo).getAbsolutePath();
			lecteurAudio.chargerMedia(cheminComplet);
		} catch (Exception e) {
			MainDanse.afficherErreur(
					"Impossible d'ouvrir la vidéo " + VariableUtile.dossierDanses + danse.nomVideo + " " + e);
		}
	}

	public static void lancerVideo(Danse danse) {
		VariableUtile.lecteurVLCJ.arreter();

		File videoDanse = new File(VariableUtile.dossierDanses + "\\" + danse.nomVideo);

		// Retirer l'ancienne vue si elle existe
		VariableUtile.root.getChildren().remove(VariableUtile.lecteurVLCJ.imageView);

		// Ajouter la nouvelle vue
		VariableUtile.lecteurVLCJ.estEnPleinEcran = true;
		VariableUtile.primaryStage.setFullScreen(true);
		VariableUtile.lecteurVLCJ.imageView.fitWidthProperty().bind(VariableUtile.scene.widthProperty());
		VariableUtile.lecteurVLCJ.imageView.fitHeightProperty().bind(VariableUtile.scene.heightProperty());
		Platform.runLater(() -> VariableUtile.root.getChildren().add(VariableUtile.lecteurVLCJ.imageView));

		VariableUtile.lecteurVLCJ.setOnVideoFinished(() -> {
			Platform.runLater(() -> {
				actionVideoTerminee();
			});
		});

		VariableUtile.lecteurVLCJ.imageView.setVisible(true);
		VariableUtile.lecteurVLCJ.demarrer(videoDanse);
		
		PauseTransition pause = new PauseTransition(Duration.seconds(120));
		pause.setOnFinished(e -> mouvSourisVsVeuille());
		pause.play();

		cacherMenuPrincipal();
//		afficherBarreAction();
		if (VariableUtile.modeSelection)
			cacherEcranSelection();

		VariableUtile.videoEnCours = true;
		VariableUtile.scene.setCursor(Cursor.NONE);
	}

	private static void actionVideoTerminee() {
		mouvSourisVsVeuille();

//		if (VariableUtile.modeIllimite) {
//			Platform.runLater(() -> {
//				VariableUtile.root.getChildren().remove(VariableUtile.lecteurVLCJ);
//				VariableUtile.lancerVideoAuHasard();
//			});
//		}
		if (VariableUtile.modeSelection) {
			// Platform.runLater(() -> {
//			VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
			// });
			numeroProchaineDanseSelection++;
			if (dansesSelectionnees.size() > numeroProchaineDanseSelection) {
				if (MainDanse.sauterDansesVides()) {
					VariableUtile.lancerVideo(dansesSelectionnees.get(numeroProchaineDanseSelection));
				}
			} else {
				finSelection();
			}
		}
	}
	
	public static void mouvSourisVsVeuille() {
		// Mouvement de souris pour empécher la veille
				Robot hal = null;
				try {
					hal = new Robot();
				} catch (Exception e) {
					MainDanse.afficherErreur("Echec de la génération du robot de mouvement de souris : " + e);
				}
				hal.mouseMove((int) (MouseInfo.getPointerInfo().getLocation().getX()) + 1,
						(int) (MouseInfo.getPointerInfo().getLocation().getY()) + 1);
	}

	public static void quitterPleinEcran() {
		VariableUtile.root.setCenter(VariableUtile.root);
		VariableUtile.scene.setCursor(Cursor.DEFAULT);
		VariableUtile.videoEnCours = false;
	}

	public static void lancerVideoAuHasard() {
		int aleaVideo = (int) Math.ceil(Math.random() * dansesFiltrees.size());
		lancerVideo(dansesFiltrees.get(aleaVideo - 1));
	}

	public static void lancerMelange() {
		// Arrêt de la musique
		if (VariableUtile.lecteurAudio != null) {
			VariableUtile.lecteurAudio.arreter();
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

			MediaPlayer playerVideoMelange = null;
			try {
				playerVideoMelange = new MediaPlayer(new Media(
						new File(VariableUtile.dossierDanses + "\\" + dansesFiltrees.get(aleaVideo - 1).nomVideo)
								.toURI().toURL().toExternalForm()));
			} catch (MalformedURLException e) {
				MainDanse
						.afficherErreur("Impossible de charger la vidéo " + dansesFiltrees.get(aleaVideo - 1).nomVideo);
			}
			System.out.println(dansesFiltrees.get(aleaVideo - 1).nomVideo + " n°" + i);
			MediaView mediaViewMelange = new MediaView(playerVideoMelange);
			Platform.runLater(() -> {
				VariableUtile.root.getChildren().add(mediaViewMelange);
			});
			mediaViewMelange.setVisible(false);
			VariableUtile.lecteurVLCJ.estEnPleinEcran = true;
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
						Platform.runLater(() -> {
							VariableUtile.root.getChildren().remove(mediaViewMelange);
						});
						VariableUtile.mediaViewsMelange.remove(mediaViewMelange);
						if (morceauJoue == 6) {
							System.out.println("fin morceau 6");
							VariableUtile.modeMelange = false;
							VariableUtile.stoperMelange();
							VariableUtile.genererBoutonSuggestion(true);
							VariableUtile.scene.setCursor(Cursor.DEFAULT);
							VariableUtile.cacherBarreAction();
							VariableUtile.afficherMenuPrincipal();
							VariableUtile.lecteurVLCJ.estEnPleinEcran = false;
							VariableUtile.primaryStage.setFullScreen(false);
						}
					});
				}
			}
		}, 35000 * (morceauJoue - 1));
	}

	public static void rembobinerVideo(int nbSecondes) {
		long tempsActuel = VariableUtile.lecteurVLCJ.tempsActuelMillis();
		long nouveauTemps = tempsActuel - (nbSecondes * 1000L);

		if (nouveauTemps < 0) {
			nouveauTemps = 0;
		}

		VariableUtile.lecteurVLCJ.lancerAuTemps(nouveauTemps);
	}

	public static void stoperMelange() {
		for (MediaPlayer playerVideoMelange : VariableUtile.playerVideosMelange) {
			playerVideoMelange.stop();
		}
		VariableUtile.playerVideosMelange.clear();
		for (MediaView mediaViewMelange : VariableUtile.mediaViewsMelange) {
			mediaViewMelange.setVisible(false);
			Platform.runLater(() -> {
				VariableUtile.root.getChildren().remove(mediaViewMelange);
			});
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

		VariableUtile.videoEnCours = false;
		
		VariableUtile.lecteurVLCJ.estEnPleinEcran = false;
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

		MainDanse.genererBoutonsDanse(true, true, true);
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

	public static void cacherMenuTri() {
		for (Bouton boutonTri : VariableUtile.boutonsTri) {
			boutonTri.setVisible(false);
		}
		for (Bouton boutonTriAnnexe : VariableUtile.boutonsTriAnnexes) {
			boutonTriAnnexe.setVisible(false);
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
				boutonAction.toFront();
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

	public static void afficherMenuTri() {
		VariableUtile.modeTri = true;
		for (Bouton boutonTri : VariableUtile.boutonsTri) {
			boutonTri.setVisible(true);
		}
		for (Bouton boutonTriAnnexe : VariableUtile.boutonsTriAnnexes) {
			boutonTriAnnexe.setVisible(true);
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
			VariableUtile.lecteurVLCJ.arreter();
			VariableUtile.lecteurVLCJ.imageView.setVisible(false);
//			VariableUtile.mediaView.setVisible(false);
//			Platform.runLater(() -> {
//				VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
//			});
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
