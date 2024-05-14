<%@ page import="cn.scutvk.bean.UserBean" %>
<%@ page import="cn.scutvk.bean.PhotoViewRecordBean" %>
<%@ page import="cn.scutvk.Utils.DBUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="cn.scutvk.bean.ImageBean" %><%--
  Created by IntelliJ IDEA.
  User: Viking
  Date: 2023/04/09
  Time: 22:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  UserBean userBean = (UserBean) request.getSession().getAttribute("userBean");
  if (userBean == null) {
    return;
  }
  int ownerId = userBean.getUid();
  List<PhotoViewRecordBean> records = DBUtils.view_getBeViewedPhotos(ownerId);
%>
<style>
  .footprint-container .table-dark {
    background-color: #343a40;
    color: #FFF;
  }
  .footprint-container .table-bordered {
    border-color: #4a4a4a;
  }
  .footprint-container .table-bordered th, .footprint-container .table-bordered td {
    border-color: #4a4a4a;
  }
  .footprint-container .alert-info {
    background-color: #2f4f4f;
    border-color: #4a4a4a;
    color: #FFF;
  }
</style>
<div class="footprint-container col-12">
  <% if (records != null && !records.isEmpty()) { %>
  <div class="table-responsive col-12">
    <table class="table table-bordered table-dark col-12">
      <thead>
      <tr>
        <th>访问记录ID</th>
        <th>访问者</th>
        <th>作品ID</th>
        <th>作品名</th>
        <th>访问时间</th>
      </tr>
      </thead>
      <tbody>
      <%
        for (PhotoViewRecordBean record : records) {
          ImageBean image = new ImageBean();
          image.setImgid(record.getImgid());
          image = DBUtils.images_getshowbean_byid(image);
          String userName = null;
          if (record.getViewer_id() == null) {
            userName = "游客";
          } else {
            UserBean user = new UserBean();
            user.setUid(record.getViewer_id());
            user = DBUtils.users_getshowbean_byid(user);
            userName = user.getUsername();
          }
      %>
      <tr>
        <td><%=record.getId()%></td>
        <td><%=userName%></td>
        <td><%=record.getImgid()%></td>
        <td><%=image.getImgname()%></td>
        <td><%=record.getView_timestamp()%></td>
      </tr>
      <%
        }
      %>
      </tbody>
    </table>
  </div>
  <% } else { %>
    <div class="alert alert-info col-12" role="alert">
      您的作品暂无访问记录
    </div>
  <% } %>
</div>

