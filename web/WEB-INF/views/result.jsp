<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="hery.itu.util.Html" %>
<%
    String originalText = (String) request.getAttribute("originalText");
    String encodedText = (String) request.getAttribute("encodedText");
    String decodedText = (String) request.getAttribute("decodedText");
    Map<Character, String> huffmanCodes = (Map<Character, String>) request.getAttribute("huffmanCodes");
    Map<Character, Integer> frequencyMap = (Map<Character, Integer>) request.getAttribute("frequencyMap");
    String huffmanTreeTraceJson = (String) request.getAttribute("huffmanTreeTraceJson");
    int textUtf8Bytes = (Integer) request.getAttribute("textUtf8Bytes");
    int fileBytes = (Integer) request.getAttribute("fileBytes");
    int fileTreeBits = (Integer) request.getAttribute("fileTreeBits");
    int fileDataBits = (Integer) request.getAttribute("fileDataBits");
    // Vue sous WEB-INF : uniquement atteinte par HuffmanServlet, les attributs sont toujours présents.
    String pageTitle = "Résultat";
    String activeNav = "encoder";
    double fileGainPercent = textUtf8Bytes == 0 ? 0 : 100.0 * (1.0 - (double) fileBytes / textUtf8Bytes);

    // Indicateurs (présentation uniquement)
    int charCount = originalText.length();
    int distinctCount = frequencyMap.size();
    int huffmanBits = encodedText.length();
    int fixedBits = charCount * 8;
    double gainPercent = fixedBits == 0 ? 0 : 100.0 * (1.0 - (double) huffmanBits / fixedBits);
    boolean roundTrip = originalText.equals(decodedText);
    int mergeCount = Math.max(0, distinctCount - 1);

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

<%-- Fichier compressé réel --%>
<section class="card p-6 mt-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h2 class="label">Fichier compressé <span class="font-mono normal-case tracking-normal text-ink ml-1">.huf</span></h2>
            <p class="text-sm text-muted mt-1">
                La suite de bits ci-dessus est affichée avec un caractère par bit. Dans un vrai fichier, les bits sont
                tassés huit par octet, précédés de l'arbre (nécessaire pour décoder) et d'un en-tête.
            </p>
        </div>
        <form action="<%= ctx %>/compress" method="post">
            <%-- Le saut de ligne après la balise ouvrante est ignoré par le navigateur : il protège un texte commençant par un retour à la ligne. --%>
            <textarea name="text" hidden aria-hidden="true">
<%= Html.esc(originalText) %></textarea>
            <button type="submit" class="btn btn-primary h-9 px-3 text-xs">Télécharger .huf</button>
        </form>
    </div>
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
        <div class="rounded-lg bg-soft px-4 py-3">
            <p class="text-xs text-muted">Texte en UTF-8</p>
            <p class="mt-1 text-xl font-semibold"><%= String.format(fr, "%,d", textUtf8Bytes) %> <span class="text-sm font-normal text-muted">octets</span></p>
        </div>
        <div class="rounded-lg bg-soft px-4 py-3">
            <p class="text-xs text-muted">Fichier .huf</p>
            <p class="mt-1 text-xl font-semibold"><%= String.format(fr, "%,d", fileBytes) %> <span class="text-sm font-normal text-muted">octets</span></p>
        </div>
        <div class="rounded-lg bg-soft px-4 py-3">
            <p class="text-xs text-muted">Dont en-tête et arbre</p>
            <p class="mt-1 text-xl font-semibold"><%= String.format(fr, "%,d", 8 * hery.itu.huffman.HuffmanFile.HEADER_BYTES + fileTreeBits) %> <span class="text-sm font-normal text-muted">bits</span></p>
            <p class="text-xs text-muted mt-0.5">données : <%= String.format(fr, "%,d", fileDataBits) %> bits</p>
        </div>
        <div class="rounded-lg bg-ink text-paper px-4 py-3">
            <p class="text-xs text-paper/70">Gain réel</p>
            <p class="mt-1 text-xl font-semibold"><%= String.format(fr, "%.1f", fileGainPercent) %> %</p>
            <p class="text-xs text-paper/70 mt-0.5"><%= fileGainPercent < 0 ? "le fichier est plus gros que le texte : trop court pour amortir l'arbre" : "par rapport au texte UTF-8" %></p>
        </div>
    </div>
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

<%-- Arbre : construction pas à pas --%>
<section class="mt-10">
    <div class="flex flex-wrap items-baseline justify-between gap-3 mb-3">
        <h2 class="label">Arbre de Huffman — construction pas à pas</h2>
        <span class="text-xs text-muted font-mono"><span class="bit-0">0</span> gauche · <span class="bit-1">1</span> droite · nœud interne = fréquence cumulée</span>
    </div>
    <div class="card p-4">
        <% if (mergeCount > 0) { %>
        <div class="flex flex-wrap items-center gap-2 mb-3">
            <button type="button" id="tree-first" class="btn btn-secondary h-9 px-3 text-xs">Début</button>
            <button type="button" id="tree-prev" class="btn btn-secondary h-9 px-3 text-xs">Précédent</button>
            <input type="range" id="tree-step" min="0" max="<%= mergeCount %>" value="<%= mergeCount %>" step="1"
                   class="flex-1 min-w-[160px] accent-ink" aria-label="Étape de construction">
            <button type="button" id="tree-next" class="btn btn-secondary h-9 px-3 text-xs">Suivant</button>
            <button type="button" id="tree-last" class="btn btn-secondary h-9 px-3 text-xs">Fin</button>
        </div>
        <% } %>
        <p id="tree-caption" class="text-sm text-muted mb-3 min-h-[1.5rem]"></p>
        <div id="tree" class="overflow-x-auto min-h-[160px]"></div>
    </div>
</section>

<script src="<%= ctx %>/assets/d3-7.9.0.min.js"></script>
<script>
    (function () {
        var trace = <%= huffmanTreeTraceJson != null ? huffmanTreeTraceJson : "null" %>;
        var container = document.getElementById('tree');
        var caption = document.getElementById('tree-caption');
        if (!trace || !trace.nodes || !trace.nodes.length) {
            container.innerHTML = '<p class="text-sm text-muted p-4">Aucun arbre à afficher.</p>';
            return;
        }

        var ZERO = '#2E6FA3', ONE = '#B9532B', INK = '#1C1B18', MUTED = '#6E6A61', SURFACE = '#FFFFFF', PAPER = '#F6F3EC';
        var MONO = '"IBM Plex Mono", ui-monospace, monospace';
        var SYMBOLS = { ' ': '␣', '\n': '↵', '\r': '␍', '\t': '⇥' };
        function label(ch) { return SYMBOLS[ch] || ch; }

        var byId = {};
        trace.nodes.forEach(function (n) { byId[n.id] = n; });
        var total = trace.steps.length;

        function describe(node) {
            return node.left == null ? '« ' + label(node.character) + ' » (' + node.frequency + ')' : '∅ (' + node.frequency + ')';
        }
        function children(d) { return d.left == null ? null : [byId[d.left], byId[d.right]]; }
        function isLeftChild(link) { return link.source.data.left === link.target.data.id; }
        function codeOf(node) {
            var path = node.ancestors().reverse(), bits = '';
            for (var i = 1; i < path.length; i++) bits += (path[i - 1].data.left === path[i].data.id) ? '0' : '1';
            return bits;
        }

        /* État de la file après `step` fusions : les racines restantes, triées comme la file de priorité
           (fréquence croissante ; à fréquence égale, la paire qui va fusionner passe devant). */
        function forestAt(step) {
            var alive = {};
            trace.nodes.forEach(function (n) { if (n.left == null) alive[n.id] = true; });
            for (var i = 0; i < step; i++) {
                var s = trace.steps[i];
                delete alive[s.leftId];
                delete alive[s.rightId];
                alive[s.parentId] = true;
            }
            var next = step < total ? trace.steps[step] : null;
            function isNext(n) { return next !== null && (n.id === next.leftId || n.id === next.rightId); }
            var roots = Object.keys(alive).map(function (id) { return byId[+id]; });
            roots.sort(function (a, b) {
                return (a.frequency - b.frequency) || ((isNext(a) ? 0 : 1) - (isNext(b) ? 0 : 1)) || (a.id - b.id);
            });
            return { roots: roots, next: next, isNext: isNext };
        }

        function drawTree(g, root, highlighted) {
            var links = root.links();
            g.selectAll('path.link').data(links).join('path')
                .attr('d', function (d) { return 'M' + d.source.x + ',' + d.source.y + 'L' + d.target.x + ',' + d.target.y; })
                .attr('fill', 'none')
                .attr('stroke', function (d) { return isLeftChild(d) ? ZERO : ONE; })
                .attr('stroke-width', 2).attr('stroke-linecap', 'round');

            var edge = g.selectAll('g.edge').data(links).join('g')
                .attr('transform', function (d) { return 'translate(' + (d.source.x + d.target.x) / 2 + ',' + (d.source.y + d.target.y) / 2 + ')'; });
            edge.append('circle').attr('r', 10).attr('fill', SURFACE)
                .attr('stroke', function (d) { return isLeftChild(d) ? ZERO : ONE; }).attr('stroke-width', 1.5);
            edge.append('text').attr('dy', 4).attr('text-anchor', 'middle')
                .attr('font-family', MONO).attr('font-size', 12).attr('font-weight', 600)
                .attr('fill', function (d) { return isLeftChild(d) ? ZERO : ONE; })
                .text(function (d) { return isLeftChild(d) ? '0' : '1'; });

            var node = g.selectAll('g.node').data(root.descendants()).join('g')
                .attr('transform', function (d) { return 'translate(' + d.x + ',' + d.y + ')'; });

            var internal = node.filter(function (d) { return d.children; });
            internal.append('circle').attr('r', 17).attr('fill', SURFACE).attr('stroke', INK).attr('stroke-width', 1.5);
            internal.append('text').attr('dy', 4).attr('text-anchor', 'middle')
                .attr('font-family', MONO).attr('font-size', 11).attr('fill', INK)
                .text(function (d) { return d.data.frequency; });
            internal.append('title').text(function (d) { return 'Fréquence cumulée : ' + d.data.frequency; });

            var leaf = node.filter(function (d) { return !d.children; });
            leaf.append('rect').attr('x', -19).attr('y', -16).attr('width', 38).attr('height', 32).attr('rx', 8).attr('fill', INK);
            leaf.append('text').attr('dy', 5).attr('text-anchor', 'middle')
                .attr('font-family', MONO).attr('font-size', 14).attr('font-weight', 500).attr('fill', PAPER)
                .text(function (d) { return label(d.data.character); });
            leaf.append('text').attr('dy', 30).attr('text-anchor', 'middle')
                .attr('font-family', MONO).attr('font-size', 10).attr('fill', MUTED)
                .text(function (d) { return d.data.frequency; });
            leaf.append('title').text(function (d) {
                return 'Symbole « ' + label(d.data.character) + ' » · fréquence ' + d.data.frequency + ' · code ' + (codeOf(d) || '0');
            });

            if (highlighted) {
                // Anneau pointillé autour de la racine : c'est elle qui va fusionner à l'étape suivante.
                g.append('circle').attr('cx', root.x).attr('cy', root.y).attr('r', root.children ? 25 : 30)
                    .attr('fill', 'none').attr('stroke', ONE).attr('stroke-width', 2).attr('stroke-dasharray', '5 4');
            }
        }

        function render(step) {
            container.innerHTML = '';
            var forest = forestAt(step);
            var LEVEL = 88, gap = 24, rowGap = 32, margin = { top: 36, right: 32, bottom: 40, left: 32 };
            var trees = forest.roots.map(function (r) {
                var h = d3.hierarchy(r, children);
                // Hauteur utile : profondeur × niveau, plus la feuille la plus basse et son étiquette de fréquence.
                return { root: h, w: Math.max(h.leaves().length * 60, 64), h: h.height * LEVEL + 48 };
            });

            // Les arbres de la forêt se placent en lignes, comme du texte : une ligne pleine passe à la suivante.
            var available = Math.max(container.clientWidth, 480) - margin.left - margin.right;
            var rows = [], row = { trees: [], w: 0, h: 0 };
            trees.forEach(function (t) {
                var extra = (row.trees.length ? gap : 0) + t.w;
                if (row.trees.length && row.w + extra > available) {
                    rows.push(row);
                    row = { trees: [], w: 0, h: 0 };
                    extra = t.w;
                }
                row.trees.push(t);
                row.w += extra;
                row.h = Math.max(row.h, t.h);
            });
            rows.push(row);

            var width = margin.left + margin.right + Math.max.apply(null, rows.map(function (r) { return r.w; }));
            var height = margin.top + margin.bottom + rowGap * (rows.length - 1)
                + rows.reduce(function (sum, r) { return sum + r.h; }, 0);
            // Un arbre seul plus large que le cadre (≤ 1,6×) est réduit pour tenir ; au-delà, le cadre défile.
            var fitToWidth = width > container.clientWidth && width <= container.clientWidth * 1.6;

            var svg = d3.select(container).append('svg')
                .attr('width', fitToWidth ? '100%' : width)
                .attr('height', fitToWidth ? null : height)
                .attr('viewBox', [0, 0, width, height])
                .attr('role', 'img')
                .attr('aria-label', 'Construction de l’arbre de Huffman, étape ' + step + ' sur ' + total);

            var y = margin.top;
            rows.forEach(function (r) {
                var x = margin.left;
                r.trees.forEach(function (t) {
                    d3.tree().size([t.w, t.root.height * LEVEL])(t.root);
                    var g = svg.append('g').attr('transform', 'translate(' + x + ',' + y + ')');
                    drawTree(g, t.root, forest.isNext(t.root.data));
                    x += t.w + gap;
                });
                y += r.h + rowGap;
            });

            if (total === 0) {
                caption.textContent = 'Un seul symbole : l’arbre est réduit à une feuille, qui reçoit le code 0.';
            } else if (step === 0) {
                caption.textContent = 'Départ — ' + forest.roots.length + ' feuilles dans la file, triées par fréquence. '
                    + 'Les deux plus petites (entourées) vont fusionner : ' + describe(byId[forest.next.leftId])
                    + ' + ' + describe(byId[forest.next.rightId]) + ' → ' + forest.next.frequency + '.';
            } else if (step < total) {
                caption.textContent = 'Après ' + step + ' fusion' + (step > 1 ? 's' : '') + ' sur ' + total
                    + ' — prochaine : ' + describe(byId[forest.next.leftId]) + ' + ' + describe(byId[forest.next.rightId])
                    + ' → ' + forest.next.frequency + '.';
            } else {
                caption.textContent = 'Terminé après ' + total + ' fusions : une seule racine, de fréquence '
                    + forest.roots[0].frequency + ' (la longueur du texte).';
            }
        }

        var current = total;
        var slider = document.getElementById('tree-step');
        function goTo(step) {
            current = Math.max(0, Math.min(total, step));
            if (slider) slider.value = current;
            render(current);
            if (history.replaceState) history.replaceState(null, '', '#etape=' + current);
        }
        if (slider) {
            slider.addEventListener('input', function () { goTo(+slider.value); });
            document.getElementById('tree-first').addEventListener('click', function () { goTo(0); });
            document.getElementById('tree-prev').addEventListener('click', function () { goTo(current - 1); });
            document.getElementById('tree-next').addEventListener('click', function () { goTo(current + 1); });
            document.getElementById('tree-last').addEventListener('click', function () { goTo(total); });
        }
        var fromHash = /#etape=(\d+)/.exec(location.hash);
        goTo(fromHash ? +fromHash[1] : total);
    })();
</script>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
