package application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Liste déroulante dans le style de l'application (suit la couleur d'ambiance).
 * Le nombre d'éléments est libre : au-delà de {@value #VISIBLES} lignes, la liste se
 * fait défiler à la molette.
 */
public class ListeDeroulante<T> extends Parent {
	private static final int VISIBLES = 6;

	private final double x;
	private final double y;
	private final double largeur;
	private final double hauteur;
	private final double hItem;
	private final List<String> libelles = new ArrayList<>();
	private final List<T> valeurs = new ArrayList<>();
	private int selection = 0;
	private int survolItem = -1;
	private Consumer<T> auditeur;

	private final Rectangle fond;
	private final Rectangle cadre;
	private final Text texte;
	private final Polygon fleche;

	private final Group popup = new Group();
	private final Rectangle panneau;
	private final Group contenu = new Group();
	private final Rectangle ascenseur;
	private final List<Rectangle> fondsItems = new ArrayList<>();
	private final List<Text> textesItems = new ArrayList<>();
	private boolean ouverte = false;
	private boolean survolEntete = false;
	private double decalage = 0;
	private final double hauteurVue;
	private EventHandler<MouseEvent> filtreClicExterieur;
	private final ChangeListener<Color> suiviAmbiance;

	public ListeDeroulante(double x, double y, double largeur, double hauteur, List<String> libelles, List<T> valeurs) {
		this.x = x;
		this.y = y;
		this.largeur = largeur;
		this.hauteur = hauteur;
		this.libelles.addAll(libelles);
		this.valeurs.addAll(valeurs);
		this.hItem = hauteur * 0.84;
		int nbVisibles = Math.min(VISIBLES, libelles.size());
		this.hauteurVue = nbVisibles * hItem;
		double arc = hauteur * 0.45;
		double taille = hauteur * 0.40;

		// ----- en-tête
		fond = new Rectangle(x, y, largeur, hauteur);
		fond.setArcWidth(arc);
		fond.setArcHeight(arc);
		fond.setStrokeWidth(Math.max(2, hauteur * 0.04));

		texte = new Text(x + hauteur * 0.45, y + hauteur / 2, "");
		texte.setTextOrigin(VPos.CENTER);
		texte.setFill(Color.WHITE);
		texte.setMouseTransparent(true);

		double c = hauteur * 0.16;
		fleche = new Polygon(-c, -c * 0.55, c, -c * 0.55, 0, c * 0.65);
		fleche.setLayoutX(x + largeur - hauteur * 0.55);
		fleche.setLayoutY(y + hauteur / 2);
		fleche.setMouseTransparent(true);

		cadre = new Rectangle(x, y, largeur, hauteur);
		cadre.setArcWidth(arc);
		cadre.setArcHeight(arc);
		cadre.setOpacity(0);
		cadre.setOnMouseEntered(e -> {
			survolEntete = true;
			VariableUtile.scene.setCursor(Cursor.HAND);
			appliquerStyle();
		});
		cadre.setOnMouseExited(e -> {
			survolEntete = false;
			VariableUtile.scene.setCursor(Cursor.DEFAULT);
			appliquerStyle();
		});
		cadre.setOnMouseReleased(e -> {
			if (ouverte) {
				fermer();
			} else {
				ouvrir();
			}
		});
		this.getChildren().addAll(fond, texte, fleche, cadre);

		// ----- liste déroulante (ajoutée à la scène seulement à l'ouverture)
		double yListe = y + hauteur + 5;
		double marge = hauteur * 0.14;
		panneau = new Rectangle(x, yListe, largeur, hauteurVue + 2 * marge);
		panneau.setArcWidth(arc);
		panneau.setArcHeight(arc);
		panneau.setStrokeWidth(Math.max(2, hauteur * 0.04));

		for (int i = 0; i < libelles.size(); i++) {
			final int index = i;
			Rectangle r = new Rectangle(x + marge, yListe + marge + i * hItem, largeur - 2 * marge, hItem);
			r.setArcWidth(hItem * 0.5);
			r.setArcHeight(hItem * 0.5);
			r.setFill(Color.web("#ffffff", 0.001)); // « presque transparent » : reste cliquable
			r.setOnMouseEntered(e -> {
				survolItem = index;
				VariableUtile.scene.setCursor(Cursor.HAND);
				majItems();
			});
			r.setOnMouseExited(e -> {
				if (survolItem == index) {
					survolItem = -1;
				}
				majItems();
			});
			r.setOnMouseReleased(e -> choisir(index));
			Text t = new Text(x + marge + hItem * 0.45, yListe + marge + i * hItem + hItem / 2, libelles.get(i));
			t.setTextOrigin(VPos.CENTER);
			t.setFill(Color.WHITE);
			t.setMouseTransparent(true);
			fondsItems.add(r);
			textesItems.add(t);
			contenu.getChildren().addAll(r, t);
		}
		Group vue = new Group(contenu);
		vue.setClip(new Rectangle(x + marge, yListe + marge, largeur - 2 * marge, hauteurVue));

		ascenseur = new Rectangle(4, 10);
		ascenseur.setArcWidth(4);
		ascenseur.setArcHeight(4);
		ascenseur.setMouseTransparent(true);

		popup.getChildren().addAll(panneau, vue, ascenseur);
		popup.setOnScroll((ScrollEvent e) -> {
			defiler(-e.getDeltaY());
			e.consume();
		});

		// polices (réduites si un libellé est trop long)
		double tailleItem = hItem * 0.46;
		texte.setFont(Theme.police(taille));
		for (Text t : textesItems) {
			t.setFont(Theme.police(tailleItem));
			double max = largeur - 2 * marge - hItem * 0.9 - (libelles.size() > VISIBLES ? 10 : 0);
			double s = tailleItem;
			while (t.getLayoutBounds().getWidth() > max && s > 8) {
				s -= 0.5;
				t.setFont(Theme.police(s));
			}
		}

		visibleProperty().addListener((o, a, visible) -> {
			if (!visible) {
				fermer();
			}
		});
		suiviAmbiance = (o, a, n) -> appliquerStyle();
		Theme.ambianceProperty().addListener(new WeakChangeListener<>(suiviAmbiance));

		majTexte();
		appliquerStyle();
		Theme.fx(() -> VariableUtile.root.getChildren().add(this));
	}

	public void setOnChoix(Consumer<T> auditeur) {
		this.auditeur = auditeur;
	}

	public T valeurSelectionnee() {
		return valeurs.get(selection);
	}

	/** Revient au premier choix (« Toutes »). */
	public void reinitialiser() {
		selection = 0;
		majTexte();
		majItems();
		if (auditeur != null) {
			auditeur.accept(valeurs.get(0));
		}
	}

	void choisir(int index) {
		selection = index;
		majTexte();
		fermer();
		if (auditeur != null) {
			auditeur.accept(valeurs.get(index));
		}
	}

	void ouvrir() {
		if (ouverte) {
			return;
		}
		ouverte = true;
		// fait apparaître la ligne sélectionnée
		decalage = 0;
		defiler(Math.max(0, (selection - 2) * hItem));
		majItems();
		VariableUtile.root.getChildren().add(popup);
		popup.toFront();
		filtreClicExterieur = e -> {
			Node cible = e.getPickResult().getIntersectedNode();
			if (!contient(popup, cible) && !contient(this, cible)) {
				fermer();
			}
		};
		VariableUtile.scene.addEventFilter(MouseEvent.MOUSE_PRESSED, filtreClicExterieur);
		appliquerStyle();
	}

	void fermer() {
		if (!ouverte) {
			return;
		}
		ouverte = false;
		VariableUtile.root.getChildren().remove(popup);
		if (filtreClicExterieur != null) {
			VariableUtile.scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, filtreClicExterieur);
			filtreClicExterieur = null;
		}
		survolItem = -1;
		appliquerStyle();
	}

	private static boolean contient(Node parent, Node n) {
		while (n != null) {
			if (n == parent) {
				return true;
			}
			n = n.getParent();
		}
		return false;
	}

	private void defiler(double delta) {
		double max = Math.max(0, libelles.size() * hItem - hauteurVue);
		decalage = Math.max(0, Math.min(max, decalage + delta));
		contenu.setTranslateY(-decalage);
		boolean utile = max > 0;
		ascenseur.setVisible(utile);
		if (utile) {
			double marge = hauteur * 0.14;
			double haut = Math.max(18, hauteurVue * hauteurVue / (libelles.size() * hItem));
			ascenseur.setHeight(haut);
			ascenseur.setX(x + largeur - marge - 6);
			ascenseur.setY(y + hauteur + 5 + marge + (decalage / max) * (hauteurVue - haut));
		}
	}

	private void majTexte() {
		texte.setText(libelles.get(selection));
	}

	private Color vive() {
		Color a = Theme.ambianceProperty().get();
		return Color.hsb(a.getHue(), Math.max(0.55, a.getSaturation()), 1.0);
	}

	private void majItems() {
		Color vive = vive();
		for (int i = 0; i < fondsItems.size(); i++) {
			Rectangle r = fondsItems.get(i);
			if (i == selection) {
				r.setFill(Color.color(vive.getRed(), vive.getGreen(), vive.getBlue(), 0.38));
			} else if (i == survolItem) {
				r.setFill(Color.color(vive.getRed(), vive.getGreen(), vive.getBlue(), 0.20));
			} else {
				r.setFill(Color.web("#ffffff", 0.001));
			}
		}
	}

	private void appliquerStyle() {
		Color ambiance = Theme.ambianceProperty().get();
		Color vive = vive();
		fond.setFill(Theme.degradeBouton(ouverte || survolEntete, ambiance));
		Color bordure = (ouverte || survolEntete) ? vive.interpolate(Color.WHITE, 0.45) : vive;
		fond.setStroke(bordure);
		fond.setEffect(Theme.lueur(bordure, ouverte || survolEntete ? 0.9 : 0.6, 18));
		fleche.setFill(bordure);
		fleche.setRotate(ouverte ? 180 : 0);
		double h = ambiance.getHue();
		panneau.setFill(Color.hsb(h, 0.80, 0.14, 0.97));
		panneau.setStroke(vive);
		panneau.setEffect(Theme.lueur(vive, 0.7, 22));
		ascenseur.setFill(vive);
		majItems();
	}
}
