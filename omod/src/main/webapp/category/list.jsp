<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View category" otherwise="/login.htm" redirect="/module/rks/listCategory.form" />

<spring:message var="pageTitle" code="rks.category.manage" scope="page"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="../includes/nav.jsp" %>

<h2><spring:message code="rks.category.manage"/></h2>	

<br />
<c:forEach items="${errors.allErrors}" var="error">
	<span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span><
</c:forEach>
<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" value="<spring:message code='rks.category.add'/>" onclick="ACT.go('category.form');"/>

<br /><br />
<form method="post" onsubmit="return false" id="form">
<table cellpadding="5" cellspacing="0"  >
	<tr>
		<td><spring:message code="general.name"/></td>
		<td><input type="text" id="searchName" name="searchName" value="${searchName}" /></td>
		<td><input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" value="Search" onclick="RKS.search('categoryList.form','searchName');"/></td>
	</tr>
</table>
<span class="boxHeader"><spring:message code="rks.category.list"/></span>
<div class="box">
<c:choose>
<c:when test="${not empty categories}">
<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" onclick="RKS.checkValue();" value="<spring:message code='rks.deleteSelected'/>"/>
<table cellpadding="5" cellspacing="0" width="100%">
<tr>
	<th>#</th>
	<th><spring:message code="general.name"/></th>
	<th><spring:message code="rks.category.description"/></th>
	<th><spring:message code="rks.category.parent"/></th>
	<th><spring:message code="rks.category.retired"/></th>
	<th><spring:message code="rks.category.createdOn"/></th>
	<th><spring:message code="rks.category.createdBy"/></th>
	<th></th>
</tr>
<c:forEach items="${categories}" var="category" varStatus="varStatus">
	<tr class='${varStatus.index % 2 == 0 ? "oddRow" : "evenRow" } '>
		<td><c:out value="${(( pagingUtil.currentPage - 1  ) * pagingUtil.pageSize ) + varStatus.count }"/></td>	
		<td><a href="#" onclick="ACT.go('category.form?categoryId=${ category.id}');">${category.name}</a> </td>
		<td>${category.description}</td>
		<td>${category.parent.name}</td>
		<td>${category.retired}</td>
		<td><openmrs:formatDate date="${category.createdOn}" type="textbox"/></td>
		<td>${category.createdBy}</td>
		<td><input type="checkbox" name="ids" value="${category.id}"/></td>
	</tr>
</c:forEach>

<tr class="paging-container">
	<td colspan="8"><%@ include file="../paging.jsp" %></td>
</tr>
</table>
</c:when>
<c:otherwise>
	No category found.
</c:otherwise>
</c:choose>
</div>
</form>



<%@ include file="/WEB-INF/template/footer.jsp" %>
