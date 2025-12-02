<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String pageTitle = "Encoder un texte";
    String activeNav = "encoder";
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<section class="grid grid-cols-1 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] gap-10 items-start">
    <div>
        <p class="label">Compression sans perte</p>
        <h1 class="mt-2 text-3xl md:text-4xl font-semibold tracking-tight leading-tight">
            Construire l'arbre de Huffman d'un texte
        </h1>
        <p class="mt-4 text-muted leading-relaxed max-w-prose">
            Les symboles fréquents reçoivent les codes les plus courts, les symboles rares les plus longs.
            Saisissez un texte : l'application calcule les fréquences, construit l'arbre, dérive les codes,
            puis encode et décode le texte pour vérifier l'aller-retour.
        </p>

        <form action="<%= ctx %>/huffman" method="post" class="mt-8 card p-6">
            <label for="text" class="label">Texte à encoder</label>
            <textarea id="text" name="text" rows="6" required autofocus
                      class="field mt-2 font-mono text-[15px] leading-relaxed resize-y"
                      placeholder="hello huffman"></textarea>
            <div class="mt-4 flex items-center justify-between gap-4">
                <span class="text-xs text-muted">Chaque caractère compte, espaces compris.</span>
                <button type="submit" class="btn btn-primary">Construire l'arbre et encoder</button>
            </div>
        </form>
    </div>

    <aside class="lg:pt-12">
        <ol class="space-y-5">
            <li class="flex gap-4">
                <span class="shrink-0 w-8 h-8 rounded-full bg-ink text-paper font-mono text-sm inline-flex items-center justify-center">1</span>
                <div>
                    <p class="font-medium">Fréquences</p>
                    <p class="text-sm text-muted mt-0.5">Compter le nombre d'occurrences de chaque symbole.</p>
                </div>
            </li>
            <li class="flex gap-4">
                <span class="shrink-0 w-8 h-8 rounded-full bg-ink text-paper font-mono text-sm inline-flex items-center justify-center">2</span>
                <div>
                    <p class="font-medium">Arbre</p>
                    <p class="text-sm text-muted mt-0.5">Fusionner à chaque étape les deux nœuds les moins fréquents.</p>
                </div>
            </li>
            <li class="flex gap-4">
                <span class="shrink-0 w-8 h-8 rounded-full bg-ink text-paper font-mono text-sm inline-flex items-center justify-center">3</span>
                <div>
                    <p class="font-medium">Codes</p>
                    <p class="text-sm text-muted mt-0.5">
                        Lire le chemin de la racine à chaque feuille :
                        <span class="font-mono"><span class="bit-0">0</span> à gauche, <span class="bit-1">1</span> à droite</span>.
                    </p>
                </div>
            </li>
        </ol>
        <p class="mt-8 text-sm text-muted border-t border-line pt-5">
            Vous pouvez aussi <a href="<%= ctx %>/insertDictionary">définir un dictionnaire à la main</a>
            et <a href="<%= ctx %>/encodeText">coder un texte avec celui-ci</a>.
        </p>
    </aside>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
