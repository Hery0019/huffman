<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="hery.itu.util.Html" %>
<%
    String originalText = (String) request.getAttribute("originalText");
    String encodedText = (String) request.getAttribute("encodedText");
    String decodedText = (String) request.getAttribute("decodedText");
    Map<Character, String> huffmanCodes = (Map<Character, String>) request.getAttribute("huffmanCodes");
    Map<Character, Integer> frequencyMap = (Map<Character, Integer>) request.getAttribute("frequencyMap");
    String huffmanTreeJson = (String) request.getAttribute("huffmanTreeJson");
    // Vue sous WEB-INF : uniquement atteinte par HuffmanServlet, les attributs sont toujours présents.
    String pageTitle = "Résultat";
    String activeNav = "encoder";

    // Indicateurs (présentation uniquement)
    int charCount = originalText.length();
    int distinctCount = frequencyMap.size();
    int huffmanBits = encodedText.length();
    int fixedBits = charCount * 8;
    double gainPercent = fixedBits == 0 ? 0 : 100.0 * (1.0 - (double) huffmanBits / fixedBits);
    boolean roundTrip = originalText.equals(decodedText);

    // Lignes de la table : par fréquence décroissante, puis par symbole
    List<Map.Entry<Character, Integer>> rows = new ArrayList<>(frequencyMap.entrySet());
    rows.sort((a, b) -> {
        int byFrequency = Integer.compare(b.getValue(), a.getValue());
        return byFrequency != 0 ? byFrequency : Character.compare(a.getKey(), b.getKey());
    });
    Locale fr = Locale.FRANCE;
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<div class="flex flex-wrap items-end justify-between gap-4 mb-8">
    <div>
        <p class="label">Résultat</p>
        <h1 class="mt-2 text-3xl font-semibold tracking-tight">Arbre et codes de Huffman</h1>
    </div>
    <a href="<%= ctx %>/index.jsp" class="btn btn-secondary">Encoder un autre texte</a>
</div>

<%-- Indicateurs --%>
<section class="grid grid-cols-2 lg:grid-cols-4 gap-4">
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Caractères</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%,d", charCount) %></p>
    </div>
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Symboles distincts</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%,d", distinctCount) %></p>
    </div>
    <div class="card px-5 py-4">
        <p class="text-sm text-muted">Bits encodés</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%,d", huffmanBits) %></p>
        <p class="text-xs text-muted mt-1">contre <%= String.format(fr, "%,d", fixedBits) %> à 8 bits par caractère</p>
    </div>
    <div class="card px-5 py-4 bg-ink text-paper border-ink">
        <p class="text-sm text-paper/70">Gain de place</p>
        <p class="mt-1 text-2xl font-semibold"><%= String.format(fr, "%.1f", gainPercent) %> %</p>
        <p class="text-xs text-paper/70 mt-1">par rapport à un codage fixe</p>
    </div>
</section>

<%-- Textes --%>
<section class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
    <div class="card p-6">
        <h2 class="label">Texte d'origine</h2>
        <p class="mt-3 font-mono text-[15px] leading-relaxed whitespace-pre-wrap break-words max-h-56 overflow-auto"><%= Html.esc(originalText) %></p>
    </div>
    <div class="card p-6">
        <div class="flex items-center justify-between gap-4">
            <h2 class="label">Texte décodé</h2>
            <% if (roundTrip) { %>
                <span class="inline-flex items-center gap-1.5 text-xs font-medium text-ok bg-ok-soft rounded-full px-2.5 py-1">
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M2.5 6.5 5 9l4.5-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    Identique à l'origine
                </span>
            <% } else { %>
                <span class="inline-flex items-center gap-1.5 text-xs font-medium text-warn bg-warn-soft rounded-full px-2.5 py-1">
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M6 3v3.5M6 8.5v.2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.4"/></svg>
                    Diffère de l'origine
                </span>
            <% } %>
        </div>
        <p class="mt-3 font-mono text-[15px] leading-relaxed whitespace-pre-wrap break-words max-h-56 overflow-auto"><%= Html.esc(decodedText) %></p>
    </div>
</section>

<section class="card p-6 mt-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
        <h2 class="label">Texte encodé <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= String.format(fr, "%,d", huffmanBits) %> bits</span></h2>
        <div class="flex gap-2">
            <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(encodedText) %>" onclick="copyEncoded(this)">Copier</button>
            <button type="button" class="btn btn-secondary h-9 px-3 text-xs" data-encoded="<%= Html.esc(encodedText) %>" data-filename="huffman_encoded.txt" onclick="downloadEncoded(this)">Télécharger .txt</button>
        </div>
    </div>
    <p class="bits mt-4"><%= Html.bits(encodedText) %></p>
</section>

<%-- Table des symboles --%>
<section class="mt-10">
    <div class="flex items-baseline justify-between mb-3">
        <h2 class="label">Fréquences et codes</h2>
        <span class="text-xs text-muted">Triés par fréquence décroissante</span>
    </div>
    <div class="card overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="border-b border-line">
                    <th class="label text-left px-5 py-3 font-medium">Symbole</th>
                    <th class="label text-right px-5 py-3 font-medium">Fréquence</th>
                    <th class="label text-left px-5 py-3 font-medium">Code</th>
                    <th class="label text-right px-5 py-3 font-medium">Bits</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-line">
            <% for (Map.Entry<Character, Integer> row : rows) {
                   Character symbol = row.getKey();
                   String code = huffmanCodes.get(symbol); %>
                <tr>
                    <td class="px-5 py-2.5">
                        <span class="inline-flex items-center justify-center min-w-[2rem] h-8 px-2 rounded-md bg-ink text-paper font-mono font-medium"><%= Html.symbol(symbol) %></span>
                    </td>
                    <td class="px-5 py-2.5 text-right font-mono tabular-nums"><%= row.getValue() %></td>
                    <td class="px-5 py-2.5 code"><%= Html.bits(code) %></td>
                    <td class="px-5 py-2.5 text-right font-mono text-muted tabular-nums"><%= code == null ? 0 : code.length() %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</section>

<%-- Arbre --%>
<section class="mt-10">
    <div class="flex items-baseline justify-between mb-3">
        <h2 class="label">Arbre de Huffman</h2>
        <span class="text-xs text-muted font-mono"><span class="bit-0">0</span> gauche · <span class="bit-1">1</span> droite · les nœuds internes portent la fréquence cumulée</span>
    </div>
    <div class="card p-4 overflow-x-auto">
        <div id="tree" class="min-h-[160px]"></div>
    </div>
</section>

<script src="https://d3js.org/d3.v7.min.js"></script>
<script>
    (function () {
        var treeData = <%= huffmanTreeJson != null ? huffmanTreeJson : "null" %>;
        var container = document.getElementById('tree');
        if (!treeData) {
            container.innerHTML = '<p class="text-sm text-muted p-4">Aucun arbre à afficher.</p>';
            return;
        }

        var ZERO = '#2E6FA3', ONE = '#B9532B', INK = '#1C1B18', LINE = '#E2DDD2', MUTED = '#6E6A61', SURFACE = '#FFFFFF';
        var SYMBOLS = { ' ': '␣', '\n': '↵', '\r': '␍', '\t': '⇥' };
        function label(ch) { return SYMBOLS[ch] || ch; }
        function isLeftChild(link) { return link.source.data.left === link.target.data; }
        function codeOf(node) {
            var path = node.ancestors().reverse(), bits = '';
            for (var i = 1; i < path.length; i++) bits += (path[i - 1].data.left === path[i].data) ? '0' : '1';
            return bits;
        }

        var root = d3.hierarchy(treeData, function (d) {
            return (d.left || d.right) ? [d.left, d.right].filter(Boolean) : null;
        });
        var leaves = root.leaves().length;
        var levels = root.height + 1;
        var margin = { top: 32, right: 32, bottom: 40, left: 32 };
        var width = Math.max(container.clientWidth, leaves * 60 + margin.left + margin.right);
        var height = levels * 88 + margin.top + margin.bottom;

        d3.tree().size([width - margin.left - margin.right, height - margin.top - margin.bottom])(root);

        var svg = d3.select(container).append('svg')
            .attr('width', width).attr('height', height)
            .attr('viewBox', [0, 0, width, height])
            .attr('role', 'img')
            .attr('aria-label', 'Arbre de Huffman : ' + leaves + ' feuilles, profondeur ' + root.height);
        var g = svg.append('g').attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

        var links = root.links();

        g.selectAll('path.link').data(links).join('path')
            .attr('d', function (d) { return 'M' + d.source.x + ',' + d.source.y + 'L' + d.target.x + ',' + d.target.y; })
            .attr('fill', 'none')
            .attr('stroke', function (d) { return isLeftChild(d) ? ZERO : ONE; })
            .attr('stroke-width', 2)
            .attr('stroke-linecap', 'round');

        var edgeLabel = g.selectAll('g.edge').data(links).join('g')
            .attr('transform', function (d) {
                return 'translate(' + (d.source.x + d.target.x) / 2 + ',' + (d.source.y + d.target.y) / 2 + ')';
            });
        edgeLabel.append('circle').attr('r', 10).attr('fill', SURFACE)
            .attr('stroke', function (d) { return isLeftChild(d) ? ZERO : ONE; }).attr('stroke-width', 1.5);
        edgeLabel.append('text').attr('dy', 4).attr('text-anchor', 'middle')
            .attr('font-family', '"IBM Plex Mono", ui-monospace, monospace').attr('font-size', 12).attr('font-weight', 600)
            .attr('fill', function (d) { return isLeftChild(d) ? ZERO : ONE; })
            .text(function (d) { return isLeftChild(d) ? '0' : '1'; });

        var node = g.selectAll('g.node').data(root.descendants()).join('g')
            .attr('transform', function (d) { return 'translate(' + d.x + ',' + d.y + ')'; });

        var internal = node.filter(function (d) { return d.children; });
        internal.append('circle').attr('r', 17).attr('fill', SURFACE).attr('stroke', INK).attr('stroke-width', 1.5);
        internal.append('text').attr('dy', 4).attr('text-anchor', 'middle')
            .attr('font-family', '"IBM Plex Mono", ui-monospace, monospace').attr('font-size', 11).attr('fill', INK)
            .text(function (d) { return d.data.frequency; });
        internal.append('title').text(function (d) { return 'Fréquence cumulée : ' + d.data.frequency; });

        var leaf = node.filter(function (d) { return !d.children; });
        leaf.append('rect').attr('x', -19).attr('y', -16).attr('width', 38).attr('height', 32).attr('rx', 8).attr('fill', INK);
        leaf.append('text').attr('dy', 5).attr('text-anchor', 'middle')
            .attr('font-family', '"IBM Plex Mono", ui-monospace, monospace').attr('font-size', 14).attr('font-weight', 500).attr('fill', '#F6F3EC')
            .text(function (d) { return label(d.data.character); });
        leaf.append('text').attr('dy', 30).attr('text-anchor', 'middle')
            .attr('font-family', '"IBM Plex Mono", ui-monospace, monospace').attr('font-size', 10).attr('fill', MUTED)
            .text(function (d) { return d.data.frequency; });
        leaf.append('title').text(function (d) {
            return 'Symbole « ' + label(d.data.character) + ' » · fréquence ' + d.data.frequency + ' · code ' + (codeOf(d) || '0');
        });
    })();
</script>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
