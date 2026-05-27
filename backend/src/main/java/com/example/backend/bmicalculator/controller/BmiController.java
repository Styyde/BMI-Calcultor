package com.example.backend.bmicalculator.controller;

import com.example.backend.bmicalculator.dto.BmiRequest;
import com.example.backend.bmicalculator.dto.BmiResponse;
import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.projection.BmiStatsProjection;
import com.example.backend.bmicalculator.security.UserPrincipal;
import com.example.backend.bmicalculator.service.BmiService;
import com.example.backend.bmicalculator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bmi")
@RequiredArgsConstructor  // ← Ajouté pour l'injection automatique
public class BmiController {

    private final BmiService bmiService;
    private final UserService userService;  // ← Ajouté pour récupérer l'utilisateur

    /**
     * Calcule l'IMC à partir des données métriques (kg, cm).
     * Supporte à la fois les utilisateurs authentifiés et anonymes.
     *
     * @param request Poids (20-300kg) et taille (100-250cm)
     * @param httpRequest Requête HTTP pour extraire l'IP
     * @return BmiResponse complète avec catégorie, conseils, couleurs
     */
    @PostMapping("/calculate")
    public ResponseEntity<BmiResponse> calculate(
            @Valid @RequestBody BmiRequest request,
            HttpServletRequest httpRequest) {

        String ip = extractClientIp(httpRequest);
        User currentUser = getCurrentUser();  // ← Récupère l'utilisateur connecté (ou null)

        // ✅ Appel avec support utilisateur
        BmiResponse response = bmiService.calculateAndSave(request, ip, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Calcule l'IMC à partir des unités impériales (lbs, ft/in).
     *
     * @param weightLbs Poids en livres
     * @param heightFt Taille en pieds
     * @param heightIn Taille en pouces
     * @param httpRequest Requête HTTP pour extraire l'IP
     * @return BmiResponse complète
     */
    @PostMapping("/calculate/imperial")
    public ResponseEntity<BmiResponse> calculateImperial(
            @RequestParam double weightLbs,
            @RequestParam int heightFt,
            @RequestParam int heightIn,
            HttpServletRequest httpRequest) {

        String ip = extractClientIp(httpRequest);
        User currentUser = getCurrentUser();

        // ✅ Appel avec support utilisateur
        BmiResponse response = bmiService.calculateAndSaveImperial(weightLbs, heightFt, heightIn, ip, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère l'historique des calculs.
     * - Si utilisateur connecté → historique basé sur son compte
     * - Si anonyme → historique basé sur l'adresse IP
     *
     * @param limit Nombre maximum d'entrées (défaut: 10)
     * @param httpRequest Requête HTTP pour extraire l'IP
     * @return Liste des derniers calculs
     */
    @GetMapping("/history")
    public ResponseEntity<List<BmiResponse>> getHistory(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest httpRequest) {

        String ip = extractClientIp(httpRequest);
        User currentUser = getCurrentUser();

        // ✅ Appel avec support utilisateur (priorité à user_id si connecté)
        List<BmiResponse> history = bmiService.getHistory(ip, currentUser, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Récupère les statistiques globales toutes catégories confondues.
     * Accessible même sans authentification.
     *
     * @return Statistiques agrégées par catégorie IMC
     */
    @GetMapping("/stats")
    public ResponseEntity<List<BmiStatsProjection>> getStats() {
        List<BmiStatsProjection> stats = bmiService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Extrait l'adresse IP du client de manière robuste.
     * Gère les proxys, load balancers et IPv6.
     *
     * @param request Requête HTTP
     * @return Adresse IP nettoyée
     */
    private String extractClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }

    /**
     * Récupère l'utilisateur actuellement authentifié.
     * Retourne null si l'utilisateur n'est pas connecté.
     *
     * @return User ou null
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifie si l'utilisateur est authentifié et n'est pas un utilisateur anonyme
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userService.findById(userPrincipal.getId());
        }

        return null;
    }
}