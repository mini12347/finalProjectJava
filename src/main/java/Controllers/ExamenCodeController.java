package Controllers;

import Entities.Candidat;
import Entities.ExamenCode;
import Entities.Res;
import Entities.TypeP;
import Service.ExamenCodeService;
import Service.CandidateService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class ExamenCodeController implements Initializable {

    @FXML
    private TextField idField;
    @FXML
    private DatePicker dateField;
    @FXML
    private TextField timeField;
    @FXML
    private ComboBox<String> candidatComboBox;
    @FXML
    private ComboBox<Res> resultatComboBox; // Changé de TextField à ComboBox<Res>
    @FXML
    private ComboBox<TypeP> typePermisComboBox;
    @FXML
    private TextField coutField; // Nouveau champ pour le coût
    @FXML
    private TableView<ExamenCode> examenTable;
    @FXML
    private TableColumn<ExamenCode, Integer> idColumn;
    @FXML
    private TableColumn<ExamenCode, LocalDate> dateColumn;
    @FXML
    private TableColumn<ExamenCode, Time> timeColumn;
    @FXML
    private TableColumn<ExamenCode, String> candidatColumn;
    @FXML
    private TableColumn<ExamenCode, Res> resultatColumn; // Changé de Integer à Res
    @FXML
    private TableColumn<ExamenCode, TypeP> typePermisColumn;
    @FXML
    private TableColumn<ExamenCode, Double> coutColumn; // Nouvelle colonne pour le coût
    @FXML
    private Label countLabel;

    private ExamenCodeService examenCodeService;
    private CandidateService candidatService;
    private ObservableList<ExamenCode> examenList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            examenCodeService = new ExamenCodeService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            candidatService = new CandidateService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        examenList = FXCollections.observableArrayList();

        // Initialisation des colonnes du tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        candidatColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue().getCandidat();
            return javafx.beans.binding.Bindings.createStringBinding(() -> String.valueOf(candidat.getCIN()));
        });
        resultatColumn.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        typePermisColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        coutColumn.setCellValueFactory(new PropertyValueFactory<>("cout")); // Initialisation de la colonne coût

        // Remplir le comboBox des types de permis
        typePermisComboBox.getItems().setAll(TypeP.values());

        // Remplir le ComboBox des résultats possibles
        resultatComboBox.getItems().setAll(Res.SUCCES, Res.ECHEC);

        // Désactiver le ComboBox des résultats quand la date est dans le futur
        dateField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isPastDate = !isDateInFuture(newVal);
                resultatComboBox.setDisable(!isPastDate);

                // Si la date est dans le futur, on met automatiquement EN_ATTENTE
                if (!isPastDate) {
                    resultatComboBox.setValue(Res.EnATTENTE);
                } else {
                    // Si on revient à une date passée, on vide le ComboBox
                    resultatComboBox.setValue(null);
                }
            }
        });

        // Remplir le comboBox des candidats
        loadCandidats();

        // Charger tous les examens
        loadAllExamens();

        // Définir le listener pour la sélection dans le tableau
        examenTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormWithExamen(newVal);
            }
        });

        // Ajouter un TextFormatter au champ cout pour n'accepter que des valeurs numériques
        coutField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }

            // Permettre les nombres décimaux (avec point ou virgule)
            if (newText.matches("^\\d*([.,]\\d*)?$")) {
                return change;
            }
            return null;
        }));
    }

    private void loadCandidats() {
        try {
            // Récupérer tous les candidats
            List<Candidat> allCandidats = candidatService.getAllCandidates();

            // Récupérer les CINs des candidats qui ont déjà réussi
            List<Integer> successfulCins = examenCodeService.getSuccessfulCandidateCins();

            // Filtrer les candidats qui n'ont pas encore réussi
            ObservableList<String> cinList = FXCollections.observableArrayList();
            for (Candidat candidat : allCandidats) {
                // N'ajouter que les candidats qui n'ont pas encore réussi
                if (!successfulCins.contains(candidat.getCIN())) {
                    cinList.add(String.valueOf(candidat.getCIN()));
                }
            }

            candidatComboBox.setItems(cinList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les candidats: " + e.getMessage());
        }
    }

    private void loadAllExamens() {
        examenList.clear();
        examenList.addAll(examenCodeService.getAllExamens());
        examenTable.setItems(examenList);
        updateCountLabel();
    }

    private void updateCountLabel() {
        countLabel.setText("Nombre d'examens: " + examenList.size());
    }

    private void fillFormWithExamen(ExamenCode examen) {
        try {
            idField.setText(String.valueOf(examen.getId()));
            dateField.setValue(examen.getDate());
            timeField.setText(examen.getTime().toString());
            candidatComboBox.setValue(String.valueOf(examen.getCandidat().getCIN()));
            coutField.setText(String.valueOf(examen.getCout())); // Afficher le coût

            // Adapter le comportement selon la date
            LocalDate examDate = examen.getDate();
            boolean isPastDate = !isDateInFuture(examDate);
            resultatComboBox.setDisable(!isPastDate);
            resultatComboBox.setValue(examen.getResultat());

            typePermisComboBox.setValue(examen.getType());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de remplir le formulaire: " + e.getMessage());
        }
    }

    private void clearForm() {
        idField.clear();
        dateField.setValue(null);
        timeField.clear();
        candidatComboBox.setValue(null);
        resultatComboBox.setValue(null);
        resultatComboBox.setDisable(false);
        typePermisComboBox.setValue(null);
        coutField.clear(); // Effacer le champ coût
        examenTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            if (!validateForm()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs correctement");
                return;
            }

            ExamenCode examen = createExamenFromForm(false);
            boolean success = examenCodeService.addExamen(examen);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen modifié avec succès");
                clearForm();
                loadAllExamens();
                loadCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            if (!validateForm() || idField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner un examen et remplir tous les champs");
                return;
            }

            ExamenCode examen = createExamenFromForm(true);
            boolean success = examenCodeService.updateExamen(examen);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen modifié avec succès");
                clearForm();
                loadAllExamens();
                loadCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un examen à supprimer");
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            boolean success = examenCodeService.deleteExamen(id);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen modifié avec succès");
                clearForm();
                loadAllExamens();
                loadCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercher(ActionEvent event) {
        if (candidatComboBox.getValue() != null && !candidatComboBox.getValue().isEmpty()) {
            try {
                String cinStr = candidatComboBox.getValue();
                int cin = Integer.parseInt(cinStr);
                List<ExamenCode> examens = examenCodeService.getExamensByCandidatCin(cin);
                examenList.clear();
                examenList.addAll(examens);
                examenTable.setItems(examenList);
                updateCountLabel();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Format de CIN invalide: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un CIN de candidat pour la recherche");
        }
    }

    @FXML
    private void handleAfficherTout(ActionEvent event) {
        loadAllExamens();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private boolean validateForm() {
        // Vérification des champs obligatoires
        boolean formValid = dateField.getValue() != null &&
                !timeField.getText().isEmpty() &&
                candidatComboBox.getValue() != null &&
                typePermisComboBox.getValue() != null &&
                !coutField.getText().isEmpty(); // Vérifier que le coût est renseigné

        // Vérifier que le coût est un nombre valide
        if (formValid && !coutField.getText().isEmpty()) {
            try {
                Double.parseDouble(coutField.getText().replace(',', '.'));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le coût doit être un nombre valide");
                return false;
            }
        }

        // Pour le résultat, vérifier selon la date
        if (formValid) {
            LocalDate examDate = dateField.getValue();
            boolean isFutureDate = isDateInFuture(examDate);

            // Si date future, résultat doit être EN_ATTENTE et est automatiquement défini
            if (isFutureDate) {
                // Le résultat sera automatiquement EN_ATTENTE, pas besoin de validation
            }
            // Si date passée, résultat doit être soit SUCCES soit ECHEC
            else if (resultatComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Pour une date passée, veuillez sélectionner un résultat (Succès ou Échec)");
                return false;
            }
        }

        return formValid;
    }

    private boolean isDateInFuture(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isAfter(today);
    }

    private ExamenCode createExamenFromForm(boolean withId) {
        LocalDate date = dateField.getValue();

        Time time;
        try {
            LocalTime localTime = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            time = Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format d'heure invalide. Utilisez HH:mm:ss");
        }

        String cinStr = candidatComboBox.getValue();
        Candidat candidat;
        try {
            candidat = candidatService.getCandidateByCin(Integer.parseInt(cinStr));
            if (candidat == null) {
                throw new IllegalArgumentException("Candidat introuvable");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Erreur lors de la recherche du candidat: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le CIN doit être un nombre entier");
        }

        // Déterminer le résultat en fonction de la date
        Res resultat;
        if (isDateInFuture(date)) {
            resultat = Res.EnATTENTE;
        } else {
            resultat = resultatComboBox.getValue();
            if (resultat == null) {
                throw new IllegalArgumentException("Pour une date passée, veuillez sélectionner un résultat");
            }
        }

        TypeP typePermis = typePermisComboBox.getValue();

        // Récupérer la valeur du coût
        double cout;
        try {
            cout = Double.parseDouble(coutField.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le coût doit être un nombre valide");
        }

        ExamenCode examen = new ExamenCode(date, time, candidat, resultat, typePermis,cout);
        examen.setCout(cout); // Définir le coût

        if (withId && !idField.getText().isEmpty()) {
            examen.setId(Integer.parseInt(idField.getText()));
        }

        return examen;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}