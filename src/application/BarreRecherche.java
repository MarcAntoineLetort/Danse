package application;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/** Barre de recherche en forme de pilule, avec loupe, texte d'aide, curseur clignotant et bouton d'effacement. */
public class BarreRecherche extends Parent {
	Rectangle rectangle;
	Rectangle cadre;
	Text text;
	Text indice;
	private final Line curseur;
	private final Circle verre;
	private final Line manche;
	private final Text effacer;
	private final double largeurTexte;
	private final double departTexte;
	private final Timeline clignotement;

	public BarreRecherche(double positionLargeur, double positionHauteur, double largeur, double hauteur,
			String nomImageBarre) {
		super();
		double centreY = positionHauteur + hauteur / 2;
		double echelle = Theme.echelleTexte();

		this.rectangle = new Rectangle(largeur, hauteur);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setArcWidth(hauteur);
		rectangle.setArcHeight(hauteur);
		rectangle.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#0d1d4a", 0.92)),
				new Stop(1, Color.web("#08122f", 0.92))));
		rectangle.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, Theme.CYAN),
				new Stop(1, Theme.BLEU)));
		rectangle.setStrokeWidth(Math.max(2, hauteur * 0.04));
		rectangle.setEffect(Theme.lueur(Theme.CYAN, 0.45, 16));

		// Loupe dessinée en vectoriel
		double rayon = hauteur * 0.17;
		double cx = positionLargeur + hauteur * 0.62;
		double cy = centreY - hauteur * 0.05;
		verre = new Circle(cx, cy, rayon);
		verre.setFill(null);
		verre.setStroke(Theme.CYAN);
		verre.setStrokeWidth(hauteur * 0.07);
		manche = new Line(cx + rayon * 0.72, cy + rayon * 0.72, cx + rayon * 1.65, cy + rayon * 1.65);
		manche.setStroke(Theme.CYAN);
		manche.setStrokeWidth(hauteur * 0.09);
		manche.setStrokeLineCap(StrokeLineCap.ROUND);

		departTexte = positionLargeur + hauteur * 1.2;
		largeurTexte = largeur - hauteur * 1.2 - hauteur * 0.95;
		double taille = hauteur * 0.36;

		indice = new Text(departTexte, centreY, "Rechercher une danse…");
		indice.setFont(Theme.police(taille));
		indice.setFill(Theme.TEXTE_ATTENUE);
		indice.setTextOrigin(VPos.CENTER);
		indice.setMouseTransparent(true);

		this.text = new Text(departTexte, centreY, "");
		text.setFont(Theme.police(taille));
		text.setFill(Color.WHITE);
		text.setTextOrigin(VPos.CENTER);
		text.setTextAlignment(TextAlignment.LEFT);
		text.setMouseTransparent(true);
		Rectangle decoupe = new Rectangle(departTexte - 2, positionHauteur, largeurTexte + 4, hauteur);
		javafx.scene.Group groupeTexte = new javafx.scene.Group(text);
		groupeTexte.setClip(decoupe);

		curseur = new Line(departTexte, centreY - taille * 0.7, departTexte, centreY + taille * 0.7);
		curseur.setStroke(Theme.CYAN);
		curseur.setStrokeWidth(2);
		curseur.setMouseTransparent(true);

		effacer = new Text(positionLargeur + largeur - hauteur * 0.75, centreY, "✕");
		effacer.setFont(Theme.police(taille * 0.9));
		effacer.setFill(Theme.TEXTE_ATTENUE);
		effacer.setTextOrigin(VPos.CENTER);
		effacer.setVisible(false);
		effacer.setOnMouseEntered(e -> {
			effacer.setFill(Color.WHITE);
			VariableUtile.scene.setCursor(Cursor.HAND);
		});
		effacer.setOnMouseExited(e -> {
			effacer.setFill(Theme.TEXTE_ATTENUE);
			VariableUtile.scene.setCursor(Cursor.DEFAULT);
		});
		effacer.setOnMouseReleased((MouseEvent e) -> MainDanse.effacerRecherche());

		this.cadre = new Rectangle(largeur, hauteur);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(hauteur);
		cadre.setArcHeight(hauteur);
		cadre.setOpacity(0);
		cadre.setMouseTransparent(true);

		this.getChildren().addAll(rectangle, verre, manche, indice, groupeTexte, curseur, effacer, cadre);

		clignotement = new Timeline(new KeyFrame(Duration.millis(0), e -> curseur.setVisible(true)),
				new KeyFrame(Duration.millis(520), e -> curseur.setVisible(false)),
				new KeyFrame(Duration.millis(1040)));
		clignotement.setCycleCount(Animation.INDEFINITE);
		this.visibleProperty().addListener((o, a, visible) -> {
			if (visible) {
				clignotement.play();
			} else {
				clignotement.pause();
			}
		});

		text.textProperty().addListener((o, ancien, nouveau) -> {
			boolean vide = nouveau == null || nouveau.isEmpty();
			indice.setVisible(vide);
			effacer.setVisible(!vide);
			double l = text.getLayoutBounds().getWidth();
			double decalage = Math.min(0, largeurTexte - l - 4);
			text.setTranslateX(decalage);
			curseur.setStartX(departTexte + Math.min(l, largeurTexte - 4));
			curseur.setEndX(departTexte + Math.min(l, largeurTexte - 4));
		});

		// la barre suit la couleur de la dernière danse survolée
		suiviAmbiance = (o, a, n) -> teinter(n);
		Theme.ambianceProperty().addListener(new javafx.beans.value.WeakChangeListener<>(suiviAmbiance));
		teinter(Theme.ambianceProperty().get());

		Theme.fx(() -> VariableUtile.root.getChildren().add(this));
	}

	private javafx.beans.value.ChangeListener<Color> suiviAmbiance;
	private Color accentCourant = Theme.CYAN;

	private void teinter(Color ambiance) {
		Color vive = Color.hsb(ambiance.getHue(), Math.max(0.55, ambiance.getSaturation()), 1.0);
		accentCourant = vive;
		double h = ambiance.getHue();
		rectangle.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.hsb(h, 0.80, 0.30, 0.94)), new Stop(1, Color.hsb(h, 0.85, 0.13, 0.94))));
		rectangle.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, vive),
				new Stop(1, vive.deriveColor(40, 1, 0.9, 1))));
		rectangle.setEffect(Theme.lueur(vive, 0.6, 18));
		verre.setStroke(vive);
		manche.setStroke(vive);
		curseur.setStroke(vive);
		indice.setFill(vive.interpolate(Color.web("#8da3d6"), 0.6));
	}

	/** Conservé pour compatibilité : la barre garde la même couleur quelle que soit la danse. */
	public void actualiserCouleur() {
	}
}