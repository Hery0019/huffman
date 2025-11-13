<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="hery.itu.*" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Encodeur Huffman</title>
</head>
<body>
    <h2>Algorithme de Huffman</h2>
    <form action="huffman" method="post">
        <label for="text">Entrez le texte :</label><br>
        <textarea name="text" rows="4" cols="50" required></textarea><br><br>
        <input type="submit" value="Encoder">
    </form>

    <a href="insertDictionary.jsp">Inserer dictionnaire</a>
</body>
</html>
