package com.mz.segiu.mvp.model.entity;

import android.text.TextUtils;

public class OrgEntity extends BaseEntity<OrgEntity> {

    /**
     * createId : 619633984
     * createTime : 2019-11-01 10:24:36
     * orgName : 武清区教育局
     * orgType : 2
     * organizationId : 957
     * updateId : 619633984
     * updateTime : 2020-05-19 09:27:56
     * userId : 619633984
     */

    public String id;
    public String createId;
    public String createTime;
    public String orgName;
    public String orgType;
    public String organizationId;
    public String updateId;
    public String updateTime;
    public String userId;
    public String projectIdentify;

    public String getOrganizationId() {
        if(TextUtils.isEmpty(organizationId)){
            organizationId=id;
        }
        return TextUtils.isEmpty(organizationId)?"":organizationId;
    }
}
