package net.dloud.platform.gateway.util;

import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.platform.BaseExceptionEnum;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;

import java.util.UUID;

/**
 * @author QuDasheng
 * @create 2018-09-12 10:13
 **/
public class ResultWrapper {
    public static ApiResponse api(BaseResult result) {
        final ApiResponse response = new ApiResponse();
        if (result.isSuccess()) {
            response.setCode(0);
            response.setMessage("请求成功");
            response.setPreload(result);
        } else {
            response.setCode(1);
            response.setMessage("请求失败");
            response.setPreload(new BaseResult(PlatformExceptionEnum.SYSTEM_BUSY.getCode()));
        }
        return response;
    }

    public static ApiResponse err(PassedException ex) {
        if (null == ex.getEnum()) {
            final ApiResponse response = new ApiResponse();
            response.setCode(1);
            response.setMessage(ex.getMessage());
            String code = ex.getCode();
            if (StringUtil.isBlank(code) || "0".equals(code)
                    || code.equals(PlatformExceptionEnum.SYSTEM_BUSY.getCode())) {
                code = PlatformExceptionEnum.BAD_REQUEST.getCode();
            }
            if (null == response.getProof()) {
                response.setProof(UUID.randomUUID().toString());
            }
            response.setPreload(new BaseResult(code));
            return response;
        } else {
            return new ApiResponse(ex.getEnum());
        }
    }

    public static ApiResponse err(RefundException ex) {
        if (null == ex.getEnum()) {
            final ApiResponse response = new ApiResponse();
            response.setCode(-1);
            response.setMessage(ex.getMessage());
            String code = ex.getCode();
            if (StringUtil.isBlank(code) || "0".equals(code)
                    || code.equals(PlatformExceptionEnum.SYSTEM_BUSY.getCode())) {
                code = PlatformExceptionEnum.UNAUTHORIZED.getCode();
            }
            if (null == response.getProof()) {
                response.setProof(UUID.randomUUID().toString());
            }
            response.setPreload(new BaseResult(code));
            return response;
        } else {
            return new ApiResponse(-1, ex.getEnum());
        }
    }

    public static ApiResponse success(Object result) {
        final ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setMessage("请求成功");
        if (null == response.getProof()) {
            response.setProof(UUID.randomUUID().toString());
        }
        response.setPreload(result);
        return response;
    }

    public static <T extends BaseResult> T success(T result) {
        result.setCode("0");
        return result;
    }

    public static ApiResponse failed() {
        final ApiResponse response = new ApiResponse();
        response.setCode(1);
        response.setMessage("请求失败");
        return response;
    }

    public static <T extends BaseResult> T failed(T result, BaseExceptionEnum base) {
        result.setCode(base.getCode());
        return result;
    }
}
