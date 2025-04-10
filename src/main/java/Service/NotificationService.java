package Service;

import DAO.NotificationDAO;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des notifications et alertes
 */
public class NotificationService {
    private NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Récupère les alertes pour le panneau de notifications
     */
    public List<Map<String, String>> getAlertes() {
        return notificationDAO.getAlertes();
    }
}