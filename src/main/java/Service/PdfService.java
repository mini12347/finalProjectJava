package Service;

import Entities.Candidat;
import Service.PdfGeneratorCandidat;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfService {

    private final PdfGeneratorCandidat pdfGenerator = new PdfGeneratorCandidat();
    private final String PDF_DIRECTORY = System.getProperty("user.home") + File.separator + "CandidatesPDF";

    public PdfService() {
        // Création du répertoire de sauvegarde s'il n'existe pas
        File directory = new File(PDF_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Génère un PDF pour un candidat spécifique
     *
     * @param candidate Le candidat
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public String generateCandidatePdf(Candidat candidate) throws IOException {
        String fileName = "candidat_" + candidate.getCIN() + ".pdf";
        String filePath = PDF_DIRECTORY + File.separator + fileName;

        pdfGenerator.generateCandidatePdf(candidate, filePath);

        return filePath;
    }

    /**
     * Génère un PDF contenant la liste des candidats
     *
     * @param candidates La liste des candidats
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public String generateCandidatesListPdf(List<Candidat> candidates) throws IOException {
        String fileName = "liste_candidats_" + System.currentTimeMillis() + ".pdf";
        String filePath = PDF_DIRECTORY + File.separator + fileName;

        pdfGenerator.generateCandidatesList(candidates, filePath);

        return filePath;
    }
}