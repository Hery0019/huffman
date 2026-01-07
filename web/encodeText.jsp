<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="hery.itu.util.Html" %>
<%
    Map<String, String> huffmanDictionary = (Map<String, String>) request.getAttribute("huffmanDictionary");
    if (huffmanDictionary == null) {
        // Page ouverte directement : passer par le servlet pour charger le dictionnaire.
%>
<jsp:forward page="/encodeText" />
<%
    }
    String pageTitle = "Coder avec le dictionnaire";
    String activeNav = "coder";

    String encodedText = (String) request.getAttribute("encodedText");
    String submittedText = request.getParameter("text");

    // Symboles du texte absents du dictionnaire (présentation uniquement : le servlet les remplace par « ? »).
    TreeSet<String> missingSymbols = new TreeSet<>();
    if (encodedText != null && submittedText != null) {
        for (char c : submittedText.toCharArray()) {
            String symbol = String.valueOf(c);
            if (!huffmanDictionary.containsKey(symbol)) missingSymbols.add(symbol);
        }
    }
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="mb-8">
    <p class="label">Parcours manuel</p>
    <h1 class="mt-2 text-3xl font-semibold tracking-tight">Coder un texte avec le dictionnaire</h1>
    <p class="mt-3 text-muted max-w-prose">
        Chaque caractère du texte est remplacé par son code. Un caractère absent du dictionnaire
        est signalé par <span class="bit-missing">?</span>.
    </p>
</div>

<section class="grid grid-cols-1 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] gap-8 items-start">
    <div class="space-y-6">
        <form action="<%= ctx %>/encodeText" method="post" class="card p-6">
            <label for="text" class="label">Texte à coder</label>
            <textarea id="text" name="text" rows="5" required
                      class="field mt-2 font-mono text-[15px] leading-relaxed resize-y"
                      placeholder="hello"><%= Html.esc(submittedText) %></textarea>
            <div class="mt-4 flex items-center justify-between gap-4">
                <% if (huffmanDictionary.isEmpty()) { %>
                    <span class="text-xs text-warn">Le dictionnaire est vide : <a href="<%= ctx %>/insertDictionary" class="text-warn">ajoutez des codes</a> avant de coder.</span>
                <% } else { %>
                    <span class="text-xs text-muted"><%= huffmanDictionary.size() %> codes disponibles.</span>
                <% } %>
                <button type="submit" class="btn btn-primary">Coder le texte</button>
            </div>
        </form>

        <% if (encodedText != null) { %>
        <div class="card p-6">
            <div class="flex items-baseline justify-between gap-4">
                <h2 class="label">Texte codé <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= encodedText.length() %> bits</span></h2>
                <div class="flex gap-2">
                    <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(encodedText) %>" onclick="copyEncoded(this)">Copier</button>
                    <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(encodedText) %>" data-filename="huffman_dictionnaire.txt" onclick="downloadEncoded(this)">Télécharger .txt</button>
                </div>
            </div>
            <p class="bits mt-4"><%= Html.bits(encodedText) %></p>

            <% if (!missingSymbols.isEmpty()) { %>
            <div class="mt-5 pt-4 border-t border-line text-sm text-warn flex flex-wrap items-center gap-2">
                <span>Absent<%= missingSymbols.size() > 1 ? "s" : "" %> du dictionnaire :</span>
                <% for (String symbol : missingSymbols) { %>
                    <span class="inline-flex items-center justify-center min-w-[1.75rem] h-7 px-2 rounded-md bg-warn-soft text-warn font-mono font-medium"><%= Html.symbol(symbol) %></span>
                <% } %>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>

    <div>
        <div class="flex items-baseline justify-between mb-3">
            <h2 class="label">Dictionnaire</h2>
            <a href="<%= ctx %>/insertDictionary" class="text-sm">Modifier →</a>
        </div>
        <%@ include file="/WEB-INF/jspf/dictionaryTable.jspf" %>
    </div>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
