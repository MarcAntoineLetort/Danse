package application;

import java.awt.MouseInfo;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class Bouton extends Parent {
	Rectangle rectangle;
	Rectangle cadre;
	ImageView imageViewBouton;
	ImageView imageViewIntensite;
	Circle fondIntensite;
	ImageView imageViewTechnique;
	Circle fondTechnique;
	Text text;
	Text superTitre;
	Danse danse;
	DropShadow effetText;
	boolean etatValide;
	Genre genre;
	int nbDanseurs;
	Tri typeTri;
	Bouton changerDanseBouton;
	int emplacement;

	public Bouton(double positionLargeur, double positionHauteur, double largeur, double hauteur, double taillePolice,
			Danse danse, boolean modeSelection, int emplacement) {
		// emplacement = numéro d'ordre dans le tableau, 6 = bouton suggestion
		super();
		this.danse = danse;
		this.rectangle = new Rectangle(largeur, hauteur);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setFill(danse.couleur2.darker().darker());
		rectangle.setArcWidth(largeur * 0.25);
		rectangle.setArcHeight(hauteur * 0.25);
		rectangle.setVisible(true);
		this.cadre = new Rectangle(largeur, hauteur);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(largeur * 0.25);
		cadre.setArcHeight(hauteur * 0.25);
		cadre.setOpacity(0);
		this.imageViewBouton = new ImageView(danse.imageDanse1);
		imageViewBouton.setX(positionLargeur + 0.07 * largeur);
		imageViewBouton.setY(positionHauteur + 0.07 * largeur);
		imageViewBouton.setFitWidth(largeur * 0.86);
		imageViewBouton.setFitHeight(largeur * 0.86);
		this.emplacement = emplacement;
		// Témoin intensité
		String intensite = null;
		switch (danse.intensite) {
		case 1:
			intensite = "Intensite_faible_petite";
			break;
		case 2:
			intensite = "Intensite_moderee_petite";
			break;
		case 3:
			intensite = "Intensite_intense_petite";
			break;
		}
		double rayonFondIntensiteTechnique = largeur * 0.04;
		double positionXFondIntensite = positionLargeur + 0.101 * largeur;
		double positionYFondIntensite = positionHauteur + 0.035 * largeur;
		if (intensite != null) {
			fondIntensite = new Circle(positionXFondIntensite, positionYFondIntensite, rayonFondIntensiteTechnique,
					danse.couleur1);
			this.imageViewIntensite = new ImageView(
					VariableUtile.main.importerImage("imageBouton/" + intensite + ".png"));
			imageViewIntensite.setX(positionXFondIntensite - (largeur * 0.06) / 2);
			imageViewIntensite.setY(positionYFondIntensite - (largeur * 0.06) / 2);
			imageViewIntensite.setFitWidth(largeur * 0.06);
			imageViewIntensite.setFitHeight(largeur * 0.06);
		}
		// Témoin technique
		String technique = null;
		switch (danse.technique) {
		case 1:
			technique = "Technique_faible_petite";
			break;
		case 2:
			technique = "Technique_moderee_petite";
			break;
		case 3:
			technique = "Technique_elevee_petite";
			break;
		}
		if (technique != null) {
			double positionXFondTechnique = positionLargeur + 0.205 * largeur;
			double positionYFondTechnique = positionYFondIntensite;

			fondTechnique = new Circle(positionXFondTechnique, positionYFondTechnique, rayonFondIntensiteTechnique,
					danse.couleur1);
			this.imageViewTechnique = new ImageView(
					VariableUtile.main.importerImage("imageBouton/" + technique + ".png"));
			imageViewTechnique.setX(positionXFondTechnique - (largeur * 0.06) / 2);
			imageViewTechnique.setY(positionYFondTechnique - (largeur * 0.06) / 2);
			imageViewTechnique.setFitWidth(largeur * 0.06);
			imageViewTechnique.setFitHeight(largeur * 0.06);
		}
		if (danse.equals(VariableUtile.danseNeant)) {
			this.text = new Text(positionLargeur, positionHauteur + 0.98 * hauteur, "Néant");
		} else {
			this.text = new Text(positionLargeur, positionHauteur + 0.98 * hauteur,
					danse.titreMusique + (danse.artistes.size() > 0 ? " - " + danse.artistes.get(0) : ""));
		}
		text.setFont(new Font(VariableUtile.police, taillePolice));
		text.setWrappingWidth(largeur);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(danse.couleur1.brighter().brighter().brighter().brighter());
		effetText = new DropShadow();
		effetText.setColor(danse.couleur1.invert());
		effetText.setRadius(50);
		effetText.setSpread(0.8);
		text.setEffect(effetText);

		this.getChildren().add(rectangle);
		this.getChildren().add(cadre);
		this.getChildren().add(imageViewBouton);
		if (intensite != null) {
			this.getChildren().add(fondIntensite);
			this.getChildren().add(imageViewIntensite);
		}
		if (technique != null) {
			this.getChildren().add(fondTechnique);
			this.getChildren().add(imageViewTechnique);
		}
		this.getChildren().add(text);
		cadre.toFront();

		this.cadre.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				imageViewBouton.setImage(danse.imageDanse2);
				VariableUtile.scene.setCursor(Cursor.HAND);

				// Si pas déjà actif
				if (VariableUtile.danseSelectionnee != danse) {

					VariableUtile.couleur1 = danse.couleur1;
					VariableUtile.couleur2 = danse.couleur2;
					VariableUtile.danseSelectionnee = danse;

					for (Bouton boutonAction : VariableUtile.boutonsAction) {
						boutonAction.actualiserCouleur();
					}
					for (Bouton boutonMenu : VariableUtile.boutonsMenu) {
						if (boutonMenu.superTitre == null || !boutonMenu.superTitre.getText().equals("Suggestion")) {
							boutonMenu.actualiserCouleur();
						}
					}
					for (Bouton boutonFiltres : VariableUtile.boutonsFiltres) {
						boutonFiltres.actualiserCouleur();
					}
					for (Bouton boutonFiltreAnnexe : VariableUtile.boutonsFiltresAnnexes) {
						boutonFiltreAnnexe.actualiserCouleur();
					}
					for (Text textsFiltre : VariableUtile.textesFiltres) {
						textsFiltre.setFill(VariableUtile.couleur1);
					}
					for (Bouton boutonTri : VariableUtile.boutonsTri) {
						boutonTri.actualiserCouleur();
					}
					for (Bouton boutonTriAnnexe : VariableUtile.boutonsTriAnnexes) {
						boutonTriAnnexe.actualiserCouleur();
					}
					for (Bouton boutonSelection : VariableUtile.boutonsSelection) {
						boutonSelection.actualiserCouleur();
					}
					MainDanse.actualiserCouleurPrincipal();
					VariableUtile.barreRecherche.actualiserCouleur();
					VariableUtile.genererTextPage();

					// Son
					// if (VariableUtile.playerVideo != null) {
					// KeyValue keyValueVolume = new
					// KeyValue(VariableUtile.playerVideo.volumeProperty(), 0);
					// KeyFrame keyFrameMusique = new
					// KeyFrame(Duration.seconds(0.5), keyValueVolume);
					// Timeline timelineMusique = new Timeline();
					// timelineMusique.getKeyFrames().addAll(keyFrameMusique);
					//
					// timelineMusique.play();
					// }
					// new java.util.Timer().schedule(new java.util.TimerTask()
					// {
					// @Override
					// public void run() {
					if (VariableUtile.playerVideo != null) {
						VariableUtile.playerVideo.stop();
						VariableUtile.playerVideo.dispose();
					}
					if (!danse.equals(VariableUtile.danseNeant)) {
						VariableUtile.lancerSon(danse);
						VariableUtile.playerVideo.setVolume(0);
						VariableUtile.playerVideo.setStartTime(Duration.seconds(20));

						KeyValue keyValueVolume = new KeyValue(VariableUtile.playerVideo.volumeProperty(), 0.3);
						KeyFrame keyFrameMusique = new KeyFrame(Duration.seconds(3), keyValueVolume);
						Timeline timelineMusique = new Timeline();
						timelineMusique.getKeyFrames().addAll(keyFrameMusique);

						timelineMusique.play();
						Platform.runLater(() -> {
							VariableUtile.playerVideo.play();
						});
					}
					// }
					// }, 500);
				}
			}
		});
		this.cadre.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				imageViewBouton.setImage(danse.imageDanse1);
				if (!VariableUtile.videoEnCours && VariableUtile.imageViewDanseEchangeSelection == null) {
					VariableUtile.scene.setCursor(Cursor.DEFAULT);
				}
			}
		});

		this.cadre.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (VariableUtile.modeSelection) {
					// Déplacement danse pour échange
					VariableUtile.imageViewDanseEchangeSelection = new ImageView(danse.imageDanse2);
					VariableUtile.imageViewDanseEchangeSelection.setFitWidth(largeur * 0.86);
					VariableUtile.imageViewDanseEchangeSelection.setFitHeight(largeur * 0.86);
					VariableUtile.imageViewDanseEchangeSelection.setOpacity(0.4);
					VariableUtile.imageViewDanseEchangeSelection.setVisible(true);
					VariableUtile.imageViewDanseEchangeSelection.setX(MouseInfo.getPointerInfo().getLocation().getX()
							- VariableUtile.imageViewDanseEchangeSelection.getFitWidth() / 2);
					VariableUtile.imageViewDanseEchangeSelection.setY(MouseInfo.getPointerInfo().getLocation().getY()
							- VariableUtile.imageViewDanseEchangeSelection.getFitHeight() / 2);
					Platform.runLater(() -> {
						VariableUtile.root.getChildren().add(VariableUtile.imageViewDanseEchangeSelection);
					});

					VariableUtile.scene.setCursor(Cursor.CLOSED_HAND);
					me.setDragDetect(true);

					VariableUtile.boutonGlissePourEchangeSelection = Bouton.this;

					VariableUtile.imageViewDanseEchangeSelection.setMouseTransparent(true);
				}

			}
		});

		this.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (!VariableUtile.modeSelection) {
					// Démarrage de la vidéo
					VariableUtile.lancerVideo(danse);
				} else if (VariableUtile.imageViewDanseEchangeSelection != null) {
					finirGlissementSelection();
				}
			}
		});

		this.cadre.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
			public void handle(MouseDragEvent event) {
				finirGlissementSelection();

				// échange de danse
				Danse danceCible = danse;

				// Mettre la danse sélectionnée dans le bouton cible
				VariableUtile.dansesSelectionnees.set(emplacement, VariableUtile.danseSelectionnee);
				// Mettre la danse cible dans le bouton source
				VariableUtile.dansesSelectionnees.set(VariableUtile.boutonGlissePourEchangeSelection.emplacement,
						danceCible);

				VariableUtile.boutonGlissePourEchangeSelection = null;
				MainDanse.genererBoutonsDanse(true, true);
			}
		});

		this.cadre.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (VariableUtile.modeSelection && VariableUtile.imageViewDanseEchangeSelection != null) {
					// image glisser-déposer
					VariableUtile.imageViewDanseEchangeSelection.setX(MouseInfo.getPointerInfo().getLocation().getX()
							- VariableUtile.imageViewDanseEchangeSelection.getFitWidth() / 2);
					VariableUtile.imageViewDanseEchangeSelection.setY(MouseInfo.getPointerInfo().getLocation().getY()
							- VariableUtile.imageViewDanseEchangeSelection.getFitHeight() / 2);

					event.setDragDetect(false);
				}
			}
		});

		this.cadre.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				VariableUtile.scene.startFullDrag();
			}
		});

		Platform.runLater(() -> {
			VariableUtile.root.getChildren().add(this);
		});

		// Bouton de changement aléatoire de danse (mode sélection)
		if (modeSelection) {
			changerDanseBouton = new Bouton(positionLargeur + (largeur * 3.9 / 5),
					positionHauteur + (hauteur * 3.7 / 5), largeur / 5, hauteur / 5, 0, "Rejouer");
			changerDanseBouton.text.setText("");
			changerDanseBouton.setVisible(true);
			changerDanseBouton.toFront();
			changerDanseBouton.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent me) {
					int aleaVideo;
					// Compter les danses non vides déjà sélectionnées
					int nombreDansesSelectionnables = 0;
					for (Danse danseSelectionnee : VariableUtile.dansesSelectionnees) {
						if (!danseSelectionnee.equals(VariableUtile.danseNeant)
								&& VariableUtile.dansesFiltrees.contains(danseSelectionnee)) {
							nombreDansesSelectionnables++;
						}
					}

					if (nombreDansesSelectionnables < VariableUtile.dansesFiltrees.size()) {
						do {
							aleaVideo = (int) Math.ceil(Math.random() * (VariableUtile.dansesFiltrees.size())) - 1;
						} while (VariableUtile.dansesSelectionnees
								.contains(VariableUtile.dansesFiltrees.get(aleaVideo)));
						VariableUtile.dansesSelectionnees.set(emplacement, VariableUtile.dansesFiltrees.get(aleaVideo));
					} else {
						VariableUtile.dansesSelectionnees.set(emplacement, VariableUtile.danseNeant);
					}

					MainDanse.genererBoutonsDanse(true, true);

				}
			});
			// VariableUtile.root.getChildren().add(changerDanseBouton);
		}
	}

	public static void finirGlissementSelection() {
		VariableUtile.root.getChildren().remove(VariableUtile.imageViewDanseEchangeSelection);
		VariableUtile.imageViewDanseEchangeSelection.setVisible(false);
		VariableUtile.imageViewDanseEchangeSelection = null;

		VariableUtile.scene.setCursor(Cursor.DEFAULT);

	}

	public Bouton(double positionLargeur, double positionHauteur, double largeur, double hauteur, double taillePolice,
			String texte) {
		super();
		this.rectangle = new Rectangle(largeur, hauteur);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setFill(VariableUtile.couleur2);
		rectangle.setArcWidth(largeur * 0.25);
		rectangle.setArcHeight(hauteur * 0.25);
		rectangle.setVisible(true);
		this.cadre = new Rectangle(largeur, hauteur);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(largeur * 0.25);
		cadre.setArcHeight(hauteur * 0.25);
		cadre.setOpacity(0);

		Image imageBouton1 = VariableUtile.main.importerImage("imageBouton/" + texte + "1.png");
		Image imageBouton2 = VariableUtile.main.importerImage("imageBouton/" + texte + "2.png");

		this.imageViewBouton = new ImageView(imageBouton1);
		imageViewBouton.setX(positionLargeur + 0.07 * largeur);
		imageViewBouton.setY(positionHauteur + 0.07 * largeur);
		imageViewBouton.setFitWidth(largeur * 0.86);
		imageViewBouton.setFitHeight(largeur * 0.86);
		this.text = new Text(positionLargeur, positionHauteur + 0.85 * hauteur, texte);
		text.setFont(new Font(VariableUtile.police, taillePolice));
		text.setWrappingWidth(largeur);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(VariableUtile.couleur1.brighter().brighter().brighter().brighter());
		DropShadow effetText = new DropShadow();
		effetText.setColor(VariableUtile.couleur1.invert());
		effetText.setRadius(50);
		effetText.setSpread(0.8);
		text.setEffect(effetText);

		this.getChildren().add(rectangle);
		this.getChildren().add(cadre);
		this.getChildren().add(imageViewBouton);
		this.getChildren().add(text);
		cadre.toFront();

		this.cadre.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				imageViewBouton.setImage(imageBouton2);
				VariableUtile.scene.setCursor(Cursor.HAND);
			}
		});
		this.cadre.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				imageViewBouton.setImage(imageBouton1);
				if (!VariableUtile.videoEnCours) {
					VariableUtile.scene.setCursor(Cursor.DEFAULT);
				}
			}
		});

		Platform.runLater(() -> {
			VariableUtile.root.getChildren().add(this);
		});
	}

	public void actualiserCouleur() {

		if (etatValide) {
			rectangle.setFill(VariableUtile.couleur1);
			text.setFill(VariableUtile.couleur2);
			((DropShadow) this.getEffect()).setColor(VariableUtile.couleur2);
		} else {
			rectangle.setFill(VariableUtile.couleur2);
			text.setFill(VariableUtile.couleur1);
		}
	}

	public void genererSuperTitre(String text) {
		superTitre = new Text(rectangle.getX(), rectangle.getY() + 0.015 * rectangle.getHeight(), text);
		superTitre.setFont(new Font(VariableUtile.police, VariableUtile.px * 1.5));
		superTitre.setWrappingWidth(rectangle.getWidth());
		superTitre.setTextAlignment(TextAlignment.CENTER);
		superTitre.setFill(danse.couleur1.brighter().brighter().brighter().brighter());
		superTitre.setEffect(effetText);
		Platform.runLater(() -> {
			this.getChildren().add(superTitre);
		});
	}

	public void validerBouton() {
		etatValide = true;
		text.setFill(VariableUtile.couleur2);
		rectangle.setFill(VariableUtile.couleur1);
		DropShadow effetText = new DropShadow();
		effetText.setColor(VariableUtile.couleur2);
		effetText.setRadius(50);
		effetText.setSpread(0.5);
		this.setEffect(effetText);
	}

	public void invaliderBouton() {
		etatValide = false;
		text.setFill(VariableUtile.couleur1);
		rectangle.setFill(VariableUtile.couleur2);
		this.setEffect(null);
	}

	public void finirGenerationBoutonFiltre(String titreDifferent) {
		finirGenerationBoutonFiltre(titreDifferent, null, 0);
	}

	public void finirGenerationBoutonFiltre(String titreDifferent, Genre genre) {
		finirGenerationBoutonFiltre(titreDifferent, genre, 0);
	}

	public void finirGenerationBoutonFiltre(String titreDifferent, int nbDanseurs) {
		finirGenerationBoutonFiltre(titreDifferent, null, nbDanseurs);
	}

	public void finirGenerationBoutonFiltre(String titreDifferent, Genre genre, int nbDanseurs) {
		if (titreDifferent != null) {
			text.setText(titreDifferent);
		}
		if (genre != null) {
			this.genre = genre;
		}
		if (nbDanseurs != 0) {
			this.nbDanseurs = nbDanseurs;
		}
		this.setVisible(false);
		this.toFront();
		cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				boolean comportementClassique = true;
				if (genre != null) {
					boolean tousLesGenresActives = true;
					boolean tousLesAutresGenresDesactives = true;
					for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
						if (boutonFiltre.genre != null) {
							if (boutonFiltre.etatValide) {
								if (boutonFiltre != Bouton.this) {
									tousLesAutresGenresDesactives = false;
								}
							} else {
								tousLesGenresActives = false;
							}
						}
					}
					if (tousLesGenresActives) {
						comportementClassique = false;
						for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
							if (boutonFiltre.genre != null && !boutonFiltre.genre.equals(genre)) {
								boutonFiltre.invaliderBouton();
							}
						}
					} else if (tousLesAutresGenresDesactives) {
						comportementClassique = false;
						for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
							if (boutonFiltre.genre != null) {
								boutonFiltre.validerBouton();
							}
						}
					}
				} else if (nbDanseurs != 0) {
					boolean tousLesNbActives = true;
					boolean tousLesAutresNbDesactives = true;
					for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
						if (boutonFiltre.nbDanseurs != 0) {
							if (boutonFiltre.etatValide) {
								if (boutonFiltre != Bouton.this) {
									tousLesAutresNbDesactives = false;
								}
							} else {
								tousLesNbActives = false;
							}
						}
					}
					if (tousLesNbActives) {
						comportementClassique = false;
						for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
							if (boutonFiltre.nbDanseurs != 0 && boutonFiltre.nbDanseurs != nbDanseurs) {
								boutonFiltre.invaliderBouton();
							}
						}
					} else if (tousLesAutresNbDesactives) {
						comportementClassique = false;
						for (Bouton boutonFiltre : VariableUtile.boutonsFiltres) {
							if (boutonFiltre.nbDanseurs != 0) {
								boutonFiltre.validerBouton();
							}
						}
					}
				}
				if (comportementClassique) {
					if (etatValide) {
						invaliderBouton();
					} else {
						validerBouton();
					}
				}
			}
		});
		VariableUtile.boutonsFiltres.add(this);
	}

	public void finirGenerationBoutonTri(String titreDifferent, Tri typeTri) {
		if (titreDifferent != null) {
			text.setText(titreDifferent);
		}
		if (typeTri != null) {
			this.typeTri = typeTri;
		}
		this.setVisible(false);
		this.toFront();
		cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (typeTri != null) {
					for (Bouton boutonFiltre : VariableUtile.boutonsTri) {
						if (boutonFiltre.typeTri != null) {
							if (boutonFiltre != Bouton.this) {
								boutonFiltre.invaliderBouton();
							} else {
								boutonFiltre.validerBouton();
							}
						}
					}
				}
			}
		});
		VariableUtile.boutonsTri.add(this);
		validerBouton(); // Pour finir l'initilisation
	}
}
