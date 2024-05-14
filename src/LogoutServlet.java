import cn.scutvk.bean.LoginLogBean;
import cn.scutvk.Utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class LogoutServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // set utf-8
        resp.setHeader("Content-type", "text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();

        LoginLogBean loginLogBean = (LoginLogBean) session.getAttribute("loginLogBean");
        // debug, print loginLogBean
        System.out.println(loginLogBean.getId());

        // 更新LoginLogBean的退出时间
        Date date = new Date();
        Timestamp t = new Timestamp(date.getTime());
        loginLogBean.setLogoutTime(t);

        // 更新数据库中的退出时间
        boolean flag = DBUtils.loginlogs_update(loginLogBean);
        if (!flag) {
            return;
        }

        // clear session
        req.getSession().invalidate();
        // jump to login page
        resp.getWriter().print("退出成功，3秒后跳转");
        resp.setHeader("refresh", "3;url=index.jsp");
    }
}
