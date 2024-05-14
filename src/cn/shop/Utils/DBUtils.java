package cn.scutvk.Utils;

import cn.scutvk.bean.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class DBUtils {
    // this class is used to do the database operation with c3p0utils and QueryRunner
    // has methods: update, query,
    // users_checkexist, users_add, users_delete, users_update_byname, users_update_byid, users_checkpassword, users_getfullbean,
    // users_queryavatarurl, users_querysignature, users_changepassword, users_getshowbean_byid
    // zones_getzonebean, zones_create, zones_update_byid
    // images_insert, images_update_order, images_update_byid, images_getshowbean_byid, images_getfullbean_byid, images_getid_byothers, images_addlikesnumber
    // orders_insert, orders_getfullbean
    // view_record, view_getBeViewedPhotos
    // loginlogs_insert
    // userbrowsingrecords_insert
    // operationlogs_insert
    public static void update(String sql, Object ... params) throws SQLException{
        Connection conn = null;
        try {
            conn = C3p0Utils.getConnection();
            QueryRunner qr = new QueryRunner();
            qr.update(conn, sql, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            C3p0Utils.release(conn);
        }
    }

    public static Object query(String sql, ResultSetHandler<?> rsh, Object ... params) throws SQLException {
        Connection conn = null;
        try {
            conn = C3p0Utils.getConnection();
            QueryRunner qr = new QueryRunner();
            return qr.query(conn, sql, rsh, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            C3p0Utils.release(conn);
        }
    }

    public static boolean users_checkexist(UserBean user){
        // this function is used to check if the user is already in the database
        // if the user is already in the database, return true
        // if the user is not in the database, return false
        boolean flag = false;
        String sql = "select * from users where username=?";
        try {
            UserBean userBean = (UserBean) query(sql, new BeanHandler(UserBean.class), user.getUsername());
            if (userBean != null) {
                flag =  true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static void users_insert(UserBean user){
        // this function is used to insert a user into the database
        String sql = "insert into users(username, password, email, signupdate, latestlogindate) values(?, ?, ?, ?, ?)";
        try {
            update(sql, user.getUsername(), user.getPassword(), user.getEmail(), user.getSignupdate(), user.getLatestlogindate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void users_delete(UserBean user) {
        // this function is used to delete a user from the database
        String sql = "delete from users where username=?";
        try {
            update(sql, user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void users_update_byname(UserBean user) {
        // this function is used to update a user in the database
        String sql = "update users set password=?, email=? where username=?";
        try {
            update(sql, user.getPassword(), user.getEmail(), user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean users_update_byid(UserBean user) {
        // this function is used to update a user in the database
        boolean flag = false;
        String sql = "update users set username=?, email=?, avatarurl=?, sex=?, signupdate=?, latestlogindate=? where uid=?";
        try {
            update(sql, user.getUsername(), user.getEmail(), user.getAvatarurl(), user.getSex(), user.getSignupdate(), user.getLatestlogindate(), user.getUid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean users_checkpassword(UserBean user) {
        // this function is used to check if the password is correct
        // if the password is correct, return true
        // if the password is not correct, return false
        boolean flag = false;
        String sql = "select * from users where username=? and password=?";
        try {
            UserBean userBean = (UserBean) query(sql, new BeanHandler(UserBean.class), user.getUsername(), user.getPassword());
            if (userBean != null) {
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static UserBean users_getfullbean(UserBean user) {
        // get uid, avatarurl, sex
        String sql = "select * from users where username=?";
        try {
            user = (UserBean) query(sql, new BeanHandler(UserBean.class), user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static boolean users_changepassword(UserBean userBean) {
        String sql = "update users set password=? where uid=?";
        boolean flag = false;
        try {
            update(sql, userBean.getPassword(), userBean.getUid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static UserBean users_getshowbean_byid(UserBean userBean) {
        String sql = "select * from users where uid=?";
        try {
            UserBean tempBean = (UserBean) query(sql, new BeanHandler(UserBean.class), userBean.getUid());
            userBean.setUsername(tempBean.getUsername());
            userBean.setAvatarurl(tempBean.getAvatarurl());
            userBean.setSex(tempBean.getSex());
            userBean.setEmail(tempBean.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userBean;
    }

    public static ZoneBean zones_getbean(UserBean user) {
        // get zone information from sql,
        // return a ZoneBean
        String sql = "select * from zones where uid=?";
        ZoneBean zoneBean = null;
        try {
            zoneBean = (ZoneBean) query(sql, new BeanHandler(ZoneBean.class), user.getUid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zoneBean;
    }

    public static Boolean zones_create(UserBean userBean) {
        // create a zone for the user
        // if the user already has a zone, return false
        // if the user does not have a zone, create a zone for the user and return true
        Boolean flag = false;
        // query uid of user
        String sql = "select * from users where username=?";
        try {
            userBean = (UserBean) query(sql, new BeanHandler(UserBean.class), userBean.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "insert into zones(uid) values(?)";
        try {
            update(sql, userBean.getUid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean zones_update_byid(ZoneBean zoneBean) {
        // update zone information
        // if the zone does not exist, return false
        // if the zone exists, update the zone and return true
        boolean flag = false;
        String sql = "update zones set signature=?, blogurl=? where uid=?";
        try {
            update(sql, zoneBean.getSignature(), zoneBean.getBlogurl(), zoneBean.getUid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static Boolean images_insert(ImageBean imageBean) {
        // insert an image into the database
        // if the image is already in the database, return false
        // if the image is not in the database, insert the image into the database and return true
        Boolean flag = false;
        String sql = "insert into images(uid, imgname, story, stoneurl, price, uploaddate) values(?, ?, ?, ?, ?, ?)";
        try {
            update(sql, imageBean.getUid(), imageBean.getImgname(), imageBean.getStory(), imageBean.getStoneurl(), imageBean.getPrice(), imageBean.getUploaddate());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean images_update_order(ImageBean imageBean) {
        // update image information
        // if update successfully, return true
        // if update failed, return false
        boolean flag = false;
        String sql = "update images set uid=?, orderid=?, havesold=?, visible=? where imgid=?";
        try {
            update(sql, imageBean.getUid(), imageBean.getOrderid(), imageBean.getHavesold(), imageBean.getVisible(), imageBean.getImgid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean images_update_byid (ImageBean imageBean) {
        // update image information
        // if update successfully, return true
        // if update failed, return false
        boolean flag = false;
        String sql = "update images set imgname=?, story=?, price=?, visible=? where imgid=?";
        try {
            update(sql, imageBean.getImgname(), imageBean.getStory(), imageBean.getPrice(), imageBean.getVisible(), imageBean.getImgid());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static ImageBean images_getshowbean_byid(ImageBean imageBean) {
        // get the bean with showing information from sql
        boolean flag = false;
        String sql = "select * from images where imgid=?";
        try {
            ImageBean tempBean = (ImageBean) query(sql, new BeanHandler(ImageBean.class), imageBean.getImgid());
            imageBean.setImgname(tempBean.getImgname());
            imageBean.setStory(tempBean.getStory());
            imageBean.setStoneurl(tempBean.getStoneurl());
            imageBean.setUid(tempBean.getUid());
            imageBean.setPrice(tempBean.getPrice());
            imageBean.setLikesnumber(tempBean.getLikesnumber());
            imageBean.setUploaddate(tempBean.getUploaddate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return imageBean;
    }

    public static ImageBean images_getfullbean_byid (ImageBean imageBean) {
        // get the bean with full information from sql
        String sql = "select * from images where imgid=?";
        try {
            imageBean = (ImageBean) query(sql, new BeanHandler(ImageBean.class), imageBean.getImgid());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return imageBean;
    }

    public static int images_getid_byothers (ImageBean imageBean) {
        int imgid = 0;
        String sql = "select * from images where uid=? and imgname=? and story=? and stoneurl=? and price=?";
        try {
            ImageBean tempBean = (ImageBean) query(sql, new BeanHandler(ImageBean.class), imageBean.getUid(), imageBean.getImgname(), imageBean.getStory(), imageBean.getStoneurl(), imageBean.getPrice());
            imgid = tempBean.getImgid();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return imgid;
    }

    public static ImageBean images_addlikesnumber (ImageBean imageBean) {
        // add likesnumber by 1
        // parameter: imageBean
        // return: imageBean
        String sql = "update images set likesnumber=? where imgid=?";
        try {
            update(sql, imageBean.getLikesnumber() + 1, imageBean.getImgid());
            imageBean.setLikesnumber(imageBean.getLikesnumber() + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return imageBean;
    }

    public static boolean orders_insert (OrderBean orderBean) {
        // insert an orderBean into database
        // if the orderBean is already in the database, return false
        // if the orderBean is not in the database, insert the orderBean into the database and return true
        boolean flag = false;
        String sql = "insert into orders(sellerid, buyerid, imgid, ordername, orderemail, remark, price, orderdate) values(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            update(sql, orderBean.getSellerid(), orderBean.getBuyerid(), orderBean.getImgid(), orderBean.getOrdername(), orderBean.getOrderemail(), orderBean.getRemark(), orderBean.getPrice(), orderBean.getOrderdate());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static OrderBean orders_getfullbean (OrderBean orderBean) {
        // get the full information of an orderBean
        // if the orderBean does not exist, return null
        // if the orderBean exists, return the full information of the orderBean
        String sql = "select * from orders where sellerid=? and buyerid=? and imgid=? and orderdate=?";
        try {
            orderBean = (OrderBean) query(sql, new BeanHandler(OrderBean.class), orderBean.getSellerid(), orderBean.getBuyerid(), orderBean.getImgid(), orderBean.getOrderdate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderBean;
    }

    public static void view_record(PhotoViewRecordBean record) {
        String sql = "INSERT INTO photo_view_records (viewer_id, photo_owner_id, imgid, view_timestamp) VALUES (?, ?, ?, ?)";
        try {
            update(sql, record.getViewer_id(), record.getPhoto_owner_id(), record.getImgid(), record.getView_timestamp());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve a list of photo view records for a specific user.
     *
     * @param viewer_id The ID of the user whose viewed photos you want to retrieve.
     * @return A list of PhotoViewRecordBean objects representing the photos viewed by the specified user.
     */
    public static List<PhotoViewRecordBean> view_getViewedPhotos(int viewer_id) {
        String sql = "SELECT * FROM photo_view_records WHERE viewer_id = ?";
        List<PhotoViewRecordBean> records = null;
        try {
            records = (List<PhotoViewRecordBean>) query(sql, new BeanListHandler(PhotoViewRecordBean.class), viewer_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static List<PhotoViewRecordBean> view_getBeViewedPhotos(int owner_id) {
        // Get all the PhotoViewRecordBeans where the specified user is the owner of the photo
        List<PhotoViewRecordBean> records = null;
        String sql = "SELECT * FROM photo_view_records WHERE photo_owner_id = ?";

        try {
            records = (List<PhotoViewRecordBean>) query(sql, new BeanListHandler<>(PhotoViewRecordBean.class), owner_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static int loginlogs_insert(LoginLogBean loginLogBean) {
        int generatedId = 0;
        String sqlInsert = "INSERT INTO login_logs(uid, login_time, logout_time, ip_address) VALUES (?, ?, ?, ?)";
        String sqlSelect = "SELECT * FROM login_logs WHERE uid=? AND login_time=? AND ip_address=?";

        try {
            // 插入新的loginLogBean记录
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String loginTimeStr = sdf.format(loginLogBean.getLoginTime());
            Timestamp loginTimeFormatted = Timestamp.valueOf(loginTimeStr);

            update(sqlInsert, loginLogBean.getUid(), loginTimeFormatted, loginLogBean.getLogoutTime(), loginLogBean.getIpAddress());

            // 查询刚插入的记录
            LoginLogBean insertedBean = (LoginLogBean) query(sqlSelect, new BeanHandler(LoginLogBean.class), loginLogBean.getUid(), loginTimeFormatted, loginLogBean.getIpAddress());
            if (insertedBean != null) {
                generatedId = insertedBean.getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public static boolean loginlogs_update(LoginLogBean loginLogBean) {
        boolean flag = false;
        String sql = "UPDATE login_logs SET logout_time = ? WHERE id = ?";
        try {
            update(sql, loginLogBean.getLogoutTime(), loginLogBean.getId());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean userbrowsingrecords_insert(UserBrowsingRecordBean userBrowsingRecordBean) {
        boolean flag = false;
        String sql = "INSERT INTO user_browsing_records(uid, imgid, visit_time, duration, ip_address) VALUES (?, ?, ?, ?, ?)";
        try {
            update(sql, userBrowsingRecordBean.getUid(), userBrowsingRecordBean.getImgid(), userBrowsingRecordBean.getVisitTime(), userBrowsingRecordBean.getDuration(), userBrowsingRecordBean.getIpAddress());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean operationlogs_insert(OperationLogBean operationLogBean) {
        boolean flag = false;
        String sql = "INSERT INTO operation_logs(uid, operation_time, operation_content, ip_address) VALUES (?, ?, ?, ?)";
        try {
            update(sql, operationLogBean.getUid(), operationLogBean.getOperationTime(), operationLogBean.getOperationContent(), operationLogBean.getIpAddress());
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

}
