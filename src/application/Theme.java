package application;

import java.io.InputStream;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * Charte graphique « néon sombre » de l'application : palette, fond décoratif,
 * lueurs, messages d'erreur discrets (toasts).
 */
public final class Theme {

	public static final Color FOND = Color.web("#050a26");
	public static final Color CYAN = Color.web("#2fd8ff");
	public static final Color BLEU = Color.web("#2f6bff");
	public static final Color OR = Color.web("#ffb52e");
	public static final Color TEXTE = Color.WHITE;
	public static final Color TEXTE_ATTENUE = Color.web("#8da3d6");

	/** Police principale (Segoe UI Semibold sous Windows, sinon police par défaut). */
	public static String famille = "Segoe UI";

	private static Group fond;
	private static Rectangle halo;
	private static Rectangle baseFond;
	private static final ObjectProperty<Color> ambiance = new SimpleObjectProperty<>(Color.web("#2f6bff"));
	private static Timeline animationAmbiance;

	private Theme() {
	}

	// ------------------------------------------------------------------ utilitaires

	/** Exécute sur le thread JavaFX (tout de suite si on y est déjà). */
	public static void fx(Runnable r) {
		if (Platform.isFxApplicationThread()) {
			r.run();
		} else {
			Platform.runLater(r);
		}
	}

	/** Tente de charger une police fournie avec l'appli (/application/fonts/*.ttf). */
	public static void chargerPolice() {
		String[] candidates = { "/application/fonts/Poppins-SemiBold.ttf", "/application/fonts/Montserrat-SemiBold.ttf" };
		for (String c : candidates) {
			try (InputStream in = Theme.class.getResourceAsStream(c)) {
				if (in != null) {
					Font f = Font.loadFont(in, 12);
					if (f != null) {
						famille = f.getFamily();
						return;
					}
				}
			} catch (Exception e) {
				Diagnostic.log("Police non chargée : " + c, e);
			}
		}
	}

	public static Font police(double taille) {
		return Font.font(famille, FontWeight.BOLD, taille);
	}

	/** Facteur d'échelle des tailles de police (réglées pour un écran de 1920 px). */
	public static double echelleTexte() {
		return Math.max(0.7, Math.min(2.2, VariableUtile.px / 19.2));
	}

	public static DropShadow lueur(Color c, double alpha, double rayon) {
		DropShadow ds = new DropShadow(BlurType.GAUSSIAN, Color.color(c.getRed(), c.getGreen(), c.getBlue(), alpha),
				rayon, 0.15, 0, 0);
		return ds;
	}

	/** Couleur d'accent (bordure néon) d'une danse, déduite de ses couleurs. */
	public static Color accentDe(Danse d) {
		Color a = d.couleur1;
		Color b = d.couleur2;
		Color base = (b != null && (a == null || b.getSaturation() > a.getSaturation())) ? b : a;
		if (base == null || base.getSaturation() < 0.18) {
			return CYAN;
		}
		return Color.hsb(base.getHue(), Math.max(0.65, Math.min(1, base.getSaturation())), 1.0);
	}

	public static Paint degradeCarte(Color accent) {
		double h = accent.getHue();
		return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.hsb(h, 0.70, 0.24, 0.96)),
				new Stop(1, Color.hsb(h, 0.80, 0.10, 0.96)));
	}

	/** Dégradé des boutons de menu, teinté par la couleur d'ambiance. */
	public static Paint degradeBouton(boolean actif, Color accent) {
		double h = accent.getHue();
		if (actif) {
			return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.hsb(h, 0.75, 0.50, 0.94)),
					new Stop(1, Color.hsb(h, 0.85, 0.22, 0.94)));
		}
		return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.hsb(h, 0.60, 0.26, 0.90)),
				new Stop(1, Color.hsb(h, 0.70, 0.12, 0.90)));
	}

	// ------------------------------------------------------------------ fond

	/**
	 * Fond : dégradé nuit + rubans lumineux + figures décoratives + halo qui prend
	 * la couleur de la danse survolée. Si imageBouton/fond.png existe, elle
	 * remplace les rubans (vous pouvez ainsi fournir un fond généré).
	 */
	public static void installerFond(Pane root) {
		javafx.geometry.Rectangle2D ecran = Screen.getPrimary().getBounds();
		double L = ecran.getWidth();
		double H = ecran.getHeight();

		fond = new Group();
		fond.setMouseTransparent(true);

		Rectangle base = new Rectangle(L, H);
		baseFond = base;
		majBase(ambiance.get());
		fond.getChildren().add(base);

		Image perso = null;
		try (InputStream in = Theme.class.getResourceAsStream("imageBouton/fond.png")) {
			if (in != null) {
				perso = new Image(in, L, H, false, true);
			}
		} catch (Exception e) {
			Diagnostic.log("fond.png illisible", e);
		}

		if (perso != null && !perso.isError()) {
			ImageView iv = new ImageView(perso);
			iv.setFitWidth(L);
			iv.setFitHeight(H);
			fond.getChildren().add(iv);
		} else {
			fond.getChildren().add(blob(L * 0.15, H * 0.05, L * 0.55, Color.web("#2fd8ff", 0.20)));
			fond.getChildren().add(blob(L * 0.90, H * 0.95, L * 0.45, Color.web("#ff2f9a", 0.22)));
			fond.getChildren().add(blob(L * 0.55, H * 0.40, L * 0.35, Color.web("#5b3bff", 0.14)));

			Group rubans = new Group();
			rubans.getChildren().addAll(
					ruban(L, H, "#17e6c1", "#1aa6ff", 0.75, 0.0, -0.02, 0.34, 0.10, 0.17, 0.22, 0.18, 0.34, 0.26, 0.20, 0.22,
							0.08, 0.27, -0.02, 0.44),
					ruban(L, H, "#ffd23a", "#ff8a00", 0.70, 0.0, -0.02, 0.50, 0.04, 0.40, 0.10, 0.37, 0.16, 0.37, 0.09, 0.42,
							0.05, 0.47, -0.02, 0.60),
					ruban(L, H, "#2fd8ff", "#2f6bff", 0.55, 0.0, 1.02, 0.28, 0.94, 0.34, 0.90, 0.46, 0.92, 0.58, 0.95, 0.48,
							0.99, 0.40, 1.02, 0.36),
					ruban(L, H, "#ffd23a", "#ff8a00", 0.60, 0.0, 1.02, 0.80, 0.93, 0.86, 0.88, 0.95, 0.84, 1.02, 0.88, 1.02,
							0.92, 0.94, 1.02, 0.86));
			rubans.getChildren().addAll(zone(L, H, "#7a2cff", "#ff3fa4", 0.38, -0.02, 0.88, 0.12, 0.72, 0.28, 0.74, 0.40, 1.02),
					zone(L, H, "#ff2f9a", "#6a2cff", 0.50, 1.02, 0.62, 0.88, 0.70, 0.80, 0.90, 0.70, 1.02));
			rubans.setEffect(new GaussianBlur(3));
			fond.getChildren().add(rubans);

			fond.getChildren().add(decor(L, H));
		}

		// Halo qui prend la teinte de la danse survolée
		halo = new Rectangle(L, H);
		halo.setBlendMode(javafx.scene.effect.BlendMode.SCREEN);
		majHalo(ambiance.get());
		ambiance.addListener((o, a, n) -> {
			majBase(n);
			majHalo(n);
		});
		fond.getChildren().add(halo);

		fond.setCache(true);
		fond.setCacheHint(CacheHint.SPEED);
		root.getChildren().add(0, fond);
	}

	/** Le dégradé de fond prend la teinte de la danse survolée. */
	private static void majBase(Color c) {
		if (baseFond == null) {
			return;
		}
		double h = c.getHue();
		baseFond.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.hsb(h, 0.90, 0.16)), new Stop(0.5, Color.hsb(h, 0.85, 0.42)),
				new Stop(1, Color.hsb((h + 40) % 360, 0.85, 0.24))));
	}

	private static void majHalo(Color c) {
		halo.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.85, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.60)),
				new Stop(0.6, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.22)),
				new Stop(1, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0))));
	}

	/** Couleur d'accent de la dernière danse survolée (suivie par les boutons). */
	public static ObjectProperty<Color> ambianceProperty() {
		return ambiance;
	}

	/** Transition douce de la couleur d'ambiance (survol d'une danse). */
	public static void changerAmbiance(Color accent) {
		fx(() -> {
			if (animationAmbiance != null) {
				animationAmbiance.stop();
			}
			animationAmbiance = new Timeline(new KeyFrame(Duration.millis(350),
					new KeyValue(ambiance, accent, Interpolator.EASE_BOTH)));
			animationAmbiance.play();
		});
	}

	/** Cache le fond pendant les vidéos (libère le processeur graphique). */
	public static void afficherFond(boolean visible) {
		if (fond != null) {
			fond.setVisible(visible);
		}
	}

	private static Circle blob(double cx, double cy, double r, Color c) {
		Circle ci = new Circle(cx, cy, r);
		ci.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, new Stop(0, c),
				new Stop(1, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0))));
		return ci;
	}

	private static Path ruban(double L, double H, String c1, String c2, double a, double unused, double... p) {
		Path path = new Path(new MoveTo(p[0] * L, p[1] * H),
				new CubicCurveTo(p[2] * L, p[3] * H, p[4] * L, p[5] * H, p[6] * L, p[7] * H),
				new CubicCurveTo(p[8] * L, p[9] * H, p[10] * L, p[11] * H, p[12] * L, p[13] * H));
		path.getElements().add(new javafx.scene.shape.ClosePath());
		path.setStroke(null);
		path.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web(c1, a)),
				new Stop(1, Color.web(c2, 0.05))));
		return path;
	}

	private static Path zone(double L, double H, String c1, String c2, double a, double x0, double y0, double... p) {
		Path path = new Path(new MoveTo(x0 * L, y0 * H),
				new CubicCurveTo(p[0] * L, p[1] * H, p[2] * L, p[3] * H, p[4] * L, p[5] * H));
		path.getElements().add(new LineTo(x0 * L, p[5] * H));
		path.getElements().add(new javafx.scene.shape.ClosePath());
		path.setStroke(null);
		path.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web(c1, a)),
				new Stop(1, Color.web(c2, 0.04))));
		return path;
	}

	/** Petites figures décoratives : triangles, anneau, grilles de points, silhouettes. */
	private static Group decor(double L, double H) {
		Group g = new Group();
		Color trait = Color.web("#2fd8ff", 0.28);
		Polygon t1 = new Polygon(0, 0, 38, 14, 10, 40);
		t1.setFill(null);
		t1.setStroke(trait);
		t1.setStrokeWidth(2);
		t1.setLayoutX(L * 0.09);
		t1.setLayoutY(H * 0.30);
		Polygon t2 = new Polygon(0, 0, 60, 20, 18, 64);
		t2.setFill(null);
		t2.setStroke(Color.web("#ff4fb0", 0.30));
		t2.setStrokeWidth(2.5);
		t2.setRotate(-20);
		t2.setLayoutX(L * 0.80);
		t2.setLayoutY(H * 0.72);
		Circle anneau = new Circle(L * 0.92, H * 0.57, L * 0.005);
		anneau.setFill(null);
		anneau.setStroke(Color.web("#6fa0ff", 0.55));
		anneau.setStrokeWidth(2);
		g.getChildren().addAll(t1, t2, anneau);

		g.getChildren().add(grillePoints(L * 0.02, H * 0.62, 7, 6, L * 0.0065, Color.web("#7fb2ff", 0.28)));
		g.getChildren().add(grillePoints(L * 0.84, H * 0.12, 6, 5, L * 0.0065, Color.web("#7fb2ff", 0.20)));

		Color sil = Color.web("#4f7dff", 0.13);
		g.getChildren().add(silhouette(L * 0.045, H * 0.70, H * 0.26, sil, 0));
		g.getChildren().add(silhouette(L * 0.115, H * 0.86, H * 0.16, sil, 1));
		g.getChildren().add(silhouette(L * 0.935, H * 0.78, H * 0.24, sil, 1));
		g.getChildren().add(silhouette(L * 0.865, H * 0.95, H * 0.12, sil, 0));
		g.getChildren().add(silhouette(L * 0.92, H * 0.36, H * 0.20, Color.web("#4f7dff", 0.09), 0));
		return g;
	}

	private static Group grillePoints(double x, double y, int cols, int rows, double pas, Color c) {
		Group g = new Group();
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				g.getChildren().add(new Circle(x + i * pas * 1.8, y + j * pas * 1.8, pas * 0.22, c));
			}
		}
		return g;
	}

	/** Silhouette de danseur stylisée (membres épais aux bouts arrondis). */
	private static Group silhouette(double x, double yPieds, double hauteur, Color c, int pose) {
		double u = hauteur / 10.0;
		Group g = new Group();
		double[][] membres;
		if (pose == 0) { // bras en l'air
			membres = new double[][] { { 0, -6, 0, -3 }, { 0, -6, -2, -9 }, { 0, -6, 2.2, -8.6 }, { 0, -3, -1.5, 0 },
					{ 0, -3, 2, 0 }, { 0, -6, 0, -3 } };
		} else { // pas de côté
			membres = new double[][] { { 0, -6, 0.3, -3 }, { 0, -6, -2.6, -4.4 }, { 0, -6, 2.4, -7.6 },
					{ 0.3, -3, -1.6, 0 }, { 0.3, -3, 2.4, -1.2 } };
		}
		for (double[] m : membres) {
			javafx.scene.shape.Line l = new javafx.scene.shape.Line(m[0] * u, m[1] * u, m[2] * u, m[3] * u);
			l.setStroke(c);
			l.setStrokeWidth(u * 0.95);
			l.setStrokeLineCap(StrokeLineCap.ROUND);
			g.getChildren().add(l);
		}
		Circle tete = new Circle(0, -7.2 * u, u * 0.8, c);
		g.getChildren().add(tete);
		g.setLayoutX(x);
		g.setLayoutY(yPieds);
		g.setEffect(new GaussianBlur(1.5));
		return g;
	}

	// ------------------------------------------------------------------ toasts

	private static StackPane dernierToast;

	/** Message discret (non bloquant) affiché en bas de l'écran. */
	public static void toast(String message) {
		Platform.runLater(() -> {
			if (VariableUtile.root == null) {
				return;
			}
			Text t = new Text(message);
			t.setFill(Color.WHITE);
			t.setFont(Font.font(famille, FontWeight.NORMAL, 15 * echelleTexte()));
			t.setWrappingWidth(Math.min(VariableUtile.largeurFenetre * 0.6, 900));
			Rectangle fondToast = new Rectangle(t.getLayoutBounds().getWidth() + 36, t.getLayoutBounds().getHeight() + 22);
			fondToast.setArcWidth(22);
			fondToast.setArcHeight(22);
			fondToast.setFill(Color.web("#2a0d1c", 0.94));
			fondToast.setStroke(Color.web("#ff5a7a"));
			fondToast.setStrokeWidth(1.5);
			StackPane sp = new StackPane(fondToast, t);
			sp.setMouseTransparent(true);
			sp.setLayoutX((VariableUtile.largeurFenetre - fondToast.getWidth()) / 2);
			double y = VariableUtile.hauteurFenetre - fondToast.getHeight() - 24;
			if (dernierToast != null && dernierToast.getParent() != null) {
				y = dernierToast.getLayoutY() - fondToast.getHeight() - 8;
			}
			sp.setLayoutY(Math.max(10, y));
			dernierToast = sp;
			VariableUtile.root.getChildren().add(sp);
			sp.toFront();
			FadeTransition fondu = new FadeTransition(Duration.millis(500), sp);
			fondu.setToValue(0);
			PauseTransition attente = new PauseTransition(Duration.seconds(7));
			SequentialTransition seq = new SequentialTransition(attente, fondu);
			seq.setOnFinished(e -> VariableUtile.root.getChildren().remove(sp));
			seq.play();
		});
	}

	public static void marquerCache(Node n) {
		n.setCache(true);
		n.setCacheHint(CacheHint.SPEED);
	}

	public static void arrondir(Path p) {
		p.setStrokeLineJoin(StrokeLineJoin.ROUND);
		p.setStrokeLineCap(StrokeLineCap.ROUND);
	}
}