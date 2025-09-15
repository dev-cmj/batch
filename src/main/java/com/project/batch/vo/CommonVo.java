package com.project.batch.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonVo {
    
    private String use_yn;
    private String createDate;
    private String createUser;
    private String updateDate;
    private String updateUser;
    
    public boolean isActive() {
        return "Y".equalsIgnoreCase(this.use_yn);
    }
}