<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Locale" %>
<%@ page import="hery.itu.util.Html" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String text = (String) request.getAttribute("decompressedText");
    String fileName = (String) request.getAttribute("fileName");
    Integer fileBytes = (Integer) request.getAttribute("fileBytes");
    Integer textBytes = (Integer) request.getAttribute("textBytes");
    String pageTitle = "Fichier décompressé";
    String activeNav = "encoder";
    Locale fr = Locale.FRANCE;
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="flex flex-wrap items-end justify-between gap-4 mb-8">
    <div>
        <p class="label">Décompression</p>
        <h1 class="mt-2 text-3xl font-semibold tracking-tight">Fichier <span class="font-mono">.huf</span> décompressé</h1>
    </div>
    <a href="<%= ctx %>/index.jsp" class="btn btn-secondary">Retour à l'accueil</a>
</div>

<% if (errorMessage != null) { %>
<div role="alert" class="rounded-lg border border-warn/30 bg-warn-soft text-warn px-4 py-3 text-sm flex items-center gap-3">
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true" class="shrink-0">
        <circle cx="9" cy="9" r="7.25" stroke="currentColor" stroke-width="1.5"/>
        <path d="M9 5.5v4.2M9 12.5v.2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
    </svg>
    <span><%= Html.esc(errorMessage) %></span>
</div>
<p class="mt-6 text-sm text-muted">Un fichier <span class="font-mono">.huf</span> s'obtient depuis la page de résultat, bouton « Télécharger .huf ».</p>
<% } else {
       double gain = textBytes == 0 ? 0 : 100.0 * (1.0 - (double) fileBytes / textBytes); %>

<section class="grid grid-cols-2 lg:grid-cols-4 gap-4">
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Fichier</p>
        <p class="mt-1 text-lg font-semibold font-mono truncate" title="<%= Html.esc(fileName) %>"><%= Html.esc(fileName) %></p>
    </div>
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Taille compressée</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%,d", fileBytes) %> <span class="text-base font-normal text-muted">octets</span></p>
    </div>
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Texte restitué</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%,d", textBytes) %> <span class="text-base font-normal text-muted">octets UTF-8</span></p>
        <p class="text-xs text-muted mt-1"><%= String.format(fr, "%,d", text.length()) %> caractères</p>
    </div>
    <div class="card px-5 py-4 bg-ink text-paper border-ink">
        <p class="text-sm text-paper/70">Gain réel</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%.1f", gain) %> %</p>
        <p class="text-xs text-paper/70 mt-1">en-tête et arbre compris</p>
    </div>
</section>

<section class="card p-6 mt-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
        <h2 class="label">Texte</h2>
        <div class="flex gap-2">
            <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(text) %>" onclick="copyEncoded(this)">Copier</button>
            <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(text) %>" data-filename="texte.txt" onclick="downloadEncoded(this)">Télécharger .txt</button>
        </div>
    </div>
    <p class="mt-4 font-mono text-[15px] leading-relaxed whitespace-pre-wrap break-words max-h-96 overflow-auto"><%= Html.esc(text) %></p>
</section>

<form action="<%= ctx %>/huffman" method="post" class="mt-6">
    <%-- Le saut de ligne après la balise ouvrante est ignoré par le navigateur : il protège un texte commençant par un retour à la ligne. --%>
    <textarea name="text" hidden aria-hidden="true">
<%= Html.esc(text) %></textarea>
    <button type="submit" class="btn btn-primary">Analyser ce texte : arbre, codes et étapes</button>
</form>
<% } %>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
