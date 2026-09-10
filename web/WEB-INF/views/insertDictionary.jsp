<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="hery.itu.util.Html" %>
<%
    Map<String, String> huffmanDictionary = (Map<String, String>) request.getAttribute("huffmanDictionary");
    boolean dbError = request.getAttribute("dbError") != null;
    String pageTitle = "Dictionnaire";
    String activeNav = "dictionnaire";

    // Messages issus de la redirection après POST (pas de switch fléché : Jasper compile en Java 11).
    String error = request.getParameter("error");
    String errorMessage = null;
    if ("empty".equals(error)) {
        errorMessage = "Le caractère et le code sont obligatoires.";
    } else if ("not_single_character".equals(error)) {
        errorMessage = "Un seul caractère à la fois.";
    } else if ("invalid_code".equals(error)) {
        errorMessage = "Le code ne doit contenir que des 0 et des 1.";
    } else if ("duplicate_character".equals(error)) {
        errorMessage = "Ce caractère a déjà un code. Videz le dictionnaire pour le redéfinir.";
    } else if ("duplicate_code".equals(error)) {
        errorMessage = "Ce code est déjà attribué à un autre caractère.";
    } else if ("prefix_conflict".equals(error)) {
        errorMessage = "Ce code est le préfixe d'un code existant (ou l'inverse) : le texte ne serait plus décodable.";
    } else if ("database".equals(error)) {
        errorMessage = "Opération refusée par la base de données. Consultez le journal du serveur.";
    } else if ("name_empty".equals(error)) {
        errorMessage = "Donnez un nom au nouveau dictionnaire.";
    } else if ("name_too_long".equals(error)) {
        errorMessage = "Le nom du dictionnaire ne doit pas dépasser 50 caractères.";
    } else if ("name_taken".equals(error)) {
        errorMessage = "Un dictionnaire porte déjà ce nom.";
    } else if (error != null) {
        errorMessage = "Une erreur est survenue.";
    }

    String successMessage = null;
    if (request.getParameter("added") != null) {
        successMessage = "Entrée ajoutée au dictionnaire.";
    } else if (request.getParameter("cleared") != null) {
        successMessage = "Dictionnaire vidé.";
    } else if (request.getParameter("created") != null) {
        successMessage = "Dictionnaire créé : il est maintenant sélectionné.";
    } else if (request.getParameter("deleted") != null) {
        successMessage = "Dictionnaire supprimé.";
    }
    String pickerAction = "insertDictionary";
    hery.itu.base.DictionaryRepository.Dictionary currentDictionary =
            (hery.itu.base.DictionaryRepository.Dictionary) request.getAttribute("currentDictionary");
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="mb-6">
    <p class="label">Parcours manuel</p>
    <h1 class="mt-2 text-3xl font-semibold tracking-tight">Dictionnaire de codes</h1>
    <p class="mt-3 text-muted max-w-prose">
        Associez à chaque caractère un code binaire. Pour que le texte codé soit décodable,
        aucun code ne doit être le préfixe d'un autre — l'application le vérifie à l'ajout.
    </p>
</div>

<%-- Choix du dictionnaire courant, création, suppression --%>
<% if (currentDictionary != null) { %>
<div class="card px-5 py-4 mb-6 flex flex-wrap items-center justify-between gap-4">
    <div class="flex flex-wrap items-center gap-4">
        <%@ include file="/WEB-INF/jspf/dictionaryPicker.jspf" %>
        <form action="<%= ctx %>/dictionaries" method="post" class="flex items-center gap-2">
            <input type="hidden" name="action" value="create">
            <input type="text" name="name" maxlength="50" required placeholder="Nouveau dictionnaire"
                   class="field w-56 h-9 py-1 text-sm" autocomplete="off">
            <button type="submit" class="btn btn-secondary h-9 px-3 text-xs">Créer</button>
        </form>
    </div>
    <form action="<%= ctx %>/dictionaries" method="post"
          onsubmit="return confirm('Supprimer le dictionnaire « <%= Html.esc(currentDictionary.name()).replace("'", "\\'") %> » et toutes ses entrées ?');">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="d" value="<%= currentDictionary.id() %>">
        <button type="submit" class="btn btn-danger h-9 px-3 text-xs">Supprimer ce dictionnaire</button>
    </form>
</div>
<% } %>

<% if (dbError) { %>
<div role="alert" class="mb-6 rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm">
    <p class="font-medium">Base de données injoignable.</p>
    <p class="mt-1">Vérifiez que PostgreSQL est démarré et que la configuration <code class="font-mono">HUFFMAN_DB_*</code> est renseignée (voir le README).</p>
</div>
<% } %>

<% if (errorMessage != null) { %>
<div role="alert" class="mb-6 rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm flex items-center gap-3">
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true" class="shrink-0">
        <circle cx="9" cy="9" r="7.25" stroke="currentColor" stroke-width="1.5"/>
        <path d="M9 5.5v4.2M9 12.5v.2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
    </svg>
    <span><%= Html.esc(errorMessage) %></span>
</div>
<% } else if (successMessage != null) { %>
<div role="status" class="mb-6 rounded-lg border border-ok/30 bg-ok-soft text-ok px-4 py-3 text-sm flex items-center gap-3">
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true" class="shrink-0">
        <circle cx="9" cy="9" r="7.25" stroke="currentColor" stroke-width="1.5"/>
        <path d="M5.5 9.5 8 12l4.5-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
    <span><%= Html.esc(successMessage) %></span>
</div>
<% } %>

<section class="grid grid-cols-1 lg:grid-cols-[minmax(0,2fr)_minmax(0,3fr)] gap-8 items-start">
    <div>
        <form action="<%= ctx %>/insertDictionary" method="post" class="card p-6">
            <% if (currentDictionary != null) { %><input type="hidden" name="d" value="<%= currentDictionary.id() %>"><% } %>
            <label for="character" class="label">Caractère</label>
            <input id="character" name="character" type="text" maxlength="2" required autocomplete="off"
                   class="field mt-2 font-mono text-lg" placeholder="a">
            <p class="mt-1 text-xs text-muted">Un seul caractère, espace compris.</p>

            <label for="huffmanCode" class="label block mt-5">Code binaire</label>
            <input id="huffmanCode" name="huffmanCode" type="text" required autocomplete="off"
                   pattern="[01]+" inputmode="numeric" title="Uniquement des 0 et des 1"
                   class="field mt-2 font-mono text-lg tracking-[0.12em]" placeholder="0101">

            <button type="submit" class="btn btn-primary w-full mt-6" <%= dbError ? "disabled" : "" %>>Ajouter au dictionnaire</button>
        </form>

        <form action="<%= ctx %>/clearDictionary" method="post" class="mt-4"
              onsubmit="return confirm('Vider tout le dictionnaire ? Cette action est irréversible.');">
            <% if (currentDictionary != null) { %><input type="hidden" name="d" value="<%= currentDictionary.id() %>"><% } %>
            <button type="submit" class="btn btn-danger w-full" <%= dbError ? "disabled" : "" %>>Vider le dictionnaire</button>
        </form>
    </div>

    <div>
        <div class="flex items-baseline justify-between mb-3">
            <h2 class="label">Entrées <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= huffmanDictionary.size() %></span><% if (currentDictionary != null) { %> <span class="normal-case tracking-normal text-muted">· <%= Html.esc(currentDictionary.name()) %></span><% } %></h2>
            <a href="<%= ctx %>/encodeText" class="text-sm">Coder un texte avec ce dictionnaire →</a>
        </div>
        <%@ include file="/WEB-INF/jspf/dictionaryTable.jspf" %>
        <% if (!huffmanDictionary.isEmpty()) {
               double kraft = hery.itu.huffman.CodeStatistics.kraftSum(huffmanDictionary.values());
               boolean complete = Math.abs(kraft - 1.0) < 1e-9; %>
        <p class="mt-3 text-sm text-muted">
            Somme de Kraft Σ 2<sup>−ℓ</sup> = <span class="font-mono text-ink"><%= String.format(java.util.Locale.FRANCE, "%.3f", kraft) %></span> —
            <% if (complete) { %>
                code complet : tout nouveau code serait le préfixe d'un code existant, ou l'inverse.
            <% } else { %>
                il reste <span class="font-mono text-ink"><%= String.format(java.util.Locale.FRANCE, "%.3f", 1.0 - kraft) %></span> de place : d'autres codes peuvent être ajoutés sans ambiguïté.
            <% } %>
        </p>
        <% } %>
    </div>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
