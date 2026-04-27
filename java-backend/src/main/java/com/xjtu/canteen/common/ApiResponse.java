package com.xjtu.canteen.common;

public record ApiResponse(int code, String message, Object data) {
    public static ApiResponse success(Object data) {
        return new ApiResponse(0, "success", data == null ? java.util.Map.of() : data);
    }

    public static ApiResponse error(int code, String message) {
        return new ApiResponse(code, message, null);
    }
}
