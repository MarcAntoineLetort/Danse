package application;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Bouton de l'application : carte de danse (bordure néon à la couleur de la danse),
 * bouton de menu, bouton à bascule (filtres / tris).
 * Seul le rectangle « cadre » (invisible) reçoit la souris : le visuel peut donc
 * grossir au survol sans faire clignoter le survol.
 */
public class Bouton extends Parent {
	Rectangle rectangle;
	Rectangle cadre;
	ImageView imageViewBouton;
	ImageView imageViewIntensite;
	Image imageBouton1;
	Image imageBouton2;
	double largeurImage;
	double margePourSuggestion;
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

	private final Group visuel = new Group();
	private Node iconeVectorielle;
	private boolean bascule = false; // bouton à bascule (filtre / tri)
	private boolean survol = false;
	private boolean carteDanse = false;
	private boolean sansPlaque = false;
	private Color accent = Theme.CYAN;
	private double largeurBouton;
	private double centreX;
	private double centreY;
	private Timeline animationSurvol;
	private double strokeNormal;
	private double hautBande;
	// zone (boîte) dans laquelle l'icône PNG doit être centrée
	private double boiteCX;
	private double boiteCY;
	private double boiteTaille;
	private javafx.beans.value.ChangeListener<Color> suiviAmbiance; // référence forte (l'écouteur est faible)

	// ====================================================================================
	// Bouton danse et suggestion
	// ====================================================================================
	public Bouton(double positionLargeur, double positionHauteur, double largeur, double hauteur, double taillePolice,
			Danse danse, boolean modeSelection, int emplacement) {
		// emplacement = numéro d'ordre dans le tableau, 6 = bouton suggestion
		super();
		this.danse = danse;
		this.emplacement = emplacement;
		this.carteDanse = true;
		this.largeurBouton = largeur;
		boolean suggestion = emplacement == 6;
		accent = suggestion ? Theme.OR : Theme.accentDe(danse);
		double margePourTexte = 0.07;
		margePourSuggestion = suggestion ? 0.12 : 0;
		double hauteurTotale = hauteur + hauteur * (margePourTexte + margePourSuggestion);
		largeurImage = largeur * 0.86;
		double positionXimage = positionLargeur + 0.07 * largeur;
		double positionYimage = positionHauteur + 0.07 * largeur + (hauteur * margePourSuggestion);
		centreX = positionLargeur + largeur / 2;
		centreY = positionHauteur + hauteurTotale / 2;
		hautBande = positionYimage - positionHauteur;
		double arc = largeur * 0.17;

		this.rectangle = new Rectangle(largeur, hauteurTotale);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setArcWidth(arc);
		rectangle.setArcHeight(arc);
		rectangle.setFill(Theme.degradeCarte(accent));
		strokeNormal = Math.max(2.5, largeur * 0.013);
		rectangle.setStroke(accent);
		rectangle.setStrokeType(StrokeType.INSIDE);
		rectangle.setStrokeWidth(strokeNormal);
		rectangle.setEffect(Theme.lueur(accent, 0.55, largeur * 0.07));

		Rectangle reflet = new Rectangle(largeur - 6, hauteurTotale * 0.40);
		reflet.setX(positionLargeur + 3);
		reflet.setY(positionHauteur + 3);
		reflet.setArcWidth(arc);
		reflet.setArcHeight(arc);
		reflet.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.color(1, 1, 1, 0.12)),
				new Stop(1, Color.color(1, 1, 1, 0))));
		reflet.setMouseTransparent(true);

		this.cadre = new Rectangle(largeur, hauteurTotale);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(arc);
		cadre.setArcHeight(arc);
		cadre.setOpacity(0);

		this.imageViewBouton = new ImageView(danse.imageDanse1);
		imageViewBouton.setX(positionXimage);
		imageViewBouton.setY(positionYimage);
		imageViewBouton.setFitWidth(largeurImage);
		imageViewBouton.setFitHeight(largeurImage);
		imageViewBouton.setSmooth(true);
		Rectangle clip = new Rectangle(largeurImage, largeurImage);
		clip.setX(positionXimage);
		clip.setY(positionYimage);
		clip.setArcWidth(largeur * 0.2);
		clip.setArcHeight(largeur * 0.2);
		imageViewBouton.setClip(clip);
		Rectangle cadreImage = new Rectangle(largeurImage, largeurImage);
		cadreImage.setX(positionXimage);
		cadreImage.setY(positionYimage);
		cadreImage.setArcWidth(largeur * 0.2);
		cadreImage.setArcHeight(largeur * 0.2);
		cadreImage.setFill(null);
		cadreImage.setStroke(Color.color(0, 0, 0, 0.55));
		cadreImage.setStrokeWidth(2);
		cadreImage.setMouseTransparent(true);

		visuel.getChildren().addAll(rectangle, reflet, imageViewBouton, cadreImage);

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
		double rayonFond = largeur * 0.042;
		double yBadge = positionYimage - 0.07 * largeur + 0.062 * largeur;
		if (intensite != null) {
			double xBadge = positionLargeur + 0.105 * largeur;
			fondIntensite = new Circle(xBadge, yBadge, rayonFond, accent);
			fondIntensite.setStroke(Color.color(1, 1, 1, 0.65));
			fondIntensite.setStrokeWidth(1.2);
			this.imageViewIntensite = new ImageView(VariableUtile.main.importerImage("imageBouton/" + intensite + ".png"));
			imageViewIntensite.setX(xBadge - (largeur * 0.06) / 2);
			imageViewIntensite.setY(yBadge - (largeur * 0.06) / 2);
			imageViewIntensite.setFitWidth(largeur * 0.06);
			imageViewIntensite.setFitHeight(largeur * 0.06);
			visuel.getChildren().addAll(fondIntensite, imageViewIntensite);
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
			double xBadge = positionLargeur + 0.205 * largeur;
			fondTechnique = new Circle(xBadge, yBadge, rayonFond, accent);
			fondTechnique.setStroke(Color.color(1, 1, 1, 0.65));
			fondTechnique.setStrokeWidth(1.2);
			this.imageViewTechnique = new ImageView(VariableUtile.main.importerImage("imageBouton/" + technique + ".png"));
			imageViewTechnique.setX(xBadge - (largeur * 0.06) / 2);
			imageViewTechnique.setY(yBadge - (largeur * 0.06) / 2);
			imageViewTechnique.setFitWidth(largeur * 0.06);
			imageViewTechnique.setFitHeight(largeur * 0.06);
			visuel.getChildren().addAll(fondTechnique, imageViewTechnique);
		}

		// Titre : centré dans la bande sous l'image
		double basImage = positionYimage + largeurImage;
		double finCarte = positionHauteur + hauteurTotale;
		String titre = danse.equals(VariableUtile.danseNeant) ? "Néant"
				: danse.titreMusique + (danse.artistes.size() > 0 ? " - " + danse.artistes.get(0) : "");
		this.text = new Text(positionLargeur + largeur * 0.03, (basImage + finCarte) / 2, titre);
		text.setTextOrigin(VPos.CENTER);
		text.setFont(Theme.police(taillePolice * Theme.echelleTexte()));
		adapterTaillePolice(taillePolice * Theme.echelleTexte(), largeur * 0.94);
		text.setWrappingWidth(largeur * 0.94);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(Color.WHITE);
		text.setMouseTransparent(true);
		visuel.getChildren().add(text);

		this.getChildren().addAll(visuel, cadre);
		animerEntree(suggestion ? 0 : emplacement, modeSelection);

		this.cadre.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (danse.imageDanse2 != null) {
					imageViewBouton.setImage(danse.imageDanse2);
				}
				VariableUtile.scene.setCursor(Cursor.HAND);
				definirSurvol(true);

				// Si pas déjà actif
				if (VariableUtile.danseSelectionnee != danse) {
					VariableUtile.couleur1 = danse.couleur1;
					VariableUtile.couleur2 = danse.couleur2;
					VariableUtile.danseSelectionnee = danse;
					Theme.changerAmbiance(Theme.accentDe(danse));
					VariableUtile.genererTextPage();
					VariableUtile.apercuMusique(danse);
				}
			}
		});
		this.cadre.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (danse.imageDanse1 != null) {
					imageViewBouton.setImage(danse.imageDanse1);
				}
				definirSurvol(false);
				if (!VariableUtile.videoEnCours && VariableUtile.imageViewDanseEchangeSelection == null) {
					VariableUtile.scene.setCursor(Cursor.DEFAULT);
				}
			}
		});

		this.cadre.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				animerEchelle(0.97, 70);
				if (VariableUtile.modeSelection) {
					// Déplacement de la danse pour échange : l'image suit le pointeur
					ImageView fantome = new ImageView(danse.imageDanse2 != null ? danse.imageDanse2 : danse.imageDanse1);
					fantome.setFitWidth(largeur * 0.86);
					fantome.setFitHeight(largeur * 0.86);
					fantome.setOpacity(0.5);
					fantome.setX(me.getSceneX() - fantome.getFitWidth() / 2);
					fantome.setY(me.getSceneY() - fantome.getFitHeight() / 2);
					fantome.setMouseTransparent(true);
					VariableUtile.imageViewDanseEchangeSelection = fantome;
					VariableUtile.root.getChildren().add(fantome);

					VariableUtile.scene.setCursor(Cursor.CLOSED_HAND);
					me.setDragDetect(true);
					VariableUtile.boutonGlissePourEchangeSelection = Bouton.this;
				}
			}
		});

		this.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				animerEchelle(survol ? 1.04 : 1.0, 90);
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
				Bouton source = VariableUtile.boutonGlissePourEchangeSelection;
				finirGlissementSelection();
				if (source == null || source == Bouton.this) {
					return;
				}
				// échange de danse entre le bouton source et ce bouton
				Danse danceCible = danse;
				VariableUtile.dansesSelectionnees.set(emplacement, source.danse);
				VariableUtile.dansesSelectionnees.set(source.emplacement, danceCible);
				VariableUtile.boutonGlissePourEchangeSelection = null;
				MainDanse.genererBoutonsDanse(true, true, true);
			}
		});

		this.cadre.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				ImageView fantome = VariableUtile.imageViewDanseEchangeSelection;
				if (VariableUtile.modeSelection && fantome != null) {
					fantome.setX(event.getSceneX() - fantome.getFitWidth() / 2);
					fantome.setY(event.getSceneY() - fantome.getFitHeight() / 2);
					event.setDragDetect(false);
				}
			}
		});

		this.cadre.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				VariableUtile.scene.startFullDrag();
			}
		});

		Theme.fx(() -> VariableUtile.root.getChildren().add(this));

		// Bouton de changement aléatoire de danse (mode sélection)
		if (modeSelection) {
			changerDanseBouton = new Bouton(positionLargeur + largeur * 0.735, positionHauteur + hauteurTotale * 0.625,
					largeur / 5, hauteur / 5, 0, "Rejouer");
			changerDanseBouton.setLabel("");
			changerDanseBouton.setVisible(true);
			changerDanseBouton.toFront();
			changerDanseBouton.cadre.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent me) {
					VariableUtile.remplacerDanseSelection(emplacement);
				}
			});
		}
	}

	/** Apparition en fondu + glissement, légèrement décalée d'une carte à l'autre. */
	private void animerEntree(int rang, boolean modeSelection) {
		if (modeSelection) {
			return;
		}
		visuel.setOpacity(0);
		visuel.setTranslateY(16);
		Timeline t = new Timeline(
				new KeyFrame(Duration.millis(260), new KeyValue(visuel.opacityProperty(), 1, Interpolator.EASE_OUT),
						new KeyValue(visuel.translateYProperty(), 0, Interpolator.EASE_OUT)));
		t.setDelay(Duration.millis(35 * rang));
		Theme.fx(t::play);
	}

	/**
	 * Affiche une icône PNG exactement au centre de sa zone. Sans cela, une image qui
	 * n'est pas carrée restait collée au coin haut-gauche de la zone (donc décalée).
	 */
	private void poserImage(Image img) {
		imageViewBouton.setImage(img);
		if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
			return;
		}
		double echelle = Math.min(boiteTaille / img.getWidth(), boiteTaille / img.getHeight());
		double w = img.getWidth() * echelle;
		double h = img.getHeight() * echelle;
		imageViewBouton.setX(boiteCX - w / 2);
		imageViewBouton.setY(boiteCY - h / 2);
	}

	/** Remplace l'image (appelé quand le chargement asynchrone est terminé). */
	void rafraichirImage() {
		if (danse != null && imageViewBouton != null && danse.imageDanse1 != null) {
			imageViewBouton.setImage(survol && danse.imageDanse2 != null ? danse.imageDanse2 : danse.imageDanse1);
		}
	}

	private void adapterTaillePolice(double taillePolice, double largeur) {
		// On mesure sans retour à la ligne, sinon la largeur mesurée vaut toujours la largeur de coupure
		double coupure = text.getWrappingWidth();
		text.setWrappingWidth(0);
		double taillePoliceDynamique = taillePolice;
		while (text.getLayoutBounds().getWidth() > largeur && taillePoliceDynamique > 6) {
			taillePoliceDynamique -= 0.5;
			text.setFont(Theme.police(taillePoliceDynamique));
		}
		text.setWrappingWidth(coupure);
	}

	/** Change le libellé d'un bouton de menu en réajustant la taille de la police. */
	public void setLabel(String libelle) {
		text.setText(libelle);
		text.setFont(Theme.police(taillePoliceBase));
		adapterTaillePolice(taillePoliceBase, largeurBouton * 0.92);
	}

	private double taillePoliceBase = 20;

	public static void finirGlissementSelection() {
		ImageView fantome = VariableUtile.imageViewDanseEchangeSelection;
		if (fantome != null) {
			VariableUtile.root.getChildren().remove(fantome);
			fantome.setVisible(false);
		}
		VariableUtile.imageViewDanseEchangeSelection = null;
		VariableUtile.scene.setCursor(Cursor.DEFAULT);
	}

	// ====================================================================================
	// Tous les boutons non danse
	// ====================================================================================
	public Bouton(double positionLargeur, double positionHauteur, double largeur, double hauteur, double taillePolice,
			String texte) {
		super();
		boolean compact = taillePolice == 0;
		sansPlaque = texte.startsWith("Chevron");
		this.largeurBouton = largeur;
		this.taillePoliceBase = Math.max(6, taillePolice * Theme.echelleTexte());
		centreX = positionLargeur + largeur / 2;
		centreY = positionHauteur + hauteur / 2;
		double arc = Math.min(largeur, hauteur) * 0.28;

		this.rectangle = new Rectangle(largeur, hauteur);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setArcWidth(arc);
		rectangle.setArcHeight(arc);
		rectangle.setStrokeType(StrokeType.INSIDE);
		strokeNormal = Math.max(2, Math.min(largeur, hauteur) * 0.022);
		rectangle.setStrokeWidth(strokeNormal);
		this.cadre = new Rectangle(largeur, hauteur);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(arc);
		cadre.setArcHeight(arc);
		cadre.setOpacity(0);

		// Icône : vectorielle si elle existe, sinon image PNG (comme avant)
		boolean vectoriel = Icones.creer(texte, 10) != null;
		// les icônes PNG (filtres, etc.) remplissent mieux leur bouton que les icônes vectorielles du menu
		double tailleIcone = compact ? Math.min(largeur, hauteur) * 0.78
				: (vectoriel ? Math.min(largeur * 0.60, hauteur * 0.56) : Math.min(largeur * 0.78, hauteur * 0.64));
		double xIcone = centreX - tailleIcone / 2;
		// centre vertical de la zone libre au-dessus du libellé
		double centreIconeY = compact ? centreY : positionHauteur + hauteur * (vectoriel ? 0.09 : 0.00) + tailleIcone / 2;
		if (!vectoriel && !compact) {
			centreIconeY = positionHauteur + hauteur * 0.405;
		}
		double yIcone = centreIconeY - tailleIcone / 2;
		if (sansPlaque) {
			tailleIcone = Math.min(largeur, hauteur) * 0.62;
			xIcone = centreX - tailleIcone / 2;
			yIcone = centreY - tailleIcone / 2;
			centreIconeY = centreY;
		}
		boiteCX = centreX;
		boiteCY = centreIconeY;
		boiteTaille = tailleIcone;
		iconeVectorielle = Icones.creer(texte, tailleIcone);
		this.imageViewBouton = new ImageView();
		imageViewBouton.setX(xIcone);
		imageViewBouton.setY(yIcone);
		imageViewBouton.setFitWidth(tailleIcone);
		imageViewBouton.setFitHeight(tailleIcone);
		imageViewBouton.setPreserveRatio(true);
		imageViewBouton.setSmooth(true);
		if (iconeVectorielle != null) {
			iconeVectorielle.setLayoutX(xIcone);
			iconeVectorielle.setLayoutY(yIcone);
			iconeVectorielle.setMouseTransparent(true);
		} else {
			Thread threadChargement = new Thread(() -> {
				Image i1 = VariableUtile.main.importerImage("imageBouton/" + texte + "1.png");
				Image i2 = VariableUtile.main.importerImage("imageBouton/" + texte + "2.png");
				Theme.fx(() -> {
					imageBouton1 = i1;
					imageBouton2 = i2;
					poserImage(survol && imageBouton2 != null ? imageBouton2 : imageBouton1);
				});
			}, "chargement-icone-" + texte);
			threadChargement.setDaemon(true);
			threadChargement.start();
		}

		this.text = new Text(positionLargeur, positionHauteur + hauteur * (vectoriel ? 0.865 : 0.915), texte);
		text.setTextOrigin(VPos.BASELINE);
		text.setFont(Theme.police(taillePoliceBase));
		adapterTaillePolice(taillePoliceBase, largeur * 0.92);
		text.setWrappingWidth(largeur);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(Color.WHITE);
		text.setMouseTransparent(true);

		visuel.getChildren().add(rectangle);
		if (iconeVectorielle != null) {
			visuel.getChildren().add(iconeVectorielle);
		} else {
			visuel.getChildren().add(imageViewBouton);
		}
		if (!compact && !sansPlaque) {
			visuel.getChildren().add(text);
		}
		this.getChildren().addAll(visuel, cadre);
		appliquerStyle();
		// Le bouton suit la couleur de la dernière danse survolée
		suiviAmbiance = (o, a, n) -> appliquerStyle();
		Theme.ambianceProperty().addListener(new javafx.beans.value.WeakChangeListener<>(suiviAmbiance));

		this.cadre.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				definirSurvol(true);
				VariableUtile.scene.setCursor(Cursor.HAND);
			}
		});
		this.cadre.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				definirSurvol(false);
				if (!VariableUtile.videoEnCours) {
					VariableUtile.scene.setCursor(Cursor.DEFAULT);
				}
			}
		});
		this.cadre.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> animerEchelle(0.95, 70));
		this.cadre.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> animerEchelle(survol ? 1.07 : 1.0, 90));

		Theme.fx(() -> VariableUtile.root.getChildren().add(this));
	}

	// ====================================================================================
	// Style et animations
	// ====================================================================================
	private void definirSurvol(boolean s) {
		survol = s;
		animerEchelle(s ? (carteDanse ? 1.04 : 1.07) : 1.0, 130);
		if (carteDanse) {
			rectangle.setStrokeWidth(s ? strokeNormal * 1.5 : strokeNormal);
			rectangle.setEffect(Theme.lueur(accent, s ? 0.95 : 0.55, largeurBouton * (s ? 0.12 : 0.07)));
		} else {
			if (imageBouton2 != null && imageBouton1 != null) {
				poserImage(s ? imageBouton2 : imageBouton1);
			}
			appliquerStyle();
		}
	}

	private void animerEchelle(double cible, double dureeMs) {
		if (animationSurvol != null) {
			animationSurvol.stop();
		}
		// le centre de mise à l'échelle est le centre du visuel (donc du bouton)
		animationSurvol = new Timeline(
				new KeyFrame(Duration.millis(dureeMs), new KeyValue(visuel.scaleXProperty(), cible, Interpolator.EASE_OUT),
						new KeyValue(visuel.scaleYProperty(), cible, Interpolator.EASE_OUT)));
		Theme.fx(animationSurvol::play);
	}

	/** Applique l'apparence selon l'état (normal / survolé / activé / désactivé) et la couleur d'ambiance. */
	private void appliquerStyle() {
		if (carteDanse) {
			return;
		}
		Color ambiance = Theme.ambianceProperty().get();
		// couleur vive dérivée de la danse survolée
		Color vive = Color.hsb(ambiance.getHue(), Math.max(0.55, ambiance.getSaturation()), 1.0);
		if (sansPlaque) {
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setStroke(null);
			Icones.regler(iconeVectorielle, survol ? 1 : 0.8, survol, vive);
			return;
		}
		boolean actif = !bascule || etatValide;
		Paint remplissage = Theme.degradeBouton(actif && (survol || bascule), ambiance);
		Color bordure;
		double lueur;
		if (!actif) {
			bordure = Color.hsb(ambiance.getHue(), 0.35, 0.45);
			lueur = 0;
		} else if (survol) {
			bordure = vive.interpolate(Color.WHITE, 0.55);
			lueur = 1.0;
		} else if (bascule) {
			bordure = vive;
			lueur = 0.75;
		} else {
			bordure = vive.deriveColor(0, 0.9, 0.95, 1);
			lueur = 0.6;
		}
		rectangle.setFill(remplissage);
		rectangle.setStroke(bordure);
		rectangle.setStrokeWidth(survol ? strokeNormal * 1.4 : strokeNormal);
		rectangle.setEffect(lueur > 0 ? Theme.lueur(bordure, lueur, survol ? 28 : 18) : null);
		text.setFill(actif ? Color.WHITE : Theme.TEXTE_ATTENUE);
		double opacite = actif ? 1 : 0.45;
		Icones.regler(iconeVectorielle, opacite, survol, vive);
		imageViewBouton.setOpacity(opacite);
	}

	/** Conservé pour compatibilité : le style ne dépend plus de la danse survolée. */
	public void actualiserCouleur() {
		appliquerStyle();
	}

	public void genererSuperTitre(String libelle) {
		Theme.fx(() -> {
			superTitre = new Text(libelle);
			superTitre.setFont(Theme.police(VariableUtile.px * 1.5));
			superTitre.setFill(Color.web("#ffd36a"));
			superTitre.setTextOrigin(VPos.CENTER);
			double cy = rectangle.getY() + hautBande * 0.5;
			double largeurTexte = superTitre.getLayoutBounds().getWidth();
			double x = centreX - largeurTexte / 2 + largeurBouton * 0.04;
			superTitre.setX(x);
			superTitre.setY(cy);
			superTitre.setEffect(Theme.lueur(Theme.OR, 0.6, 8));
			superTitre.setMouseTransparent(true);

			// Couronne à gauche du titre, étincelle à droite
			double h = superTitre.getLayoutBounds().getHeight() * 0.62;
			double w = h * 1.25;
			double cx0 = x - w - largeurBouton * 0.03;
			Polygon couronne = new Polygon(0, h, 0, h * 0.25, w * 0.25, h * 0.6, w * 0.5, 0, w * 0.75, h * 0.6, w, h * 0.25, w,
					h);
			couronne.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#ffe27a")),
					new Stop(1, Color.web("#ff9f1a"))));
			couronne.setLayoutX(cx0);
			couronne.setLayoutY(cy - h / 2);
			couronne.setEffect(Theme.lueur(Theme.OR, 0.7, 8));
			couronne.setMouseTransparent(true);

			Polygon etincelle = new Polygon(0, -1, 0.22, -0.22, 1, 0, 0.22, 0.22, 0, 1, -0.22, 0.22, -1, 0, -0.22, -0.22);
			double r = h * 0.55;
			etincelle.getTransforms().add(new javafx.scene.transform.Scale(r, r, 0, 0));
			etincelle.setLayoutX(rectangle.getX() + largeurBouton - largeurBouton * 0.11);
			etincelle.setLayoutY(cy);
			etincelle.setFill(Color.web("#fff2b0"));
			etincelle.setMouseTransparent(true);

			visuel.getChildren().addAll(couronne, superTitre, etincelle);
		});
	}

	public void validerBouton() {
		etatValide = true;
		appliquerStyle();
	}

	public void invaliderBouton() {
		etatValide = false;
		appliquerStyle();
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
		bascule = true;
		if (titreDifferent != null) {
			setLabel(titreDifferent);
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
		bascule = true;
		if (titreDifferent != null) {
			setLabel(titreDifferent);
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