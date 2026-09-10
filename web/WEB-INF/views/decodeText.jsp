<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="hery.itu.huffman.DictionaryDecoder" %>
<%@ page import="hery.itu.util.Html" %>
<%
    Map<String, String> huffmanDictionary = (Map<String, String>) request.getAttribute("huffmanDictionary");
    boolean dbError = request.getAttribute("dbError") != null;
    String pageTitle = "Décoder avec le dictionnaire";
    String activeNav = "decoder";

    String submittedBits = (String) request.getAttribute("submittedBits");
    DictionaryDecoder.Result result = (DictionaryDecoder.Result) request.getAttribute("decodeResult");
    String dictionaryError = (String) request.getAttribute("dictionaryError");
    String pickerAction = "decodeText";
    hery.itu.base.DictionaryRepository.Dictionary currentDictionary =
            (hery.itu.base.DictionaryRepository.Dictionary) request.getAttribute("currentDictionary");
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="mb-8">
    <p class="label">Parcours manuel</p>
    <h1 class="mt-2 text-3xl font-semibold tracking-tight">Décoder une suite de bits avec le dictionnaire</h1>
    <p class="mt-3 text-muted max-w-prose">
        Les bits sont lus de gauche à droite ; dès qu'ils forment un code du dictionnaire, le caractère est émis
        et la lecture repart. C'est possible sans séparateur parce qu'aucun code n'est le préfixe d'un autre.
    </p>
</div>

<% if (dbError) { %>
<div role="alert" class="mb-6 rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm">
    <p class="font-medium">Base de données injoignable.</p>
    <p class="mt-1">Vérifiez que PostgreSQL est démarré et que la configuration <code class="font-mono">HUFFMAN_DB_*</code> est renseignée (voir le README).</p>
</div>
<% } %>

<% if (dictionaryError != null) { %>
<div role="alert" class="mb-6 rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm">
    <p class="font-medium">Dictionnaire inutilisable pour décoder.</p>
    <p class="mt-1"><%= Html.esc(dictionaryError) %>. Videz le dictionnaire et ressaisissez des codes sans préfixe commun.</p>
</div>
<% } %>

<section class="grid grid-cols-1 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] gap-8 items-start">
    <div class="space-y-6">
        <form action="<%= ctx %>/decodeText" method="post" class="card p-6">
            <%@ include file="/WEB-INF/jspf/dictionaryPicker.jspf" %>
            <% if (currentDictionary != null) { %><input type="hidden" name="d" value="<%= currentDictionary.id() %>"><% } %>
            <label for="bits" class="label">Suite de bits</label>
            <textarea id="bits" name="bits" rows="4" required spellcheck="false"
                      class="field mt-2 font-mono text-[15px] leading-relaxed tracking-[0.08em] resize-y"
                      placeholder="0110 1110 10"><%= Html.esc(submittedBits) %></textarea>
            <div class="mt-4 flex items-center justify-between gap-4">
                <% if (huffmanDictionary.isEmpty()) { %>
                    <span class="text-xs text-warn">Le dictionnaire est vide : <a href="<%= ctx %>/insertDictionary" class="text-warn">ajoutez des codes</a> avant de décoder.</span>
                <% } else { %>
                    <span class="text-xs text-muted">Les espaces et retours à la ligne sont ignorés.</span>
                <% } %>
                <button type="submit" class="btn btn-primary" <%= dbError ? "disabled" : "" %>>Décoder</button>
            </div>
        </form>

        <% if (result != null) { %>
        <div class="card p-6">
            <div class="flex flex-wrap items-center justify-between gap-4">
                <h2 class="label">Texte décodé <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= result.symbolCount() %> caractère<%= result.symbolCount() > 1 ? "s" : "" %></span></h2>
                <% if (result.isOk()) { %>
                    <span class="inline-flex items-center gap-1.5 text-xs font-medium text-ok bg-ok-soft rounded-full px-2.5 py-1">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M2.5 6.5 5 9l4.5-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Séquence entièrement décodée
                    </span>
                <% } else { %>
                    <span class="inline-flex items-center gap-1.5 text-xs font-medium text-warn bg-warn-soft rounded-full px-2.5 py-1">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M6 3v3.5M6 8.5v.2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.4"/></svg>
                        Décodage interrompu
                    </span>
                <% } %>
            </div>

            <% if (result.text().isEmpty()) { %>
                <p class="mt-4 text-sm text-muted italic">Aucun caractère décodé.</p>
            <% } else { %>
                <p class="mt-4 font-mono text-[15px] leading-relaxed whitespace-pre-wrap break-words"><%= Html.esc(result.text()) %></p>
            <% } %>

            <% if (!result.isOk()) {
                   int cut = Math.max(0, Math.min(result.errorPosition(), submittedBits.length())); %>
                <p class="mt-4 text-sm text-warn"><%= Html.esc(result.error()) %></p>
                <p class="bits mt-2"><%= Html.bits(submittedBits.substring(0, cut)) %><span class="bg-warn-soft text-warn rounded px-1"><%= Html.esc(submittedBits.substring(cut)) %></span></p>
            <% } else if (!submittedBits.isEmpty()) { %>
                <p class="bits mt-4 text-muted"><%= Html.bits(submittedBits) %></p>
            <% } %>
        </div>
        <% } %>
    </div>

    <div>
        <div class="flex items-baseline justify-between mb-3">
            <h2 class="label">Dictionnaire<% if (currentDictionary != null) { %> <span class="normal-case tracking-normal text-muted">· <%= Html.esc(currentDictionary.name()) %></span><% } %></h2>
            <a href="<%= ctx %>/insertDictionary" class="text-sm">Modifier →</a>
        </div>
        <%@ include file="/WEB-INF/jspf/dictionaryTable.jspf" %>
    </div>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
