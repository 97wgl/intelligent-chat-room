package com.hust.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author WGL
 * @since 2021-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dialog_library")
@ApiModel(value="DialogLibrary对象", description="")
public class DialogLibrary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "课程编号")
    private Integer classId;

    @ApiModelProperty(value = "轮次")
    private Integer roundNo;

    @ApiModelProperty(value = "正确回复")
    private String correctReply;

    @ApiModelProperty(value = "学伴回复")
    private String assistantReply;


}
