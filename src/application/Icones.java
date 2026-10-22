package application;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;

/**
 * Icônes vectorielles néon des boutons du menu (aucune image à fournir).
 * Chaque icône est dessinée dans un carré de 100 x 100 puis mise à l'échelle.
 */
public final class Icones {

	private Icones() {
	}

	private static Paint degrade(String haut, String bas) {
		return new LinearGradient(0, 0, 0, 100, false, CycleMethod.NO_CYCLE, new Stop(0, Color.web(haut)),
				new Stop(1, Color.web(bas)));
	}

	private static SVGPath forme(String chemin, Paint remplissage) {
		SVGPath p = new SVGPath();
		p.setContent(chemin);
		p.setFill(remplissage);
		p.setStroke(Color.web("#ffffff", 0.55));
		p.setStrokeWidth(1.6);
		p.setStrokeLineJoin(StrokeLineJoin.ROUND);
		return p;
	}

	private static Rectangle rect(double x, double y, double w, double h, double arc, Paint f) {
		Rectangle r = new Rectangle(x, y, w, h);
		r.setArcWidth(arc);
		r.setArcHeight(arc);
		r.setFill(f);
		r.setStroke(Color.web("#ffffff", 0.45));
		r.setStrokeWidth(1.4);
		return r;
	}

	private static SVGPath trait(String chemin, double epaisseur, Paint peinture) {
		SVGPath p = new SVGPath();
		p.setContent(chemin);
		p.setFill(null);
		p.setStroke(peinture);
		p.setStrokeWidth(epaisseur);
		p.setStrokeLineCap(StrokeLineCap.ROUND);
		p.setStrokeLineJoin(StrokeLineJoin.ROUND);
		return p;
	}

	/** @return l'icône, ou null si ce bouton n'a pas d'icône vectorielle. */
	public static Node creer(String nom, double taille) {
		Group g = new Group();
		Color lueur = Theme.CYAN;
		switch (nom) {
		case "Filtrer":
			g.getChildren().add(forme(
					"M8 12 Q8 6 14 6 H86 Q92 6 92 12 Q92 15 90 17 L60 52 V84 Q60 90 54 88 L42 82 Q38 80 38 76 V52 L10 17 Q8 15 8 12 Z",
					degrade("#8cf0ff", "#2f6bff")));
			break;
		case "Trier":
			g.getChildren().add(forme("M18 12 H34 V58 H46 L26 90 L6 58 H18 Z", degrade("#8cf0ff", "#2f6bff")));
			g.getChildren().add(forme("M66 90 V44 H54 L74 10 L94 44 H82 V90 Z", degrade("#aef6ff", "#35a8ff")));
			Paint or = degrade("#ffe27a", "#ff9f1a");
			g.getChildren().add(rect(40, 28, 24, 8, 6, or));
			g.getChildren().add(rect(40, 46, 18, 8, 6, or));
			g.getChildren().add(rect(40, 64, 12, 8, 6, or));
			break;
		case "Illimite":
			String inf = "M50 50 C38 28 8 28 8 50 C8 72 38 72 50 50 C62 28 92 28 92 50 C92 72 62 72 50 50";
			Paint grad = new LinearGradient(8, 0, 92, 0, false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#29d9ff")),
					new Stop(0.55, Color.web("#3a6bff")), new Stop(1, Color.web("#d04bff")));
			g.getChildren().add(trait(inf, 12, grad));
			g.getChildren().add(trait(inf, 2.6, Color.web("#ffffff", 0.55)));
			break;
		case "Mélange":
			g.getChildren().add(trait("M22 86 L60 40", 13, degrade("#ff6ec7", "#ff9f1a")));
			g.getChildren().add(trait("M60 40 L68 30", 13, Color.web("#ffffff", 0.9)));
			Paint etoile = degrade("#d9fbff", "#2fd8ff");
			g.getChildren().add(forme(etoile4(76, 22, 16), etoile));
			g.getChildren().add(forme(etoile4(34, 34, 9), etoile));
			g.getChildren().add(forme(etoile4(86, 60, 8), degrade("#fff2b0", "#ffc13a")));
			g.getChildren().add(forme(etoile4(46, 14, 6), etoile));
			lueur = Color.web("#ff6ec7");
			break;
		case "Selection":
			g.getChildren().add(rect(8, 8, 38, 38, 12, degrade("#8cf0ff", "#35a8ff")));
			g.getChildren().add(rect(54, 8, 38, 38, 12, degrade("#6cc8ff", "#2f6bff")));
			g.getChildren().add(rect(8, 54, 38, 38, 12, degrade("#6cc8ff", "#2f6bff")));
			g.getChildren().add(rect(54, 54, 38, 38, 12, degrade("#4d9bff", "#2750d8")));
			break;
		case "Précédent":
		case "ChevronGauche":
			g.getChildren().add(trait("M66 12 L30 50 L66 88", 15, degrade("#ffffff", "#2fd8ff")));
			break;
		case "Suivant":
		case "ChevronDroite":
			g.getChildren().add(trait("M34 12 L70 50 L34 88", 15, degrade("#ffffff", "#2fd8ff")));
			break;
		default:
			return null;
		}
		// pour que l'icône occupe bien 100 x 100 même si le dessin est plus petit
		Rectangle zone = new Rectangle(0, 0, 100, 100);
		zone.setFill(Color.TRANSPARENT);
		g.getChildren().add(0, zone);
		g.setEffect(new DropShadow(14, Color.color(lueur.getRed(), lueur.getGreen(), lueur.getBlue(), 0.7)));
		double s = taille / 100.0;
		g.getTransforms().add(new Scale(s, s, 0, 0));
		return g;
	}

	/** Étincelle à 4 branches centrée en (cx, cy). */
	private static String etoile4(double cx, double cy, double r) {
		double c = r * 0.18;
		return "M" + cx + " " + (cy - r) + " Q" + (cx + c) + " " + (cy - c) + " " + (cx + r) + " " + cy + " Q" + (cx + c)
				+ " " + (cy + c) + " " + cx + " " + (cy + r) + " Q" + (cx - c) + " " + (cy + c) + " " + (cx - r) + " " + cy
				+ " Q" + (cx - c) + " " + (cy - c) + " " + cx + " " + (cy - r) + " Z";
	}

	/** Changer la luminosité d'une icône (survol / état inactif). */
	public static void regler(Node icone, double opacite, boolean lumineux, Color lueur) {
		if (icone == null) {
			return;
		}
		icone.setOpacity(opacite);
		if (icone.getEffect() instanceof DropShadow) {
			DropShadow ds = (DropShadow) icone.getEffect();
			ds.setRadius(lumineux ? 24 : 12);
			ds.setColor(Color.color(lueur.getRed(), lueur.getGreen(), lueur.getBlue(), 0.75));
		}
	}

	@SuppressWarnings("unused")
	private static void inutilise(Shape s) {
	}
}