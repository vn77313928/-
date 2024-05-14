import cn.scutvk.Utils.DBUtils;
import cn.scutvk.bean.ImageBean;
import cn.scutvk.bean.PhotoViewRecordBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;

@WebServlet(name = "GotoItemServlet",
        value = "/GotoItemServlet"
        )
public class GotoItemServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // params: imgid
        int imgid = (int) req.getSession().getAttribute("imgid");
        // clear Attribute
        req.getSession().removeAttribute("imgid");
        // check if imgid is 0
        if (imgid == 0) {
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return;
        }
        // get imageBean
        ImageBean imageBean = new ImageBean();
        imageBean.setImgid(imgid);
        imageBean = DBUtils.images_getshowbean_byid(imageBean);
        // set imageBean
        req.getSession().setAttribute("imageBean", imageBean);
        // Add view record
        // Judge if the viewer has logged in and set the viewer_id
        Integer viewer_id = null;
        if (req.getSession().getAttribute("userBean") != null) {
            viewer_id = ((cn.scutvk.bean.UserBean) req.getSession().getAttribute("userBean")).getUid();
        }
        // Set owner_id
        int owner_id = imageBean.getUid();
        // Create a PhotoViewRecordBean object
        PhotoViewRecordBean record = new PhotoViewRecordBean();
        record.setViewer_id(viewer_id);
        record.setPhoto_owner_id(owner_id);
        record.setImgid(imgid);
        record.setView_timestamp(new Timestamp(System.currentTimeMillis()));
        // Add record to database
        DBUtils.view_record(record);
        // forward
        req.getRequestDispatcher("/item.jsp").forward(req, resp);
    }
}
