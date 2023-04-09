package com.feiwin.imserver.controller;

import com.feiwin.imserver.dto.PrivateMessage;
import com.feiwin.imserver.service.ImService;
import com.feiwin.imserver.vo.Response;
import jakarta.annotation.Resource;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/private-chat")
public class PrivateChatController {

    @Resource
    private ImService imService;

    @PostMapping("/message")
    public Response sendMessage(PrivateMessage privateMessage, Principal principal) {
        privateMessage.setUsername(principal.getName());
        imService.sendPrivateMessage(privateMessage);
        return Response.ok();
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public Response handleException(MessageException exception) {
        return Response.fail(exception.getMessage(), exception.getUuid());
    }

}
