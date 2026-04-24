package com.axzl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author liuchao
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AxzlApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(AxzlApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  安心诊聊平台启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "  █████╗  ███████╗   ██████╗  ██╗          \n" +
                " ██╔══██╗ ██╔════╝  ██╔════╝  ██║          \n" +
                " ███████║ █████╗    ██║       ██║          \n" +
                " ██╔══██║ ██╔══╝    ██║       ██║          \n" +
                " ██║  ██║ ███████╗  ╚██████╗ ███████╗      \n" +
                " ╚═╝  ╚═╝ ╚══════╝   ╚═════╝ ╚══════╝      \n");
    }
}
