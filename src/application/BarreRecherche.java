package application;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class BarreRecherche extends Parent {
	Rectangle rectangle;
	Rectangle cadre;
	ImageView imageViewBarre;
	Text text;

	public BarreRecherche(double positionLargeur, double positionHauteur, double largeur, double hauteur,
			String nomImageBarre) {
		super();
		this.rectangle = new Rectangle(largeur, hauteur);
		rectangle.setX(positionLargeur);
		rectangle.setY(positionHauteur);
		rectangle.setArcWidth(largeur * 0.1);
		rectangle.setArcHeight(hauteur * 0.3);
		rectangle.setVisible(true);
		rectangle.setFill(VariableUtile.couleur1);
		this.cadre = new Rectangle(largeur, hauteur);
		cadre.setX(positionLargeur);
		cadre.setY(positionHauteur);
		cadre.setArcWidth(largeur * 0.1);
		cadre.setArcHeight(hauteur * 0.3);
		cadre.setOpacity(0);
		Image imageBouton1 = VariableUtile.main.importerImage("imageBouton/" + nomImageBarre + "1.png");
		this.imageViewBarre = new ImageView(imageBouton1);
		double largeurImage = hauteur * 0.86;
		imageViewBarre.setX(positionLargeur + 0.96*largeur - largeurImage);
		imageViewBarre.setY(positionHauteur + 0.07 * hauteur);
		imageViewBarre.setFitWidth(largeurImage);
		imageViewBarre.setFitHeight(hauteur * 0.86);

		this.text = new Text(positionLargeur + 0.04 * largeur, positionHauteur + 0.85 * hauteur, "");
		text.setFont(new Font(VariableUtile.police, VariableUtile.px * 1.5));
		text.setWrappingWidth(largeur);
		text.setTextAlignment(TextAlignment.LEFT);
		text.setFill(Color.WHITE);

		this.getChildren().add(rectangle);
		this.getChildren().add(cadre);
		this.getChildren().add(imageViewBarre);
		this.getChildren().add(text);
		VariableUtile.root.getChildren().add(this);
		cadre.toFront();
	}

	public void actualiserCouleur() {

		rectangle.setFill(VariableUtile.couleur1);
		text.setFill(VariableUtile.couleur1.invert());
	}
}
