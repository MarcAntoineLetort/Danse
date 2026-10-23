package application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainDanse extends Application {
	Media sonDecompte;
	MediaPlayer mediaPlayerCompteur;
	ImageView imageChargement;
	private static Timeline decompteAnim;

	@Override
	public void start(Stage pStage) {
		Diagnostic.init();
		Theme.chargerPolice();
		VariableUtile.primaryStage = pStage;
		VariableUtile.root = new BorderPane();
		VariableUtile.main = this;
		VariableUtile.largeurFenetre = Screen.getPrimary().getVisualBounds().getWidth();
		VariableUtile.hauteurFenetre = Screen.getPrimary().getVisualBounds().getHeight();
		VariableUtile.scene = new Scene(VariableUtile.root, VariableUtile.largeurFenetre, VariableUtile.hauteurFenetre);
		VariableUtile.scene.setFill(Theme.FOND);
		VariableUtile.primaryStage.setMaximized(true);
		VariableUtile.primaryStage.setTitle("Danse");
		VariableUtile.px = VariableUtile.largeurFenetre / 100;
		VariableUtile.py = VariableUtile.hauteurFenetre / 100;
		VariableUtile.police = Theme.famille;

		Theme.installerFond(VariableUtile.root);

		int alea = (int) (Math.random() * 6);
		String imageChargementURI = null;
		switch (alea) {
		case 0:
			imageChargementURI = "imageBouton/chargement_2.jpg";
			VariableUtile.couleur1 = Color.rgb(55, 20, 95);
			VariableUtile.couleur2 = Color.rgb(15, 55, 15);
			break;
		case 1:
			imageChargementURI = "imageBouton/chargement_3.jpg";
			VariableUtile.couleur1 = Color.rgb(95, 55, 0);
			VariableUtile.couleur2 = Color.rgb(0, 20, 10);
			break;
		case 2:
			imageChargementURI = "imageBouton/chargement_6.jpg";
			VariableUtile.couleur1 = Color.rgb(0, 0, 95);
			VariableUtile.couleur2 = Color.rgb(30, 20, 0);
			break;
		case 3:
			imageChargementURI = "imageBouton/chargement_8.jpg";
			VariableUtile.couleur1 = Color.rgb(0, 50, 90);
			VariableUtile.couleur2 = Color.rgb(90, 90, 0);
			break;
		case 4:
			imageChargementURI = "imageBouton/chargement_13_latino.png";
			VariableUtile.couleur1 = Color.rgb(3, 183, 48);
			VariableUtile.couleur2 = Color.rgb(253, 221, 42);
			break;
		default:
			imageChargementURI = "imageBouton/chargement_14_folk.png";
			VariableUtile.couleur1 = Color.rgb(198, 92, 33);
			VariableUtile.couleur2 = Color.rgb(26, 130, 67);
			break;
		}

		VariableUtile.primaryStage.setScene(VariableUtile.scene);
		VariableUtile.primaryStage.show();

		Image imgChargement = importerImage(imageChargementURI);
		if (imgChargement != null) {
			imageChargement = new ImageView(imgChargement);
			imageChargement.setX((VariableUtile.largeurFenetre - imgChargement.getWidth()) / 2);
			imageChargement.setY((VariableUtile.hauteurFenetre - imgChargement.getHeight()) / 2);
			VariableUtile.root.getChildren().add(imageChargement);
		}

		// Désactivation des interactions souris pendant le chargement
		VariableUtile.root.setDisable(true);

		// Lectures de fichiers et initialisation de VLC : hors du thread JavaFX.
		// La construction de l'interface se fait ensuite SUR le thread JavaFX.
		Thread threadChargement = new Thread(() -> {
			try {
				chargerDonnees();
			} catch (Throwable e) {
				Diagnostic.log("Erreur de chargement des données", e);
				afficherErreur("Erreur de chargement : " + e);
			}
			Platform.runLater(() -> {
				try {
					construireInterface();
				} catch (Throwable e) {
					Diagnostic.log("Erreur de construction de l'interface", e);
					afficherErreur("Erreur d'interface : " + e);
				}
				VariableUtile.root.setDisable(false);
				if (imageChargement != null) {
					VariableUtile.root.getChildren().remove(imageChargement);
				}
				validerFiltre();
				VariableUtile.textCompteur.toFront();
				VariableUtile.demarrerAntiVeille();
			});
		}, "chargement");
		threadChargement.setDaemon(true);
		threadChargement.start();
	}

	public static Media mediaDepuisRessource(String chemin) throws IOException {
		try (InputStream in = MainDanse.class.getResourceAsStream(chemin)) {
			if (in == null)
				throw new IOException("Ressource introuvable : " + chemin);

			File tempFile = File.createTempFile("tempMedia", ".mp3");
			tempFile.deleteOnExit();
			Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			return new Media(tempFile.toURI().toString());
		}
	}

	/** Partie « données » du chargement (thread de chargement, pas d'éléments d'interface). */
	private void chargerDonnees() {
		try {
			sonDecompte = mediaDepuisRessource("/application/selection1.mp3");
			mediaPlayerCompteur = new MediaPlayer(sonDecompte);
		} catch (Exception e) {
			Diagnostic.log("Erreur chargement MP3", e);
			afficherErreur("Erreur chargement MP3 : " + e.getMessage());
		}

		try {
			VariableUtile.lecteurVLCJ = new LecteurVLCJ();
			VariableUtile.lecteurAudio = new LecteurAudioVLC();
		} catch (Throwable e) {
			Diagnostic.log("Initialisation de VLC impossible", e);
			afficherErreur("VLC n'a pas pu démarrer : " + e);
		}

		VariableUtile.danses = new HashMap<String, Danse>();
		VariableUtile.dansesFiltrees = new ArrayList<>();
		VariableUtile.dansesSelectionnees = new ArrayList<>();
		VariableUtile.danseNeant = new Danse("Néant", "Quitter", Color.BLACK, Color.WHITE);
		VariableUtile.artistes = new HashMap<Integer, String>();

		importerConfig();
		if (VariableUtile.dossierDanses != null) {
			importerTitresVideosDanses();
			importerListeArtistes();
			completerDanses();
		}
	}

	/** Partie « interface » du chargement (thread JavaFX). */
	private void construireInterface() {
		genererBoutonsFinDeDanse();

		VariableUtile.dansesFiltrees.addAll(VariableUtile.danses.values());

		genererBoutonsPagePrincipale();
		genererBoutonsFiltre();
		genererBoutonsTri();
		genererBoutonIllimite();
		genererBoutonMelange();
		genererBoutonsSelection();

		VariableUtile.boutonsMenu.add(VariableUtile.boutonSuivant);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonPrecedent);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonFiltrer);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonTrier);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonIllimite);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonMelange);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonSelection);
		VariableUtile.boutonsFiltresAnnexes.add(VariableUtile.boutonFiltreValider);
		VariableUtile.boutonsFiltresAnnexes.add(VariableUtile.boutonFiltreReinitialiser);
		VariableUtile.boutonsTriAnnexes.add(VariableUtile.boutonTriValider);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonLancerSelection);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonReinitialiserSelection);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonAnnulerSelection);

		for (Bouton bouton : VariableUtile.boutonsFiltres) {
			bouton.validerBouton();
		}
		VariableUtile.boutonsAction.add(VariableUtile.boutonQuitter);
		VariableUtile.boutonsAction.add(VariableUtile.boutonRejouer);
		VariableUtile.boutonsAction.add(VariableUtile.boutonPasser);

		VariableUtile.effetTextPage = Theme.lueur(Color.BLACK, 0.7, 10);

		// Indicateur « Danse n/N - Page p/P »
		VariableUtile.textPage = new Text(VariableUtile.px * 35, VariableUtile.hauteurFenetre * 0.18, "");
		VariableUtile.textes.add(VariableUtile.textPage);
		VariableUtile.textPage.setFont(Theme.police(VariableUtile.py * 2));
		VariableUtile.textPage.setWrappingWidth(VariableUtile.px * 17);
		VariableUtile.textPage.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textPage.setFill(Color.web("#e8f0ff"));
		VariableUtile.textPage.setEffect(VariableUtile.effetTextPage);
		VariableUtile.textPage.setMouseTransparent(true);

		// Gros chiffre du décompte
		VariableUtile.textCompteur = new Text(VariableUtile.px * 0, VariableUtile.py * 65, "");
		VariableUtile.textCompteur.setFont(Theme.police(VariableUtile.py * 55));
		VariableUtile.textCompteur.setWrappingWidth(VariableUtile.px * 100);
		VariableUtile.textCompteur.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textCompteur.setFill(Color.web("#ffffff", 0.95));
		VariableUtile.textCompteur.setEffect(Theme.lueur(Theme.CYAN, 0.9, 40));
		VariableUtile.textCompteur.setVisible(false);
		VariableUtile.textCompteur.setMouseTransparent(true);

		VariableUtile.root.getChildren().add(VariableUtile.textPage);
		VariableUtile.root.getChildren().add(VariableUtile.textCompteur);

		// Barre de recherche
		VariableUtile.barreRecherche = new BarreRecherche(VariableUtile.px * 46.5, VariableUtile.py * 6.1,
				VariableUtile.px * 28, VariableUtile.py * 6.6, "LoupePetite");
		VariableUtile.barreRecherche.setVisible(false);

		// Touches de contrôle (Échap, espace, flèches, retour arrière)
		VariableUtile.scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				KeyCode code = ke.getCode();
				if (!VariableUtile.ecranMenuActif() || VariableUtile.lecteurVLCJ.estEnPleinEcran) {
					if (code == KeyCode.ESCAPE) {
						VariableUtile.quitterPleinEcran();
					} else if (code == KeyCode.SPACE) {
						if (VariableUtile.lecteurVLCJ.estEnPause()) {
							VariableUtile.rembobinerVideo(3);
							VariableUtile.lecteurVLCJ.demarrer(null);
							VariableUtile.reprendreMelange();
						} else if (VariableUtile.lecteurVLCJ.estEnLecture()) {
							VariableUtile.lecteurVLCJ.pause();
							VariableUtile.pauseMelange();
						}
					}
				} else {
					if (code == KeyCode.BACK_SPACE && VariableUtile.barreRecherche.text.getText().length() > 0) {
						String t = VariableUtile.barreRecherche.text.getText();
						VariableUtile.barreRecherche.text.setText(t.substring(0, t.length() - 1));
						validerFiltre();
					} else if (code == KeyCode.ESCAPE) {
						effacerRecherche();
					} else if ((code == KeyCode.LEFT || code == KeyCode.PAGE_UP) && !VariableUtile.modeFiltre
							&& !VariableUtile.modeTri && !VariableUtile.modeSelection) {
						pagePrecedente();
					} else if ((code == KeyCode.RIGHT || code == KeyCode.PAGE_DOWN) && !VariableUtile.modeFiltre
							&& !VariableUtile.modeTri && !VariableUtile.modeSelection) {
						pageSuivante();
					}
				}
			}
		});
		// Saisie du texte : KEY_TYPED gère aussi les accents, apostrophes, tirets…
		VariableUtile.scene.setOnKeyTyped(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (!VariableUtile.ecranMenuActif()) {
					return;
				}
				String c = ke.getCharacter();
				if (c == null || c.isEmpty() || c.charAt(0) < 32 || c.charAt(0) == 127 || ke.isControlDown()
						|| ke.isMetaDown()) {
					return;
				}
				String actuel = VariableUtile.barreRecherche.text.getText();
				if (actuel.length() < 40) {
					VariableUtile.barreRecherche.text.setText(actuel + c);
					validerFiltre();
				}
			}
		});
	}

	public static void effacerRecherche() {
		if (VariableUtile.barreRecherche != null && VariableUtile.barreRecherche.text.getText().length() > 0) {
			VariableUtile.barreRecherche.text.setText("");
			VariableUtile.main.validerFiltre();
		}
	}

	public static void pageSuivante() {
		if (VariableUtile.page < VariableUtile.nbPages() - 1) {
			VariableUtile.page++;
		} else {
			VariableUtile.page = 0;
		}
		genererBoutonsDanse(true);
		VariableUtile.genererTextPage();
	}

	public static void pagePrecedente() {
		if (VariableUtile.page > 0) {
			VariableUtile.page--;
		} else {
			VariableUtile.page = VariableUtile.nbPages() - 1;
		}
		genererBoutonsDanse(true);
		VariableUtile.genererTextPage();
	}

	private static final double LB = 7.4; // largeur des boutons de menu (en % de la largeur)
	private static final double HB = 12.4; // hauteur des boutons de menu (en % de la hauteur)

	private void genererBoutonsPagePrincipale() {

		genererBoutonsDanse(false, false, false);

		VariableUtile.boutonSuivant = new Bouton(VariableUtile.px * 76, VariableUtile.py * 47.5, VariableUtile.px * LB,
				VariableUtile.py * HB, 22, "Suivant");
		VariableUtile.boutonSuivant.setVisible(false);
		VariableUtile.boutonSuivant.cadre.setOnMouseReleased(me -> pageSuivante());

		VariableUtile.boutonPrecedent = new Bouton(VariableUtile.px * 4.2, VariableUtile.py * 47.5,
				VariableUtile.px * LB, VariableUtile.py * HB, 22, "Précédent");
		VariableUtile.boutonPrecedent.setVisible(false);
		VariableUtile.boutonPrecedent.cadre.setOnMouseReleased(me -> pagePrecedente());

		// Petites flèches de part et d'autre de l'indicateur de page
		double cote = VariableUtile.px * 2.2;
		double yFleche = VariableUtile.hauteurFenetre * 0.18 - cote * 0.62;
		Bouton flecheGauche = new Bouton(VariableUtile.px * 34.4, yFleche, cote, cote, 0, "ChevronGauche");
		flecheGauche.setVisible(false);
		flecheGauche.cadre.setOnMouseReleased(me -> pagePrecedente());
		Bouton flecheDroite = new Bouton(VariableUtile.px * 51.6, yFleche, cote, cote, 0, "ChevronDroite");
		flecheDroite.setVisible(false);
		flecheDroite.cadre.setOnMouseReleased(me -> pageSuivante());
		VariableUtile.boutonsMenu.add(flecheGauche);
		VariableUtile.boutonsMenu.add(flecheDroite);
	}

	private void genererBoutonsFinDeDanse() {
		VariableUtile.boutonQuitter = new Bouton(VariableUtile.px * 93, VariableUtile.py * 45, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Quitter");
		VariableUtile.boutonQuitter.setVisible(false);
		VariableUtile.boutonQuitter.cadre.setOnMouseReleased(me -> VariableUtile.quitterDanse());

		VariableUtile.boutonRejouer = new Bouton(VariableUtile.px * 93, VariableUtile.py * 60, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Rejouer");
		VariableUtile.boutonRejouer.setVisible(false);
		VariableUtile.boutonRejouer.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (!VariableUtile.modeMelange) {
					VariableUtile.lecteurVLCJ.arreter();
					VariableUtile.lecteurVLCJ.lancerAuTemps(0);
					VariableUtile.lecteurVLCJ.imageView.setVisible(false);

					VariableUtile.lecteurVLCJ.estEnPleinEcran = true;
					VariableUtile.primaryStage.setFullScreen(true);
					VariableUtile.cacherMenuPrincipal();
					VariableUtile.cacherBarreAction();
					VariableUtile.videoEnCours = true;

					// Décompte 3-2-1 : tout se passe sur le thread JavaFX et peut être annulé
					annulerDecompte();
					decompteAnim = new Timeline(new KeyFrame(Duration.millis(500), e -> bipDecompte("3")),
							new KeyFrame(Duration.millis(1500), e -> bipDecompte("2")),
							new KeyFrame(Duration.millis(2500), e -> bipDecompte("1")),
							new KeyFrame(Duration.millis(3500), e -> {
								finirDecompte();
								Theme.afficherFond(false);
								VariableUtile.lecteurVLCJ.imageView.setVisible(true);
								VariableUtile.lecteurVLCJ.demarrer(null);
								VariableUtile.scene.setCursor(Cursor.NONE);
							}));
					decompteAnim.play();
				} else {
					VariableUtile.stoperMelange();
					VariableUtile.lancerMelange();
				}
			}
		});

		VariableUtile.boutonPasser = new Bouton(VariableUtile.px * 93, VariableUtile.py * 75, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Passer");
		VariableUtile.boutonPasser.setVisible(false);
		VariableUtile.boutonPasser.cadre.setOnMouseReleased(me -> {
			VariableUtile.lecteurVLCJ.arreter();
			VariableUtile.lancerVideoAuHasard();
		});
	}

	private void bipDecompte(String nombre) {
		try {
			if (mediaPlayerCompteur != null) {
				mediaPlayerCompteur.seek(Duration.ZERO);
				mediaPlayerCompteur.play();
			}
		} catch (Exception e) {
			Diagnostic.log("Son du décompte", e);
		}
		decompte(nombre);
	}

	/** Annule un décompte en cours (sinon la vidéo démarrerait quand même après « Quitter »). */
	public static void annulerDecompte() {
		if (decompteAnim != null) {
			decompteAnim.stop();
			decompteAnim = null;
		}
		if (VariableUtile.textCompteur != null) {
			VariableUtile.textCompteur.setVisible(false);
		}
	}

	private void genererBoutonMelange() {
		VariableUtile.boutonMelange = new Bouton(VariableUtile.px * 27.2, VariableUtile.py * 3.2, VariableUtile.px * LB,
				VariableUtile.py * HB, 22, "Mélange");
		VariableUtile.boutonMelange.setVisible(false);
		VariableUtile.boutonMelange.cadre.setOnMouseReleased(me -> VariableUtile.lancerMelange());
	}

	private void genererBoutonIllimite() {
		VariableUtile.boutonIllimite = new Bouton(VariableUtile.px * 18.8, VariableUtile.py * 3.2, VariableUtile.px * LB,
				VariableUtile.py * HB, 22, "Illimite");
		VariableUtile.boutonIllimite.setLabel("Illimité");
		VariableUtile.boutonIllimite.setVisible(false);
		VariableUtile.boutonIllimite.cadre.setOnMouseReleased(me -> {
			if (!VariableUtile.dansesFiltrees.isEmpty()) {
				VariableUtile.modeIllimite = true;
			}
			VariableUtile.lancerVideoAuHasard();
		});
	}

	private void genererBoutonsSelection() {
		VariableUtile.boutonSelection = new Bouton(VariableUtile.px * 35.6, VariableUtile.py * 3.2,
				VariableUtile.px * LB, VariableUtile.py * HB, 22, "Selection");
		VariableUtile.boutonSelection.setLabel("Sélection");
		VariableUtile.boutonSelection.setVisible(false);
		VariableUtile.boutonSelection.cadre.setOnMouseReleased(me -> VariableUtile.ouvrirEcranSelection());

		VariableUtile.boutonLancerSelection = new Bouton(VariableUtile.px * 85, VariableUtile.py * 80,
				VariableUtile.px * 8.8, VariableUtile.px * 8, 25, "Valider_filtre");
		VariableUtile.boutonLancerSelection.setLabel("C'est parti !");
		VariableUtile.boutonLancerSelection.setVisible(false);
		VariableUtile.boutonLancerSelection.cadre.setOnMouseReleased(me -> {
			VariableUtile.numeroProchaineDanseSelection = 0;
			if (sauterDansesVides()) {
				VariableUtile.lancerVideo(
						VariableUtile.dansesSelectionnees.get(VariableUtile.numeroProchaineDanseSelection));
			}
		});
		VariableUtile.boutonReinitialiserSelection = new Bouton(VariableUtile.px * 77, VariableUtile.py * 2,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Rejouer");
		VariableUtile.boutonReinitialiserSelection.setLabel("Au hasard");
		VariableUtile.boutonReinitialiserSelection.setVisible(false);
		VariableUtile.boutonReinitialiserSelection.cadre
				.setOnMouseReleased(me -> VariableUtile.ouvrirEcranSelection());
		VariableUtile.boutonAnnulerSelection = new Bouton(VariableUtile.px * 85, VariableUtile.py * 2,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Quitter");
		VariableUtile.boutonAnnulerSelection.setLabel("Annuler");
		VariableUtile.boutonAnnulerSelection.setVisible(false);
		VariableUtile.boutonAnnulerSelection.cadre.setOnMouseReleased(me -> VariableUtile.finSelection());
	}

	private void genererBoutonsTri() {
		VariableUtile.boutonTrier = new Bouton(VariableUtile.px * 10.4, VariableUtile.py * 3.2, VariableUtile.px * LB,
				VariableUtile.py * HB, 22, "Trier");
		VariableUtile.boutonTrier.setVisible(false);
		VariableUtile.boutonTrier.cadre.setOnMouseReleased(me -> {
			VariableUtile.cacherMenuPrincipal();
			VariableUtile.cacherEcranSelection();
			VariableUtile.afficherMenuTri();
		});
		VariableUtile.boutonTriValider = new Bouton(VariableUtile.px * 90, VariableUtile.py * 76,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Valider_filtre");
		VariableUtile.boutonTriValider.setLabel("Valider");
		VariableUtile.boutonTriValider.setVisible(false);
		VariableUtile.boutonTriValider.cadre.setOnMouseReleased(me -> {
			VariableUtile.modeTri = false;
			validerTri();
		});

		int nbBouton = 5;
		double margeX = VariableUtile.px * 5;
		double largeurX = VariableUtile.px * 9.8;
		double positiondebutX = (VariableUtile.largeurFenetre - (nbBouton - 1) * margeX - nbBouton * largeurX) / 2;

		VariableUtile.boutonTriAleatoire = new Bouton(positiondebutX, VariableUtile.py * 17, largeurX,
				VariableUtile.px * 10, 25, "Aleatoire");
		VariableUtile.boutonTriAleatoire.etatValide = true;
		VariableUtile.boutonTriAleatoire.finirGenerationBoutonTri("Aléatoire", Tri.Aleatoire);

		VariableUtile.boutonTriAlphabetique = new Bouton(positiondebutX + 1 * (largeurX + margeX),
				VariableUtile.py * 17, largeurX, VariableUtile.px * 10, 25, "Alphabetique");
		VariableUtile.boutonTriAlphabetique.finirGenerationBoutonTri("Alphabétique", Tri.Alphabetique);

		VariableUtile.boutonTriArtiste = new Bouton(positiondebutX + 2 * (largeurX + margeX), VariableUtile.py * 17,
				largeurX, VariableUtile.px * 10, 25, "Artiste");
		VariableUtile.boutonTriArtiste.finirGenerationBoutonTri(null, Tri.ParArtiste);

		VariableUtile.boutonTriVersion = new Bouton(positiondebutX + 3 * (largeurX + margeX), VariableUtile.py * 17,
				largeurX, VariableUtile.px * 10, 25, "Version");
		VariableUtile.boutonTriVersion.finirGenerationBoutonTri(null, Tri.ParVersion);

		VariableUtile.boutonTriOrdre = new Bouton(positiondebutX + 4 * (largeurX + margeX), VariableUtile.py * 17,
				largeurX, VariableUtile.px * 10, 25, "Ordre");
		VariableUtile.boutonTriOrdre.finirGenerationBoutonTri("Ajout", Tri.OrdreCreation);

		for (Bouton bouton : VariableUtile.boutonsTri) {
			if (bouton.typeTri != Tri.Aleatoire) {
				bouton.invaliderBouton();
			}
		}
	}

	private Text titreFiltre(String libelle, double xCentre, double y, double largeur) {
		// xCentre : milieu du titre, pour l'aligner sur ses boutons
		Text t = new Text(VariableUtile.px * (xCentre - largeur / 2), VariableUtile.py * y, libelle);
		t.setFont(Theme.police(VariableUtile.py * 2.6));
		t.setWrappingWidth(VariableUtile.px * largeur);
		t.setTextAlignment(TextAlignment.CENTER);
		t.setFill(Color.WHITE);
		t.setEffect(Theme.lueur(Theme.CYAN, 0.8, 14));
		t.setVisible(false);
		t.setMouseTransparent(true);
		VariableUtile.textesFiltres.add(t);
		VariableUtile.root.getChildren().add(t);
		return t;
	}

	// Boutons de filtre : un peu plus grands qu'avant pour mieux porter leur icône
	private static final double LF = 7.6; // largeur (% de la largeur de la fenêtre, en px)
	private static final double HF = 8.0; // hauteur (en px)
	private static final double Y1 = 20; // 3 rangées de boutons (% de la hauteur)
	private static final double Y2 = 40;
	private static final double Y3 = 60;
	private static final double Y_TITRES = 17.6; // sous la barre de recherche (qui se termine vers 12,7)

	private Bouton boutonFiltre(double x, double y, String image) {
		return new Bouton(VariableUtile.px * x, VariableUtile.py * y, VariableUtile.px * LF, VariableUtile.px * HF, 25,
				image);
	}

	/** Versions réellement présentes dans la bibliothèque, dans l'ordre de l'enum. */
	private void genererListeVersions() {
		ArrayList<String> libelles = new ArrayList<>();
		ArrayList<Version> valeurs = new ArrayList<>();
		libelles.add("Toutes");
		valeurs.add(null);
		for (Version version : Version.values()) {
			boolean presente = false;
			for (Danse danse : VariableUtile.danses.values()) {
				if (danse.version == version) {
					presente = true;
					break;
				}
			}
			// pour proposer aussi les versions sans aucune danse, passer MASQUER_VERSIONS_VIDES à false
			if (presente || !MASQUER_VERSIONS_VIDES) {
				libelles.add(version.getNomComplet());
				valeurs.add(version);
			}
		}
		VariableUtile.versionFiltre = null;
		VariableUtile.listeVersions = new ListeDeroulante<Version>(VariableUtile.px * 61, VariableUtile.py * 46,
				VariableUtile.px * 18, VariableUtile.py * 6.2, libelles, valeurs);
		VariableUtile.listeVersions.setVisible(false);
		VariableUtile.listeVersions.setOnChoix(version -> VariableUtile.versionFiltre = version);
	}

	private static final boolean MASQUER_VERSIONS_VIDES = true;

	private void genererBoutonsFiltre() {
		VariableUtile.boutonFiltrer = new Bouton(VariableUtile.px * 2, VariableUtile.py * 3.2, VariableUtile.px * LB,
				VariableUtile.py * HB, 22, "Filtrer");
		VariableUtile.boutonFiltrer.setVisible(false);
		VariableUtile.boutonFiltrer.cadre.setOnMouseReleased(me -> {
			VariableUtile.cacherMenuPrincipal();
			VariableUtile.cacherEcranSelection();
			VariableUtile.afficherMenuFiltres();
		});

		// Valider / Réinitialiser : remontés (ils touchaient le bas de l'écran)
		VariableUtile.boutonFiltreValider = new Bouton(VariableUtile.px * 90, VariableUtile.py * 76,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Valider_filtre");
		VariableUtile.boutonFiltreValider.setLabel("Valider");
		VariableUtile.boutonFiltreValider.setVisible(false);
		VariableUtile.boutonFiltreValider.cadre.setOnMouseReleased(me -> {
			VariableUtile.modeFiltre = false;
			validerFiltre();
		});
		VariableUtile.boutonFiltreReinitialiser = new Bouton(VariableUtile.px * 79.6, VariableUtile.py * 76,
				VariableUtile.px * 8.8, VariableUtile.px * 7, 20, "Rejouer");
		VariableUtile.boutonFiltreReinitialiser.setLabel("Réinitialiser");
		VariableUtile.boutonFiltreReinitialiser.setVisible(false);
		VariableUtile.boutonFiltreReinitialiser.cadre.setOnMouseReleased(me -> {
			for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
				boutonFiltre.validerBouton();
			}
			if (VariableUtile.listeVersions != null) {
				VariableUtile.listeVersions.reinitialiser();
			}
		});

		// Les titres sont centrés au-dessus de leurs colonnes de boutons
		double demi = LF / 2;
		VariableUtile.textFiltreIntensite = titreFiltre("Intensité", 2 + demi, Y_TITRES, 9);
		VariableUtile.boutonFiltreIntensiteFaible = boutonFiltre(2, Y1, "Intensite_faible");
		VariableUtile.boutonFiltreIntensiteFaible.finirGenerationBoutonFiltre("Faible");
		VariableUtile.boutonFiltreIntensiteModeree = boutonFiltre(2, Y2, "Intensite_moderee");
		VariableUtile.boutonFiltreIntensiteModeree.finirGenerationBoutonFiltre("Modérée");
		VariableUtile.boutonFiltreIntensiteIntense = boutonFiltre(2, Y3, "Intensite_intense");
		VariableUtile.boutonFiltreIntensiteIntense.finirGenerationBoutonFiltre("Intense");

		VariableUtile.textFiltreTechnique = titreFiltre("Technique", 11 + demi, Y_TITRES, 9);
		VariableUtile.boutonFiltreTechniqueFaible = boutonFiltre(11, Y1, "Technique_faible");
		VariableUtile.boutonFiltreTechniqueFaible.finirGenerationBoutonFiltre("Faible");
		VariableUtile.boutonFiltreTechniqueModeree = boutonFiltre(11, Y2, "Technique_moderee");
		VariableUtile.boutonFiltreTechniqueModeree.finirGenerationBoutonFiltre("Modérée");
		VariableUtile.boutonFiltreTechniqueIntense = boutonFiltre(11, Y3, "Technique_elevee");
		VariableUtile.boutonFiltreTechniqueIntense.finirGenerationBoutonFiltre("Elevée");

		VariableUtile.texteFiltreGenre = titreFiltre("Genre", 36 + demi, Y_TITRES, 9);
		VariableUtile.boutonFiltreSportif = boutonFiltre(25, Y1, "Sportif");
		VariableUtile.boutonFiltreSportif.finirGenerationBoutonFiltre(null, Genre.Sportif);
		VariableUtile.boutonFiltreElectro = boutonFiltre(25, Y2, "Electro");
		VariableUtile.boutonFiltreElectro.finirGenerationBoutonFiltre(null, Genre.Electro);
		VariableUtile.boutonFiltreLatino = boutonFiltre(25, Y3, "Latino");
		VariableUtile.boutonFiltreLatino.finirGenerationBoutonFiltre(null, Genre.Latino);
		VariableUtile.boutonFiltreFolklore = boutonFiltre(36, Y1, "Folklore");
		VariableUtile.boutonFiltreFolklore.finirGenerationBoutonFiltre("Folk", Genre.Folklore);
		VariableUtile.boutonFiltreRandB = boutonFiltre(36, Y2, "RandB");
		VariableUtile.boutonFiltreRandB.finirGenerationBoutonFiltre("R&B", Genre.RandB);
		VariableUtile.boutonFiltreRock = boutonFiltre(36, Y3, "Rock");
		VariableUtile.boutonFiltreRock.finirGenerationBoutonFiltre(null, Genre.Rock);
		VariableUtile.boutonFiltreRetro = boutonFiltre(47, Y1, "Retro");
		VariableUtile.boutonFiltreRetro.finirGenerationBoutonFiltre("Retro", Genre.Retro);
		VariableUtile.boutonFiltrePop = boutonFiltre(47, Y2, "Pop");
		VariableUtile.boutonFiltrePop.finirGenerationBoutonFiltre("Pop", Genre.Pop);
		VariableUtile.boutonFiltreJazzy = boutonFiltre(47, Y3, "Jazzy");
		VariableUtile.boutonFiltreJazzy.finirGenerationBoutonFiltre("Jazzy", Genre.Jazzy);

		VariableUtile.texteFiltreNbDanseurs = titreFiltre("Nombre de danseurs", 61 + (88 - 61) / 2.0 + demi, Y_TITRES, 32);
		VariableUtile.boutonFiltre1Danseur = boutonFiltre(61, Y1, "UnDanseur");
		VariableUtile.boutonFiltre1Danseur.finirGenerationBoutonFiltre("Un", 1);
		VariableUtile.boutonFiltre2Danseurs = boutonFiltre(70, Y1, "DeuxDanseurs");
		VariableUtile.boutonFiltre2Danseurs.finirGenerationBoutonFiltre("Deux", 2);
		VariableUtile.boutonFiltre3Danseurs = boutonFiltre(79, Y1, "TroisDanseurs");
		VariableUtile.boutonFiltre3Danseurs.finirGenerationBoutonFiltre("Trois", 3);
		VariableUtile.boutonFiltre4Danseurs = boutonFiltre(88, Y1, "QuatreDanseurs");
		VariableUtile.boutonFiltre4Danseurs.finirGenerationBoutonFiltre("Quatre +", 4);

		// Filtre par version : titre + liste déroulante sous les boutons « nombre de danseurs »
		VariableUtile.texteFiltreVersion = titreFiltre("Version", 61 + 9, 42.6, 18);
		genererListeVersions();
	}

	public static void trierDanse(Tri modeDeTri) {

		switch (modeDeTri) {
		case Aleatoire:
			Collections.shuffle(VariableUtile.dansesFiltrees);
			break;
		case Alphabetique:
			VariableUtile.dansesFiltrees.sort(Comparator.comparing(danse -> danse.titreMusique));
			break;
		case ParArtiste:
			VariableUtile.dansesFiltrees
					.sort(Comparator.comparing(danse -> danse.artistes.isEmpty() ? "" : danse.artistes.get(0),
							Comparator.nullsLast(Comparator.naturalOrder())));
			break;
		case ParVersion:
			VariableUtile.dansesFiltrees.sort(
					Comparator.comparing(danse -> danse.version, Comparator.nullsLast(Comparator.naturalOrder())));
			break;
		case OrdreCreation:
			VariableUtile.dansesFiltrees.sort(Comparator.comparingInt(danse -> danse.ordreAjout));
			break;
		default:
			break;
		}
		int ordreActuel = 1;
		for (Danse danse : VariableUtile.dansesFiltrees) {
			danse.ordreActuel = ordreActuel;
			ordreActuel++;
		}

		// (corrigé : on testait deux fois modeFiltre et jamais modeTri, donc taper dans
		// la barre de recherche pendant l'écran de tri faisait quitter cet écran)
		if (!VariableUtile.modeFiltre && !VariableUtile.modeTri) {
			VariableUtile.cacherMenuTri();
			VariableUtile.cacherMenuFiltres();
			if (!VariableUtile.modeSelection) {
				VariableUtile.genererBoutonSuggestion(true);
				VariableUtile.genererTextPage();
				genererBoutonsDanse(true);

				VariableUtile.afficherMenuPrincipal();
			} else {
				VariableUtile.reafficherEcranSelection();
			}
		}
	}

	private void completerDanses() {
		int ordreAjout = 1;
		int ignorees = 0;
		File fichier = VariableUtile.fichierInfosDanses;
		if (fichier == null || !fichier.exists()) {
			afficherErreur("Impossible d'ouvrir le fichier " + fichier);
			return;
		}
		try (Scanner scanner = new Scanner(fichier, "UTF-8")) {
			int numeroLigne = 0;
			while (scanner.hasNextLine()) {
				numeroLigne++;
				String ligne = scanner.nextLine();
				try {
					String[] infosStr = ligne.split("\\|");
					String titreMusique = infosStr[0];

					if (VariableUtile.danses.containsKey(titreMusique)) {
						Danse danseACompleter = VariableUtile.danses.get(titreMusique);
						String listeIdArtistes[] = infosStr[1].split("-");
						danseACompleter.ordreAjout = ordreAjout;
						for (int i = 0; i < listeIdArtistes.length; i++) {
							String artiste = VariableUtile.artistes.get(Integer.valueOf(listeIdArtistes[i]));
							danseACompleter.artistes.add(artiste);
						}
						danseACompleter.intensite = Integer.valueOf(infosStr[2]);
						danseACompleter.technique = Integer.valueOf(infosStr[3]);
						String couleur1[] = infosStr[4].split("-");
						String couleur2[] = infosStr[5].split("-");
						danseACompleter.couleur1 = Color.rgb(Integer.valueOf(couleur1[0]), Integer.valueOf(couleur1[1]),
								Integer.valueOf(couleur1[2]));
						danseACompleter.couleur2 = Color.rgb(Integer.valueOf(couleur2[0]), Integer.valueOf(couleur2[1]),
								Integer.valueOf(couleur2[2]));
						danseACompleter.unlimited = Boolean.valueOf(infosStr[6]);
						String listeGenreStr[] = infosStr[7].split("-");
						for (int i = 0; i < listeGenreStr.length; i++) {
							danseACompleter.genres.add(Genre.valueOf(listeGenreStr[i]));
						}
					}
				} catch (Exception e) {
					// une ligne invalide n'empêche plus de lire les suivantes
					ignorees++;
					Diagnostic.log("Ligne " + numeroLigne + " ignorée dans " + fichier.getName() + " : " + e);
				}
				ordreAjout++;
			}
			for (Danse danse : VariableUtile.danses.values()) {
				if (danse.ordreAjout == 0) {
					danse.ordreAjout = ordreAjout;
					ordreAjout++;
				}
			}
		} catch (IOException e) {
			afficherErreur("Impossible d'ouvrir le fichier " + fichier + " " + e);
		}
		if (ignorees > 0) {
			afficherErreur(ignorees + " ligne(s) invalide(s) ignorée(s) dans " + fichier.getName()
					+ " (détail dans danse.log)");
		}
	}

	private void importerConfig() {
		File fichierConfig = new File(Diagnostic.dossierApp(), "Config.txt");
		if (!fichierConfig.exists()) {
			afficherErreur("Fichier Config.txt introuvable à : " + fichierConfig.getAbsolutePath());
			return;
		}
		try (Scanner scanner = new Scanner(fichierConfig, "UTF-8")) {
			VariableUtile.dossierDanses = new File(scanner.nextLine().trim());
			VariableUtile.dossierImagesDanses = new File(scanner.nextLine().trim());
			VariableUtile.dossierImports = new File(scanner.nextLine().trim());
			VariableUtile.fichierInfosDanses = new File(scanner.nextLine().trim());
		} catch (Exception e) {
			Diagnostic.log("Config.txt illisible", e);
			afficherErreur("Impossible de lire Config.txt (4 lignes attendues) : " + e);
			VariableUtile.dossierDanses = null;
		}
	}

	private void importerTitresVideosDanses() {
		try (Stream<java.nio.file.Path> flux = Files.list(VariableUtile.dossierDanses.toPath())) {
			flux.limit(5000).forEach(path -> {
				File fichierDanse = path.toFile();
				String nomCompletDanse = fichierDanse.getName();
				String titreMusique;
				Version version = null;
				if (nomCompletDanse.contains(" - ")) {
					titreMusique = nomCompletDanse.substring(0, nomCompletDanse.indexOf(" - "));
					String versionString = nomCompletDanse.substring(nomCompletDanse.indexOf(" - ") + 3,
							nomCompletDanse.lastIndexOf("."));
					Version[] versions = Version.values();
					for (int i = 0; i < versions.length; i++) {
						if (String.valueOf(versions[i]).equals(versionString)) {
							version = Version.valueOf(versionString);
							break;
						}
					}
					if (version == null) {
						// Nom non conforme
						titreMusique = nomCompletDanse;
					}
				} else {
					// Nom non conforme
					titreMusique = nomCompletDanse;
				}
				new Danse(titreMusique, version, Color.rgb(10, 10, 10), Color.rgb(240, 240, 240));
			});
		} catch (IOException e) {
			afficherErreur("Impossible de charger le dossier " + VariableUtile.dossierDanses + " " + e);
		}
	}

	private void importerListeArtistes() {
		File fichierArtistes = new File(VariableUtile.dossierImports, "artistes.txt");
		try (Scanner scanner = new Scanner(fichierArtistes, "UTF-8")) {
			while (scanner.hasNextLine()) {
				String[] artisteStr = scanner.nextLine().split("\\|");
				try {
					VariableUtile.artistes.put(Integer.valueOf(artisteStr[0]), artisteStr[1]);
				} catch (Exception e) {
					Diagnostic.log("Ligne d'artiste ignorée : " + String.join("|", artisteStr));
				}
			}
		} catch (IOException e) {
			afficherErreur("Impossible d'ouvrir le fichier " + fichierArtistes + " " + e);
		}
	}

	public static void genererBoutonsDanse(boolean nettoyer) {
		genererBoutonsDanse(nettoyer, false, true);
	}

	public static void genererBoutonsDanse(boolean nettoyer, boolean modeSelection, boolean visible) {
		// emplacement : mettre 7 par défaut
		if (nettoyer) {
			for (Bouton bouton : VariableUtile.boutonsDanse) {
				bouton.setVisible(false);
				if (bouton.changerDanseBouton != null) {
					VariableUtile.root.getChildren().remove(bouton.changerDanseBouton);
				}
				VariableUtile.root.getChildren().remove(bouton);
			}
			VariableUtile.boutonsDanse.clear();
		}
		ArrayList<Danse> dansesAafficher = modeSelection ? VariableUtile.dansesSelectionnees
				: VariableUtile.dansesFiltrees;
		ArrayList<Danse> affichees = new ArrayList<>();

		for (int i = 0; i < 6; i++) {
			int numeroDanse = modeSelection ? i : i + 6 * VariableUtile.page;
			if (dansesAafficher.size() > numeroDanse) {

				double largeurPositionBouton = i > 2 ? VariableUtile.px * (i - 3) * 20 + VariableUtile.px * 15
						: VariableUtile.px * i * 20 + VariableUtile.px * 15;
				double hauteurPositionBouton = i > 2 ? VariableUtile.py * 60 : VariableUtile.py * 22;
				Danse danse = dansesAafficher.get(numeroDanse);
				affichees.add(danse);
				Bouton boutonDanse = new Bouton(largeurPositionBouton, hauteurPositionBouton, VariableUtile.px * 17,
						VariableUtile.py * 33, 20, danse, modeSelection, i);
				if (danse.imageDanse1 == null) {
					// chargement en arrière-plan, image posée ensuite sur le thread JavaFX
					danse.importerImagesAsync(boutonDanse::rafraichirImage);
				}

				if (!visible) {
					boutonDanse.setVisible(false);
				}

				VariableUtile.boutonsDanse.add(boutonDanse);
			}
		}
		if (VariableUtile.boutonSuggestion != null) {
			affichees.add(VariableUtile.boutonSuggestion.danse);
		}
		Danse.limiterMemoire(affichees);
	}

	public static void decompte(String nombre) {
		VariableUtile.formerNombreCompteur(nombre);
		VariableUtile.textCompteur.setVisible(true);
	}

	public static void finirDecompte() {
		VariableUtile.textCompteur.setVisible(false);
	}

	public void validerTri() {
		for (Bouton boutonFiltre : VariableUtile.boutonsTri) {
			if (boutonFiltre.typeTri != null && boutonFiltre.etatValide) {
				trierDanse(boutonFiltre.typeTri);
			}
		}
	}

	public void validerFiltre() {
		VariableUtile.page = 0;
		VariableUtile.dansesFiltrees.clear();
		for (Danse danse : VariableUtile.danses.values()) {
			if ((VariableUtile.boutonFiltreIntensiteFaible.etatValide || danse.intensite != 1)
					&& (VariableUtile.boutonFiltreIntensiteModeree.etatValide || danse.intensite != 2)
					&& (VariableUtile.boutonFiltreIntensiteIntense.etatValide || danse.intensite != 3)
					&& (VariableUtile.boutonFiltreTechniqueFaible.etatValide || danse.technique != 1)
					&& (VariableUtile.boutonFiltreTechniqueModeree.etatValide || danse.technique != 2)
					&& (VariableUtile.boutonFiltreTechniqueIntense.etatValide || danse.technique != 3)
					&& aAuMoinsUnGenreActive(danse) && aAuMoinsUnNbDanseursActive(danse)
					&& (VariableUtile.versionFiltre == null || danse.version == VariableUtile.versionFiltre)) {
				VariableUtile.dansesFiltrees.add(danse);
			}
		}
		// Barre de recherche
		String recherche = VariableUtile.barreRecherche.text.getText().toUpperCase();
		if (recherche.length() > 0) {
			ArrayList<Danse> dansesRecherche = new ArrayList<>();
			for (Danse danse : VariableUtile.dansesFiltrees) {
				if (danse.titreMusique.toUpperCase().contains(recherche)
						|| danse.artistes.toString().toUpperCase().contains(recherche)) {
					dansesRecherche.add(danse);
				}
			}
			VariableUtile.dansesFiltrees.retainAll(dansesRecherche);
		}

		try {
			validerTri();
		} catch (Exception e) {
			Diagnostic.log("Erreur validerTri", e);
			MainDanse.afficherErreur("Erreur validerTri : " + e.getMessage());
		}
	}

	protected boolean aAuMoinsUnGenreActive(Danse danse) {
		boolean tousFiltresGenreActifs = true;
		for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
			if (boutonFiltre.genre != null) {
				if (boutonFiltre.etatValide) {
					if (danse.genres.contains(boutonFiltre.genre)) {
						return true;
					}
				} else {
					tousFiltresGenreActifs = false;
				}
			}
		}
		return tousFiltresGenreActifs;
	}

	protected boolean aAuMoinsUnNbDanseursActive(Danse danse) {
		boolean tousFiltresNbActifs = true;
		for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
			if (boutonFiltre.nbDanseurs != 0) {
				if (boutonFiltre.etatValide) {
					if (danse.genres.contains(Genre.Duo)) {
						if (boutonFiltre.nbDanseurs == 2) {
							return true;
						}
					} else if (danse.genres.contains(Genre.Trio)) {
						if (boutonFiltre.nbDanseurs == 3) {
							return true;
						}
					} else if (danse.genres.contains(Genre.Quatuor) || danse.genres.contains(Genre.Quintette)) {
						if (boutonFiltre.nbDanseurs == 4) {
							return true;
						}
					} else if (boutonFiltre.nbDanseurs == 1) {
						return true;
					}
				} else {
					tousFiltresNbActifs = false;
				}
			}
		}
		return tousFiltresNbActifs;
	}

	public static boolean sauterDansesVides() {
		// Attention VariableUtile.numeroProchaineDanseSelection est modifié
		// dans la méthode
		boolean tousVides = false;
		while (VariableUtile.numeroProchaineDanseSelection < 6 && VariableUtile.dansesSelectionnees
				.get(VariableUtile.numeroProchaineDanseSelection).equals(VariableUtile.danseNeant)) {
			VariableUtile.numeroProchaineDanseSelection++;
			if (VariableUtile.numeroProchaineDanseSelection == 6) {
				tousVides = true;
			}
		}
		return !tousVides;
	}

	/** Conservé pour compatibilité : la couleur du fond est gérée par le thème. */
	public static void actualiserCouleurPrincipal() {
		if (VariableUtile.danseSelectionnee != null) {
			Theme.changerAmbiance(Theme.accentDe(VariableUtile.danseSelectionnee));
		}
	}

	public Image importerImage(String imageURI) {
		URL imageURL = getClass().getResource(imageURI);
		if (imageURL == null) {
			afficherErreur("Image non trouvée : " + imageURI);
			return null;
		}
		try {
			return new Image(imageURL.toExternalForm());
		} catch (Exception e) {
			afficherErreur("Erreur lors du chargement image : " + imageURI + "\n" + e.getMessage());
			return null;
		}
	}

	/**
	 * Affiche une erreur SANS fenêtre modale (une boîte Alert modale ouverte
	 * derrière la fenêtre plein écran bloquait toute l'interface sans rien
	 * montrer). L'erreur est écrite dans danse.log et affichée brièvement.
	 */
	public static void afficherErreur(String message) {
		Diagnostic.log("ERREUR : " + message);
		Theme.toast(message);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() {
		Diagnostic.log("Arrêt de l'application");
		Platform.runLater(() -> {
		});
		annulerDecompte();
		try {
			VariableUtile.stoperMelange();
		} catch (Exception e) {
			Diagnostic.log("stoperMelange à la fermeture", e);
		}
		if (VariableUtile.lecteurAudio != null) {
			VariableUtile.lecteurAudio.liberer();
		}
		if (VariableUtile.lecteurVLCJ != null) {
			VariableUtile.lecteurVLCJ.liberer();
		}
		if (mediaPlayerCompteur != null) {
			mediaPlayerCompteur.dispose();
		}
		System.exit(0);
	}
}