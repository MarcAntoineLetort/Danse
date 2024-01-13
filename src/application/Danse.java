package application;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Danse {
	int ordre = 0;
	String nomVideo;
	String titreMusique;
	ArrayList<String> artistes = new ArrayList<String>();
	Image imageDanse1;
	Image imageDanse2;
	ArrayList<Genre> genres = new ArrayList<Genre>();;
	int intensite = 1;
	int technique = 1;
	Version version;
	boolean unlimited = false;
	Color couleur1;
	Color couleur2;

	public Danse(String titreMusique, Version version, Color couleur1, Color couleur2) {
		super();
		this.titreMusique = titreMusique;
		if (version != null) {
			this.nomVideo = titreMusique + " - " + version.toString() + ".mp4";
			this.version = version;

			importerImages(titreMusique);
		} else {
			// Nom non conforme
			this.nomVideo = titreMusique;
		}
		this.couleur1 = couleur1;
		this.couleur2 = couleur2;
		VariableUtile.danses.put(titreMusique, this);
	}

	private void importerImages(String titreMusique2) {
		URL imageURL1 = null;
		URL imageURL2 = null;
		File dossierImagesDanses = new File("C:\\Users\\marca\\Documents\\Java\\Danse\\Ressources\\images danses");
		try {
			imageURL1 = new URL("file:\\" + dossierImagesDanses + "\\" + titreMusique + "1.png");
		} catch (MalformedURLException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("Echec de l'import de l'image : " + titreMusique + "1.png");
			a.show();
		}
		try {
			imageURL2 = new URL("file:\\" + dossierImagesDanses + "\\" + titreMusique + "2.png");
		} catch (MalformedURLException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("Echec de l'import de l'image : " + titreMusique + "2.png");
			a.show();
		}

		this.imageDanse1 = new Image(imageURL1.toExternalForm());
		this.imageDanse2 = new Image(imageURL2.toExternalForm());
	}

	// Constructeur utilisé pour le néant
	public Danse(String titreMusique, String titreImage, Color couleur1, Color couleur2) {
		super();
		this.nomVideo = "aucun";
		this.titreMusique = titreMusique;

		this.imageDanse1 = VariableUtile.main.importerImage("imageBouton/" + titreImage + "1.png");
		this.imageDanse2 = VariableUtile.main.importerImage("imageBouton/" + titreImage + "2.png");
		this.couleur1 = couleur1;
		this.couleur2 = couleur2;
	}

}
