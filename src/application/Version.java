package application;

public enum Version {
	jd ("Just Dance"), jd2("Just Dance 2"), jd3("Just Dance 3"), jd4("Just Dance 4"), 
	jd2014 ("Just Dance 2014"), jd2015 ("Just Dance 2015"), jd2016 ("Just Dance 2016"), jd2017 ("Just Dance 2017"), jd2018 ("Just Dance 2018"), 
	jd2019 ("Just Dance 2019"), jd2020 ("Just Dance 2020"), jd2021 ("Just Dance 2021"), jd2022 ("Just Dance 2022"), jd2023 ("Just Dance 2023"), 
	jd2024 ("Just Dance 2024"), jd2025 ("Just Dance 2025"), jd2026 ("Just Dance 2026"), jd2027 ("Just Dance 2027"), jd2028 ("Just Dance 2028"),
	jd2029 ("Just Dance 2029"), jd2030 ("Just Dance 2030"),
	mj ("Mickael Jackson"), tbep ("The Black Eyed Peas");

	private final String nomComplet;

	Version(String nomComplet) {
		this.nomComplet = nomComplet;
	}

	/** Nom affiché dans l'interface (toString() reste le nom technique, utilisé dans les noms de fichiers vidéo). */
	public String getNomComplet() {
		return nomComplet;
	}
}