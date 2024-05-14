import cn.scutvk.Utils.DBUtils;
import cn.scutvk.bean.ErrorsBean;
import cn.scutvk.bean.UserBean;
import cn.scutvk.bean.LoginLogBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class LoginServlet extends HttpServlet {
    // this servlet is used to process the login request
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // set utf-8
        resp.setHeader("Content-type", "text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // get parameters
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        // set parameters to bean
        UserBean user = new UserBean();
        user.setUsername(username);
        user.setPassword(password);
        // check if the user exists
        boolean res = DBUtils.users_checkexist(user);
        if (!res) {
            ErrorsBean errorsBean = new ErrorsBean();
            errorsBean.setErrors("username", "用户名不存在");
            req.setAttribute("errorsBean", errorsBean);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        // if the user exists
        // check if the password is correct
        res = DBUtils.users_checkpassword(user);
        if (!res) {
            ErrorsBean errorsBean = new ErrorsBean();
            errorsBean.setErrors("password", "密码错误");
            req.setAttribute("errorsBean", errorsBean);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // if the password is correct
        // get full information
        user = DBUtils.users_getfullbean(user);
        // set latestlogindate
        Date date = new Date();//获得当前时间
        Timestamp t = new Timestamp(date.getTime());//将时间转换成Timestamp类型，这样便可以存入到Mysql数据库中
        user.setLatestlogindate(t);
        // update userBean
        DBUtils.users_update_byid(user);

        // 创建LoginLogBean并记录登录信息
        LoginLogBean loginLogBean = new LoginLogBean();
        loginLogBean.setUid(user.getUid());
        loginLogBean.setLoginTime(t);
        loginLogBean.setIpAddress(req.getRemoteAddr());
        // 将loginLogBean插入到数据库中
        Integer loginlogs_id = DBUtils.loginlogs_insert(loginLogBean);
        if (loginlogs_id == null) {
            System.out.println("loginlogs_id == null");
            return;
        }
        loginLogBean.setId(loginlogs_id);

        // 将loginLogBean保存到session中，以便在用户退出时更新退出时间
        req.getSession().setAttribute("loginLogBean", loginLogBean);

        // finish login, jump to index page
        resp.getWriter().print("登录成功，马上跳转");
        req.getSession().setAttribute("userBean", user);
        resp.setHeader("refresh", "0;url=index.jsp");
    }
}
