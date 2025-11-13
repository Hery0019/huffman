<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ page import="hery.itu.*" %> 
<%@ page import="java.util.Map" %>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Insertion Dictionnaire Huffman</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<div class="container mt-5">
    <h2 class="text-center">Insertion Dictionnaire Huffman</h2>

    <!-- Formulaire pour l'insertion du dictionnaire -->
    <form action="insertDictionary" method="POST">
        <div class="mb-3">
            <label for="character" class="form-label">Caractère</label>
            <input type="text" class="form-control" id="character" name="character" required>
        </div>

        <div class="mb-3">
            <label for="huffmanCode" class="form-label">Code Huffman</label>
            <input type="text" class="form-control" id="huffmanCode" name="huffmanCode" required>
        </div>

        <button type="submit" class="btn btn-primary">Ajouter au Dictionnaire</button>
    </form>
    <!-- Bouton pour vider le dictionnaire -->
    <form action="clearDictionary" method="POST">
        <button type="submit" class="btn btn-danger mt-3">Vider Dictionnaire</button>
    </form>


    <h3 class="mt-5">Dictionnaire Huffman</h3>
    <table class="table table-bordered">
        <thead>
            <tr>
                <th>Caractère</th>
                <th>Code Huffman</th>
            </tr>
        </thead>
        <tbody>
            <%
                // Récupérer le dictionnaire à partir de l'attribut du servlet
                Map<String, String> huffmanDictionary = (Map<String, String>) request.getAttribute("huffmanDictionary");
                if (huffmanDictionary != null) {
                    for (Map.Entry<String, String> entry : huffmanDictionary.entrySet()) {
            %>
                        <tr>
                            <td><%= entry.getKey() %></td>
                            <td><%= entry.getValue() %></td>
                        </tr>
            <%
                    }
                }
            %>
        </tbody>
    </table>

    <!-- Bouton pour rediriger vers la page de codage -->
    <a href="encodeText.jsp" class="btn btn-success mt-3">Aller à la page de codage</a>

    <a href="index.jsp" class="btn btn-secondary mt-3">⬅ Retour</a>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
