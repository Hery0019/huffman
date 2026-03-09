<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ page import="hery.itu.util.Html" %>
<%
    String errorTitle = (String) request.getAttribute("errorTitle");
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorTitle == null) errorTitle = "Erreur";
    if (errorMessage == null) errorMessage = "Une erreur est survenue.";
    int status = response.getStatus();
    String pageTitle = errorTitle;
    String activeNav = "";
%>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<section class="max-w-xl mx-auto py-10">
    <div class="card p-8">
        <p class="label">Code <span class="font-mono normal-case tracking-normal text-ink ml-1"><%= status %></span></p>
        <h1 class="mt-2 text-2xl font-semibold tracking-tight"><%= Html.esc(errorTitle) %></h1>
        <p class="mt-3 text-muted"><%= Html.esc(errorMessage) %></p>
        <div class="mt-6 flex gap-3">
            <a href="<%= ctx %>/index.jsp" class="btn btn-primary">Retour à l'accueil</a>
            <a href="<%= ctx %>/insertDictionary" class="btn btn-secondary">Dictionnaire</a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jspf/foot.jspf" %>
