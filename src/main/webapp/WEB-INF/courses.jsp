<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>BrightPath</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 760px; margin: 2rem auto; padding: 0 1rem; color: #1f2933; }
    header { display: flex; justify-content: space-between; align-items: baseline; }
    .meta { color: #616e7c; font-size: .875rem; }
    table { width: 100%; border-collapse: collapse; margin: 1rem 0 2rem; }
    th, td { text-align: left; padding: .5rem; border-bottom: 1px solid #e4e7eb; }
    form { display: flex; gap: .5rem; flex-wrap: wrap; margin-bottom: 1rem; }
    input, select, button { padding: .4rem .6rem; font: inherit; }
  </style>
</head>
<body>
<header>
  <h1>BrightPath</h1>
  <span class="meta">customer: <c:out value="${customer}"/> &middot; v<c:out value="${version}"/></span>
</header>

<table>
  <thead><tr><th>#</th><th>Course</th><th>Category</th><th>Enrolled</th></tr></thead>
  <tbody>
  <c:forEach var="course" items="${courses}">
    <tr>
      <td><c:out value="${course.id}"/></td>
      <td><c:out value="${course.title}"/></td>
      <td><c:out value="${course.category}"/></td>
      <td><c:out value="${course.enrolled}"/></td>
    </tr>
  </c:forEach>
  <c:if test="${empty courses}">
    <tr><td colspan="4">No courses yet.</td></tr>
  </c:if>
  </tbody>
</table>

<h2>Add a course</h2>
<form method="post" action="courses">
  <input type="hidden" name="action" value="add">
  <input name="title" placeholder="Title" required>
  <input name="category" placeholder="Category (optional)">
  <button type="submit">Add</button>
</form>

<h2>Enroll a student</h2>
<form method="post" action="courses">
  <input type="hidden" name="action" value="enroll">
  <select name="courseId" required>
    <c:forEach var="course" items="${courses}">
      <option value="${course.id}"><c:out value="${course.title}"/></option>
    </c:forEach>
  </select>
  <input name="email" type="email" placeholder="student@example.com" required>
  <button type="submit">Enroll</button>
</form>
</body>
</html>
