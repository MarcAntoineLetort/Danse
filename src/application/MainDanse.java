package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainDanse extends Application {
	Media soundDecompte;
	MediaPlayer mediaPlayerCompteur;

	@Override
	public void start(Stage pStage) {
		VariableUtile.primaryStage = pStage;
		VariableUtile.root = new BorderPane();
		VariableUtile.main = this;
		VariableUtile.largeurFenetre = Screen.getPrimary().getVisualBounds().getWidth();
		VariableUtile.hauteurFenetre = Screen.getPrimary().getVisualBounds().getHeight() - 33;
		VariableUtile.scene = new Scene(VariableUtile.root, VariableUtile.largeurFenetre, VariableUtile.hauteurFenetre);
		VariableUtile.primaryStage.setMaximized(true);
		VariableUtile.px = VariableUtile.largeurFenetre / 100;
		VariableUtile.py = VariableUtile.hauteurFenetre / 100;

		VariableUtile.shadePaint = new RadialGradient(0.5, 0.5, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(1, VariableUtile.couleur1), new Stop(0, VariableUtile.couleur2));

		VariableUtile.scene.setFill(VariableUtile.shadePaint);

		try {
			soundDecompte = new Media(MainDanse.class.getResource("/application/selection1.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			afficherErreur("Son /application/selection1.mp3 introuvable " + e);
		}
		mediaPlayerCompteur = new MediaPlayer(soundDecompte);

		VariableUtile.danses = new HashMap<String, Danse>();
		VariableUtile.dansesFiltrees = new ArrayList<>();
		VariableUtile.dansesSelectionnees = new ArrayList<>();

		VariableUtile.danseNeant = new Danse("N�ant", "Quitter", Color.BLACK, Color.WHITE);

		VariableUtile.artistes = new HashMap<Integer, String>();

		importerConfig();

		importerTitresVideosDanses();

		importerListeArtistes();

		completerDanses();

		// TODO pr�charger les vid�os enti�rement avant de les lancer
		// TODO afficher les vid�os dans un ordre al�atoire sur la page
		// principale + ajouter un onglet tri
		// TODO ajouter un bouton de pause
		// TODO ajouter le filtre nombre de danseurs + version jd

		// TODO A retelecharger : love is all
		// TODO A mettre plus fort : hey girl

		VariableUtile.boutonQuitter = new Bouton(VariableUtile.px * 93, VariableUtile.py * 45, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Quitter");
		VariableUtile.boutonQuitter.setVisible(false);
		VariableUtile.boutonQuitter.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.quitterDanse();
			}
		});

		VariableUtile.boutonRejouer = new Bouton(VariableUtile.px * 93, VariableUtile.py * 60, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Rejouer");
		VariableUtile.boutonRejouer.setVisible(false);
		VariableUtile.boutonRejouer.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (!VariableUtile.modeMelange) {
					VariableUtile.playerVideo.stop();
					VariableUtile.playerVideo.seek(Duration.ZERO);
					VariableUtile.mediaView.setVisible(false);

					VariableUtile.primaryStage.setFullScreen(true);
					VariableUtile.cacherMenuPrincipal();
					VariableUtile.cacherBarreAction();

					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							mediaPlayerCompteur.setStartTime(Duration.ZERO);
							mediaPlayerCompteur.seek(Duration.ZERO);
							mediaPlayerCompteur.play();

							decompte("3");
						}
					}, 500);
					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							mediaPlayerCompteur.setStartTime(Duration.ZERO);
							mediaPlayerCompteur.seek(Duration.ZERO);
							mediaPlayerCompteur.play();

							decompte("2");
						}
					}, 1500);
					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							mediaPlayerCompteur.setStartTime(Duration.ZERO);
							mediaPlayerCompteur.seek(Duration.ZERO);
							mediaPlayerCompteur.play();

							decompte("1");
						}
					}, 2500);
					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							VariableUtile.playerVideo.play();

							finirDecompte();
							VariableUtile.afficherBarreAction();
							VariableUtile.mediaView.setVisible(true);
							VariableUtile.scene.setCursor(Cursor.NONE);
						}
					}, 3500);
				} else {
					VariableUtile.stoperMelange();
					VariableUtile.lancerMelange();
				}
			}
		});

		VariableUtile.boutonPasser = new Bouton(VariableUtile.px * 93, VariableUtile.py * 75, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Passer");
		VariableUtile.boutonPasser.setVisible(false);
		VariableUtile.boutonPasser.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.playerVideo.stop();
				VariableUtile.mediaView.setVisible(false);
				VariableUtile.root.getChildren().remove(VariableUtile.mediaView);
				VariableUtile.lancerVideoAuHasard();
			}
		});

		VariableUtile.dansesFiltrees.addAll(VariableUtile.danses.values());

		genererBoutonsDanse(false);

		Bouton boutonSuivant = new Bouton(VariableUtile.px * 75, VariableUtile.py * 45, VariableUtile.px * 7,
				VariableUtile.px * 7, 25, "Suivant");

		boutonSuivant.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (VariableUtile.page < (int) Math.ceil(VariableUtile.dansesFiltrees.size() / 6)) {
					VariableUtile.page++;
				} else {
					VariableUtile.page = 0;
				}

				genererBoutonsDanse(true);
			}
		});
		Bouton boutonPrecedant = new Bouton(VariableUtile.px * 5, VariableUtile.py * 45, VariableUtile.px * 7,
				VariableUtile.px * 7, 25, "Pr�c�dant");

		boutonPrecedant.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (VariableUtile.page > 0) {
					VariableUtile.page--;
				} else {
					VariableUtile.page = (int) Math.ceil(VariableUtile.dansesFiltrees.size() / 6);
				}
				genererBoutonsDanse(true);
			}
		});

		// Filtres
		VariableUtile.boutonFiltrer = new Bouton(VariableUtile.px * 2, VariableUtile.py * 2, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Filtrer");
		VariableUtile.boutonFiltrer.setVisible(true);
		VariableUtile.boutonFiltrer.toFront();
		VariableUtile.boutonFiltrer.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.cacherMenuPrincipal();
				VariableUtile.cacherEcranSelection();
				VariableUtile.afficherMenuFiltres();
			}
		});

		VariableUtile.boutonFiltreValider = new Bouton(VariableUtile.px * 90, VariableUtile.py * 85,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Valider_filtre");
		VariableUtile.boutonFiltreValider.text.setText("Valider");
		VariableUtile.boutonFiltreValider.setVisible(false);
		VariableUtile.boutonFiltreValider.toFront();
		VariableUtile.boutonFiltreValider.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.modeFiltre = false;
				validerFiltre();
			}
		});
		VariableUtile.boutonFiltreReinitialiser = new Bouton(VariableUtile.px * 81, VariableUtile.py * 85,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 20, "Rejouer");
		VariableUtile.boutonFiltreReinitialiser.text.setText("R�initialiser");
		VariableUtile.boutonFiltreReinitialiser.setVisible(false);
		VariableUtile.boutonFiltreReinitialiser.toFront();
		VariableUtile.boutonFiltreReinitialiser.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
					boutonFiltre.validerBouton();
				}
			}
		});

		VariableUtile.textFiltreIntensite = new Text(VariableUtile.px * 1, VariableUtile.py * 14, "Intensit�");
		VariableUtile.textFiltreIntensite.setFont(new Font(VariableUtile.police, VariableUtile.py * 3));
		VariableUtile.textFiltreIntensite.setWrappingWidth(VariableUtile.px * 9);
		VariableUtile.textFiltreIntensite.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textFiltreIntensite.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		VariableUtile.textFiltreIntensite.setEffect(VariableUtile.effetTextPage);
		VariableUtile.textFiltreIntensite.setVisible(false);
		VariableUtile.textesFiltres.add(VariableUtile.textFiltreIntensite);
		VariableUtile.root.getChildren().add(VariableUtile.textFiltreIntensite);

		VariableUtile.boutonFiltreIntensiteFaible = new Bouton(VariableUtile.px * 2, VariableUtile.py * 17,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Intensite_faible");
		VariableUtile.boutonFiltreIntensiteFaible.finirGenerationBoutonFiltre("Faible", null);

		VariableUtile.boutonFiltreIntensiteModeree = new Bouton(VariableUtile.px * 2, VariableUtile.py * 37,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Intensite_moderee");
		VariableUtile.boutonFiltreIntensiteModeree.finirGenerationBoutonFiltre("Mod�r�e", null);

		VariableUtile.boutonFiltreIntensiteIntense = new Bouton(VariableUtile.px * 2, VariableUtile.py * 57,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Intensite_intense");
		VariableUtile.boutonFiltreIntensiteIntense.finirGenerationBoutonFiltre("Intense", null);

		VariableUtile.textFiltreTechnique = new Text(VariableUtile.px * 10, VariableUtile.py * 14, "Technique");
		VariableUtile.textFiltreTechnique.setFont(new Font(VariableUtile.police, VariableUtile.py * 3));
		VariableUtile.textFiltreTechnique.setWrappingWidth(VariableUtile.px * 9);
		VariableUtile.textFiltreTechnique.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textFiltreTechnique.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		VariableUtile.textFiltreTechnique.setEffect(VariableUtile.effetTextPage);
		VariableUtile.textFiltreTechnique.setVisible(false);
		VariableUtile.textesFiltres.add(VariableUtile.textFiltreTechnique);
		VariableUtile.root.getChildren().add(VariableUtile.textFiltreTechnique);

		VariableUtile.boutonFiltreTechniqueFaible = new Bouton(VariableUtile.px * 11, VariableUtile.py * 17,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Technique_faible");
		VariableUtile.boutonFiltreTechniqueFaible.finirGenerationBoutonFiltre("Faible", null);

		VariableUtile.boutonFiltreTechniqueModeree = new Bouton(VariableUtile.px * 11, VariableUtile.py * 37,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Technique_moderee");
		VariableUtile.boutonFiltreTechniqueModeree.finirGenerationBoutonFiltre("Mod�r�e", null);

		VariableUtile.boutonFiltreTechniqueIntense = new Bouton(VariableUtile.px * 11, VariableUtile.py * 57,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Technique_elevee");
		VariableUtile.boutonFiltreTechniqueIntense.finirGenerationBoutonFiltre("Elev�e", null);

		// Filtres genre -- Opacit� paint.net 150
		VariableUtile.textFiltreGenre = new Text(VariableUtile.px * 35, VariableUtile.py * 14, "Genre");
		VariableUtile.textFiltreGenre.setFont(new Font(VariableUtile.police, VariableUtile.py * 3));
		VariableUtile.textFiltreGenre.setWrappingWidth(VariableUtile.px * 9);
		VariableUtile.textFiltreGenre.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textFiltreGenre.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		VariableUtile.textFiltreGenre.setEffect(VariableUtile.effetTextPage);
		VariableUtile.textFiltreGenre.setVisible(false);
		VariableUtile.textesFiltres.add(VariableUtile.textFiltreGenre);
		VariableUtile.root.getChildren().add(VariableUtile.textFiltreGenre);

		VariableUtile.boutonFiltreSportif = new Bouton(VariableUtile.px * 25, VariableUtile.py * 17,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Sportif");
		VariableUtile.boutonFiltreSportif.finirGenerationBoutonFiltre(null, Genre.Sportif);

		VariableUtile.boutonFiltreElectro = new Bouton(VariableUtile.px * 25, VariableUtile.py * 37,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Electro");
		VariableUtile.boutonFiltreElectro.finirGenerationBoutonFiltre(null, Genre.Electro);

		VariableUtile.boutonFiltreLatino = new Bouton(VariableUtile.px * 25, VariableUtile.py * 57,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Latino");
		VariableUtile.boutonFiltreLatino.finirGenerationBoutonFiltre(null, Genre.Latino);

		VariableUtile.boutonFiltreFolklore = new Bouton(VariableUtile.px * 36, VariableUtile.py * 17,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Folklore");
		VariableUtile.boutonFiltreFolklore.finirGenerationBoutonFiltre("Folk", Genre.Folklore);

		VariableUtile.boutonFiltreRandB = new Bouton(VariableUtile.px * 36, VariableUtile.py * 37,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "RandB");
		VariableUtile.boutonFiltreRandB.finirGenerationBoutonFiltre("R&B", Genre.RandB);

		VariableUtile.boutonFiltreRock = new Bouton(VariableUtile.px * 36, VariableUtile.py * 57,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Rock");
		VariableUtile.boutonFiltreRock.finirGenerationBoutonFiltre(null, Genre.Rock);

		VariableUtile.boutonFiltreRetro = new Bouton(VariableUtile.px * 47, VariableUtile.py * 17,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Retro");
		VariableUtile.boutonFiltreRetro.finirGenerationBoutonFiltre("Retro", Genre.Retro);

		VariableUtile.boutonFiltrePop = new Bouton(VariableUtile.px * 47, VariableUtile.py * 37, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Pop");
		VariableUtile.boutonFiltrePop.finirGenerationBoutonFiltre("Pop", Genre.Pop);

		VariableUtile.boutonFiltreJazzy = new Bouton(VariableUtile.px * 47, VariableUtile.py * 57,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Jazzy");
		VariableUtile.boutonFiltreJazzy.finirGenerationBoutonFiltre("Jazzy", Genre.Jazzy);
		// Fin filtres

		// Illimit�
		VariableUtile.boutonIllimite = new Bouton(VariableUtile.px * 10, VariableUtile.py * 2, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Illimite");
		VariableUtile.boutonIllimite.text.setText("Illimit�");
		VariableUtile.boutonIllimite.setVisible(true);
		VariableUtile.boutonIllimite.toFront();
		VariableUtile.boutonIllimite.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.modeIllimite = true;
				VariableUtile.lancerVideoAuHasard();
			}
		});
		// M�lange
		VariableUtile.boutonMelange = new Bouton(VariableUtile.px * 18, VariableUtile.py * 2, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "M�lange");
		VariableUtile.boutonMelange.setVisible(true);
		VariableUtile.boutonMelange.toFront();
		VariableUtile.boutonMelange.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.lancerMelange();
			}
		});
		// S�lection
		VariableUtile.boutonSelection = new Bouton(VariableUtile.px * 26, VariableUtile.py * 2, VariableUtile.px * 6.8,
				VariableUtile.px * 7, 25, "Selection");
		VariableUtile.boutonSelection.text.setText("S�lection");
		VariableUtile.boutonSelection.setVisible(true);
		VariableUtile.boutonSelection.toFront();
		VariableUtile.boutonSelection.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.ouvrirEcranSelection();
			}
		});
		VariableUtile.boutonLancerSelection = new Bouton(VariableUtile.px * 85, VariableUtile.py * 80,
				VariableUtile.px * 8.8, VariableUtile.px * 8, 25, "Valider_filtre");
		VariableUtile.boutonLancerSelection.text.setText("C'est parti !");
		VariableUtile.boutonLancerSelection.setVisible(false);
		VariableUtile.boutonLancerSelection.toFront();
		VariableUtile.boutonLancerSelection.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.numeroProchaineDanseSelection = 0;
				if (sauterDansesVides()) {
					VariableUtile.lancerVideo(
							VariableUtile.dansesSelectionnees.get(VariableUtile.numeroProchaineDanseSelection));
				}

			}
		});
		VariableUtile.boutonReinitialiserSelection = new Bouton(VariableUtile.px * 77, VariableUtile.py * 2,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Rejouer");
		VariableUtile.boutonReinitialiserSelection.text.setText("Au hasard");
		VariableUtile.boutonReinitialiserSelection.setVisible(false);
		VariableUtile.boutonReinitialiserSelection.toFront();
		VariableUtile.boutonReinitialiserSelection.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.ouvrirEcranSelection();
			}
		});
		VariableUtile.boutonAnnulerSelection = new Bouton(VariableUtile.px * 85, VariableUtile.py * 2,
				VariableUtile.px * 6.8, VariableUtile.px * 7, 25, "Quitter");
		VariableUtile.boutonAnnulerSelection.text.setText("Annuler");
		VariableUtile.boutonAnnulerSelection.setVisible(false);
		VariableUtile.boutonAnnulerSelection.toFront();
		VariableUtile.boutonAnnulerSelection.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				VariableUtile.finSelection();
			}
		});

		VariableUtile.genererBoutonSuggestion(false);

		VariableUtile.primaryStage.getIcons().add(VariableUtile.boutonSuggestion.danse.imageDanse2);

		VariableUtile.boutonsMenu.add(VariableUtile.boutonSuggestion);
		VariableUtile.boutonsMenu.add(boutonSuivant);
		VariableUtile.boutonsMenu.add(boutonPrecedant);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonFiltrer);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonIllimite);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonMelange);
		VariableUtile.boutonsMenu.add(VariableUtile.boutonSelection);
		VariableUtile.boutonsFiltresAnnexes.add(VariableUtile.boutonFiltreValider);
		VariableUtile.boutonsFiltresAnnexes.add(VariableUtile.boutonFiltreReinitialiser);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonLancerSelection);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonReinitialiserSelection);
		VariableUtile.boutonsSelection.add(VariableUtile.boutonAnnulerSelection);

		for (Bouton bouton : VariableUtile.boutonsFiltres) {
			bouton.validerBouton();
		}
		VariableUtile.boutonsAction.add(VariableUtile.boutonQuitter);
		VariableUtile.boutonsAction.add(VariableUtile.boutonRejouer);
		VariableUtile.boutonsAction.add(VariableUtile.boutonPasser);

		VariableUtile.textPage = new Text(VariableUtile.px * 35, VariableUtile.hauteurFenetre * 0.18, "");
		VariableUtile.textes.add(VariableUtile.textPage);
		VariableUtile.textPage.setFont(new Font(VariableUtile.police, VariableUtile.py * 2));
		VariableUtile.textPage.setWrappingWidth(VariableUtile.px * 17);
		VariableUtile.textPage.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textPage.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		VariableUtile.effetTextPage = new DropShadow();
		VariableUtile.effetTextPage.setColor(VariableUtile.couleur1.invert());
		VariableUtile.effetTextPage.setRadius(50);
		VariableUtile.effetTextPage.setSpread(0.8);
		VariableUtile.textPage.setEffect(VariableUtile.effetTextPage);
		VariableUtile.root.getChildren().add(VariableUtile.textPage);

		VariableUtile.textCompteur = new Text(VariableUtile.px * 0, VariableUtile.py * 65, "");
		VariableUtile.textCompteur.setFont(new Font(VariableUtile.police, VariableUtile.py * 55));
		VariableUtile.textCompteur.setWrappingWidth(VariableUtile.px * 100);
		VariableUtile.textCompteur.setTextAlignment(TextAlignment.CENTER);
		VariableUtile.textCompteur.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		VariableUtile.textCompteur.setEffect(VariableUtile.effetTextPage);
		VariableUtile.textCompteur.setVisible(false);
		VariableUtile.root.getChildren().add(VariableUtile.textCompteur);

		// Barre de recherche
		VariableUtile.barreRecherche = new BarreRecherche(VariableUtile.px * 50, VariableUtile.py * 2,
				VariableUtile.px * 17, VariableUtile.py * 3.5, "LoupePetite");

		VariableUtile.scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (VariableUtile.videoEnCours && ke.equals(KeyCode.ESCAPE)) {
					VariableUtile.scene.setCursor(Cursor.DEFAULT);
					VariableUtile.videoEnCours = false;
				} else {
					// Barre de recherche
					if (ke.getCode().isLetterKey() || ke.getCode().isDigitKey() || ke.getCode().equals(KeyCode.SPACE)) {
						VariableUtile.barreRecherche.text
								.setText(VariableUtile.barreRecherche.text.getText() + ke.getText());
						validerFiltre();
					} else if (ke.getCode().equals(KeyCode.BACK_SPACE)
							&& VariableUtile.barreRecherche.text.getText().length() > 0) {
						VariableUtile.barreRecherche.text.setText(VariableUtile.barreRecherche.text.getText()
								.substring(0, VariableUtile.barreRecherche.text.getText().length() - 1));
						validerFiltre();
					}
				}
			}
		});

		VariableUtile.textCompteur.toFront();

		VariableUtile.primaryStage.setScene(VariableUtile.scene);
		VariableUtile.primaryStage.show();
	}

	private void completerDanses() {
		Scanner scanner = null;
		int ordre = 1;
		try {
			scanner = new Scanner(VariableUtile.fichierInfosDanses);
			while (scanner.hasNextLine()) {
				String[] infosStr = scanner.nextLine().split("\\|");
				String titreMusique = infosStr[0];

				if (VariableUtile.danses.containsKey(titreMusique)) {
					Danse danseACompleter = VariableUtile.danses.get(titreMusique);
					String listeIdArtistes[] = infosStr[1].split("-");
					danseACompleter.ordre = ordre;
					for (int i = 0; i < listeIdArtistes.length; i++) {
						String artiste = VariableUtile.artistes.get(Integer.valueOf(listeIdArtistes[i]));
						danseACompleter.artistes.add(artiste);
					}
					danseACompleter.intensite = Integer.valueOf(infosStr[2]);
					danseACompleter.technique = Integer.valueOf(infosStr[3]);
					String couleur1Str = infosStr[4];
					String couleur2Str = infosStr[5];
					String couleur1[] = couleur1Str.split("-");
					String couleur2[] = couleur2Str.split("-");
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
				ordre++;
			}
		} catch (FileNotFoundException e) {
			afficherErreur("Impossible d'ouvrir le fichier " + VariableUtile.fichierInfosDanses + " " + e);
		} catch (Exception e) {
			afficherErreur("Erreur pendant la r�cup�ration des infos danse ligne " + ordre + " " + e);
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}

	private void importerConfig() {
		File fichierConfig = new File("Config.txt");
		try {

			Scanner scanner;
			scanner = new Scanner(fichierConfig);
			VariableUtile.dossierDanses = new File(scanner.nextLine());
			VariableUtile.dossierImports = new File(scanner.nextLine());
			VariableUtile.fichierInfosDanses = new File(scanner.nextLine());

			scanner.close();
		} catch (FileNotFoundException e) {
			afficherErreur("Impossible d'ouvrir le fichier " + fichierConfig + " " + e);
		}
	}

	private void importerTitresVideosDanses() {
		try {
			Files.list(VariableUtile.dossierDanses.toPath()).limit(800).forEach(path -> {
				File fichierDanse = path.toFile();
				String nomCompletDanse = fichierDanse.getName();
				String titreMusique;
				Version version = null;
				if (nomCompletDanse.contains(" - ")) {
					titreMusique = nomCompletDanse.substring(0, nomCompletDanse.indexOf(" - "));
					String versionString = nomCompletDanse.substring(nomCompletDanse.indexOf(" - ") + 3,
							nomCompletDanse.lastIndexOf("."));
					if (!versionString.contains(" ")) {
						version = Version.valueOf(versionString);
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
		File fichierArtistes = new File(VariableUtile.dossierImports + "\\artistes.txt");
		try {
			Scanner scanner;
			scanner = new Scanner(fichierArtistes);
			while (scanner.hasNextLine()) {
				String[] artisteStr = scanner.nextLine().split("\\|");
				VariableUtile.artistes.put(Integer.valueOf(artisteStr[0]), artisteStr[1]);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			afficherErreur("Impossible d'ouvrir le fichier " + fichierArtistes + " " + e);
		}
	}

	public static void genererBoutonsDanse(boolean nettoyer) {
		genererBoutonsDanse(nettoyer, false);
	}

	public static void genererBoutonsDanse(boolean nettoyer, boolean modeSelection) {
		// emplacement : mettre 7 par d�faut
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

		for (int i = 0; i < 6; i++) {
			int numeroDanse = modeSelection ? i : i + 6 * VariableUtile.page;
			if (dansesAafficher.size() > numeroDanse) {

				double largeurPositionBouton = i > 2 ? VariableUtile.px * (i - 3) * 20 + VariableUtile.px * 15
						: VariableUtile.px * i * 20 + VariableUtile.px * 15;
				double hauteurPositionBouton = i > 2 ? VariableUtile.py * 60 : VariableUtile.py * 22;
				Bouton boutonDanse = new Bouton(largeurPositionBouton, hauteurPositionBouton, VariableUtile.px * 17,
						VariableUtile.py * 33, 20, dansesAafficher.get(numeroDanse), modeSelection ? true : false, i);

				VariableUtile.boutonsDanse.add(boutonDanse);

			}
		}
	}

	public static void decompte(String nombre) {
		VariableUtile.formerNombreCompteur(nombre);
		VariableUtile.textCompteur.setVisible(true);
	}

	public static void finirDecompte() {
		VariableUtile.textCompteur.setVisible(false);
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
					&& (aAuMoinsUnGenreActive(danse))) {
				VariableUtile.dansesFiltrees.add(danse);
				danse.ordre = VariableUtile.dansesFiltrees.size();
			}
		}
		// Barre de recherche
		if (VariableUtile.barreRecherche.text.getText().length() > 0) {
			ArrayList<Danse> dansesRecherche = new ArrayList<>();
			for (Danse danse : VariableUtile.dansesFiltrees) {
				if (danse.titreMusique.toUpperCase().contains(VariableUtile.barreRecherche.text.getText().toUpperCase())
						|| danse.artistes.toString().toUpperCase()
								.contains(VariableUtile.barreRecherche.text.getText().toUpperCase())) {
					dansesRecherche.add(danse);
				}
			}
			VariableUtile.dansesFiltrees.retainAll(dansesRecherche);
		}
		if (!VariableUtile.modeFiltre) {
			if (!VariableUtile.modeSelection) {
				VariableUtile.genererBoutonSuggestion(true);
				VariableUtile.genererTextPage();
				genererBoutonsDanse(true);

				VariableUtile.cacherMenuFiltres();
				VariableUtile.afficherMenuPrincipal();
			} else {
				VariableUtile.cacherMenuFiltres();
				VariableUtile.reafficherEcranSelection();
			}
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
		if (tousFiltresGenreActifs) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean sauterDansesVides() {
		// Attention VariableUtile.numeroProchaineDanseSelection est modifi�
		// dans la m�thode
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

	public static void actualiserCouleurPrincipal() {
		VariableUtile.shadePaint = new RadialGradient(0.5, 0.5, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(1, VariableUtile.couleur1), new Stop(0, VariableUtile.couleur2));

		VariableUtile.scene.setFill(VariableUtile.shadePaint);
	}

	public Image importerImage(String imageURI) {
		URL imageURL = getClass().getResource(imageURI);
		try {
			return new Image(imageURL.toExternalForm());
		} catch (Exception e) {
			afficherErreur("Image non trouv�e : " + imageURI);
		}
		return null;
	}

	public static void afficherErreur(String message) {
		Alert a = new Alert(AlertType.ERROR);
		a.setContentText(message);
		a.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}