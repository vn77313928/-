package cn.scutvk.bean;

import java.sql.Timestamp;

public class PhotoViewRecordBean {
    private int id;
    private Integer viewer_id;
    private int photo_owner_id;
    private int imgid;
    private Timestamp view_timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getViewer_id() {
        return viewer_id;
    }

    public void setViewer_id(Integer viewer_id) {
        this.viewer_id = viewer_id;
    }

    public int getPhoto_owner_id() {
        return photo_owner_id;
    }

    public void setPhoto_owner_id(int photo_owner_id) {
        this.photo_owner_id = photo_owner_id;
    }

    public int getImgid() {
        return imgid;
    }

    public void setImgid(int imgid) {
        this.imgid = imgid;
    }

    public Timestamp getView_timestamp() {
        return view_timestamp;
    }

    public void setView_timestamp(Timestamp view_timestamp) {
        this.view_timestamp = view_timestamp;
    }
}
