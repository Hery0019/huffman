<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Codage Huffman</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<div class="container mt-5">
    <h2 class="text-center">Codage Huffman</h2>

    <!-- Formulaire pour entrer le texte à coder -->
    <form action="encodeText" method="POST">
        <div class="mb-3">
            <label for="text" class="form-label">Texte à coder</label>
            <textarea class="form-control" id="text" name="text" rows="4" required></textarea>
        </div>

        <button type="submit" class="btn btn-primary">Coder le texte</button>
    </form>

    <%
        // Récupérer le texte codé à partir de la requête (si disponible)
        String encodedText = (String) request.getAttribute("encodedText");
        if (encodedText != null) {
    %>
        <h3 class="mt-5">Texte Codé</h3>
        <p><strong><%= encodedText %></strong></p>
    <%
        }
    %>

    <a href="index.jsp" class="btn btn-secondary mt-3">⬅ Retour</a>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
