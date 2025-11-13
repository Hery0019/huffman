<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="hery.itu.*" %>

<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>

<% 
    String originalText = (String) request.getAttribute("originalText");
    String encodedText = (String) request.getAttribute("encodedText");
    String decodedText = (String) request.getAttribute("decodedText");
    Map<Character, String> huffmanCodes = (Map<Character, String>) request.getAttribute("huffmanCodes");
    Map<Character, Integer> frequencyMap = (Map<Character, Integer>) request.getAttribute("frequencyMap");
    String huffmanTreeJson = (String) request.getAttribute("huffmanTreeJson");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Résultat Huffman</title>
    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- D3.js -->
    <script src="https://d3js.org/d3.v7.min.js"></script>
    <style>
        body { background-color: #f8f9fa; }
        .container { margin-top: 30px; }
        table { margin-top: 20px; }
        .table th, .table td { text-align: center; }
        .download-btn { margin-top: 20px; }
        .tree-container { margin-top: 30px; }
        .node circle { fill: #007bff; stroke: black; stroke-width: 2px; }
        .node text { font-size: 12px; font-weight: bold; }
        .link { fill: none; stroke: #ccc; stroke-width: 2px; }
    </style>
</head>
<body>

<div class="container">
    <h2 class="text-center">Résultat de l'algorithme de Huffman</h2>

    <div class="card mt-4">
        <div class="card-body">
            <h5 class="card-title">Texte d'origine</h5>
            <p class="card-text"><strong><%= originalText %></strong></p>

            <h5 class="card-title mt-3">Texte encodé</h5>
            <p class="card-text"><%= encodedText %></p>

            <h5 class="card-title mt-3">Texte décodé</h5>
            <p class="card-text"><%= decodedText %></p>

            <a href="download?data=<%= encodedText %>" class="btn btn-success download-btn">
                📥 Télécharger le texte compressé
            </a>
        </div>
    </div>

    <h3 class="mt-4">Table des Fréquences et Codes Huffman</h3>
    <table class="table table-bordered table-striped">
        <thead class="table-primary">
            <tr>
                <th>Caractère</th>
                <th>Fréquence</th>
                <th>Code Huffman</th>
            </tr>
        </thead>
        <tbody>
            <% 
                if (huffmanCodes != null && frequencyMap != null) {
                    for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                        Character key = entry.getKey();
                        String code = entry.getValue();
                        Integer freq = frequencyMap.get(key);
            %>
                        <tr>
                            <td><%= key %></td>
                            <td><%= (freq != null ? freq : 0) %></td>
                            <td><%= code %></td>
                        </tr>
            <% 
                    }
                }
            %>
        </tbody>
    </table>

    <h3 class="mt-5 text-center">🌳 Arbre de Huffman</h3>
    <div id="tree-container" class="tree-container text-center"></div>

    <br>
    <div class="text-center">
        <a href="index.jsp" class="btn btn-secondary">⬅ Retour</a>
    </div>
</div>

<!-- Script D3.js pour afficher l'arbre Huffman -->
<script>
    var treeData = <%= huffmanTreeJson != null ? huffmanTreeJson : "{}" %>;

    var width = 800, height = 500;
    var svg = d3.select("#tree-container").append("svg")
                .attr("width", width)
                .attr("height", height)
                .append("g")
                .attr("transform", "translate(40, 20)");

    var tree = d3.tree().size([width - 100, height - 100]);
    var hierarchyData = d3.hierarchy(treeData, function(d) {
        return d.left || d.right ? [d.left, d.right] : null;
    });

    var links = tree(hierarchyData).links();
    var nodes = hierarchyData.descendants();

    // Tracer les liens
    svg.selectAll(".link")
        .data(links)
        .enter()
        .append("line")
        .attr("class", "link")
        .attr("x1", d => d.source.x)
        .attr("y1", d => d.source.y)
        .attr("x2", d => d.source.x)
        .attr("y2", d => d.source.y)
        .transition()
        .duration(800)
        .attr("x2", d => d.target.x)
        .attr("y2", d => d.target.y)
        .style("stroke", "#000")
        .style("stroke-width", "2px");

    // Ajouter les étiquettes '0' et '1' sur les liens
    svg.selectAll(".link-label")
        .data(links)
        .enter()
        .append("text")
        .attr("class", "link-label")
        .attr("x", d => (d.source.x + d.target.x) / 2)
        .attr("y", d => (d.source.y + d.target.y) / 2 - 5)
        .attr("text-anchor", "middle")
        .text(d => (d.source.data.left === d.target.data ? "0" : "1"))
        .style("font-size", "16px")
        .style("font-weight", "bold")
        .style("fill", "red");

    // Ajouter les nœuds
    var node = svg.selectAll(".node")
        .data(nodes)
        .enter()
        .append("g")
        .attr("class", "node")
        .attr("transform", d => "translate(" + d.x + "," + d.y + ")");

    // Ajouter les cercles
    node.append("circle")
        .attr("r", 15)
        .style("fill", "#007bff")
        .style("stroke", "black")
        .style("stroke-width", "2px");

    // Ajouter le texte dans les nœuds
    node.append("text")
        .attr("dy", 5)
        .attr("x", 0)
        .attr("text-anchor", "middle")
        .text(d => d.data.character === '\0' ? "*" : d.data.character)
        .style("font-size", "18px")
        .style("font-weight", "bold")
        .style("fill", "white")
        .style("background", "black");

</script>


<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
