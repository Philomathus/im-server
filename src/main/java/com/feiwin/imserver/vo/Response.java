package com.feiwin.imserver.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response<T> {
    private int    code;
    private String msg;
    private T      data;

    public Response(int code, String msg ) {
        this.code = code;
        this.msg  = msg;
    }

    public Response(int code, String msg, T data ) {
        this.code = code;
        this.msg  = msg;
        if ( data != null ) {
            this.data = data;
        }
    }

    public static <T> Response<T> ok(final String msg, T data ) {
        return new Response<>( 200, msg, data );
    }

    public static <T> Response<T> ok(final String msg ) {
        return new Response<>( 200, msg );
    }

    public static <T> Response<T> ok(T data ) {
        return new Response<>( 200, "操作成功", data );
    }

    public static <T> Response<T> ok() {
        return Response.ok( "操作成功" );
    }

    /**
     * 业务异常提示
     */
    public static <T> Response<T> businessError(String error ) {
        return new Response<>( 500, error );
    }
}
