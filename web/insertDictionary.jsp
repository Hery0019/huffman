<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="hery.itu.util.Html" %>
<%
    Map<String, String> huffmanDictionary = (Map<String, String>) request.getAttribute("huffmanDictionary");
    if (huffmanDictionary == null) {
        // Page ouverte directement : passer par le servlet pour charger le dictionnaire.
%>
<jsp:forward page="/insertDictionary" />
<%
    }
    String pageTitle = "Dictionnaire";
    String activeNav = "dictionnaire";

    String error = request.getParameter("error");
    String errorMessage = null;
    // Pas de switch fléché ici : Jasper compile les JSP en Java 11 par défaut.
    if ("empty".equals(error)) {
        errorMessage = "Le caractère et le code sont obligatoires.";
    } else if ("invalid_bit_string".equals(error)) {
        errorMessage = "Le code ne doit contenir que des 0 et des 1.";
    } else if ("db_connection".equals(error)) {
        errorMessage = "Connexion à la base de données impossible.";
    } else if ("sql".equals(error)) {
        errorMessage = "Insertion refusée par la base : un seul caractère et un code binaire.";
    } else if (error != null) {
        errorMessage = "Une erreur est survenue.";
    }
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="mb-8">
    <p class="label">Parcours manuel</p>
    <h1 class="mt-2 text-3xl font-semibold tracking-tight">Dictionnaire de codes</h1>
    <p class="mt-3 text-muted max-w-prose">
        Associez à chaque caractère un code binaire. Pour que le texte codé soit décodable,
        aucun code ne doit être le préfixe d'un autre.
    </p>
</div>

<% if (errorMessage != null) { %>
<div role="alert" class="mb-6 rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm flex items-center gap-3">
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true" class="shrink-0">
        <circle cx="9" cy="9" r="7.25" stroke="currentColor" stroke-width="1.5"/>
        <path d="M9 5.5v4.2M9 12.5v.2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
    </svg>
    <span><%= Html.esc(errorMessage) %></span>
</div>
<% } %>

<section class="grid grid-cols-1 lg:grid-cols-[minmax(0,2fr)_minmax(0,3fr)] gap-8 items-start">
    <div>
        <form action="<%= ctx %>/insertDictionary" method="post" class="card p-6">
            <label for="character" class="label">Caractère</label>
            <input id="character" name="character" type="text" maxlength="1" required autocomplete="off"
                   class="field mt-2 font-mono text-lg" placeholder="a">

            <label for="huffmanCode" class="label block mt-5">Code binaire</label>
            <input id="huffmanCode" name="huffmanCode" type="text" required autocomplete="off"
                   pattern="[01]+" inputmode="numeric" title="Uniquement des 0 et des 1"
                   class="field mt-2 font-mono text-lg tracking-[0.12em]" placeholder="0101">

            <button type="submit" class="btn btn-primary w-full mt-6">Ajouter au dictionnaire</button>
        </form>

        <form action="<%= ctx %>/clearDictionary" method="post" class="mt-4"
              onsubmit="return confirm('Vider tout le dictionnaire ? Cette action est irréversible.');">
            <button type="submit" class="btn btn-danger w-full">Vider le dictionnaire</button>
        </form>
    </div>

    <div>
        <div class="flex items-baseline justify-between mb-3">
            <h2 class="label">Entrées <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= huffmanDictionary.size() %></span></h2>
            <a href="<%= ctx %>/encodeText" class="text-sm">Coder un texte avec ce dictionnaire →</a>
        </div>
        <%@ include file="/WEB-INF/jspf/dictionaryTable.jspf" %>
    </div>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
