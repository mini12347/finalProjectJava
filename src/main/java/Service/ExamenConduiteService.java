package Service;

import Entities.ExamenConduite;
import Entities.Res;
import DAO.ExamenConduiteDAO;
import com.sothawo.mapjfx.Coordinate;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExamenConduiteService {

    private ExamenConduiteDAO examenConduiteDAO;
    private PaiementService paiementService;

    public ExamenConduiteService() throws SQLException {
        this.examenConduiteDAO = new ExamenConduiteDAO();
        this.paiementService = new PaiementService();
    }

    public boolean addExamen(ExamenConduite examen, boolean createPayment, boolean parFacilite) {
        // Validation des données incluant le cout
        if (examen.getDate() == null || examen.getTime() == null ||
                examen.getCandidat() == null || examen.getType() == null ||
                examen.getResultat() == null || examen.getLocalisation() == null ||
                examen.getLocalisation().isEmpty() || examen.getCout() <= 0) {
            return false;
        }

        // Vérification que le résultat est l'une des valeurs autorisées
        if (examen.getResultat() != Res.SUCCES &&
                examen.getResultat() != Res.ECHEC &&
                examen.getResultat() != Res.EnATTENTE) {
            return false;
        }

        // Vérification supplémentaire pour la localisation
        if (!isValidLocalisation(examen.getLocalisation())) {
            return false;
        }

        // Insertion de l'examen
        boolean examenInserted = examenConduiteDAO.insert(examen);

        // Si l'insertion de l'examen a réussi et qu'on veut créer un paiement
        if (examenInserted && createPayment) {
            // Utiliser la méthode générique pour créer un paiement
            return paiementService.createPaiementFromExamen(examen, parFacilite);
        }

        return examenInserted;
    }

    // Pour maintenir la compatibilité avec le code existant
    public boolean addExamen(ExamenConduite examen) {
        // Par défaut, on crée un paiement sans facilité
        return addExamen(examen, true, false);
    }

    // Méthode pour obtenir les CINs des candidats qui ont déjà réussi l'examen de conduite
    public List<Integer> getSuccessfulCandidateCins() {
        List<ExamenConduite> successExamens = getExamensByResultat(Res.SUCCES);
        return successExamens.stream()
                .map(examen -> examen.getCandidat().getCIN())
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean updateExamen(ExamenConduite examen) {
        // Validation des données incluant le cout
        if (examen.getId() <= 0 || examen.getDate() == null || examen.getTime() == null ||
                examen.getCandidat() == null || examen.getType() == null ||
                examen.getResultat() == null || examen.getLocalisation() == null ||
                examen.getLocalisation().isEmpty() || examen.getCout() <= 0) {
            return false;
        }

        // Vérification que le résultat est l'une des valeurs autorisées
        if (examen.getResultat() != Res.SUCCES &&
                examen.getResultat() != Res.ECHEC &&
                examen.getResultat() != Res.EnATTENTE) {
            return false;
        }

        // Vérification supplémentaire pour la localisation
        if (!isValidLocalisation(examen.getLocalisation())) {
            return false;
        }

        return examenConduiteDAO.update(examen);
    }

    // Méthode pour vérifier si une localisation est valide
    private boolean isValidLocalisation(String localisation) {
        // Si la localisation contient des coordonnées GPS, vérifier qu'elles sont dans la zone de Tunis
        if (localisation.matches(".*\\d+\\.\\d+.*,.*\\d+\\.\\d+.*")) {
            try {
                String[] parts = localisation.split(",");
                // Rechercher les parties qui ressemblent à des coordonnées
                Optional<Double> latitude = Optional.empty();
                Optional<Double> longitude = Optional.empty();

                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("\\d+\\.\\d+")) {
                        double value = Double.parseDouble(part);
                        if (!latitude.isPresent() && value >= 30 && value <= 40) {
                            latitude = Optional.of(value);
                        } else if (!longitude.isPresent() && value >= 5 && value <= 15) {
                            longitude = Optional.of(value);
                        }
                    }
                }

                if (latitude.isPresent() && longitude.isPresent()) {
                    return isInTunisArea(new Coordinate(latitude.get(), longitude.get()));
                }
            } catch (Exception e) {
                return false;
            }
        }
        // Si la localisation est un texte descriptif, considérer comme valide
        return true;
    }

    // Méthode pour vérifier si des coordonnées sont dans la zone de Tunis
    public boolean isInTunisArea(Coordinate coord) {
        final double MIN_LAT = 36.70;
        final double MAX_LAT = 36.90;
        final double MIN_LON = 10.05;
        final double MAX_LON = 10.25;

        return coord.getLatitude() >= MIN_LAT && coord.getLatitude() <= MAX_LAT &&
                coord.getLongitude() >= MIN_LON && coord.getLongitude() <= MAX_LON;
    }

    // Méthode pour extraire les coordonnées d'une chaîne de localisation
    public Optional<Coordinate> extractCoordinatesFromLocalisation(String localisation) {
        try {
            if (localisation != null && localisation.matches(".*\\d+\\.\\d+.*,.*\\d+\\.\\d+.*")) {
                String[] parts = localisation.split(",");
                // Rechercher les parties qui ressemblent à des coordonnées
                Optional<Double> latitude = Optional.empty();
                Optional<Double> longitude = Optional.empty();

                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("\\d+\\.\\d+")) {
                        double value = Double.parseDouble(part);
                        if (!latitude.isPresent() && value >= 30 && value <= 40) {
                            latitude = Optional.of(value);
                        } else if (!longitude.isPresent() && value >= 5 && value <= 15) {
                            longitude = Optional.of(value);
                        }
                    }
                }

                if (latitude.isPresent() && longitude.isPresent()) {
                    return Optional.of(new Coordinate(latitude.get(), longitude.get()));
                }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner un Optional vide
        }
        return Optional.empty();
    }

    public boolean deleteExamen(int id) {
        if (id <= 0) {
            return false;
        }
        return examenConduiteDAO.delete(id);
    }

    public ExamenConduite getExamenById(int id) {
        if (id <= 0) {
            return null;
        }
        return examenConduiteDAO.getById(id);
    }

    public List<ExamenConduite> getAllExamens() {
        return examenConduiteDAO.getAll();
    }

    public List<ExamenConduite> getExamensByCandidatCin(Integer cin) {
        if (cin == null) {
            return List.of();
        }
        return examenConduiteDAO.getByCandidatCin(String.valueOf(cin));
    }

    // Autres méthodes métier selon les besoins
    public List<ExamenConduite> getExamensByTypePermis(String typePermis) {
        if (typePermis == null || typePermis.isEmpty()) {
            return List.of();
        }
        return examenConduiteDAO.getByTypePermis(typePermis);
    }

    public double getSuccessRateByCandidatCin(String cin) {
        if (cin == null || cin.isEmpty()) {
            return 0.0;
        }
        List<ExamenConduite> examens = examenConduiteDAO.getByCandidatCin(cin);
        if (examens.isEmpty()) {
            return 0.0;
        }

        long countSuccess = examens.stream()
                .filter(e -> e.getResultat() == Res.SUCCES)
                .count();

        return (double) countSuccess / examens.size() * 100;
    }

    // Méthode supplémentaire pour obtenir les examens selon leur résultat
    public List<ExamenConduite> getExamensByResultat(Res resultat) {
        if (resultat == null) {
            return List.of();
        }
        return examenConduiteDAO.getByResult(resultat);
    }

    // Méthode pour obtenir le nombre d'examens en attente
    public int getCountExamensEnAttente() {
        return examenConduiteDAO.getByResult(Res.EnATTENTE).size();
    }

    // Méthode pour obtenir le taux de réussite global
    public double getGlobalSuccessRate() {
        List<ExamenConduite> allExamens = examenConduiteDAO.getAll();
        if (allExamens.isEmpty()) {
            return 0.0;
        }

        // On ne considère que les examens qui ont un résultat définitif (pas EN_ATTENTE)
        List<ExamenConduite> completedExamens = allExamens.stream()
                .filter(e -> e.getResultat() != Res.EnATTENTE)
                .toList();

        if (completedExamens.isEmpty()) {
            return 0.0;
        }

        long countSuccess = completedExamens.stream()
                .filter(e -> e.getResultat() == Res.SUCCES)
                .count();

        return (double) countSuccess / completedExamens.size() * 100;
    }

    // Méthode pour obtenir les examens par localisation
    public List<ExamenConduite> getExamensByLocalisation(String localisation) {
        if (localisation == null || localisation.isEmpty()) {
            return List.of();
        }
        return examenConduiteDAO.getByLocalisation(localisation);
    }

    // Méthode pour rechercher des examens à proximité d'une coordonnée donnée
    public List<ExamenConduite> getExamensByProximity(Coordinate coord, double radiusKm) {
        if (coord == null || radiusKm <= 0) {
            return List.of();
        }

        List<ExamenConduite> allExamens = examenConduiteDAO.getAll();
        return allExamens.stream()
                .filter(examen -> {
                    Optional<Coordinate> examCoord = extractCoordinatesFromLocalisation(examen.getLocalisation());
                    if (examCoord.isPresent()) {
                        return calculateDistance(coord, examCoord.get()) <= radiusKm;
                    }
                    return false;
                })
                .toList();
    }

    // Méthode pour obtenir les examens par plage de coût
    public List<ExamenConduite> getExamensByCostRange(double minCost, double maxCost) {
        if (minCost < 0 || maxCost <= 0 || minCost > maxCost) {
            return List.of();
        }

        List<ExamenConduite> allExamens = examenConduiteDAO.getAll();
        return allExamens.stream()
                .filter(examen -> examen.getCout() >= minCost && examen.getCout() <= maxCost)
                .toList();
    }

    // Méthode pour calculer le coût moyen des examens
    public double getAverageCost() {
        List<ExamenConduite> allExamens = examenConduiteDAO.getAll();
        if (allExamens.isEmpty()) {
            return 0.0;
        }

        double totalCost = allExamens.stream()
                .mapToDouble(ExamenConduite::getCout)
                .sum();

        return totalCost / allExamens.size();
    }

    // Méthode pour calculer la distance entre deux coordonnées (formule de Haversine)
    private double calculateDistance(Coordinate coord1, Coordinate coord2) {
        final int R = 6371; // Rayon de la Terre en kilomètres

        double latDistance = Math.toRadians(coord2.getLatitude() - coord1.getLatitude());
        double lonDistance = Math.toRadians(coord2.getLongitude() - coord1.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(coord1.getLatitude())) * Math.cos(Math.toRadians(coord2.getLatitude())) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}