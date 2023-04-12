package com.feiwin.imserver.controller;

import static com.feiwin.imserver.constant.Constants.USERNAME;
import static com.feiwin.imserver.constant.Constants.TOKEN;
import com.feiwin.imserver.dto.PrivateMessageDto;
import com.feiwin.imserver.service.ImService;
import com.feiwin.imserver.service.TokenService;
import com.feiwin.imserver.vo.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/private-chat")
public class PrivateChatController {

    @Resource
    private ImService imService;

    @Resource
    private TokenService tokenService;

    @PostMapping
    public Response<?> sendMessage(@RequestBody PrivateMessageDto privateMessageDto, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.sendPrivateMessage(username, privateMessageDto.getContent(), privateMessageDto.getTo());
        return Response.ok();
    }

    @GetMapping
    public Response<?> getAll(@RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        return Response.ok( imService.getUsersConversingWithUsername(username) );
    }

    @GetMapping("/{otherUsername}")
    public Response<?> getHistory(@PathVariable String otherUsername, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        return Response.ok( imService.getPrivateMessageHistory(username, otherUsername) );
    }

    @DeleteMapping("/{otherUsername}")
    public Response<?> delete(@PathVariable String otherUsername, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.deletePrivateChatByUsers(username, otherUsername);
        return Response.ok();
    }

    @DeleteMapping("/{privateMessageId}")
    public Response<?> deletePrivateMessage(@PathVariable String privateMessageId, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.deletePrivateMessageOnUserSideById(username, privateMessageId);
        return Response.ok();
    }
}
